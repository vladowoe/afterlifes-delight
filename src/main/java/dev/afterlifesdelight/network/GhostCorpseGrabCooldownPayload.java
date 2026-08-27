package dev.afterlifesdelight.network;

import dev.afterlifesdelight.AfterlifesDelight;
import dev.afterlifesdelight.client.GhostCorpseGrabCooldownClient;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record GhostCorpseGrabCooldownPayload(int durationTicks) implements CustomPacketPayload {
    public static final Type<GhostCorpseGrabCooldownPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(
            AfterlifesDelight.MOD_ID,
            "ghost_corpse_grab_cooldown"
    ));
    public static final StreamCodec<RegistryFriendlyByteBuf, GhostCorpseGrabCooldownPayload> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.VAR_INT,
                    GhostCorpseGrabCooldownPayload::durationTicks,
                    GhostCorpseGrabCooldownPayload::new
            );

    public static void handle(GhostCorpseGrabCooldownPayload payload, IPayloadContext context) {
        GhostCorpseGrabCooldownClient.start(payload.durationTicks());
    }

    @Override
    public Type<GhostCorpseGrabCooldownPayload> type() {
        return TYPE;
    }
}
