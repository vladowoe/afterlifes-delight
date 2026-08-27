package dev.afterlifesdelight.mixin.compat;

import dev.afterlifesdelight.compat.SableGrabCleanup;
import java.lang.reflect.InvocationTargetException;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "dev.leo.ragdollcorpse.corpse.CorpseInteractHandler", remap = false)
public abstract class RagdollCorpseInteractionMixin {
    @Inject(
            method = "onRagdollInteract",
            at = @At(
                    value = "INVOKE",
                    target = "Ldev/leo/ragdollcorpse/corpse/CorpseSavedData;releaseEmptyCorpseNow(Ljava/util/UUID;Lnet/minecraft/server/level/ServerLevel;)V"
            ),
            remap = false
    )
    private static void afterlifesDelight$releaseDisappearingCorpseGrab(
            @Coerce Object event,
            CallbackInfo callback
    ) {
        ServerPlayer player = eventPlayer(event);
        if (player != null) {
            // The interaction packet and Sable's grab packet are sent almost
            // together. Clear for several server ticks so a late grab cannot
            // re-enable the pose and movement penalty after the corpse vanished.
            SableGrabCleanup.schedule(player);
        }
    }

    private static ServerPlayer eventPlayer(Object event) {
        try {
            Object player = event.getClass().getMethod("player").invoke(event);
            if (player instanceof ServerPlayer serverPlayer) {
                return serverPlayer;
            }
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException ignored) {
            // Optional compatibility target: leave the base mods untouched if its API changes.
        }
        return null;
    }
}
