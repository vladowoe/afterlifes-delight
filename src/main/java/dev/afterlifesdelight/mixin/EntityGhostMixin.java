package dev.afterlifesdelight.mixin;

import dev.afterlifesdelight.ghost.GhostManager;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class EntityGhostMixin {
    @Inject(method = "isIgnoringBlockTriggers", at = @At("HEAD"), cancellable = true)
    private void afterlifesDelight$ignoreBlockTriggers(CallbackInfoReturnable<Boolean> callback) {
        Entity entity = (Entity) (Object) this;
        if (entity instanceof Player player && GhostManager.isGhost(player)) {
            callback.setReturnValue(true);
        }
    }
}
