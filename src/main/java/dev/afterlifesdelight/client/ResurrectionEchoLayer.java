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
import net.minecraft.util.Mth;

public final class ResurrectionEchoLayer extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {
    private static final float[] SHELL_OFFSETS = {0.24F, 0.52F, 0.84F};

    public ResurrectionEchoLayer(RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> renderer) {
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
        float progress = GhostResurrectionClient.progress(player, partialTick);
        if (progress <= 0.001F || progress >= 1.0F) {
            return;
        }

        float merge = smoothStep(Mth.clamp(progress / 0.72F, 0.0F, 1.0F));
        float remaining = 1.0F - merge;
        byte flags = GhostResurrectionClient.ingredientFlags(player);
        int red = 132;
        int green = 222;
        int blue = 255;
        if ((flags & GhostResurrectionClient.ENDER_FLAG) != 0) {
            red = 184;
            green = 144;
            blue = 255;
        }

        VertexConsumer echo = buffer.getBuffer(RenderType.entityTranslucentEmissive(player.getSkin().texture()));
        for (int index = SHELL_OFFSETS.length - 1; index >= 0; index--) {
            float layerStrength = (float) (SHELL_OFFSETS.length - index) / SHELL_OFFSETS.length;
            int alpha = Math.round(52.0F * remaining * layerStrength);
            if (alpha <= 0) {
                continue;
            }

            poseStack.pushPose();
            poseStack.translate(0.0F, -SHELL_OFFSETS[index] * remaining, 0.0F);
            getParentModel().renderToBuffer(
                    poseStack,
                    echo,
                    LightTexture.FULL_BRIGHT,
                    OverlayTexture.NO_OVERLAY,
                    FastColor.ARGB32.color(alpha, red, green, blue)
            );
            poseStack.popPose();
        }
    }

    private static float smoothStep(float value) {
        return value * value * (3.0F - 2.0F * value);
    }
}
