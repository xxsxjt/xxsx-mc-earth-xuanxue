package com.xxsx.earth.xuanxue.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.xxsx.earth.xuanxue.XuanhuanEarth;
import com.xxsx.earth.xuanxue.client.model.SpiritFoxModel;
import com.xxsx.earth.xuanxue.entity.ContractableSpiritBeastEntity;
import com.xxsx.earth.xuanxue.entity.SpiritFoxEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;

public final class SpiritFoxRenderer extends MobRenderer<SpiritFoxEntity, SpiritFoxRenderState, SpiritFoxModel> {
    private static final Identifier TEXTURE = XuanhuanEarth.id("textures/entity/spirit_fox.png");

    public SpiritFoxRenderer(EntityRendererProvider.Context context) {
        super(context, new SpiritFoxModel(context.bakeLayer(SpiritFoxModel.LAYER_LOCATION)), 0.46F);
        this.addLayer(new SpiritBeastEyesLayer<>(this, "spirit_fox"));
    }

    @Override
    public SpiritFoxRenderState createRenderState() {
        return new SpiritFoxRenderState();
    }

    @Override
    public void extractRenderState(SpiritFoxEntity entity, SpiritFoxRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        state.spiritAffinity = entity.getAffinity() / (float) ContractableSpiritBeastEntity.MAX_AFFINITY;
        state.attackProgress = entity.getAttackAnim(partialTick);
        state.sitting = entity.isInSittingPose();
        state.aggressive = entity.getTarget() != null;
        state.companionMode = entity.getCompanionMode().id();
        state.runAmount = Mth.clamp(entity.walkAnimation.speed(partialTick) * 1.6F, 0.0F, 1.0F);
        state.hurtTime = Math.max(0.0F, entity.hurtTime - partialTick);
        state.inWater = entity.isInWater();
        state.leapProgress = Mth.clamp((float) (entity.getDeltaMovement().y * 4.0D), -1.0F, 1.0F);
        state.tailWag = Mth.sin(entity.tickCount * 0.22F + entity.getId() * 0.7F);
        state.breathPhase = Mth.sin((entity.tickCount + partialTick) * 0.11F);
    }

    @Override
    public void submit(SpiritFoxRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState cameraRenderState) {
        super.submit(state, poseStack, collector, cameraRenderState);
        float age = state.ageInTicks;
        // 3 orbiting cyan-violet fox-fire spirit orbs
        SpiritVfxHelper.submitOrbitOrbs(poseStack, collector, 3, state.aggressive ? 1.15F : 0.88F, 0.65F, 0.22F, age, 145, 225, 255, state.aggressive ? 235 : 185);
        if (state.aggressive || state.attackProgress > 0.02F) {
            SpiritVfxHelper.submitGroundSeal(poseStack, collector, SpiritVfxHelper.TEX_CONSTELLATION, 1.45F, 0.06F, age * 0.10F, 175, 130, 255, 190);
            SpiritVfxHelper.submitAttackSlash(poseStack, collector, state.bodyRot, 0.62F, 1.95F, state.attackProgress, 155, 210, 255);
        }
    }

    @Override
    public Identifier getTextureLocation(SpiritFoxRenderState state) {
        return TEXTURE;
    }

    @Override
    protected void scale(SpiritFoxRenderState state, PoseStack poseStack) {
        float base = state.isBaby ? 0.68F : 1.0F;
        float breath = 1.0F + state.breathPhase * 0.014F;
        poseStack.scale(base, base * breath, base);
        if (state.inWater) {
            poseStack.translate(0.0F, 0.10F, 0.0F);
        }
    }
}
