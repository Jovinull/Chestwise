package dev.chestwise.forge;

import dev.chestwise.core.ItemDescriptor;
import dev.chestwise.core.ItemIdentity;
import dev.chestwise.core.StorageSlot;
import dev.chestwise.core.StorageView;
import dev.chestwise.minecraft.ItemStackIdentity;
import dev.chestwise.minecraft.MinecraftStorageSource;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.IItemHandler;

/** Forge capability adapter for placed, loaded item inventories. */
public final class ForgeItemStorageSource implements MinecraftStorageSource {
    private final String sourceId;
    private final BlockPos position;
    private final ServerLevel level;
    private final BlockEntity owner;
    private final IItemHandler handler;

    private ForgeItemStorageSource(
        String sourceId,
        BlockPos position,
        ServerLevel level,
        BlockEntity owner,
        IItemHandler handler
    ) {
        this.sourceId = sourceId;
        this.position = position.immutable();
        this.level = level;
        this.owner = owner;
        this.handler = handler;
    }

    public static Optional<ForgeItemStorageSource> adapt(ServerLevel level, BlockPos position, BlockEntity owner) {
        return owner.getCapability(ForgeCapabilities.ITEM_HANDLER).resolve().map(handler -> {
            String sourceId = level.dimension().identifier() + "@" + position.getX() + "," + position.getY() + "," + position.getZ();
            return new ForgeItemStorageSource(sourceId, position, level, owner, handler);
        });
    }

    public Object deduplicationKey() {
        return handler;
    }

    @Override public String sourceId() { return sourceId; }
    @Override public BlockPos position() { return position; }

    @Override
    public StorageView snapshot() {
        List<StorageSlot> slots = new ArrayList<>(handler.getSlots());
        long revision = 0xcbf29ce484222325L;
        for (int slot = 0; slot < handler.getSlots(); slot++) {
            ItemStack stack = handler.getStackInSlot(slot);
            long capacity = Math.max(stack.getCount(), handler.getSlotLimit(slot));
            if (stack.isEmpty()) {
                slots.add(StorageSlot.empty(slot, capacity));
                revision = mix(revision, capacity);
            } else {
                ItemDescriptor descriptor = ItemStackIdentity.describe(stack);
                slots.add(new StorageSlot(slot, descriptor, stack.getCount(), capacity));
                revision = mix(mix(revision, descriptor.identity().hashCode()), stack.getCount());
            }
        }
        return new StorageView(sourceId, revision & Long.MAX_VALUE, slots);
    }

    @Override
    public long simulateExtract(int slot, ItemIdentity identity, long maximum) {
        if (slot < 0 || slot >= handler.getSlots() || maximum <= 0
            || !ItemStackIdentity.matches(handler.getStackInSlot(slot), identity)) {
            return 0;
        }
        return handler.extractItem(slot, bounded(maximum), true).getCount();
    }

    @Override
    public long extract(int slot, ItemIdentity identity, long maximum) {
        if (slot < 0 || slot >= handler.getSlots() || maximum <= 0
            || !ItemStackIdentity.matches(handler.getStackInSlot(slot), identity)) {
            return 0;
        }
        return handler.extractItem(slot, bounded(maximum), false).getCount();
    }

    @Override
    public ItemStack extractStack(ItemIdentity identity, int maximum) {
        ItemStack result = ItemStack.EMPTY;
        int remaining = maximum;
        for (int slot = 0; slot < handler.getSlots() && remaining > 0; slot++) {
            if (!ItemStackIdentity.matches(handler.getStackInSlot(slot), identity)) {
                continue;
            }
            ItemStack removed = handler.extractItem(slot, remaining, false);
            if (result.isEmpty()) {
                result = removed;
            } else if (!removed.isEmpty()) {
                result.grow(removed.getCount());
            }
            remaining -= removed.getCount();
        }
        return result;
    }

    @Override
    public ItemStack representative(ItemIdentity identity) {
        for (int slot = 0; slot < handler.getSlots(); slot++) {
            ItemStack stack = handler.getStackInSlot(slot);
            if (ItemStackIdentity.matches(stack, identity)) {
                return stack.copyWithCount(1);
            }
        }
        return ItemStack.EMPTY;
    }

    @Override
    public int insertStack(ItemStack incoming, boolean matchingOnly) {
        if (incoming.isEmpty() || matchingOnly && !contains(ItemStackIdentity.identity(incoming))) {
            return 0;
        }
        int before = incoming.getCount();
        for (int pass = 0; pass < 2 && !incoming.isEmpty(); pass++) {
            for (int slot = 0; slot < handler.getSlots() && !incoming.isEmpty(); slot++) {
                ItemStack current = handler.getStackInSlot(slot);
                boolean eligible = pass == 0
                    ? ItemStackIdentity.sameVariant(current, incoming)
                    : current.isEmpty();
                if (!eligible) {
                    continue;
                }
                ItemStack remainder = handler.insertItem(slot, incoming, false);
                incoming.setCount(remainder.getCount());
            }
        }
        return before - incoming.getCount();
    }

    @Override
    public boolean contains(ItemIdentity identity) {
        for (int slot = 0; slot < handler.getSlots(); slot++) {
            if (ItemStackIdentity.matches(handler.getStackInSlot(slot), identity)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isValid() {
        return !owner.isRemoved() && level.isLoaded(position) && level.getBlockEntity(position) == owner
            && owner.getCapability(ForgeCapabilities.ITEM_HANDLER).isPresent();
    }

    private static int bounded(long amount) {
        return (int) Math.min(Integer.MAX_VALUE, amount);
    }

    private static long mix(long hash, long value) {
        return (hash ^ value) * 0x100000001b3L;
    }
}
