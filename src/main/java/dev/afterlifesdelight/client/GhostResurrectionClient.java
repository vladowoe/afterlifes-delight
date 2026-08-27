package dev.afterlifesdelight.client;

import dev.afterlifesdelight.AfterlifesDelight;
import dev.afterlifesdelight.network.ResurrectionEffectPayload;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

@EventBusSubscriber(modid = AfterlifesDelight.MOD_ID, value = Dist.CLIENT)
public final class GhostResurrectionClient {
    public static final int TOTAL_TICKS = 32;
    public static final byte NETHERITE_FLAG = 1;
    public static final byte ENDER_FLAG = 2;
    private static final Map<Integer, Animation> ANIMATIONS = new HashMap<>();

    public static void start(ResurrectionEffectPayload payload) {
        ANIMATIONS.put(
                payload.entityId(),
                new Animation(payload.entityId(), payload.ingredientFlags())
        );
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) {
            ANIMATIONS.clear();
            return;
        }

        Iterator<Animation> iterator = ANIMATIONS.values().iterator();
        while (iterator.hasNext()) {
            Animation animation = iterator.next();
            animation.age++;
            if (animation.age > TOTAL_TICKS || minecraft.level.getEntity(animation.entityId) == null) {
                iterator.remove();
            }
        }
    }

    public static float progress(Player player, float partialTick) {
        Animation animation = ANIMATIONS.get(player.getId());
        return animation == null ? 0.0F : animation.progress(partialTick);
    }

    public static byte ingredientFlags(Player player) {
        Animation animation = ANIMATIONS.get(player.getId());
        return animation == null ? 0 : animation.ingredientFlags;
    }

    private static final class Animation {
        private final int entityId;
        private final byte ingredientFlags;
        private int age;

        private Animation(int entityId, byte ingredientFlags) {
            this.entityId = entityId;
            this.ingredientFlags = ingredientFlags;
        }

        private float progress(float partialTick) {
            return Mth.clamp((age + partialTick) / TOTAL_TICKS, 0.0F, 1.0F);
        }
    }

    private GhostResurrectionClient() {
    }
}
