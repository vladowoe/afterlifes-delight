package dev.afterlifesdelight.compat;

import com.mojang.logging.LogUtils;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.GameType;
import net.neoforged.fml.ModList;
import org.slf4j.Logger;

/**
 * Optional compatibility with Hardcore Lite.
 *
 * <p>Hardcore Lite injects at the head of {@code ServerPlayer#die}. On the true
 * final death it subtracts the last heart, switches the player to spectator and
 * resets its saved heart modifier to {@code 0} before NeoForge fires
 * {@code LivingDeathEvent}. The integration therefore requires both signals:
 * spectator game mode and Hardcore Lite's reset heart state.</p>
 *
 * <p>After the final death has been identified, the spectator switch is reverted
 * immediately. Vanilla skips death-loot drops for spectators, so leaving the
 * player in spectator until respawn would incorrectly preserve the inventory.</p>
 *
 * <p>The integration uses reflection so Afterlife's Delight does not gain a hard
 * dependency on Hardcore Lite.</p>
 */
public final class HardcoreLiteCompat {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String MOD_ID = "hardcorelite";
    private static final String HARDCORE_LITE_CLASS = "net.petemc.hardcorelite.HardcoreLite";
    private static final int FINAL_DEATH_RESET_MODIFIER = 0;
    private static final int ONE_HEART_MODIFIER = -9;
    private static final double ONE_HEART_HEALTH = 2.0D;

    private static final Set<UUID> PENDING_FINAL_DEATHS = ConcurrentHashMap.newKeySet();
    private static boolean reflectionFailureLogged;

    public static boolean isLoaded() {
        if (ModList.get().isLoaded(MOD_ID)) {
            return true;
        }

        try {
            Class.forName(HARDCORE_LITE_CLASS, false, HardcoreLiteCompat.class.getClassLoader());
            return true;
        } catch (ClassNotFoundException ignored) {
            return false;
        }
    }

    /**
     * Marks only Hardcore Lite's actual final death.
     *
     * <p>Every non-final death clears any stale marker first. A final death must
     * already be spectator <em>and</em> have Hardcore Lite's reset heart state
     * ({@code 0}). This prevents a normal 2 hearts -> 1 heart death from entering
     * ghost state.</p>
     *
     * <p>Once those signals are captured, the previous playable game mode is
     * restored before vanilla continues processing {@code ServerPlayer#die}.
     * This is required so vanilla still drops death loot normally.</p>
     */
    public static boolean beginFinalDeath(ServerPlayer player) {
        clearPendingFinalDeath(player);

        if (!isLoaded() || player.gameMode.getGameModeForPlayer() != GameType.SPECTATOR) {
            return false;
        }

        try {
            Integer heartModifier = getHeartModifier(player);
            if (heartModifier == null || heartModifier != FINAL_DEATH_RESET_MODIFIER) {
                return false;
            }

            PENDING_FINAL_DEATHS.add(player.getUUID());
            restorePlayableGameMode(player);
            return true;
        } catch (ReflectiveOperationException | RuntimeException exception) {
            logReflectionFailure(exception);
            return false;
        }
    }

    public static boolean consumePendingFinalDeath(ServerPlayer player) {
        return PENDING_FINAL_DEATHS.remove(player.getUUID());
    }

    public static void clearPendingFinalDeath(ServerPlayer player) {
        PENDING_FINAL_DEATHS.remove(player.getUUID());
    }

    /**
     * Hardcore Lite resets its saved counter when it reaches its final death.
     * Once Afterlife's Delight has taken over that death, restore the saved state
     * to one heart so resurrection leaves the player with one remaining life and
     * the same final-death flow can happen again later.
     */
    public static boolean restoreLastHeartForGhost(ServerPlayer player) {
        if (!isLoaded()) {
            return true;
        }

        try {
            Object playerHearts = getPlayerHearts(player);
            if (playerHearts == null) {
                return false;
            }

            Method setNumberOfHearts = playerHearts.getClass().getMethod("setNumberOfHearts", int.class);
            setNumberOfHearts.invoke(playerHearts, ONE_HEART_MODIFIER);

            AttributeInstance maxHealth = player.getAttribute(Attributes.MAX_HEALTH);
            if (maxHealth != null) {
                maxHealth.setBaseValue(ONE_HEART_HEALTH);
            }
            if (!player.isDeadOrDying()) {
                player.setHealth((float) ONE_HEART_HEALTH);
            }
            return true;
        } catch (ReflectiveOperationException | RuntimeException exception) {
            logReflectionFailure(exception);
            return false;
        }
    }

    public static void restoreCanceledFinalDeath(ServerPlayer player) {
        clearPendingFinalDeath(player);
        if (!isLoaded()) {
            return;
        }

        restoreLastHeartForGhost(player);
        restorePlayableGameMode(player);
        player.setHealth((float) ONE_HEART_HEALTH);
    }

    public static void restorePlayableGameMode(ServerPlayer player) {
        if (!isLoaded() || player.gameMode.getGameModeForPlayer() != GameType.SPECTATOR) {
            return;
        }

        GameType previousGameMode = player.gameMode.getPreviousGameModeForPlayer();
        if (previousGameMode == null || previousGameMode == GameType.SPECTATOR) {
            previousGameMode = GameType.SURVIVAL;
        }
        player.setGameMode(previousGameMode);
    }

    private static Integer getHeartModifier(ServerPlayer player) throws ReflectiveOperationException {
        Object playerHearts = getPlayerHearts(player);
        if (playerHearts == null) {
            return null;
        }

        Method getNumberOfHearts = playerHearts.getClass().getMethod("getNumberOfHearts");
        return ((Number) getNumberOfHearts.invoke(playerHearts)).intValue();
    }

    private static Object getPlayerHearts(ServerPlayer player) throws ReflectiveOperationException {
        Class<?> hardcoreLiteClass = Class.forName(HARDCORE_LITE_CLASS);
        Field serverStateField = hardcoreLiteClass.getField("serverState");
        Object serverState = serverStateField.get(null);
        if (serverState == null) {
            return null;
        }

        Method getPlayerHearts = serverState.getClass().getMethod("getPlayerHearts", LivingEntity.class);
        return getPlayerHearts.invoke(serverState, player);
    }

    private static void logReflectionFailure(Exception exception) {
        if (reflectionFailureLogged) {
            return;
        }
        reflectionFailureLogged = true;
        LOGGER.warn(
                "Hardcore Lite was detected, but Afterlife's Delight could not read or restore its heart state. "
                        + "Compatibility may be incomplete until the integration is updated.",
                exception
        );
    }

    private HardcoreLiteCompat() {
    }
}
