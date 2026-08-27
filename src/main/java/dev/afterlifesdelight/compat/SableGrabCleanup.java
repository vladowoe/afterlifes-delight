package dev.afterlifesdelight.compat;

import com.mojang.logging.LogUtils;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.fml.ModList;
import org.slf4j.Logger;

public final class SableGrabCleanup {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int CLEANUP_TICKS = 4;
    private static final String SABLE_RAGDOLL_MOD_ID = "sable_player_ragdoll";
    private static final String GRAB_CALLBACKS_CLASS =
            "dev.leo.sableplayerragdoll.RagdollGrabCallbacks";
    private static final Map<UUID, Integer> PENDING_RELEASES = new HashMap<>();
    private static boolean callbackLookupAttempted;
    private static Method notifyReleasedMethod;

    public static void schedule(ServerPlayer player) {
        PENDING_RELEASES.put(player.getUUID(), CLEANUP_TICKS);
    }

    public static void tick(ServerPlayer player) {
        Integer ticksRemaining = PENDING_RELEASES.get(player.getUUID());
        if (ticksRemaining == null) {
            return;
        }

        notifyReleased(player);
        if (ticksRemaining <= 1) {
            PENDING_RELEASES.remove(player.getUUID());
        } else {
            PENDING_RELEASES.put(player.getUUID(), ticksRemaining - 1);
        }
    }

    public static void clear(ServerPlayer player) {
        PENDING_RELEASES.remove(player.getUUID());
    }

    private static void notifyReleased(ServerPlayer player) {
        Method method = findNotifyReleasedMethod();
        if (method == null) {
            return;
        }

        try {
            method.invoke(null, player);
        } catch (IllegalAccessException | InvocationTargetException exception) {
            LOGGER.warn("Could not clear Sable ragdoll grab state for {}", player.getGameProfile().getName(), exception);
        }
    }

    private static Method findNotifyReleasedMethod() {
        if (callbackLookupAttempted) {
            return notifyReleasedMethod;
        }
        callbackLookupAttempted = true;

        if (!ModList.get().isLoaded(SABLE_RAGDOLL_MOD_ID)) {
            return null;
        }

        try {
            Class<?> callbacksClass = Class.forName(GRAB_CALLBACKS_CLASS);
            notifyReleasedMethod = callbacksClass.getMethod("notifyReleased", ServerPlayer.class);
        } catch (ClassNotFoundException | NoSuchMethodException exception) {
            LOGGER.warn("Could not find the Sable ragdoll release callback", exception);
        }
        return notifyReleasedMethod;
    }

    private SableGrabCleanup() {
    }
}
