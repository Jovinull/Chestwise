package dev.chestwise.minecraft;

import dev.chestwise.core.ItemIdentity;
import dev.chestwise.core.StorageSource;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;

public interface MinecraftStorageSource extends StorageSource {
    BlockPos position();

    ItemStack extractStack(ItemIdentity identity, int maximum);

    ItemStack representative(ItemIdentity identity);

    int insertStack(ItemStack stack, boolean matchingOnly);

    boolean contains(ItemIdentity identity);
}
