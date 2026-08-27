package dev.afterlifesdelight.network;

import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public final class ModNetwork {
    public static void registerPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");
        registrar.playToClient(
                ResurrectionEffectPayload.TYPE,
                ResurrectionEffectPayload.STREAM_CODEC,
                ResurrectionEffectPayload::handle
        );
        registrar.playToClient(
                GhostCorpseGrabCooldownPayload.TYPE,
                GhostCorpseGrabCooldownPayload.STREAM_CODEC,
                GhostCorpseGrabCooldownPayload::handle
        );
        registrar.playToClient(
                GhostCameraTransitionPayload.TYPE,
                GhostCameraTransitionPayload.STREAM_CODEC,
                GhostCameraTransitionPayload::handle
        );
    }

    private ModNetwork() {
    }
}
