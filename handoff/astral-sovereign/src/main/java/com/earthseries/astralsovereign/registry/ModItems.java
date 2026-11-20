package com.earthseries.astralsovereign.registry;

import com.earthseries.astralsovereign.AstralSovereignMod;
import com.earthseries.astralsovereign.item.AstralHalberdItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.ToolMaterial;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {
    public static final DeferredRegister.Items ITEMS =
            DeferredRegister.createItems(AstralSovereignMod.MOD_ID);

    public static final DeferredItem<SpawnEggItem> ASTRAL_SOVEREIGN_SPAWN_EGG =
            ITEMS.registerItem(
                    "astral_sovereign_spawn_egg",
                    props -> new SpawnEggItem(props.spawnEgg(ModEntities.ASTRAL_SOVEREIGN.get()))
            );

    public static final DeferredItem<AstralHalberdItem> ASTRAL_FANGTIAN_HALBERD =
            ITEMS.registerItem(
                    "astral_fangtian_halberd",
                    props -> new AstralHalberdItem(
                            props.sword(ToolMaterial.NETHERITE, 9.0F, -2.6F)
                                    .durability(2500)
                                    .rarity(Rarity.EPIC)
                                    .fireResistant()
                    )
            );
}
