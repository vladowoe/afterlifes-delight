package dev.afterlifesdelight.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.FastColor;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

public final class GhostMotionTrailLayer extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {
    private static final int[] ECHO_ALPHA = {18, 27, 40};
    private static final double MINIMUM_OFFSET_SQUARED = 0.055 * 0.055;

    public GhostMotionTrailLayer(RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> renderer) {
        super(renderer);
    }

    @Override
    public void render(
            PoseStack poseStack,
            MultiBufferSource buffer,
            int packedLight,
            AbstractClientPlayer player,
            float limbSwing,
            float limbSwingAmount,
            float partialTick,
            float ageInTicks,
            float netHeadYaw,
            float headPitch
    ) {
        float intensity = GhostPlayerVisuals.intensity(player);
        if (intensity <= 0.001F || !GhostMotionTrailClient.isWithinRenderDistance(player)) {
            return;
        }

        Vec3 currentPosition = player.getPosition(partialTick);
        VertexConsumer echo = buffer.getBuffer(RenderType.entityTranslucentEmissive(player.getSkin().texture()));
        for (int index = 0; index < GhostMotionTrailClient.ECHO_DELAYS.length; index++) {
            Vec3 echoPosition = GhostMotionTrailClient.sample(
                    player,
                    GhostMotionTrailClient.ECHO_DELAYS[index],
                    partialTick
            );
            if (echoPosition == null || echoPosition.distanceToSqr(currentPosition) < MINIMUM_OFFSET_SQUARED) {
                continue;
            }

            Vec3 offset = echoPosition.subtract(currentPosition);
            int alpha = Math.round(ECHO_ALPHA[index] * intensity);
            poseStack.pushPose();
            translateInWorldSpace(poseStack, offset);
            getParentModel().renderToBuffer(
                    poseStack,
                    echo,
                    LightTexture.FULL_BRIGHT,
                    OverlayTexture.NO_OVERLAY,
                    FastColor.ARGB32.color(alpha, 132, 218, 255)
            );
            poseStack.popPose();
        }
    }

    private static void translateInWorldSpace(PoseStack poseStack, Vec3 offset) {
        Matrix4f pose = poseStack.last().pose();
        pose.m30(pose.m30() + (float) offset.x);
        pose.m31(pose.m31() + (float) offset.y);
        pose.m32(pose.m32() + (float) offset.z);
    }
}
