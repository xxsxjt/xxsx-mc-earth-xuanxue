package com.xxsx.xuanhuanearth.client.renderer;

import com.xxsx.xuanhuanearth.XuanhuanEarth;
import com.xxsx.xuanhuanearth.client.model.AstralSovereignModel;
import com.xxsx.xuanhuanearth.entity.AstralSovereignEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.Identifier;

public class AstralSovereignRenderer extends MobRenderer<AstralSovereignEntity, AstralSovereignRenderState, AstralSovereignModel> {
    private static final Identifier TEXTURE_PHASE1 =
            XuanhuanEarth.id("textures/entity/astral_sovereign_p1.png");
    private static final Identifier TEXTURE_PHASE2 =
            XuanhuanEarth.id("textures/entity/astral_sovereign_p2.png");

    public AstralSovereignRenderer(EntityRendererProvider.Context context) {
        super(context, new AstralSovereignModel(context.bakeLayer(AstralSovereignModel.LAYER_LOCATION)), 1.05F);
    }

    @Override
    public AstralSovereignRenderState createRenderState() {
        return new AstralSovereignRenderState();
    }

    @Override
    public void extractRenderState(AstralSovereignEntity entity, AstralSovereignRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        state.phase2 = entity.isPhase2();
        state.actionType = entity.getActionType();
        state.actionProgress = entity.getActionProgress();
        state.swordBuffActive = entity.isSwordBuffActive();
    }

    @Override
    public Identifier getTextureLocation(AstralSovereignRenderState state) {
        return state.phase2 ? TEXTURE_PHASE2 : TEXTURE_PHASE1;
    }
}
