package dev.afterlifesdelight.mixin.compat;

import dev.afterlifesdelight.ghost.GhostManager;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "de.maxhenkel.gravestone.blocks.GraveStoneBlock", remap = false)
public abstract class GravestoneCompatMixin {
    @Inject(method = "entityInside", at = @At("HEAD"), cancellable = true, remap = false)
    private void afterlifesDelight$denySneakPickup(
            BlockState state,
            Level level,
            BlockPos position,
            Entity entity,
            CallbackInfo callback
    ) {
        if (entity instanceof Player player && GhostManager.isGhost(player)) {
            callback.cancel();
        }
    }
}
