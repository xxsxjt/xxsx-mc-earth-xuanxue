package com.earthseries.astralsovereign;

import com.earthseries.astralsovereign.entity.AstralSovereignEntity;
import com.earthseries.astralsovereign.registry.ModCreativeTabs;
import com.earthseries.astralsovereign.registry.ModEntities;
import com.earthseries.astralsovereign.registry.ModItems;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;

@Mod(AstralSovereignMod.MOD_ID)
public class AstralSovereignMod {
    public static final String MOD_ID = "astral_sovereign";

    public AstralSovereignMod(IEventBus modEventBus) {
        ModEntities.ENTITY_TYPES.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);
        ModCreativeTabs.CREATIVE_MODE_TABS.register(modEventBus);

        modEventBus.addListener(this::registerAttributes);
    }

    private void registerAttributes(EntityAttributeCreationEvent event) {
        event.put(ModEntities.ASTRAL_SOVEREIGN.get(), AstralSovereignEntity.createAttributes().build());
    }
}
