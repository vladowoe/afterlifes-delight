package dev.afterlifesdelight.compat;

import com.mojang.logging.LogUtils;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
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
 * <p>Hardcore Lite removes one heart at the start of {@code ServerPlayer#die}
 * and switches the player to spectator when the last heart is consumed. By the
 * time NeoForge fires {@code LivingDeathEvent}, spectator mode therefore acts as
 * a stable signal that this is Hardcore Lite's final death.</p>
 *
 * <p>The integration deliberately uses reflection for Hardcore Lite's saved
 * heart state so that Afterlife's Delight does not gain a hard dependency on
 * the other mod.</p>
 */
public final class HardcoreLiteCompat {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String MOD_ID = "hardcorelite";
    private static final String HARDCORE_LITE_CLASS = "net.petemc.hardcorelite.HardcoreLite";
    private static final int ONE_HEART_MODIFIER = -9;
    private static final double ONE_HEART_HEALTH = 2.0D;

    private static boolean reflectionFailureLogged;

    public static boolean isLoaded() {
        return ModList.get().isLoaded(MOD_ID);
    }

    /**
     * Returns whether this death should enter Afterlife's Delight's ghost flow.
     * Without Hardcore Lite every normal death keeps the existing behaviour.
     * With Hardcore Lite installed only its final, spectator-producing death
     * becomes a ghost death.
     */
    public static boolean shouldBecomeGhost(ServerPlayer player) {
        return !isLoaded() || player.gameMode.getGameModeForPlayer() == GameType.SPECTATOR;
    }

    /**
     * Hardcore Lite consumes the last heart before the death event is fired.
     * Preserve one heart in its saved state so resurrection returns the player
     * to a valid Hardcore Lite state and a later death can become final again.
     */
    public static void preserveLastHeartForGhost(ServerPlayer player) {
        if (!isLoaded() || player.gameMode.getGameModeForPlayer() != GameType.SPECTATOR) {
            return;
        }

        try {
            Object playerHearts = getPlayerHearts(player);
            if (playerHearts == null) {
                return;
            }

            Method getNumberOfHearts = playerHearts.getClass().getMethod("getNumberOfHearts");
            int heartModifier = ((Number) getNumberOfHearts.invoke(playerHearts)).intValue();
            if (heartModifier > ONE_HEART_MODIFIER) {
                return;
            }

            Method setNumberOfHearts = playerHearts.getClass().getMethod("setNumberOfHearts", int.class);
            setNumberOfHearts.invoke(playerHearts, ONE_HEART_MODIFIER);

            AttributeInstance maxHealth = player.getAttribute(Attributes.MAX_HEALTH);
            if (maxHealth != null) {
                maxHealth.setBaseValue(ONE_HEART_HEALTH);
            }
        } catch (ReflectiveOperationException | RuntimeException exception) {
            logReflectionFailure(exception);
        }
    }

    /**
     * Hardcore Lite marks its final death as spectator before respawn. Once the
     * cloned player has the ghost record, return them to the game mode they had
     * before that forced spectator switch so Afterlife's Delight can control the
     * ghost state normally.
     */
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
                "Hardcore Lite was detected, but Afterlife's Delight could not preserve its final heart. "
                        + "Compatibility may be incomplete until the integration is updated.",
                exception
        );
    }

    private HardcoreLiteCompat() {
    }
}
