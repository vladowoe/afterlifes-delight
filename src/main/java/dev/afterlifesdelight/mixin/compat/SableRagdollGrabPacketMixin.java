package dev.afterlifesdelight.mixin.compat;

import dev.afterlifesdelight.ghost.GhostCorpseGrabCooldowns;
import dev.afterlifesdelight.ghost.GhostManager;
import java.lang.reflect.InvocationTargetException;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "dev.leo.sableplayerragdoll.neoforge.network.RagdollGrabPacket", remap = false)
public abstract class SableRagdollGrabPacketMixin {
    @Inject(method = "handle", at = @At("HEAD"), cancellable = true, remap = false)
    private static void afterlifesDelight$enforceGhostGrabCooldown(
            @Coerce Object packet,
            IPayloadContext context,
            CallbackInfo callback
    ) {
        if (!(context.player() instanceof ServerPlayer player)
                || !GhostManager.isGhost(player)
                || !GhostCorpseGrabCooldowns.isActive(player)
                || isReleasePacket(packet)) {
            return;
        }

        GhostCorpseGrabCooldowns.resendRemaining(player);
        callback.cancel();
    }

    private static boolean isReleasePacket(Object packet) {
        try {
            return (boolean) packet.getClass().getMethod("release").invoke(packet);
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException exception) {
            return true;
        }
    }
}
