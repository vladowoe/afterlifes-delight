package dev.afterlifesdelight.block;

import dev.afterlifesdelight.ghost.GhostInteractionRules;
import dev.afterlifesdelight.ghost.GhostManager;
import dev.afterlifesdelight.item.ResurrectionEffect;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import vectorwing.farmersdelight.common.block.PieBlock;

public final class AfterlifePieBlock extends PieBlock {
    private final ResurrectionEffect resurrectionEffect;

    public AfterlifePieBlock(
            BlockBehaviour.Properties properties,
            Supplier<Item> pieSlice,
            ResurrectionEffect resurrectionEffect
    ) {
        super(properties, pieSlice);
        this.resurrectionEffect = resurrectionEffect;
    }

    public ResurrectionEffect resurrectionEffect() {
        return resurrectionEffect;
    }

    @Override
    protected InteractionResult consumeBite(
            Level level,
            BlockPos position,
            BlockState state,
            Player player
    ) {
        if (!GhostInteractionRules.isGhost(player)) {
            return super.consumeBite(level, position, state, player);
        }

        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResult.PASS;
        }

        consumeGhostServing(level, position, state);
        GhostManager.resurrect(serverPlayer, resurrectionEffect);
        return InteractionResult.SUCCESS;
    }

    private void consumeGhostServing(Level level, BlockPos position, BlockState state) {
        int bites = state.getValue(BITES);
        if (bites < getMaxBites() - 1) {
            level.setBlock(position, state.setValue(BITES, bites + 1), 3);
        } else {
            level.removeBlock(position, false);
        }

        level.playSound(
                null,
                position,
                SoundEvents.GENERIC_EAT,
                SoundSource.PLAYERS,
                0.8F,
                0.8F
        );
        if (level instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(
                    new BlockParticleOption(ParticleTypes.BLOCK, state),
                    position.getX() + 0.5D,
                    position.getY() + 0.3D,
                    position.getZ() + 0.5D,
                    3,
                    0.1D,
                    0.1D,
                    0.1D,
                    0.001D
            );
        }
    }
}
