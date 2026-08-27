package dev.afterlifesdelight.ghost;

import dev.afterlifesdelight.registry.ModItems;
import net.minecraft.world.entity.player.Player;

public final class GhostInteractionRules {
    public static boolean isGhost(Player player) {
        if (player.level().isClientSide()) {
            return player.getData(ModAttachments.GHOST_VISUAL_STATE.get());
        }

        return GhostManager.isGhost(player);
    }

    public static boolean hasUsableAfterlifeFood(Player player) {
        return ModItems.isAfterlifeFood(player.getMainHandItem())
                || ModItems.isAfterlifeFood(player.getOffhandItem());
    }

    private GhostInteractionRules() {
    }
}
