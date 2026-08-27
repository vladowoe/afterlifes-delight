package dev.afterlifesdelight.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.afterlifesdelight.client.GhostPlayerVisuals;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityGhostVisualMixin {
    @Unique
    private LivingEntity afterlifesDelight$currentEntity;

    @Inject(method = "render", at = @At("HEAD"))
    private void afterlifesDelight$beginLivingRender(
            LivingEntity entity,
            float entityYaw,
            float partialTicks,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int packedLight,
            CallbackInfo callback
    ) {
        afterlifesDelight$currentEntity = entity;
    }

    @ModifyArg(
            method = "render",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/model/EntityModel;renderToBuffer(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;III)V"
            ),
            index = 2
    )
    private int afterlifesDelight$applyGhostLight(int packedLight) {
        if (afterlifesDelight$currentEntity instanceof Player player) {
            float intensity = GhostPlayerVisuals.intensity(player);
            if (intensity > 0.001F) {
                return GhostPlayerVisuals.ghostLight(packedLight, intensity);
            }
        }
        return packedLight;
    }

    @ModifyArg(
            method = "render",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/model/EntityModel;renderToBuffer(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;III)V"
            ),
            index = 4
    )
    private int afterlifesDelight$applyGhostColor(int packedColor) {
        if (afterlifesDelight$currentEntity instanceof Player player) {
            float intensity = GhostPlayerVisuals.intensity(player);
            if (intensity > 0.001F) {
                return GhostPlayerVisuals.bodyColor(intensity);
            }
        }
        return packedColor;
    }

    @Inject(method = "getShadowRadius", at = @At("RETURN"), cancellable = true)
    private void afterlifesDelight$fadeGhostShadow(
            LivingEntity entity,
            CallbackInfoReturnable<Float> callback
    ) {
        if (entity instanceof Player player) {
            float intensity = GhostPlayerVisuals.intensity(player);
            if (intensity > 0.001F) {
                callback.setReturnValue(callback.getReturnValue() * (1.0F - intensity));
            }
        }
    }

    @Inject(method = "render", at = @At("RETURN"))
    private void afterlifesDelight$finishLivingRender(
            LivingEntity entity,
            float entityYaw,
            float partialTicks,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int packedLight,
            CallbackInfo callback
    ) {
        afterlifesDelight$currentEntity = null;
    }
}
