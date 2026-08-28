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
    /** Nine slots of comma-separated item ids need considerably more than a search box. */
    private static final int MAX_PAYLOAD = 1024;
    private static final int KIND_SEARCH = 0;
    private static final int KIND_RECIPE = 1;

    //? if fabric && <= 1.20.1 {
    private static final ResourceLocation INTENT = ChestwiseContent.id("intent");
    //?}
    /*? if (fabric || neoforge) && > 1.20.1 {*/
    /*private static final CustomPacketPayload.Type<TerminalIntent> INTENT_TYPE =
        new CustomPacketPayload.Type<>(ChestwiseContent.id("intent"));
    private static final StreamCodec<RegistryFriendlyByteBuf, TerminalIntent> INTENT_CODEC = StreamCodec.of(
        (buffer, intent) -> {
            buffer.writeVarInt(intent.kind);
            buffer.writeUtf(intent.payload, MAX_PAYLOAD);
        },
        buffer -> new TerminalIntent(buffer.readVarInt(), buffer.readUtf(MAX_PAYLOAD))
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

    private static String clamp(String value) {
        return value.length() > MAX_PAYLOAD ? value.substring(0, MAX_PAYLOAD) : value;
    }

    public static void registerServer() {
        //? if fabric && <= 1.20.1 {
        ServerPlayNetworking.registerGlobalReceiver(INTENT, (server, player, handler, buffer, responseSender) -> {
            int kind = buffer.readVarInt();
            String payload = buffer.readUtf(MAX_PAYLOAD);
            server.execute(() -> applyIntent(player, kind, payload));
        });
        //?}
        /*? if fabric && > 1.20.1 {*/
        /*//? if < 26.2 {
        PayloadTypeRegistry.playC2S().register(INTENT_TYPE, INTENT_CODEC);
        //?} else {
        PayloadTypeRegistry.serverboundPlay().register(INTENT_TYPE, INTENT_CODEC);
        //?}
        ServerPlayNetworking.registerGlobalReceiver(INTENT_TYPE, (intent, context) ->
            context.server().execute(() -> applyIntent(context.player(), intent.kind, intent.payload))
        );
        *//*?}*/
        /*? if forgeLike && <= 1.20.1 {*/
        /*CHANNEL.registerMessage(0, TerminalIntent.class, TerminalIntent::encode, TerminalIntent::decode, TerminalIntent::handle);
        *//*?}*/
        /*? if forge && > 1.20.1 {*/
        /*CHANNEL.messageBuilder(TerminalIntent.class)
            .encoder(TerminalIntent::encode)
            .decoder(TerminalIntent::decode)
            .consumerMainThread(ChestwiseNetworking::handleForgeSearch)
            .add();
        *//*?}*/
    }

    //? if neoforge && > 1.20.1 {
    /*public static void registerNeoForge(IEventBus modBus) {
        modBus.addListener(ChestwiseNetworking::registerNeoForgePayloads);
    }

    private static void registerNeoForgePayloads(RegisterPayloadHandlersEvent event) {
        event.registrar("1").playToServer(INTENT_TYPE, INTENT_CODEC, (intent, context) ->
            context.enqueueWork(() -> applyIntent(context.player(), intent.kind, intent.payload))
        );
    }
    *///?}

    public static void sendSearch(String query) {
        send(KIND_SEARCH, query);
    }

    /** Asks the server to lay a recipe out on the terminal's crafting grid. */
    public static void sendRecipe(String encodedSlots) {
        send(KIND_RECIPE, encodedSlots);
    }

    private static void send(int kind, String payload) {
        //? if fabric && <= 1.20.1 {
        FriendlyByteBuf buffer = PacketByteBufs.create();
        buffer.writeVarInt(kind);
        buffer.writeUtf(clamp(payload), MAX_PAYLOAD);
        ClientPlayNetworking.send(INTENT, buffer);
        //?}
        /*? if fabric && > 1.20.1 {*/
        /*ClientPlayNetworking.send(new TerminalIntent(kind, payload));
        *//*?}*/
        /*? if neoforge && > 1.20.1 {*/
        /*//? if < 26.2 {
        PacketDistributor.sendToServer(new TerminalIntent(kind, payload));
        //?} else {
        ClientPacketDistributor.sendToServer(new TerminalIntent(kind, payload));
        //?}
        *//*?}*/
        /*? if forgeLike && <= 1.20.1 {*/
        /*CHANNEL.sendToServer(new TerminalIntent(kind, payload));
        *//*?}*/
        /*? if forge && > 1.20.1 {*/
        /*CHANNEL.send(new TerminalIntent(kind, payload), PacketDistributor.SERVER.noArg());
        *//*?}*/
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

    //? if (fabric || neoforge) && > 1.20.1 {
    /*private record TerminalIntent(int kind, String payload) implements CustomPacketPayload {
        private TerminalIntent {
            payload = clamp(payload);
        }

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return INTENT_TYPE;
        }
    }
    *///?}

    //? if forgeLike && <= 1.20.1 {
    /*private record TerminalIntent(int kind, String payload) {
        private TerminalIntent {
            payload = clamp(payload);
        }

        private static void encode(TerminalIntent intent, FriendlyByteBuf buffer) {
            buffer.writeVarInt(intent.kind);
            buffer.writeUtf(intent.payload, MAX_PAYLOAD);
        }

        private static TerminalIntent decode(FriendlyByteBuf buffer) {
            return new TerminalIntent(buffer.readVarInt(), buffer.readUtf(MAX_PAYLOAD));
        }

        private static void handle(TerminalIntent intent, Supplier<NetworkEvent.Context> contextSupplier) {
            NetworkEvent.Context context = contextSupplier.get();
            context.enqueueWork(() -> {
                if (context.getSender() != null) {
                    applyIntent(context.getSender(), intent.kind, intent.payload);
                }
            });
            context.setPacketHandled(true);
        }
    }
    *///?}

    //? if forge && > 1.20.1 {
    /*private record TerminalIntent(int kind, String payload) {
        private TerminalIntent {
            payload = clamp(payload);
        }

        private static void encode(TerminalIntent intent, FriendlyByteBuf buffer) {
            buffer.writeVarInt(intent.kind);
            buffer.writeUtf(intent.payload, MAX_PAYLOAD);
        }

        private static TerminalIntent decode(FriendlyByteBuf buffer) {
            return new TerminalIntent(buffer.readVarInt(), buffer.readUtf(MAX_PAYLOAD));
        }
    }

    private static void handleForgeSearch(TerminalIntent intent, CustomPayloadEvent.Context context) {
        if (context.getSender() != null) {
            applyIntent(context.getSender(), intent.kind, intent.payload);
        }
        context.setPacketHandled(true);
    }
    *///?}
}
