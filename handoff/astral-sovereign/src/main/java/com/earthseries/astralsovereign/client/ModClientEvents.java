package com.earthseries.astralsovereign.client;

import com.earthseries.astralsovereign.AstralSovereignMod;
import com.earthseries.astralsovereign.client.model.AstralSovereignModel;
import com.earthseries.astralsovereign.client.renderer.AstralSovereignRenderer;
import com.earthseries.astralsovereign.registry.ModEntities;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

@EventBusSubscriber(modid = AstralSovereignMod.MOD_ID, value = Dist.CLIENT)
public class ModClientEvents {
    @SubscribeEvent
    public static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(AstralSovereignModel.LAYER_LOCATION, AstralSovereignModel::createBodyLayer);
    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.ASTRAL_SOVEREIGN.get(), AstralSovereignRenderer::new);
    }
}
