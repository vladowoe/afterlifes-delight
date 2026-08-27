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

public final class GhostGlowLayer extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {
    public GhostGlowLayer(RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> renderer) {
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
        if (intensity <= 0.001F) {
            return;
        }

        VertexConsumer glow = buffer.getBuffer(RenderType.entityTranslucentEmissive(player.getSkin().texture()));
        getParentModel().renderToBuffer(
                poseStack,
                glow,
                LightTexture.FULL_BRIGHT,
                OverlayTexture.NO_OVERLAY,
                GhostPlayerVisuals.glowColor(intensity)
        );
    }
}
