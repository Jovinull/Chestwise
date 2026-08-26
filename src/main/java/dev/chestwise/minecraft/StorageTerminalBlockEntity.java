package dev.chestwise.minecraft;

import dev.chestwise.core.ExtractionPlan;
import dev.chestwise.core.ExtractionStep;
import dev.chestwise.core.IncrementalStorageIndex;
import dev.chestwise.core.IndexedItem;
import dev.chestwise.core.ItemIdentity;
import dev.chestwise.core.ItemSearch;
import dev.chestwise.core.SearchQuery;
import dev.chestwise.core.SortMode;
import dev.chestwise.core.StorageSource;
import dev.chestwise.core.StorageView;
import dev.chestwise.core.TransferPlanner;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public final class StorageTerminalBlockEntity extends BlockEntity implements net.minecraft.world.MenuProvider {
    private final IncrementalStorageIndex index = new IncrementalStorageIndex();
    private final Map<String, MinecraftStorageSource> sources = new LinkedHashMap<>();
    private int ticks;
    private int pollCursor;

    public StorageTerminalBlockEntity(BlockPos position, BlockState state) {
        super(ChestwiseContent.STORAGE_TERMINAL_ENTITY, position, state);
    }

    public static void serverTick(
        net.minecraft.world.level.Level level,
        BlockPos position,
        BlockState state,
        StorageTerminalBlockEntity terminal
    ) {
        terminal.serverTick();
    }

    private void serverTick() {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }
        ChestwiseConfig config = ChestwiseContent.CONFIG;
        if (ticks == 0 || ticks % config.discoveryIntervalTicks() == 0) {
            discover(serverLevel, config);
        }
        poll(config.inventoriesPolledPerTick());
        ticks++;
    }

    private void discover(ServerLevel serverLevel, ChestwiseConfig config) {
        LoadedContainerDiscovery discovery = new LoadedContainerDiscovery(serverLevel);
        Set<String> found = new HashSet<>();
        for (StorageSource discovered : discovery.discover(worldPosition, config.scanRadius(), config.maxInventories())) {
            MinecraftStorageSource source = (MinecraftStorageSource) discovered;
            found.add(source.sourceId());
            sources.put(source.sourceId(), source);
        }
        List<String> removed = sources.keySet().stream().filter(id -> !found.contains(id)).toList();
        removed.forEach(id -> {
            sources.remove(id);
            index.invalidate(id);
        });
        if (pollCursor >= sources.size()) {
            pollCursor = 0;
        }
    }

    private void poll(int budget) {
        if (sources.isEmpty()) {
            return;
        }
        List<MinecraftStorageSource> ordered = List.copyOf(sources.values());
        int checks = Math.min(budget, ordered.size());
        for (int checked = 0; checked < checks; checked++) {
            MinecraftStorageSource source = ordered.get(pollCursor % ordered.size());
            pollCursor = (pollCursor + 1) % ordered.size();
            if (source.isValid()) {
                index.reconcile(source.snapshot());
            } else {
                sources.remove(source.sourceId());
                index.invalidate(source.sourceId());
            }
        }
    }

    public List<IndexedItem> search(String query, SortMode sortMode) {
        return ItemSearch.apply(index.snapshot(), SearchQuery.parse(query), sortMode);
    }

    public ItemStack representative(ItemIdentity identity) {
        for (MinecraftStorageSource source : sources.values()) {
            ItemStack stack = source.representative(identity);
            if (!stack.isEmpty()) {
                return stack;
            }
        }
        return ItemStack.EMPTY;
    }

    public Optional<BlockPos> locateNearest(ItemIdentity identity, Player player) {
        return sources.values().stream()
            .filter(MinecraftStorageSource::isValid)
            .filter(source -> source.contains(identity))
            .map(MinecraftStorageSource::position)
            .min(java.util.Comparator
                .comparingDouble((BlockPos position) -> player.distanceToSqr(
                    position.getX() + 0.5,
                    position.getY() + 0.5,
                    position.getZ() + 0.5
                ))
                .thenComparingLong(BlockPos::asLong));
    }

    public ItemStack withdraw(ItemIdentity identity, int requested) {
        IndexedItem item = index.get(identity).orElse(null);
        if (item == null || requested <= 0) {
            return ItemStack.EMPTY;
        }
        ExtractionPlan plan = TransferPlanner.extract(item, requested);
        if (plan.planned() == 0 || !validate(plan)) {
            markPlanDirty(plan);
            return ItemStack.EMPTY;
        }

        Map<String, Integer> bySource = new LinkedHashMap<>();
        for (ExtractionStep step : plan.steps()) {
            bySource.merge(step.slot().sourceId(), (int) step.amount(), Integer::sum);
        }
        ItemStack result = ItemStack.EMPTY;
        for (Map.Entry<String, Integer> entry : bySource.entrySet()) {
            MinecraftStorageSource source = sources.get(entry.getKey());
            ItemStack removed = source.extractStack(identity, entry.getValue());
            if (result.isEmpty()) {
                result = removed;
            } else if (!removed.isEmpty()) {
                result.grow(removed.getCount());
            }
            index.reconcile(source.snapshot());
        }
        return result;
    }

    private boolean validate(ExtractionPlan plan) {
        for (ExtractionStep step : plan.steps()) {
            MinecraftStorageSource source = sources.get(step.slot().sourceId());
            if (source == null || !source.isValid()) {
                return false;
            }
            StorageView current = source.snapshot();
            if (current.revision() != step.slot().sourceRevision()
                || source.simulateExtract(step.slot().slot(), plan.identity(), step.amount()) != step.amount()) {
                return false;
            }
        }
        return true;
    }

    private void markPlanDirty(ExtractionPlan plan) {
        plan.steps().stream().map(step -> step.slot().sourceId()).distinct().forEach(id -> {
            MinecraftStorageSource source = sources.get(id);
            if (source != null && source.isValid()) {
                index.reconcile(source.snapshot());
            } else {
                index.invalidate(id);
            }
        });
    }

    public int deposit(ItemStack stack, boolean matchingOnly) {
        if (stack.isEmpty()) {
            return 0;
        }
        ItemIdentity identity = ItemStackIdentity.identity(stack);
        List<MinecraftStorageSource> matching = sources.values().stream()
            .filter(source -> source.contains(identity))
            .toList();
        if (matchingOnly && matching.isEmpty()) {
            return 0;
        }

        int before = stack.getCount();
        for (MinecraftStorageSource source : matching) {
            source.insertStack(stack, false);
            index.reconcile(source.snapshot());
            if (stack.isEmpty()) {
                return before;
            }
        }
        if (!matchingOnly) {
            for (MinecraftStorageSource source : sources.values()) {
                if (matching.contains(source)) {
                    continue;
                }
                source.insertStack(stack, false);
                index.reconcile(source.snapshot());
                if (stack.isEmpty()) {
                    break;
                }
            }
        }
        return before - stack.getCount();
    }

    public boolean canUse(Player player) {
        if (isRemoved() || level == null || level.getBlockEntity(worldPosition) != this) {
            return false;
        }
        double distance = ChestwiseContent.CONFIG.interactionDistance();
        return player.distanceToSqr(
            worldPosition.getX() + 0.5,
            worldPosition.getY() + 0.5,
            worldPosition.getZ() + 0.5
        ) <= distance * distance;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("container.chestwise.storage_terminal");
    }

    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new StorageTerminalMenu(containerId, inventory, this);
    }
}
