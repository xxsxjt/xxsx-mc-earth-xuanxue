package com.xxsx.earth.xuanxue.client;

import com.xxsx.earth.xuanxue.client.model.AstralFlyingSwordModel;
import com.xxsx.earth.xuanxue.client.model.AstralSovereignModel;
import com.xxsx.earth.xuanxue.client.model.CrystalTurtleModel;
import com.xxsx.earth.xuanxue.client.model.CultivationSettlerModel;
import com.xxsx.earth.xuanxue.client.model.EmberCraneModel;
import com.xxsx.earth.xuanxue.client.model.SpiritFoxModel;
import com.xxsx.earth.xuanxue.client.model.StonehornGoatModel;
import com.xxsx.earth.xuanxue.client.model.WindWolfModel;
import com.xxsx.earth.xuanxue.client.particle.XuanhuanParticleProviders;
import com.xxsx.earth.xuanxue.client.renderer.AstralFlyingSwordRenderer;
import com.xxsx.earth.xuanxue.client.renderer.AstralSovereignRenderer;
import com.xxsx.earth.xuanxue.client.renderer.HalberdSwingVfx;
import com.xxsx.earth.xuanxue.client.renderer.XuanhuanMachineBlockEntityRenderer;
import com.xxsx.earth.xuanxue.client.renderer.CultivationSettlerRenderer;
import com.xxsx.earth.xuanxue.client.renderer.SpiritBeastRenderer;
import com.xxsx.earth.xuanxue.client.renderer.SpiritFoxRenderer;
import com.xxsx.earth.xuanxue.XuanhuanEarth;
import com.xxsx.earth.xuanxue.CultivationActionPayload;
import com.xxsx.earth.xuanxue.CultivationFocus;
import com.xxsx.earth.xuanxue.CultivationStatusPayload;
import com.xxsx.earth.xuanxue.CultivationVisualPayload;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.minecraft.client.renderer.block.FluidModel;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.event.RegisterFluidModelsEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.fluid.FluidTintSource;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.neoforged.neoforge.client.network.event.RegisterClientPayloadHandlersEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.lwjgl.glfw.GLFW;

public final class XuanhuanEarthClient {
    private static final KeyMapping.Category CATEGORY =
            new KeyMapping.Category(XuanhuanEarth.id("controls"));
    private static final KeyMapping OPEN_CULTIVATION = new KeyMapping(
            "key.earth_xuanxue.open_cultivation",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_K,
            CATEGORY);
    private static final KeyMapping ACTIVATE_TECHNIQUE = new KeyMapping(
            "key.earth_xuanxue.activate_technique",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_V,
            CATEGORY);
    private static CultivationStatusPayload cultivationStatus = CultivationStatusPayload.empty();
    private XuanhuanEarthClient() {
    }

    public static void register(IEventBus modBus) {
        modBus.addListener(XuanhuanEarthClient::registerScreens);
        modBus.addListener(XuanhuanEarthClient::registerLayerDefinitions);
        modBus.addListener(XuanhuanEarthClient::registerEntityRenderers);
        modBus.addListener(XuanhuanEarthClient::registerGuiLayers);
        modBus.addListener(XuanhuanEarthClient::registerPayloadHandlers);
        modBus.addListener(XuanhuanEarthClient::registerKeyMappings);
        modBus.addListener(XuanhuanParticleProviders::register);
        modBus.addListener(XuanhuanEarthClient::registerFluidModels);
        NeoForge.EVENT_BUS.addListener(XuanhuanEarthClient::clientTick);
        HalberdSwingVfx.register();
        NeoForge.EVENT_BUS.addListener(CultivationPlayerAnimations::renderPlayer);
        NeoForge.EVENT_BUS.addListener(XuanhuanTooltipHandler::onItemTooltip);
    }

    private static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(XuanhuanEarth.XUANHUAN_MACHINE_MENU.get(), XuanhuanMachineScreen::new);
    }

    private static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(XuanhuanEarth.MEDITATION_SEAT.get(), InvisibleSeatRenderer::new);
        event.registerEntityRenderer(XuanhuanEarth.SPIRIT_FOX.get(), SpiritFoxRenderer::new);
        event.registerEntityRenderer(XuanhuanEarth.WIND_WOLF.get(), context ->
                new SpiritBeastRenderer<>(context, WindWolfModel.LAYER_LOCATION, WindWolfModel::new,
                        XuanhuanEarth.id("textures/entity/wind_wolf.png"), 0.5F, 1.0F));
        event.registerEntityRenderer(XuanhuanEarth.STONEHORN_GOAT.get(), context ->
                new SpiritBeastRenderer<>(context, StonehornGoatModel.LAYER_LOCATION, StonehornGoatModel::new,
                        XuanhuanEarth.id("textures/entity/stonehorn_goat.png"), 0.68F, 1.0F));
        event.registerEntityRenderer(XuanhuanEarth.CRYSTAL_TURTLE.get(), context ->
                new SpiritBeastRenderer<>(context, CrystalTurtleModel.LAYER_LOCATION, CrystalTurtleModel::new,
                        XuanhuanEarth.id("textures/entity/crystal_turtle.png"), 0.62F, 1.0F));
        event.registerEntityRenderer(XuanhuanEarth.EMBER_CRANE.get(), context ->
                new SpiritBeastRenderer<>(context, EmberCraneModel.LAYER_LOCATION, EmberCraneModel::new,
                        XuanhuanEarth.id("textures/entity/ember_crane.png"), 0.42F, 1.0F));
        event.registerEntityRenderer(XuanhuanEarth.CULTIVATION_SETTLER.get(), CultivationSettlerRenderer::new);
        event.registerEntityRenderer(XuanhuanEarth.ASTRAL_SOVEREIGN.get(), AstralSovereignRenderer::new);
        event.registerEntityRenderer(XuanhuanEarth.ASTRAL_FLYING_SWORD.get(), AstralFlyingSwordRenderer::new);
        event.registerBlockEntityRenderer(XuanhuanEarth.XUANHUAN_MACHINE_BLOCK_ENTITY.get(), XuanhuanMachineBlockEntityRenderer::new);
    }

    private static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(SpiritFoxModel.LAYER_LOCATION, SpiritFoxModel::createBodyLayer);
        event.registerLayerDefinition(WindWolfModel.LAYER_LOCATION, WindWolfModel::createBodyLayer);
        event.registerLayerDefinition(StonehornGoatModel.LAYER_LOCATION, StonehornGoatModel::createBodyLayer);
        event.registerLayerDefinition(CrystalTurtleModel.LAYER_LOCATION, CrystalTurtleModel::createBodyLayer);
        event.registerLayerDefinition(EmberCraneModel.LAYER_LOCATION, EmberCraneModel::createBodyLayer);
        event.registerLayerDefinition(CultivationSettlerModel.LAYER_LOCATION, CultivationSettlerModel::createBodyLayer);
        event.registerLayerDefinition(AstralSovereignModel.LAYER_LOCATION, AstralSovereignModel::createBodyLayer);
        event.registerLayerDefinition(AstralFlyingSwordModel.LAYER_LOCATION, AstralFlyingSwordModel::createBodyLayer);
    }

    private static void registerGuiLayers(RegisterGuiLayersEvent event) {
        event.registerAboveAll(XuanhuanEarth.id("meditation_hud"), MeditationHud::render);
    }

    private static void registerPayloadHandlers(RegisterClientPayloadHandlersEvent event) {
        event.register(CultivationStatusPayload.TYPE, XuanhuanEarthClient::handleCultivationStatus);
        event.register(CultivationVisualPayload.TYPE, XuanhuanEarthClient::handleCultivationVisual);
    }

    private static void registerFluidModels(RegisterFluidModelsEvent event) {
        FluidModel.Unbaked model = new FluidModel.Unbaked(
                new Material(Identifier.withDefaultNamespace("block/water_still"), true),
                new Material(Identifier.withDefaultNamespace("block/water_flow"), true),
                new Material(Identifier.withDefaultNamespace("block/water_overlay"), true),
                (FluidTintSource) state -> 0xFF3CE8D4
        );
        event.register(model, XuanhuanEarth.SPIRIT_SPRING_FLUID, XuanhuanEarth.SPIRIT_SPRING_FLOWING);
    }

    private static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        event.registerCategory(CATEGORY);
        event.register(OPEN_CULTIVATION);
        event.register(ACTIVATE_TECHNIQUE);
    }

    private static void clientTick(ClientTickEvent.Post event) {
        CultivationPlayerAnimations.tick();
        while (OPEN_CULTIVATION.consumeClick()) {
            Minecraft minecraft = Minecraft.getInstance();
            if (minecraft.player != null && minecraft.getConnection() != null) {
                requestOpenCultivation();
            }
        }
        while (ACTIVATE_TECHNIQUE.consumeClick()) {
            Minecraft minecraft = Minecraft.getInstance();
            if (minecraft.player != null && minecraft.getConnection() != null && minecraft.screen == null) {
                requestActivateTechnique();
            }
        }
    }

    private static void handleCultivationStatus(CultivationStatusPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            cultivationStatus = payload;
            if (payload.openScreen() && !(Minecraft.getInstance().screen instanceof CultivationScreen)) {
                Minecraft.getInstance().setScreen(new CultivationScreen());
            }
        });
    }

    private static void handleCultivationVisual(CultivationVisualPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> CultivationPlayerAnimations.start(payload));
    }

    public static CultivationStatusPayload cultivationStatus() {
        return cultivationStatus;
    }

    public static void requestCultivationFocus(CultivationFocus focus) {
        ClientPacketDistributor.sendToServer(new CultivationActionPayload(focus.id()));
    }

    public static void requestStopCultivating() {
        ClientPacketDistributor.sendToServer(new CultivationActionPayload(CultivationActionPayload.STOP_RIDING));
    }

    public static void requestOpenCultivation() {
        ClientPacketDistributor.sendToServer(new CultivationActionPayload(CultivationActionPayload.OPEN_PANEL));
    }

    public static void requestPractice() {
        ClientPacketDistributor.sendToServer(new CultivationActionPayload(CultivationActionPayload.PRACTICE));
    }

    public static void requestBreakthrough() {
        ClientPacketDistributor.sendToServer(
                new CultivationActionPayload(CultivationActionPayload.BREAKTHROUGH));
    }

    public static void requestActivateTechnique() {
        ClientPacketDistributor.sendToServer(new CultivationActionPayload(CultivationActionPayload.ACTIVATE_SKILL));
    }

    public static void requestCultivationRefresh() {
        ClientPacketDistributor.sendToServer(new CultivationActionPayload(CultivationActionPayload.REFRESH_STATUS));
    }

    public static void openHandbook() {
        if (Minecraft.getInstance().getConnection() != null) {
            requestCultivationRefresh();
        }
        Minecraft.getInstance().setScreen(new XuanhuanHandbookScreen());
    }
}
