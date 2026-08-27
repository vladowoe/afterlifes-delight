package dev.afterlifesdelight.mixin.client;

import dev.afterlifesdelight.client.GhostCorpseGrabCooldownClient;
import dev.afterlifesdelight.ghost.GhostInteractionRules;
import net.minecraft.client.Minecraft;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "dev.leo.sableplayerragdoll.neoforge.client.RagdollGrabClient", remap = false)
public abstract class SableRagdollGrabClientCooldownMixin {
    @Inject(method = "onClientTick", at = @At("HEAD"), cancellable = true, remap = false)
    private static void afterlifesDelight$enforceGhostGrabCooldown(
            ClientTickEvent.Post event,
            CallbackInfo callback
    ) {
        Minecraft minecraft = Minecraft.getInstance();
        boolean cooldownActive = GhostCorpseGrabCooldownClient.isActive();
        GhostCorpseGrabCooldownClient.tick();

        if (cooldownActive
                && minecraft.player != null
                && GhostInteractionRules.isGhost(minecraft.player)) {
            callback.cancel();
        }
    }
}
