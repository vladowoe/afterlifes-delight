package dev.afterlifesdelight.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.afterlifesdelight.client.GhostPlayerVisuals;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerRenderer.class)
public abstract class PlayerRendererGhostArmMixin {
    @Unique
    private AbstractClientPlayer afterlifesDelight$handPlayer;
    @Unique
    private MultiBufferSource afterlifesDelight$handBuffer;

    @Inject(method = "renderHand", at = @At("HEAD"))
    private void afterlifesDelight$beginHandRender(
            PoseStack poseStack,
            MultiBufferSource buffer,
            int combinedLight,
            AbstractClientPlayer player,
            ModelPart rendererArm,
            ModelPart rendererArmwear,
            CallbackInfo callback
    ) {
        afterlifesDelight$handPlayer = player;
        afterlifesDelight$handBuffer = buffer;
    }

    @Redirect(
            method = "renderHand",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/model/geom/ModelPart;render(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;II)V"
            )
    )
    private void afterlifesDelight$renderGhostArm(
            ModelPart modelPart,
            PoseStack poseStack,
            VertexConsumer originalConsumer,
            int packedLight,
            int packedOverlay
    ) {
        AbstractClientPlayer player = afterlifesDelight$handPlayer;
        MultiBufferSource buffer = afterlifesDelight$handBuffer;
        float intensity = player == null ? 0.0F : GhostPlayerVisuals.intensity(player);
        if (intensity <= 0.001F || buffer == null) {
            modelPart.render(poseStack, originalConsumer, packedLight, packedOverlay);
            return;
        }

        modelPart.render(
                poseStack,
                buffer.getBuffer(RenderType.entityTranslucent(player.getSkin().texture())),
                GhostPlayerVisuals.ghostLight(packedLight, intensity),
                packedOverlay,
                GhostPlayerVisuals.bodyColor(intensity)
        );
        modelPart.render(
                poseStack,
                buffer.getBuffer(RenderType.entityTranslucentEmissive(player.getSkin().texture())),
                LightTexture.FULL_BRIGHT,
                packedOverlay,
                GhostPlayerVisuals.glowColor(intensity)
        );
    }

    @Inject(method = "renderHand", at = @At("RETURN"))
    private void afterlifesDelight$finishHandRender(
            PoseStack poseStack,
            MultiBufferSource buffer,
            int combinedLight,
            AbstractClientPlayer player,
            ModelPart rendererArm,
            ModelPart rendererArmwear,
            CallbackInfo callback
    ) {
        afterlifesDelight$handPlayer = null;
        afterlifesDelight$handBuffer = null;
    }
}
