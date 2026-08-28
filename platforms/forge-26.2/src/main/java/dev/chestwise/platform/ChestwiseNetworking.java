package dev.chestwise.platform;

import dev.chestwise.minecraft.ChestwiseContent;
import dev.chestwise.minecraft.StorageTerminalMenu;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraftforge.event.network.CustomPayloadEvent;
import net.minecraftforge.network.Channel;
import net.minecraftforge.network.ChannelBuilder;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.SimpleChannel;

/** Forge network bridge. The packet carries intent only, never a mutation plan. */
public final class ChestwiseNetworking {
    /** Nine slots of comma-separated item ids need far more room than a search box. */
    private static final int MAX_PAYLOAD = 1024;
    private static final int KIND_SEARCH = 0;
    private static final int KIND_RECIPE = 1;

    private static final SimpleChannel CHANNEL = ChannelBuilder.named(ChestwiseContent.id("main"))
        .networkProtocolVersion(1)
        .acceptedVersions(Channel.VersionTest.exact(1))
        .simpleChannel();
    private static final StreamCodec<RegistryFriendlyByteBuf, TerminalIntent> INTENT_CODEC = StreamCodec.of(
        (buffer, intent) -> {
            buffer.writeVarInt(intent.kind);
            buffer.writeUtf(intent.payload, MAX_PAYLOAD);
        },
        buffer -> new TerminalIntent(buffer.readVarInt(), buffer.readUtf(MAX_PAYLOAD))
    );

    private ChestwiseNetworking() {
    }

    public static void registerServer() {
        CHANNEL.play().serverbound()
            .addMain(TerminalIntent.class, INTENT_CODEC, ChestwiseNetworking::handleSearch)
            .build();
    }

    public static void sendSearch(String query) {
        send(KIND_SEARCH, query);
    }

    /** Asks the server to lay a recipe out on the terminal's crafting grid. */
    public static void sendRecipe(String encodedSlots) {
        send(KIND_RECIPE, encodedSlots);
    }

    private static void send(int kind, String payload) {
        CHANNEL.send(new TerminalIntent(kind, clamp(payload)), PacketDistributor.SERVER.noArg());
    }

    private static void applyIntent(net.minecraft.world.entity.player.Player player, int kind, String payload) {
        if (player == null || !(player.containerMenu instanceof StorageTerminalMenu menu)) {
            return;
        }
        if (kind == KIND_RECIPE) {
            menu.fillRecipe(player, payload);
        } else {
            menu.updateQuery(payload);
        }
    }

    private static String clamp(String value) {
        return value.length() > MAX_PAYLOAD ? value.substring(0, MAX_PAYLOAD) : value;
    }

    private record TerminalIntent(int kind, String payload) {
        private TerminalIntent {
            payload = payload.length() > MAX_PAYLOAD ? payload.substring(0, MAX_PAYLOAD) : payload;
        }
    }

    private static void handleSearch(TerminalIntent intent, CustomPayloadEvent.Context context) {
        if (context.getSender() != null) {
            applyIntent(context.getSender(), intent.kind, intent.payload);
        }
        context.setPacketHandled(true);
    }
}
