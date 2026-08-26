package dev.chestwise.minecraft;

import dev.chestwise.core.ItemDescriptor;
import dev.chestwise.core.ItemIdentity;
import dev.chestwise.core.StorageSlot;
import dev.chestwise.core.StorageView;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

public final class ContainerStorageSource implements MinecraftStorageSource {
    private final String sourceId;
    private final BlockPos position;
    private final Container container;
    private final BlockEntity owner;

    public ContainerStorageSource(String sourceId, BlockPos position, Container container, BlockEntity owner) {
        this.sourceId = Objects.requireNonNull(sourceId, "sourceId");
        this.position = position.immutable();
        this.container = Objects.requireNonNull(container, "container");
        this.owner = Objects.requireNonNull(owner, "owner");
    }

    @Override
    public String sourceId() {
        return sourceId;
    }

    @Override
    public BlockPos position() {
        return position;
    }

    @Override
    public StorageView snapshot() {
        List<StorageSlot> slots = new ArrayList<>(container.getContainerSize());
        long revision = 0xcbf29ce484222325L;
        for (int slot = 0; slot < container.getContainerSize(); slot++) {
            ItemStack stack = container.getItem(slot);
            if (stack.isEmpty()) {
                slots.add(StorageSlot.empty(slot, container.getMaxStackSize()));
                revision = mix(revision, slot);
                continue;
            }
            ItemDescriptor descriptor = ItemStackIdentity.describe(stack);
            long capacity = Math.min(container.getMaxStackSize(), stack.getMaxStackSize());
            slots.add(new StorageSlot(slot, descriptor, stack.getCount(), capacity));
            revision = mix(mix(revision, descriptor.identity().hashCode()), stack.getCount());
        }
        return new StorageView(sourceId, revision & Long.MAX_VALUE, slots);
    }

    @Override
    public long simulateExtract(int slot, ItemIdentity identity, long maximum) {
        if (slot < 0 || slot >= container.getContainerSize() || maximum <= 0) {
            return 0;
        }
        ItemStack stack = container.getItem(slot);
        return ItemStackIdentity.matches(stack, identity) ? Math.min(maximum, stack.getCount()) : 0;
    }

    @Override
    public long extract(int slot, ItemIdentity identity, long maximum) {
        long allowed = simulateExtract(slot, identity, maximum);
        if (allowed == 0) {
            return 0;
        }
        ItemStack removed = container.removeItem(slot, (int) allowed);
        container.setChanged();
        return removed.getCount();
    }

    @Override
    public ItemStack extractStack(ItemIdentity identity, int maximum) {
        if (maximum <= 0) {
            return ItemStack.EMPTY;
        }
        ItemStack result = ItemStack.EMPTY;
        int remaining = maximum;
        for (int slot = 0; slot < container.getContainerSize() && remaining > 0; slot++) {
            ItemStack current = container.getItem(slot);
            if (!ItemStackIdentity.matches(current, identity)) {
                continue;
            }
            int amount = Math.min(remaining, current.getCount());
            ItemStack removed = container.removeItem(slot, amount);
            if (result.isEmpty()) {
                result = removed;
            } else {
                result.grow(removed.getCount());
            }
            remaining -= removed.getCount();
        }
        if (!result.isEmpty()) {
            container.setChanged();
        }
        return result;
    }

    @Override
    public ItemStack representative(ItemIdentity identity) {
        return prototype(identity);
    }

    @Override
    public int insertStack(ItemStack incoming, boolean matchingOnly) {
        if (incoming.isEmpty()) {
            return 0;
        }
        int before = incoming.getCount();
        for (int pass = 0; pass < 2 && !incoming.isEmpty(); pass++) {
            for (int slot = 0; slot < container.getContainerSize() && !incoming.isEmpty(); slot++) {
                ItemStack current = container.getItem(slot);
                boolean eligible = pass == 0
                    ? ItemStackIdentity.sameVariant(current, incoming)
                    : current.isEmpty() && !matchingOnly;
                if (!eligible || !container.canPlaceItem(slot, incoming)) {
                    continue;
                }
                int capacity = Math.min(container.getMaxStackSize(), incoming.getMaxStackSize());
                int moved = Math.min(incoming.getCount(), capacity - current.getCount());
                if (moved <= 0) {
                    continue;
                }
                if (current.isEmpty()) {
                    ItemStack placed = incoming.copy();
                    placed.setCount(moved);
                    container.setItem(slot, placed);
                } else {
                    current.grow(moved);
                }
                incoming.shrink(moved);
            }
        }
        int inserted = before - incoming.getCount();
        if (inserted > 0) {
            container.setChanged();
        }
        return inserted;
    }

    @Override
    public boolean contains(ItemIdentity identity) {
        for (int slot = 0; slot < container.getContainerSize(); slot++) {
            if (ItemStackIdentity.matches(container.getItem(slot), identity)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isValid() {
        return !owner.isRemoved() && owner.getLevel() != null && owner.getLevel().isLoaded(position);
    }

    private ItemStack prototype(ItemIdentity identity) {
        for (int slot = 0; slot < container.getContainerSize(); slot++) {
            ItemStack stack = container.getItem(slot);
            if (ItemStackIdentity.matches(stack, identity)) {
                ItemStack copy = stack.copy();
                copy.setCount(1);
                return copy;
            }
        }
        return ItemStack.EMPTY;
    }

    private static long mix(long hash, long value) {
        return (hash ^ value) * 0x100000001b3L;
    }
}
