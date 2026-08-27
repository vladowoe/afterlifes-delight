package dev.afterlifesdelight.mixin;

import dev.afterlifesdelight.ghost.GhostManager;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityGhostMixin {
    @Inject(method = "isPushable", at = @At("HEAD"), cancellable = true)
    private void afterlifesDelight$disablePushing(CallbackInfoReturnable<Boolean> callback) {
        LivingEntity entity = (LivingEntity) (Object) this;
        if (entity instanceof Player player && GhostManager.isGhost(player)) {
            callback.setReturnValue(false);
        }
    }
}
