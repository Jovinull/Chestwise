package dev.chestwise.minecraft;

import dev.chestwise.core.RestockTargetBook;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.saveddata.SavedData;
//? if < 26.2 {
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
//?}
/*? if > 1.20.1 && < 26.2 {*/
/*import net.minecraft.core.HolderLookup;
import net.minecraft.util.datafix.DataFixTypes;
*//*?}*/
/*? if >= 26.2 {*/
/*import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedDataType;
*//*?}*/

/** World-saved, UUID-keyed restock preferences; player death and dimension changes do not clear them. */
public final class RestockSavedData extends SavedData {
    private static final String DATA_KEY = "chestwise_restock_targets";
    public static final int MAX_TARGETS = RestockTargetBook.MAX_TARGETS;

    private final Map<UUID, List<Target>> players = new LinkedHashMap<>();

    /*? if >= 26.2 {*/
    /*private static final Codec<SavedTarget> TARGET_CODEC = RecordCodecBuilder.create(instance -> instance.group(
        ItemStack.CODEC.fieldOf("item").forGetter(SavedTarget::item),
        Codec.INT.fieldOf("desired").forGetter(SavedTarget::desired)
    ).apply(instance, SavedTarget::new));
    private static final Codec<SavedPlayer> PLAYER_CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codec.STRING.fieldOf("uuid").forGetter(SavedPlayer::uuid),
        Codec.list(TARGET_CODEC).fieldOf("targets").forGetter(SavedPlayer::targets)
    ).apply(instance, SavedPlayer::new));
    private static final Codec<RestockSavedData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codec.list(PLAYER_CODEC).fieldOf("players").forGetter(RestockSavedData::serializedPlayers)
    ).apply(instance, RestockSavedData::fromSerializedPlayers));
    private static final SavedDataType<RestockSavedData> TYPE = new SavedDataType<>(
        ChestwiseContent.id("restock_targets"),
        RestockSavedData::new,
        CODEC,
        DataFixTypes.LEVEL
    );
    *//*?}*/

    private RestockSavedData() {
    }

    public static RestockSavedData get(ServerLevel level) {
        // Restock preferences belong to the player/world, not to a dimension.
        ServerLevel overworld = level.getServer().overworld();
        //? if <= 1.20.1 {
        return overworld.getDataStorage().computeIfAbsent(RestockSavedData::load, RestockSavedData::new, DATA_KEY);
        //?}
        /*? if > 1.20.1 && < 26.2 {*/
        /*return overworld.getDataStorage().computeIfAbsent(
            new SavedData.Factory<>(RestockSavedData::new, RestockSavedData::load, DataFixTypes.LEVEL),
            DATA_KEY
        );
        *//*?}*/
        /*? if >= 26.2 {*/
        /*return overworld.getDataStorage().computeIfAbsent(TYPE);
        *//*?}*/
    }

    public List<Target> targets(UUID playerId) {
        return List.copyOf(players.getOrDefault(playerId, List.of()));
    }

    public RestockTargetBook.Change setTarget(UUID playerId, ItemStack sample, int desiredCount) {
        if (sample.isEmpty() || desiredCount < 1
            || desiredCount > Math.min(4096L, (long) sample.getMaxStackSize() * 36L)) {
            return RestockTargetBook.Change.INVALID;
        }
        List<Target> current = players.computeIfAbsent(playerId, ignored -> new ArrayList<>());
        for (int index = 0; index < current.size(); index++) {
            Target target = current.get(index);
            if (ItemStackIdentity.sameVariant(target.item(), sample)) {
                current.set(index, new Target(sample, desiredCount));
                setDirty();
                return RestockTargetBook.Change.UPDATED;
            }
        }
        if (current.size() >= MAX_TARGETS) {
            return RestockTargetBook.Change.FULL;
        }
        current.add(new Target(sample, desiredCount));
        setDirty();
        return RestockTargetBook.Change.ADDED;
    }

    public boolean removeTarget(UUID playerId, int index) {
        List<Target> current = players.get(playerId);
        if (current == null || index < 0 || index >= current.size()) {
            return false;
        }
        current.remove(index);
        if (current.isEmpty()) {
            players.remove(playerId);
        }
        setDirty();
        return true;
    }

    //? if <= 1.20.1 {
    @Override
    public CompoundTag save(CompoundTag tag) {
        return writeLegacy(tag, stack -> stack.save(new CompoundTag()));
    }

    private static RestockSavedData load(CompoundTag tag) {
        return read(tag, value -> value instanceof CompoundTag compound ? ItemStack.of(compound) : ItemStack.EMPTY);
    }
    //?}

    /*? if > 1.20.1 && < 26.2 {*/
    /*@Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        return writeLegacy(tag, stack -> stack.save(registries));
    }

    private static RestockSavedData load(CompoundTag tag, HolderLookup.Provider registries) {
        return read(tag, value -> ItemStack.parse(registries, value).orElse(ItemStack.EMPTY));
    }
    *//*?}*/

    //? if < 26.2 {
    private CompoundTag writeLegacy(CompoundTag tag, Function<ItemStack, Tag> encodeStack) {
        ListTag playerList = new ListTag();
        for (Map.Entry<UUID, List<Target>> player : players.entrySet()) {
            CompoundTag playerTag = new CompoundTag();
            playerTag.putUUID("uuid", player.getKey());
            ListTag targetList = new ListTag();
            for (Target target : player.getValue()) {
                CompoundTag targetTag = new CompoundTag();
                targetTag.put("item", encodeStack.apply(target.item()));
                targetTag.putInt("desired", target.desiredCount());
                targetList.add(targetTag);
            }
            playerTag.put("targets", targetList);
            playerList.add(playerTag);
        }
        tag.put("players", playerList);
        return tag;
    }

    private static RestockSavedData read(CompoundTag tag, Function<Tag, ItemStack> decodeStack) {
        RestockSavedData data = new RestockSavedData();
        ListTag playerList = tag.getList("players", Tag.TAG_COMPOUND);
        for (int playerIndex = 0; playerIndex < playerList.size(); playerIndex++) {
            CompoundTag playerTag = playerList.getCompound(playerIndex);
            if (!playerTag.hasUUID("uuid")) {
                continue;
            }
            UUID playerId = playerTag.getUUID("uuid");
            List<Target> targets = new ArrayList<>();
            ListTag targetList = playerTag.getList("targets", Tag.TAG_COMPOUND);
            for (int targetIndex = 0; targetIndex < targetList.size() && targets.size() < MAX_TARGETS; targetIndex++) {
                CompoundTag targetTag = targetList.getCompound(targetIndex);
                ItemStack stack = decodeStack.apply(targetTag.get("item"));
                int desired = targetTag.getInt("desired");
                if (!stack.isEmpty() && desired > 0
                    && desired <= Math.min(4096L, (long) stack.getMaxStackSize() * 36L)
                    && targets.stream().noneMatch(existing -> ItemStackIdentity.sameVariant(existing.item(), stack))) {
                    targets.add(new Target(stack, desired));
                }
            }
            if (!targets.isEmpty()) {
                data.players.put(playerId, targets);
            }
        }
        return data;
    }
    //?}

    /*? if >= 26.2 {*/
    /*private List<SavedPlayer> serializedPlayers() {
        return players.entrySet().stream().map(entry -> new SavedPlayer(
            entry.getKey().toString(),
            entry.getValue().stream().map(target -> new SavedTarget(target.item(), target.desiredCount())).toList()
        )).toList();
    }

    private static RestockSavedData fromSerializedPlayers(List<SavedPlayer> serialized) {
        RestockSavedData data = new RestockSavedData();
        for (SavedPlayer player : serialized) {
            UUID playerId;
            try {
                playerId = UUID.fromString(player.uuid());
            } catch (IllegalArgumentException exception) {
                continue;
            }
            List<Target> targets = new ArrayList<>();
            for (SavedTarget target : player.targets()) {
                ItemStack stack = target.item();
                if (!stack.isEmpty() && target.desired() > 0
                    && target.desired() <= Math.min(4096L, (long) stack.getMaxStackSize() * 36L)
                    && targets.size() < MAX_TARGETS
                    && targets.stream().noneMatch(existing -> ItemStackIdentity.sameVariant(existing.item(), stack))) {
                    targets.add(new Target(stack, target.desired()));
                }
            }
            if (!targets.isEmpty()) {
                data.players.put(playerId, targets);
            }
        }
        return data;
    }

    private record SavedTarget(ItemStack item, int desired) {
    }

    private record SavedPlayer(String uuid, List<SavedTarget> targets) {
        private SavedPlayer {
            targets = List.copyOf(targets);
        }
    }
    *//*?}*/

    public record Target(ItemStack item, int desiredCount) {
        public Target {
            item = singleItem(item);
            if (item.isEmpty() || desiredCount < 1) {
                throw new IllegalArgumentException("Invalid persisted restock target");
            }
        }

        @Override
        public ItemStack item() {
            return singleItem(item);
        }

        private static ItemStack singleItem(ItemStack input) {
            ItemStack copy = input.copy();
            if (!copy.isEmpty()) {
                copy.setCount(1);
            }
            return copy;
        }
    }
}
