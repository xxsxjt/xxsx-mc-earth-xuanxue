package com.xxsx.earth.xuanxue.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.xxsx.earth.xuanxue.entity.ContractableSpiritBeastEntity;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;

import java.util.function.Function;

public final class SpiritBeastRenderer<T extends ContractableSpiritBeastEntity,
        M extends EntityModel<SpiritBeastRenderState>> extends MobRenderer<T, SpiritBeastRenderState, M> {
    private final Identifier texture;
    private final String speciesKey;
    private final float scale;

    public SpiritBeastRenderer(EntityRendererProvider.Context context, ModelLayerLocation layer,
                               Function<ModelPart, M> modelFactory, Identifier texture,
                               float shadowRadius, float scale) {
        super(context, modelFactory.apply(context.bakeLayer(layer)), shadowRadius);
        this.texture = texture;
        this.speciesKey = texture.getPath().replace("textures/entity/", "").replace(".png", "");
        this.scale = scale;
        this.addLayer(new SpiritBeastEyesLayer<>(this, this.speciesKey));
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
    public void submit(SpiritBeastRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState cameraRenderState) {
        super.submit(state, poseStack, collector, cameraRenderState);
        float age = state.ageInTicks;
        boolean combat = state.aggressive || state.attackProgress > 0.02F;

        if (speciesKey.contains("wind_wolf")) {
            // Wind Wolf: Dual emerald-cyan wind vortex rings + gale crescent slash
            SpiritVfxHelper.submitAuraRing(poseStack, collector, 0.85F + state.runAmount * 0.25F, 0.24F, 0.35F, age * 0.14F, 110, 245, 210, combat ? 215 : 145);
            SpiritVfxHelper.submitAuraRing(poseStack, collector, 1.15F, 0.18F, 0.65F, -age * 0.18F, 145, 255, 235, combat ? 190 : 105);
            SpiritVfxHelper.submitAttackSlash(poseStack, collector, state.bodyRot, 0.75F, 2.35F, state.attackProgress, 120, 255, 220);
        } else if (speciesKey.contains("stonehorn_goat")) {
            // Stonehorn Goat: Earth-gold geomancy rune seal + amber tremor shockwave on charge
            SpiritVfxHelper.submitGroundSeal(poseStack, collector, SpiritVfxHelper.TEX_BAGUA, combat ? 1.75F : 1.25F, 0.05F, age * 0.04F, 245, 195, 95, combat ? 210 : 135);
            if (state.attackProgress > 0.02F) {
                SpiritVfxHelper.submitAuraRing(poseStack, collector, 1.1F + state.attackProgress * 1.9F, 0.55F, 0.12F, age * 0.08F, 255, 185, 70, (int) ((1.0F - state.attackProgress) * 235.0F));
            }
        } else if (speciesKey.contains("crystal_turtle")) {
            // Crystalback Turtle: Octagonal crystal rune seal + dual prismatic barrier rings
            SpiritVfxHelper.submitGroundSeal(poseStack, collector, SpiritVfxHelper.TEX_SWORD_SEAL, combat ? 1.85F : 1.45F, 0.05F, -age * 0.05F, 115, 225, 255, combat ? 225 : 155);
            SpiritVfxHelper.submitAuraRing(poseStack, collector, 1.25F, 0.42F, 0.25F, age * 0.07F, 135, 235, 255, (combat || state.hurtTime > 0.0F) ? 220 : 125);
        } else if (speciesKey.contains("ember_crane")) {
            // Emberfeather Crane: Radiant golden-crimson sun-wheel back halo + blazing swoop rings
            SpiritVfxHelper.submitBackHalo(poseStack, collector, SpiritVfxHelper.TEX_BAGUA, state.bodyRot, 1.05F, 0.58F, combat ? 0.92F : 0.72F, age * 0.08F, 255, 155, 65, combat ? 235 : 175);
            if (combat) {
                SpiritVfxHelper.submitAuraRing(poseStack, collector, 1.35F, 0.28F, 0.42F, age * 0.16F, 255, 120, 45, 210);
                SpiritVfxHelper.submitAttackSlash(poseStack, collector, state.bodyRot, 0.95F, 2.25F, state.attackProgress, 255, 165, 65);
            }
        }
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
