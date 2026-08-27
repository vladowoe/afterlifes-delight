package dev.afterlifesdelight.mixin.client;

import dev.afterlifesdelight.client.GhostAtmosphereClient;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientLevel.class)
public abstract class ClientLevelGhostAtmosphereMixin {
    private static final float SOUL_SAND_VALLEY_ASH_CHANCE = 0.00625F;
    private static final float RARE_WHITE_ASH_CHANCE = 0.00025F;

    @Inject(method = "doAnimateTick", at = @At("TAIL"))
    private void afterlifesDelight$spawnGhostAmbientParticles(
            int posX,
            int posY,
            int posZ,
            int range,
            RandomSource random,
            @Nullable Block markerBlock,
            BlockPos.MutableBlockPos blockPos,
            CallbackInfo callback
    ) {
        float intensity = GhostAtmosphereClient.getParticleIntensity();
        if (intensity <= 0.001F) {
            return;
        }

        ClientLevel level = (ClientLevel) (Object) this;
        BlockState blockState = level.getBlockState(blockPos);
        if (blockState.isCollisionShapeFullBlock(level, blockPos)) {
            return;
        }

        if (random.nextFloat() < SOUL_SAND_VALLEY_ASH_CHANCE * intensity) {
            level.addParticle(
                    ParticleTypes.ASH,
                    blockPos.getX() + random.nextDouble(),
                    blockPos.getY() + random.nextDouble(),
                    blockPos.getZ() + random.nextDouble(),
                    0.0,
                    0.0,
                    0.0
            );
        }
        if (random.nextFloat() < RARE_WHITE_ASH_CHANCE * intensity) {
            level.addParticle(
                    ParticleTypes.WHITE_ASH,
                    blockPos.getX() + random.nextDouble(),
                    blockPos.getY() + random.nextDouble(),
                    blockPos.getZ() + random.nextDouble(),
                    0.0,
                    0.0,
                    0.0
            );
        }
    }
}
