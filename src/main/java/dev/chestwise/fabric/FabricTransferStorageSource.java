package dev.chestwise.fabric;

//? if fabric {
import dev.chestwise.core.ItemDescriptor;
import dev.chestwise.core.ItemIdentity;
import dev.chestwise.core.StorageSlot;
import dev.chestwise.core.StorageView;
import dev.chestwise.minecraft.ItemStackIdentity;
import dev.chestwise.minecraft.MinecraftStorageSource;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

/** Transactional bridge for modded storages exposed through Fabric Transfer API. */
public final class FabricTransferStorageSource implements MinecraftStorageSource {
    private final String sourceId;
    private final BlockPos position;
    private final ServerLevel level;
    private final BlockEntity owner;
    private final Storage<ItemVariant> storage;

    private FabricTransferStorageSource(
        String sourceId,
        BlockPos position,
        ServerLevel level,
        BlockEntity owner,
        Storage<ItemVariant> storage
    ) {
        this.sourceId = sourceId;
        this.position = position.immutable();
        this.level = level;
        this.owner = owner;
        this.storage = storage;
    }

    public static Optional<FabricTransferStorageSource> adapt(
        ServerLevel level,
        BlockPos position,
        BlockEntity owner
    ) {
        Storage<ItemVariant> storage = ItemStorage.SIDED.find(level, position, null);
        if (storage == null || !storage.supportsExtraction() && !storage.supportsInsertion()) {
            return Optional.empty();
        }
        //? if < 26.2 {
        String sourceId = level.dimension().location() + "@" + position.getX() + "," + position.getY() + "," + position.getZ();
        //?} else {
        /*String sourceId = level.dimension().identifier() + "@" + position.getX() + "," + position.getY() + "," + position.getZ();
        *///?}
        return Optional.of(new FabricTransferStorageSource(sourceId, position, level, owner, storage));
    }

    public Object deduplicationKey() {
        return storage;
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
        List<StorageSlot> slots = new ArrayList<>();
        long revision = 0xcbf29ce484222325L;
        int slot = 0;
        for (net.fabricmc.fabric.api.transfer.v1.storage.StorageView<ItemVariant> view : storage) {
            long count = view.getAmount();
            long capacity = Math.max(count, view.getCapacity());
            if (view.isResourceBlank() || count == 0) {
                slots.add(StorageSlot.empty(slot++, capacity));
                revision = mix(revision, capacity);
                continue;
            }
            ItemStack stack = view.getResource().toStack();
            ItemDescriptor descriptor = ItemStackIdentity.describe(stack);
            slots.add(new StorageSlot(slot++, descriptor, count, capacity));
            revision = mix(mix(revision, descriptor.identity().hashCode()), count);
        }
        return new StorageView(sourceId, revision & Long.MAX_VALUE, slots);
    }

    @Override
    public long simulateExtract(int slot, ItemIdentity identity, long maximum) {
        net.fabricmc.fabric.api.transfer.v1.storage.StorageView<ItemVariant> view = view(slot);
        ItemVariant variant = variant(identity);
        if (view == null || variant == null || maximum <= 0) {
            return 0;
        }
        try (Transaction transaction = Transaction.openOuter()) {
            return view.extract(variant, maximum, transaction);
        }
    }

    @Override
    public long extract(int slot, ItemIdentity identity, long maximum) {
        net.fabricmc.fabric.api.transfer.v1.storage.StorageView<ItemVariant> view = view(slot);
        ItemVariant variant = variant(identity);
        if (view == null || variant == null || maximum <= 0) {
            return 0;
        }
        try (Transaction transaction = Transaction.openOuter()) {
            long extracted = view.extract(variant, maximum, transaction);
            transaction.commit();
            return extracted;
        }
    }

    @Override
    public ItemStack extractStack(ItemIdentity identity, int maximum) {
        ItemVariant variant = variant(identity);
        if (variant == null || maximum <= 0) {
            return ItemStack.EMPTY;
        }
        try (Transaction transaction = Transaction.openOuter()) {
            long extracted = storage.extract(variant, maximum, transaction);
            if (extracted <= 0) {
                return ItemStack.EMPTY;
            }
            transaction.commit();
            return variant.toStack(Math.toIntExact(extracted));
        }
    }

    @Override
    public ItemStack representative(ItemIdentity identity) {
        ItemVariant variant = variant(identity);
        return variant == null ? ItemStack.EMPTY : variant.toStack();
    }

    @Override
    public int insertStack(ItemStack stack, boolean matchingOnly) {
        if (stack.isEmpty() || matchingOnly && !contains(ItemStackIdentity.identity(stack))) {
            return 0;
        }
        ItemVariant variant = ItemVariant.of(stack);
        try (Transaction transaction = Transaction.openOuter()) {
            long inserted = storage.insert(variant, stack.getCount(), transaction);
            if (inserted <= 0) {
                return 0;
            }
            transaction.commit();
            stack.shrink(Math.toIntExact(inserted));
            return Math.toIntExact(inserted);
        }
    }

    @Override
    public boolean contains(ItemIdentity identity) {
        return variant(identity) != null;
    }

    @Override
    public boolean isValid() {
        return !owner.isRemoved() && level.isLoaded(position) && level.getBlockEntity(position) == owner;
    }

    private ItemVariant variant(ItemIdentity identity) {
        for (net.fabricmc.fabric.api.transfer.v1.storage.StorageView<ItemVariant> view : storage.nonEmptyViews()) {
            ItemVariant resource = view.getResource();
            if (ItemStackIdentity.matches(resource.toStack(), identity)) {
                return resource;
            }
        }
        return null;
    }

    private net.fabricmc.fabric.api.transfer.v1.storage.StorageView<ItemVariant> view(int requested) {
        if (requested < 0) {
            return null;
        }
        int index = 0;
        for (net.fabricmc.fabric.api.transfer.v1.storage.StorageView<ItemVariant> view : storage) {
            if (index++ == requested) {
                return view;
            }
        }
        return null;
    }

    private static long mix(long hash, long value) {
        return (hash ^ value) * 0x100000001b3L;
    }
}
//?}
