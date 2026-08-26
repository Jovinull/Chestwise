package dev.chestwise.platform;

import dev.chestwise.minecraft.ChestwiseContent;
import dev.chestwise.minecraft.StorageTerminalMenu;

//? if fabric && <= 1.20.1 {
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
//?}

//? if fabric && > 1.20.1 {
/*import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
*///?}

//? if (fabric || neoforge) && > 1.20.1 {
/*import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
*///?}

//? if neoforge && > 1.20.1 {
/*import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.network.PacketDistributor;
//? if >= 26.2 {
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
//?}
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
*///?}

//? if forgeLike && <= 1.20.1 {
/*import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
*///?}

//? if forge && > 1.20.1 {
/*import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.event.network.CustomPayloadEvent;
import net.minecraftforge.network.Channel;
import net.minecraftforge.network.ChannelBuilder;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.SimpleChannel;
*///?}

/** Loader networking bridge. Packets carry intent only; the active server menu performs validation. */
@SuppressWarnings({"deprecation", "removal"})
public final class ChestwiseNetworking {
    //? if fabric && <= 1.20.1 {
    private static final ResourceLocation SEARCH = ChestwiseContent.id("search");
    //?}
    /*? if (fabric || neoforge) && > 1.20.1 {*/
    /*private static final CustomPacketPayload.Type<SearchIntent> SEARCH_TYPE =
        new CustomPacketPayload.Type<>(ChestwiseContent.id("search"));
    private static final StreamCodec<RegistryFriendlyByteBuf, SearchIntent> SEARCH_CODEC = StreamCodec.of(
        (buffer, intent) -> buffer.writeUtf(intent.query, 80),
        buffer -> new SearchIntent(buffer.readUtf(80))
    );
    *//*?}*/
    /*? if forgeLike && <= 1.20.1 {*/
    /*private static final String PROTOCOL = "1";
    private static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
        ChestwiseContent.id("main"),
        () -> PROTOCOL,
        PROTOCOL::equals,
        PROTOCOL::equals
    );
    *//*?}*/
    /*? if forge && > 1.20.1 {*/
    /*private static final SimpleChannel CHANNEL = ChannelBuilder.named(ChestwiseContent.id("main"))
        .networkProtocolVersion(1)
        .acceptedVersions(Channel.VersionTest.exact(1))
        .simpleChannel();
    *//*?}*/

    private ChestwiseNetworking() {
    }

    public static void registerServer() {
        //? if fabric && <= 1.20.1 {
        ServerPlayNetworking.registerGlobalReceiver(SEARCH, (server, player, handler, buffer, responseSender) -> {
            String query = buffer.readUtf(80);
            server.execute(() -> applySearch(player.containerMenu, query));
        });
        //?}
        /*? if fabric && > 1.20.1 {*/
        /*//? if < 26.2 {
        PayloadTypeRegistry.playC2S().register(SEARCH_TYPE, SEARCH_CODEC);
        //?} else {
        PayloadTypeRegistry.serverboundPlay().register(SEARCH_TYPE, SEARCH_CODEC);
        //?}
        ServerPlayNetworking.registerGlobalReceiver(SEARCH_TYPE, (intent, context) ->
            context.server().execute(() -> applySearch(context.player().containerMenu, intent.query))
        );
        *//*?}*/
        /*? if forgeLike && <= 1.20.1 {*/
        /*CHANNEL.registerMessage(0, SearchIntent.class, SearchIntent::encode, SearchIntent::decode, SearchIntent::handle);
        *//*?}*/
        /*? if forge && > 1.20.1 {*/
        /*CHANNEL.messageBuilder(SearchIntent.class)
            .encoder(SearchIntent::encode)
            .decoder(SearchIntent::decode)
            .consumerMainThread(ChestwiseNetworking::handleForgeSearch)
            .add();
        *//*?}*/
    }

    //? if neoforge && > 1.20.1 {
    /*public static void registerNeoForge(IEventBus modBus) {
        modBus.addListener(ChestwiseNetworking::registerNeoForgePayloads);
    }

    private static void registerNeoForgePayloads(RegisterPayloadHandlersEvent event) {
        event.registrar("1").playToServer(SEARCH_TYPE, SEARCH_CODEC, (intent, context) ->
            context.enqueueWork(() -> applySearch(context.player().containerMenu, intent.query))
        );
    }
    *///?}

    public static void sendSearch(String query) {
        //? if fabric && <= 1.20.1 {
        FriendlyByteBuf buffer = PacketByteBufs.create();
        buffer.writeUtf(query, 80);
        ClientPlayNetworking.send(SEARCH, buffer);
        //?}
        /*? if fabric && > 1.20.1 {*/
        /*ClientPlayNetworking.send(new SearchIntent(query));
        *//*?}*/
        /*? if neoforge && > 1.20.1 {*/
        /*//? if < 26.2 {
        PacketDistributor.sendToServer(new SearchIntent(query));
        //?} else {
        ClientPacketDistributor.sendToServer(new SearchIntent(query));
        //?}
        *//*?}*/
        /*? if forgeLike && <= 1.20.1 {*/
        /*CHANNEL.sendToServer(new SearchIntent(query));
        *//*?}*/
        /*? if forge && > 1.20.1 {*/
        /*CHANNEL.send(new SearchIntent(query), PacketDistributor.SERVER.noArg());
        *//*?}*/
    }

    private static void applySearch(net.minecraft.world.inventory.AbstractContainerMenu activeMenu, String query) {
        if (activeMenu instanceof StorageTerminalMenu menu) {
            menu.updateQuery(query);
        }
    }

    //? if (fabric || neoforge) && > 1.20.1 {
    /*private record SearchIntent(String query) implements CustomPacketPayload {
        private SearchIntent {
            query = query.length() > 80 ? query.substring(0, 80) : query;
        }

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return SEARCH_TYPE;
        }
    }
    *///?}

    //? if forgeLike && <= 1.20.1 {
    /*private record SearchIntent(String query) {
        private SearchIntent {
            query = query.length() > 80 ? query.substring(0, 80) : query;
        }

        private static void encode(SearchIntent intent, FriendlyByteBuf buffer) {
            buffer.writeUtf(intent.query, 80);
        }

        private static SearchIntent decode(FriendlyByteBuf buffer) {
            return new SearchIntent(buffer.readUtf(80));
        }

        private static void handle(SearchIntent intent, Supplier<NetworkEvent.Context> contextSupplier) {
            NetworkEvent.Context context = contextSupplier.get();
            context.enqueueWork(() -> {
                if (context.getSender() != null) {
                    applySearch(context.getSender().containerMenu, intent.query);
                }
            });
            context.setPacketHandled(true);
        }
    }
    *///?}

    //? if forge && > 1.20.1 {
    /*private record SearchIntent(String query) {
        private SearchIntent {
            query = query.length() > 80 ? query.substring(0, 80) : query;
        }

        private static void encode(SearchIntent intent, FriendlyByteBuf buffer) {
            buffer.writeUtf(intent.query, 80);
        }

        private static SearchIntent decode(FriendlyByteBuf buffer) {
            return new SearchIntent(buffer.readUtf(80));
        }
    }

    private static void handleForgeSearch(SearchIntent intent, CustomPayloadEvent.Context context) {
        if (context.getSender() != null) {
            applySearch(context.getSender().containerMenu, intent.query);
        }
        context.setPacketHandled(true);
    }
    *///?}
}
