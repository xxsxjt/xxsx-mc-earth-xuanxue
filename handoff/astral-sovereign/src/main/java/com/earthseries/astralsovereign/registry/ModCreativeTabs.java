package com.earthseries.astralsovereign.registry;

import com.earthseries.astralsovereign.AstralSovereignMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, AstralSovereignMod.MOD_ID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> ASTRAL_TAB =
            CREATIVE_MODE_TABS.register("astral_sovereign_tab", () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.astral_sovereign"))
                    .icon(() -> new ItemStack(ModItems.ASTRAL_FANGTIAN_HALBERD.get()))
                    .displayItems((params, output) -> {
                        output.accept(ModItems.ASTRAL_SOVEREIGN_SPAWN_EGG.get());
                        output.accept(ModItems.ASTRAL_FANGTIAN_HALBERD.get());
                    })
                    .build());
}
