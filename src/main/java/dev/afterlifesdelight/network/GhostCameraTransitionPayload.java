package dev.afterlifesdelight.network;

import dev.afterlifesdelight.AfterlifesDelight;
import dev.afterlifesdelight.client.GhostCameraTransitionClient;
import dev.afterlifesdelight.client.GhostRespawnCollisionGraceClient;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record GhostCameraTransitionPayload(boolean entering) implements CustomPacketPayload {
    public static final Type<GhostCameraTransitionPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(
            AfterlifesDelight.MOD_ID,
            "ghost_camera_transition"
    ));
    public static final StreamCodec<RegistryFriendlyByteBuf, GhostCameraTransitionPayload> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.BOOL,
                    GhostCameraTransitionPayload::entering,
                    GhostCameraTransitionPayload::new
            );

    public static void handle(GhostCameraTransitionPayload payload, IPayloadContext context) {
        GhostCameraTransitionClient.start(payload.entering());
        if (payload.entering()) {
            GhostRespawnCollisionGraceClient.start();
        } else {
            GhostRespawnCollisionGraceClient.clear();
        }
    }

    @Override
    public Type<GhostCameraTransitionPayload> type() {
        return TYPE;
    }
}
