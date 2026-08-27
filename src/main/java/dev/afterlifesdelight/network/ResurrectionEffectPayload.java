package dev.afterlifesdelight.network;

import dev.afterlifesdelight.AfterlifesDelight;
import dev.afterlifesdelight.client.GhostResurrectionClient;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ResurrectionEffectPayload(int entityId, byte ingredientFlags, long seed)
        implements CustomPacketPayload {
    public static final Type<ResurrectionEffectPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(
            AfterlifesDelight.MOD_ID,
            "resurrection_effect"
    ));
    public static final StreamCodec<RegistryFriendlyByteBuf, ResurrectionEffectPayload> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.VAR_INT,
                    ResurrectionEffectPayload::entityId,
                    ByteBufCodecs.BYTE,
                    ResurrectionEffectPayload::ingredientFlags,
                    ByteBufCodecs.VAR_LONG,
                    ResurrectionEffectPayload::seed,
                    ResurrectionEffectPayload::new
            );

    public static void handle(ResurrectionEffectPayload payload, IPayloadContext context) {
        GhostResurrectionClient.start(payload);
    }

    @Override
    public Type<ResurrectionEffectPayload> type() {
        return TYPE;
    }
}
