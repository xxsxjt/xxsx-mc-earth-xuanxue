package com.xxsx.earth.xuanxue.client;

import com.xxsx.earth.xuanxue.MeditationSeatEntity;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;

public class InvisibleSeatRenderer extends EntityRenderer<MeditationSeatEntity, EntityRenderState> {
    public InvisibleSeatRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.0F;
        this.shadowStrength = 0.0F;
    }

    @Override
    public EntityRenderState createRenderState() {
        return new EntityRenderState();
    }
}
