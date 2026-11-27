package com.xxsx.earth.xuanxue.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.xxsx.earth.xuanxue.XuanhuanEarth;
import com.xxsx.earth.xuanxue.client.model.CultivationSettlerModel;
import com.xxsx.earth.xuanxue.entity.CultivationSettlerEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;

public final class CultivationSettlerRenderer extends MobRenderer<CultivationSettlerEntity,
        CultivationSettlerRenderState, CultivationSettlerModel> {
    private static final Identifier[] TEXTURES = {
            XuanhuanEarth.id("textures/entity/cultivation_settler_merchant.png"),
            XuanhuanEarth.id("textures/entity/cultivation_settler_spring_keeper.png"),
            XuanhuanEarth.id("textures/entity/cultivation_settler_steward.png"),
            XuanhuanEarth.id("textures/entity/cultivation_settler_spring_keeper.png"),
            XuanhuanEarth.id("textures/entity/cultivation_settler_steward.png")
    };

    public CultivationSettlerRenderer(EntityRendererProvider.Context context) {
        super(context, new CultivationSettlerModel(context.bakeLayer(CultivationSettlerModel.LAYER_LOCATION)), 0.48F);
    }

    @Override
    public CultivationSettlerRenderState createRenderState() {
        return new CultivationSettlerRenderState();
    }

    @Override
    public void extractRenderState(CultivationSettlerEntity entity, CultivationSettlerRenderState state,
                                   float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        state.role = entity.getRole().id();
        state.element = entity.getElementAffinity().id();
        state.weapon = entity.getWeaponStyle().id();
        state.trading = entity.isTrading();
        state.hurtTime = Math.max(0.0F, entity.hurtTime - partialTick);
        state.breathPhase = Mth.sin((entity.tickCount + partialTick) * 0.09F);
        state.gesturePhase = Mth.sin((entity.tickCount + partialTick) * 0.045F);
        state.walking = entity.walkAnimation.speed(partialTick) > 0.02F;
        state.runAmount = Mth.clamp(entity.walkAnimation.speed(partialTick) * 1.5F, 0.0F, 1.0F);
        state.attackTime = entity.getAttackAnim(partialTick);
        state.combatPose = entity.getCombatPose();
    }

    @Override
    protected void scale(CultivationSettlerRenderState state, PoseStack poseStack) {
        if (state.role == CultivationSettlerEntity.Role.SECT_ELDER.id()) {
            poseStack.scale(1.08F, 1.08F, 1.08F);
        } else if (state.role == CultivationSettlerEntity.Role.OUTER_DISCIPLE.id()) {
            poseStack.scale(0.95F, 0.96F, 0.95F);
        }
    }

    @Override
    public void submit(CultivationSettlerRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState cameraRenderState) {
        super.submit(state, poseStack, collector, cameraRenderState);
        float age = state.ageInTicks;
        CultivationSettlerEntity.ElementAffinity elem = CultivationSettlerEntity.ElementAffinity.byId(state.element);
        int r = elem.r();
        int g = elem.g();
        int b = elem.b();
        boolean isElder = state.role == CultivationSettlerEntity.Role.SECT_ELDER.id();
        boolean meditating = state.combatPose == CultivationSettlerEntity.POSE_MEDITATE;
        boolean combat = (state.combatPose >= CultivationSettlerEntity.POSE_MELEE
                && state.combatPose <= CultivationSettlerEntity.POSE_BURST)
                || state.attackTime > 0.02F;

        if (isElder || combat || meditating) {
            SpiritVfxHelper.submitBackHalo(poseStack, collector, SpiritVfxHelper.TEX_SWORD_SEAL,
                    state.bodyRot, isElder ? 1.48F : (meditating ? 1.18F : 1.35F), 0.56F,
                    isElder ? (combat ? 0.95F : 0.75F) : (meditating ? 0.58F : 0.65F),
                    age * (meditating ? 0.035F : (isElder ? 0.08F : 0.06F)),
                    r, g, b, combat ? 225 : (meditating ? 165 : 170));
        }
        if (state.trading || combat || meditating || isElder) {
            SpiritVfxHelper.submitGroundSeal(poseStack, collector, SpiritVfxHelper.TEX_BAGUA,
                    combat ? (isElder ? 2.10F : 1.65F) : (meditating ? 1.60F : 1.35F), 0.06F,
                    age * (combat ? 0.09F : (meditating ? 0.045F : 0.04F)),
                    r, g, b, combat ? 220 : (meditating ? 175 : 145));
        }
        if (combat || meditating) {
            SpiritVfxHelper.submitOrbitOrbs(poseStack, collector,
                    meditating ? 4 : (isElder ? 6 : 3),
                    meditating ? 1.05F : (isElder ? 1.20F : 0.95F),
                    meditating ? 0.95F : 1.25F,
                    meditating ? 0.14F : 0.18F,
                    age * (meditating ? 0.75F : 1.45F), r, g, b, meditating ? 185 : 225);
        }
    }

    @Override
    public Identifier getTextureLocation(CultivationSettlerRenderState state) {
        return TEXTURES[Math.max(0, Math.min(TEXTURES.length - 1, state.role))];
    }
}
