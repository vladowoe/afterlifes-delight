package dev.afterlifesdelight.mixin.compat;

import dev.afterlifesdelight.config.AfterlifesConfig;
import dev.afterlifesdelight.ghost.GhostCorpseGrabCooldowns;
import dev.afterlifesdelight.ghost.GhostManager;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "dev.leo.sableplayerragdoll.block.entity.RagdollPartBlockEntity", remap = false)
public abstract class SableRagdollCorpseGripMixin {
    private static final long GRIP_CHECK_INTERVAL_TICKS = 20L;

    @Shadow(remap = false)
    @Final
    private Map<UUID, ?> grabbers;

    @Shadow(remap = false)
    public abstract boolean isCorpse();

    @Shadow(remap = false)
    public abstract void stopGrab(UUID playerId);

    @Shadow(remap = false)
    private void notifyReleased(UUID playerId) {
        throw new AssertionError();
    }

    @Unique
    private final Map<UUID, Long> afterlifesDelight$nextGhostGripChecks = new HashMap<>();

    @Unique
    private long afterlifesDelight$lastGripTick = Long.MIN_VALUE;

    @Inject(method = "checkGrabbers", at = @At("TAIL"), remap = false)
    private void afterlifesDelight$makeCorpseSlipFromGhostGrip(CallbackInfo callback) {
        BlockEntity blockEntity = (BlockEntity) (Object) this;
        if (!(blockEntity.getLevel() instanceof ServerLevel level)) {
            return;
        }

        long gameTime = level.getGameTime();
        if (afterlifesDelight$lastGripTick == gameTime) {
            return;
        }
        afterlifesDelight$lastGripTick = gameTime;

        if (!isCorpse() || grabbers.isEmpty()) {
            afterlifesDelight$nextGhostGripChecks.clear();
            return;
        }

        afterlifesDelight$nextGhostGripChecks.keySet().removeIf(playerId -> !grabbers.containsKey(playerId));
        double dropChance = AfterlifesConfig.GHOST_CORPSE_GRIP_DROP_CHANCE_PERCENT.getAsDouble() / 100.0D;

        for (UUID playerId : new ArrayList<>(grabbers.keySet())) {
            if (!(level.getPlayerByUUID(playerId) instanceof ServerPlayer player)
                    || !GhostManager.isGhost(player)) {
                afterlifesDelight$nextGhostGripChecks.remove(playerId);
                continue;
            }

            long nextCheck = afterlifesDelight$nextGhostGripChecks.computeIfAbsent(
                    playerId,
                    ignored -> gameTime + GRIP_CHECK_INTERVAL_TICKS
            );
            if (gameTime < nextCheck) {
                continue;
            }

            afterlifesDelight$nextGhostGripChecks.put(playerId, gameTime + GRIP_CHECK_INTERVAL_TICKS);
            if (dropChance > 0.0D && level.random.nextDouble() < dropChance) {
                GhostCorpseGrabCooldowns.start(player);
                afterlifesDelight$releaseGrab(playerId);
                afterlifesDelight$nextGhostGripChecks.remove(playerId);
            }
        }
    }

    @Unique
    private void afterlifesDelight$releaseGrab(UUID playerId) {
        stopGrab(playerId);
        notifyReleased(playerId);
    }
}
