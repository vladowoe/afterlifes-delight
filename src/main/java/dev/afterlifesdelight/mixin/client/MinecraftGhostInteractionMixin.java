package dev.afterlifesdelight.mixin.client;

import dev.afterlifesdelight.block.AfterlifePieBlock;
import dev.afterlifesdelight.ghost.GhostInteractionRules;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Minecraft.class)
public abstract class MinecraftGhostInteractionMixin {
    @Shadow
    @Nullable
    public LocalPlayer player;

    @Shadow
    @Nullable
    public MultiPlayerGameMode gameMode;

    @Inject(method = "startAttack", at = @At("HEAD"), cancellable = true)
    private void afterlifesDelight$preventGhostAttack(CallbackInfoReturnable<Boolean> callback) {
        if (isLocalGhost()) {
            callback.setReturnValue(false);
        }
    }

    @Inject(method = "continueAttack", at = @At("HEAD"), cancellable = true)
    private void afterlifesDelight$preventGhostMining(boolean attacking, CallbackInfo callback) {
        if (!isLocalGhost()) {
            return;
        }

        if (gameMode != null && gameMode.isDestroying()) {
            gameMode.stopDestroyBlock();
        }
        callback.cancel();
    }

    @Inject(method = "startUseItem", at = @At("HEAD"), cancellable = true)
    private void afterlifesDelight$preventGhostUse(CallbackInfo callback) {
        if (isLocalGhost()
                && !GhostInteractionRules.hasUsableAfterlifeFood(player)
                && !isTargetingAfterlifePie()) {
            callback.cancel();
        }
    }

    private boolean isTargetingAfterlifePie() {
        Minecraft minecraft = (Minecraft) (Object) this;
        return player != null
                && minecraft.hitResult instanceof BlockHitResult blockHit
                && player.level().getBlockState(blockHit.getBlockPos()).getBlock() instanceof AfterlifePieBlock;
    }

    private boolean isLocalGhost() {
        return player != null && GhostInteractionRules.isGhost(player);
    }
}
