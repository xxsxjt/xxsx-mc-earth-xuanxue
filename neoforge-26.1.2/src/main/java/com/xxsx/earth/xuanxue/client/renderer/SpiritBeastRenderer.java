package com.xxsx.earth.xuanxue.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.xxsx.earth.xuanxue.entity.ContractableSpiritBeastEntity;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;

import java.util.function.Function;

public final class SpiritBeastRenderer<T extends ContractableSpiritBeastEntity,
        M extends EntityModel<SpiritBeastRenderState>> extends MobRenderer<T, SpiritBeastRenderState, M> {
    private final Identifier texture;
    private final float scale;

    public SpiritBeastRenderer(EntityRendererProvider.Context context, ModelLayerLocation layer,
                               Function<ModelPart, M> modelFactory, Identifier texture,
                               float shadowRadius, float scale) {
        super(context, modelFactory.apply(context.bakeLayer(layer)), shadowRadius);
        this.texture = texture;
        this.scale = scale;
        this.addLayer(new SpiritBeastEyesLayer<>(this, texture.getPath()
                .replace("textures/entity/", "")
                .replace(".png", "")));
    }

    @Override
    public SpiritBeastRenderState createRenderState() {
        return new SpiritBeastRenderState();
    }

    @Override
    public void extractRenderState(T entity, SpiritBeastRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        state.affinity = entity.getAffinity() / (float) ContractableSpiritBeastEntity.MAX_AFFINITY;
        state.companionMode = entity.getCompanionMode().id();
        state.aggressive = entity.getTarget() != null;
        state.attackProgress = entity.getAttackAnim(partialTick);
        state.sitting = entity.isInSittingPose();
        state.runAmount = Mth.clamp(entity.walkAnimation.speed(partialTick) * 1.6F, 0.0F, 1.0F);
        state.hurtTime = Math.max(0.0F, entity.hurtTime - partialTick);
        state.swimAmount = entity.isInWater() ? 1.0F : 0.0F;
        state.inWater = entity.isInWater();
        state.onGround = entity.onGround();
        state.leapProgress = Mth.clamp((float) (entity.getDeltaMovement().y * 4.0D), -1.0F, 1.0F);
        state.tailWag = Mth.sin(entity.tickCount * 0.22F + entity.getId() * 0.7F);
        state.breathPhase = Mth.sin((entity.tickCount + partialTick) * 0.11F);
        state.variant = entity.getAffinity();
    }

    @Override
    public Identifier getTextureLocation(SpiritBeastRenderState state) {
        return texture;
    }

    @Override
    protected void scale(SpiritBeastRenderState state, PoseStack poseStack) {
        float resolved = state.isBaby ? scale * 0.62F : scale;
        float breath = 1.0F + state.breathPhase * 0.012F;
        poseStack.scale(resolved, resolved * breath, resolved);
        if (state.swimAmount > 0.0F) {
            poseStack.translate(0.0F, 0.12F, 0.0F);
        }
    }
}
