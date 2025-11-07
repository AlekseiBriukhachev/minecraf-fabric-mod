package com.fabric.messagemod.network;

import com.fabric.messagemod.database.DatabaseManager;
import com.fabric.messagemod.network.proto.;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NetworkHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(NetworkHandler.class);
    public static final ResourceLocation MESSAGE_PACKET_ID =
            ResourceLocation.fromNamespaceAndPath("messagemod", "message");

    public static void registerPackets() {
        PayloadTypeRegistry.playC2S().register(MessagePacket.TYPE, MessagePacket.CODEC);
    }

    public static void registerServerHandlers() {
        ServerPlayNetworking.registerGlobalReceiver(MessagePacket.TYPE, (packet, context) -> {
            context.server().execute(() -> {
                try {
                    MessageProto.Message message = MessageProto.Message.parseFrom(packet.data());
                    String text = message.getText();

                    if (text != null && !text.isEmpty() && text.length() <= 256) {
                        DatabaseManager.getInstance()
                                .getMessageRepository()
                                .save(context.player().getUUID(), text);

                        LOGGER.info("Saved message from player {}: {}",
                                context.player().getName().getString(), text);
                    } else {
                        LOGGER.warn("Invalid message from player {}",
                                context.player().getName().getString());
                    }
                } catch (InvalidProtocolBufferException e) {
                    LOGGER.error("Failed to parse protobuf message", e);
                }
            });
        });
    }

    public record MessagePacket(byte[] data) implements CustomPacketPayload {

        public static final Type<MessagePacket> TYPE =
                new Type<>(MESSAGE_PACKET_ID);

        public static final StreamCodec<RegistryFriendlyByteBuf, MessagePacket> CODEC =
                StreamCodec.of(
                        (buf, packet) -> {
                            buf.writeInt(packet.data.length);
                            buf.writeBytes(packet.data);
                        },
                        buf -> {
                            int length = buf.readInt();
                            byte[] data = new byte[length];
                            buf.readBytes(data);
                            return new MessagePacket(data);
                        }
                );

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }
}
