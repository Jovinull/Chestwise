package dev.chestwise.minecraft;

import dev.chestwise.core.StorageDiscovery;
import dev.chestwise.core.StorageSource;
import dev.chestwise.forge.ForgeItemStorageSource;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;

/** Discovers only block entities in chunks already loaded by the server. */
public final class LoadedContainerDiscovery implements StorageDiscovery<BlockPos> {
    private final ServerLevel level;

    public LoadedContainerDiscovery(ServerLevel level) {
        this.level = level;
    }

    @Override
    public Collection<StorageSource> discover(BlockPos terminal, int radius, int maximumSources) {
        List<BlockEntity> candidates = new ArrayList<>();
        int minChunkX = (terminal.getX() - radius) >> 4;
        int maxChunkX = (terminal.getX() + radius) >> 4;
        int minChunkZ = (terminal.getZ() - radius) >> 4;
        int maxChunkZ = (terminal.getZ() + radius) >> 4;
        for (int chunkX = minChunkX; chunkX <= maxChunkX; chunkX++) {
            for (int chunkZ = minChunkZ; chunkZ <= maxChunkZ; chunkZ++) {
                LevelChunk chunk = level.getChunkSource().getChunkNow(chunkX, chunkZ);
                if (chunk == null) {
                    continue;
                }
                for (BlockEntity blockEntity : chunk.getBlockEntities().values()) {
                    BlockPos position = blockEntity.getBlockPos();
                    if (position.equals(terminal)
                        || Math.abs(position.getX() - terminal.getX()) > radius
                        || Math.abs(position.getY() - terminal.getY()) > radius
                        || Math.abs(position.getZ() - terminal.getZ()) > radius) {
                        continue;
                    }
                    candidates.add(blockEntity);
                }
            }
        }

        candidates.sort(Comparator
            .comparingDouble((BlockEntity entity) -> entity.getBlockPos().distSqr(terminal))
            .thenComparingLong(entity -> entity.getBlockPos().asLong()));
        List<StorageSource> result = new ArrayList<>(Math.min(maximumSources, candidates.size()));
        Set<Object> platformKeys = Collections.newSetFromMap(new IdentityHashMap<>());
        for (BlockEntity candidate : candidates) {
            if (result.size() == maximumSources) {
                break;
            }
            BlockPos position = candidate.getBlockPos();
            String sourceId = level.dimension().identifier() + "@" + position.getX() + "," + position.getY() + "," + position.getZ();
            if (candidate instanceof Container container) {
                result.add(new ContainerStorageSource(sourceId, position, container, candidate));
                continue;
            }
            ForgeItemStorageSource.adapt(level, position, candidate).ifPresent(source -> {
                if (platformKeys.add(source.deduplicationKey())) {
                    result.add(source);
                }
            });
        }
        return result;
    }
}
