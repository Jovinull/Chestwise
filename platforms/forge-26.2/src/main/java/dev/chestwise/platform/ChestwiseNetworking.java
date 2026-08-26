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

/** Forge network bridge. The packet carries search intent, never a mutation plan. */
public final class ChestwiseNetworking {
    private static final SimpleChannel CHANNEL = ChannelBuilder.named(ChestwiseContent.id("main"))
        .networkProtocolVersion(1)
        .acceptedVersions(Channel.VersionTest.exact(1))
        .simpleChannel();
    private static final StreamCodec<RegistryFriendlyByteBuf, SearchIntent> SEARCH_CODEC = StreamCodec.of(
        (buffer, intent) -> buffer.writeUtf(intent.query, 80),
        buffer -> new SearchIntent(buffer.readUtf(80))
    );

    private ChestwiseNetworking() {
    }

    public static void registerServer() {
        CHANNEL.play().serverbound()
            .addMain(SearchIntent.class, SEARCH_CODEC, ChestwiseNetworking::handleSearch)
            .build();
    }

    public static void sendSearch(String query) {
        CHANNEL.send(new SearchIntent(query), PacketDistributor.SERVER.noArg());
    }

    private static void applySearch(net.minecraft.world.inventory.AbstractContainerMenu activeMenu, String query) {
        if (activeMenu instanceof StorageTerminalMenu menu) {
            menu.updateQuery(query);
        }
    }

    private record SearchIntent(String query) {
        private SearchIntent {
            query = query.length() > 80 ? query.substring(0, 80) : query;
        }

    }

    private static void handleSearch(SearchIntent intent, CustomPayloadEvent.Context context) {
        if (context.getSender() != null) {
            applySearch(context.getSender().containerMenu, intent.query);
        }
        context.setPacketHandled(true);
    }
}
