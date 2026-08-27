package dev.afterlifesdelight.mixin.compat;

import dev.afterlifesdelight.ghost.GhostManager;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets = "dev.leo.sableplayerragdoll.block.RagdollPartBlock", remap = false)
public abstract class SableRagdollPartBlockGhostCollisionMixin {
    @Inject(method = "getCollisionShape", at = @At("HEAD"), cancellable = true, remap = false)
    private void afterlifesDelight$delayGhostRagdollCollision(
            BlockState blockState,
            BlockGetter level,
            BlockPos blockPos,
            CollisionContext collisionContext,
            CallbackInfoReturnable<VoxelShape> callback
    ) {
        if (collisionContext instanceof EntityCollisionContext entityContext
                && entityContext.getEntity() instanceof Player player
                && GhostManager.hasRagdollCollisionGrace(player)) {
            callback.setReturnValue(Shapes.empty());
        }
    }
}
