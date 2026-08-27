package dev.afterlifesdelight.mixin.compat;

import dev.afterlifesdelight.ghost.GhostManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets = "dev.ryanhcode.sable.sublevel.entity_collision.SubLevelEntityCollision", remap = false)
public abstract class SableSubLevelGhostCollisionMixin {
    @Inject(method = "getSubLevelEntityCollisionShape", at = @At("HEAD"), cancellable = true, remap = false)
    private static void afterlifesDelight$delayGhostRagdollCollision(
            Entity entity,
            @Coerce Object transformedBoundsCenter,
            @Coerce Object subLevelPose,
            BlockState blockState,
            @Coerce Object levelAccelerator,
            BlockPos blockPos,
            @Coerce Object reusedVectors,
            CallbackInfoReturnable<VoxelShape> callback
    ) {
        if (!(entity instanceof Player player) || !GhostManager.hasRagdollCollisionGrace(player)) {
            return;
        }

        ResourceLocation blockId = BuiltInRegistries.BLOCK.getKey(blockState.getBlock());
        if (blockId.getNamespace().equals("sable_player_ragdoll")
                && blockId.getPath().endsWith("ragdoll_part")) {
            callback.setReturnValue(Shapes.empty());
        }
    }

    private SableSubLevelGhostCollisionMixin() {
    }
}
