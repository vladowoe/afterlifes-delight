package dev.afterlifesdelight.mixin.compat;

import dev.afterlifesdelight.ghost.GhostManager;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "de.maxhenkel.gravestone.events.DeathEvents", remap = false)
public abstract class GravestoneCloneCompatMixin {
    @Inject(method = "onPlayerCloneLast", at = @At("HEAD"), cancellable = true, remap = false)
    private void afterlifesDelight$keepObituaryOutOfGhostInventory(
            PlayerEvent.Clone event,
            CallbackInfo callback
    ) {
        if (GhostManager.isGhost(event.getEntity())) {
            callback.cancel();
        }
    }
}
