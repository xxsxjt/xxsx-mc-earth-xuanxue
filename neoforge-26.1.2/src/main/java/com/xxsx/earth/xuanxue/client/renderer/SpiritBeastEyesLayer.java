package com.xxsx.earth.xuanxue.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.xxsx.earth.xuanxue.XuanhuanEarth;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;

/**
 * Emissive eye overlay for spirit beasts.
 *
 * <p>The eye texture is extracted from the same UV atlas as the body, so it lines up
 * exactly and only the eye pixels are drawn. Rendering it with the emissive render type
 * makes the eyes self-lit at night, which is the main visual cue that a creature is a
 * spirit beast rather than an ordinary animal.</p>
 */
public final class SpiritBeastEyesLayer<S extends LivingEntityRenderState, M extends EntityModel<S>>
        extends RenderLayer<S, M> {
    private final RenderType renderType;

    public SpiritBeastEyesLayer(RenderLayerParent<S, M> parent, String textureName) {
        super(parent);
        Identifier texture = XuanhuanEarth.id("textures/entity/" + textureName + "_eyes.png");
        this.renderType = RenderTypes.entityTranslucentEmissive(texture);
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector collector, int lightCoords, S state,
                       float yRot, float xRot) {
        collector.order(1).submitModel(this.getParentModel(), state, poseStack, this.renderType,
                lightCoords, OverlayTexture.NO_OVERLAY, state.outlineColor, null);
    }
}
