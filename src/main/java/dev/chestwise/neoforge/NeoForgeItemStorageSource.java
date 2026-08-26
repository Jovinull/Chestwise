package dev.chestwise.neoforge;

//? if neoforge && > 1.20.1 {
/*import dev.chestwise.core.ItemDescriptor;
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
import net.neoforged.neoforge.capabilities.Capabilities;
//? if < 26.2 {
import net.neoforged.neoforge.items.IItemHandler;
//?} else {
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemUtil;
import net.neoforged.neoforge.transfer.transaction.Transaction;
//?}

// NeoForge block-capability bridge.
public final class NeoForgeItemStorageSource implements MinecraftStorageSource {
    private final String sourceId;
    private final BlockPos position;
    private final ServerLevel level;
    private final BlockEntity owner;
    //? if < 26.2 {
    private final IItemHandler handler;
    //?} else {
    private final ResourceHandler<ItemResource> handler;
    //?}

    private NeoForgeItemStorageSource(
        String sourceId,
        BlockPos position,
        ServerLevel level,
        BlockEntity owner,
        //? if < 26.2 {
        IItemHandler handler
        //?} else {
        ResourceHandler<ItemResource> handler
        //?}
    ) {
        this.sourceId = sourceId;
        this.position = position.immutable();
        this.level = level;
        this.owner = owner;
        this.handler = handler;
    }

    public static Optional<NeoForgeItemStorageSource> adapt(ServerLevel level, BlockPos position, BlockEntity owner) {
        //? if < 26.2 {
        IItemHandler handler = level.getCapability(Capabilities.ItemHandler.BLOCK, position, null);
        //?} else {
        ResourceHandler<ItemResource> handler = level.getCapability(Capabilities.Item.BLOCK, position, null);
        //?}
        if (handler == null) {
            return Optional.empty();
        }
        //? if < 26.2 {
        String sourceId = level.dimension().location() + "@" + position.getX() + "," + position.getY() + "," + position.getZ();
        //?} else {
        String sourceId = level.dimension().identifier() + "@" + position.getX() + "," + position.getY() + "," + position.getZ();
        //?}
        return Optional.of(new NeoForgeItemStorageSource(sourceId, position, level, owner, handler));
    }

    public Object deduplicationKey() { return handler; }
    @Override public String sourceId() { return sourceId; }
    @Override public BlockPos position() { return position; }

    @Override
    public StorageView snapshot() {
        List<StorageSlot> slots = new ArrayList<>(handlerSize());
        long revision = 0xcbf29ce484222325L;
        for (int slot = 0; slot < handlerSize(); slot++) {
            ItemStack stack = stackAt(slot);
            long capacity = Math.max(stack.getCount(), capacityAt(slot, stack));
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
        if (slot < 0 || slot >= handlerSize() || maximum <= 0
            || !ItemStackIdentity.matches(stackAt(slot), identity)) {
            return 0;
        }
        return removeAt(slot, bounded(maximum), true).getCount();
    }

    @Override
    public long extract(int slot, ItemIdentity identity, long maximum) {
        if (slot < 0 || slot >= handlerSize() || maximum <= 0
            || !ItemStackIdentity.matches(stackAt(slot), identity)) {
            return 0;
        }
        return removeAt(slot, bounded(maximum), false).getCount();
    }

    @Override
    public ItemStack extractStack(ItemIdentity identity, int maximum) {
        ItemStack result = ItemStack.EMPTY;
        int remaining = maximum;
        for (int slot = 0; slot < handlerSize() && remaining > 0; slot++) {
            if (!ItemStackIdentity.matches(stackAt(slot), identity)) {
                continue;
            }
            ItemStack removed = removeAt(slot, remaining, false);
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
        for (int slot = 0; slot < handlerSize(); slot++) {
            ItemStack stack = stackAt(slot);
            if (ItemStackIdentity.matches(stack, identity)) {
                ItemStack copy = stack.copy();
                copy.setCount(1);
                return copy;
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
            for (int slot = 0; slot < handlerSize() && !incoming.isEmpty(); slot++) {
                ItemStack current = stackAt(slot);
                boolean eligible = pass == 0 ? ItemStackIdentity.sameVariant(current, incoming) : current.isEmpty();
                if (!eligible) {
                    continue;
                }
                incoming.shrink(insertAt(slot, incoming));
            }
        }
        return before - incoming.getCount();
    }

    @Override
    public boolean contains(ItemIdentity identity) {
        for (int slot = 0; slot < handlerSize(); slot++) {
            if (ItemStackIdentity.matches(stackAt(slot), identity)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isValid() {
        return !owner.isRemoved() && level.isLoaded(position) && level.getBlockEntity(position) == owner
            //? if < 26.2 {
            && level.getCapability(Capabilities.ItemHandler.BLOCK, position, null) != null;
            //?} else {
            && level.getCapability(Capabilities.Item.BLOCK, position, null) != null;
            //?}
    }

    private int handlerSize() {
        //? if < 26.2 {
        return handler.getSlots();
        //?} else {
        return handler.size();
        //?}
    }

    private ItemStack stackAt(int slot) {
        //? if < 26.2 {
        return handler.getStackInSlot(slot);
        //?} else {
        return ItemUtil.getStack(handler, slot);
        //?}
    }

    private long capacityAt(int slot, ItemStack stack) {
        //? if < 26.2 {
        return handler.getSlotLimit(slot);
        //?} else {
        return handler.getCapacityAsLong(slot, stack.isEmpty() ? ItemResource.EMPTY : ItemResource.of(stack));
        //?}
    }

    private ItemStack removeAt(int slot, int maximum, boolean simulate) {
        //? if < 26.2 {
        return handler.extractItem(slot, maximum, simulate);
        //?} else {
        ItemResource resource = handler.getResource(slot);
        if (resource.isEmpty()) {
            return ItemStack.EMPTY;
        }
        try (Transaction transaction = Transaction.openRoot()) {
            int extracted = handler.extract(slot, resource, maximum, transaction);
            if (!simulate) {
                transaction.commit();
            }
            return resource.toStack(extracted);
        }
        //?}
    }

    private int insertAt(int slot, ItemStack incoming) {
        //? if < 26.2 {
        return incoming.getCount() - handler.insertItem(slot, incoming, false).getCount();
        //?} else {
        try (Transaction transaction = Transaction.openRoot()) {
            int inserted = handler.insert(slot, ItemResource.of(incoming), incoming.getCount(), transaction);
            transaction.commit();
            return inserted;
        }
        //?}
    }

    private static int bounded(long amount) { return (int) Math.min(Integer.MAX_VALUE, amount); }
    private static long mix(long hash, long value) { return (hash ^ value) * 0x100000001b3L; }
}
*///?}
