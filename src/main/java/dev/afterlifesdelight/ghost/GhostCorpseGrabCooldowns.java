package dev.afterlifesdelight.ghost;

import dev.afterlifesdelight.config.AfterlifesConfig;
import dev.afterlifesdelight.network.GhostCorpseGrabCooldownPayload;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;

public final class GhostCorpseGrabCooldowns {
    private static final Map<UUID, Long> BLOCKED_UNTIL_TICK = new HashMap<>();

    public static void start(ServerPlayer player) {
        int durationTicks = Math.max(
                0,
                (int) Math.round(AfterlifesConfig.GHOST_CORPSE_GRIP_COOLDOWN_SECONDS.getAsDouble() * 20.0D)
        );
        if (durationTicks == 0) {
            BLOCKED_UNTIL_TICK.remove(player.getUUID());
            return;
        }

        long blockedUntil = currentServerTick(player) + durationTicks;
        BLOCKED_UNTIL_TICK.put(player.getUUID(), blockedUntil);
        PacketDistributor.sendToPlayer(player, new GhostCorpseGrabCooldownPayload(durationTicks));
    }

    public static boolean isActive(ServerPlayer player) {
        Long blockedUntil = BLOCKED_UNTIL_TICK.get(player.getUUID());
        if (blockedUntil == null) {
            return false;
        }

        if (currentServerTick(player) >= blockedUntil) {
            BLOCKED_UNTIL_TICK.remove(player.getUUID());
            return false;
        }
        return true;
    }

    public static void resendRemaining(ServerPlayer player) {
        Long blockedUntil = BLOCKED_UNTIL_TICK.get(player.getUUID());
        if (blockedUntil == null) {
            return;
        }

        int remaining = (int) Math.max(0L, blockedUntil - currentServerTick(player));
        if (remaining > 0) {
            PacketDistributor.sendToPlayer(player, new GhostCorpseGrabCooldownPayload(remaining));
        }
    }

    public static void clear(ServerPlayer player) {
        BLOCKED_UNTIL_TICK.remove(player.getUUID());
    }

    public static void clearAndSync(ServerPlayer player) {
        clear(player);
        PacketDistributor.sendToPlayer(player, new GhostCorpseGrabCooldownPayload(0));
    }

    private static long currentServerTick(ServerPlayer player) {
        return player.getServer().overworld().getGameTime();
    }

    private GhostCorpseGrabCooldowns() {
    }
}
