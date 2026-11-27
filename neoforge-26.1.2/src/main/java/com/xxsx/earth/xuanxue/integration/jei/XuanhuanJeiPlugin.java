package com.xxsx.earth.xuanxue.integration.jei;

import com.xxsx.earth.xuanxue.XuanhuanEarth;
import com.xxsx.earth.xuanxue.XuanhuanMachineBlock;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.recipe.types.IRecipeType;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

@JeiPlugin
public class XuanhuanJeiPlugin implements IModPlugin {
    private static final Map<XuanhuanMachineBlock.Kind, IRecipeType<XuanhuanMachineBlock.Recipe>> TYPES = createTypes();

    @Override
    public Identifier getPluginUid() {
        return XuanhuanEarth.id("jei_plugin");
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        var guiHelper = registration.getJeiHelpers().getGuiHelper();
        for (XuanhuanMachineBlock.Kind kind : XuanhuanMachineBlock.Kind.values()) {
            registration.addRecipeCategories(new XuanhuanMachineJeiCategory(guiHelper, kind, recipeTypeFor(kind), machineFor(kind)));
        }
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        for (XuanhuanMachineBlock.Kind kind : XuanhuanMachineBlock.Kind.values()) {
            registration.addRecipes(recipeTypeFor(kind), XuanhuanMachineBlock.recipesFor(kind));
        }
        registration.addItemStackInfo(new ItemStack(XuanhuanEarth.XUANHUAN_HANDBOOK.get()),
                line("jei.earth_xuanxue.handbook.0", ChatFormatting.GOLD),
                line("jei.earth_xuanxue.handbook.1", ChatFormatting.WHITE),
                line("jei.earth_xuanxue.handbook.2", ChatFormatting.AQUA),
                line("jei.earth_xuanxue.handbook.3", ChatFormatting.YELLOW));
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        for (XuanhuanMachineBlock.Kind kind : XuanhuanMachineBlock.Kind.values()) {
            registration.addCraftingStation(recipeTypeFor(kind), machineFor(kind));
        }
    }

    public static IRecipeType<XuanhuanMachineBlock.Recipe> recipeTypeFor(XuanhuanMachineBlock.Kind kind) {
        return TYPES.get(kind);
    }

    private static Map<XuanhuanMachineBlock.Kind, IRecipeType<XuanhuanMachineBlock.Recipe>> createTypes() {
        EnumMap<XuanhuanMachineBlock.Kind, IRecipeType<XuanhuanMachineBlock.Recipe>> types =
                new EnumMap<>(XuanhuanMachineBlock.Kind.class);
        for (XuanhuanMachineBlock.Kind kind : XuanhuanMachineBlock.Kind.values()) {
            types.put(kind, IRecipeType.create(XuanhuanEarth.MODID, "processing_" + kind.blockId(), XuanhuanMachineBlock.Recipe.class));
        }
        return Collections.unmodifiableMap(types);
    }

    private static ItemLike machineFor(XuanhuanMachineBlock.Kind kind) {
        return switch (kind) {
            case ALCHEMY_FURNACE -> XuanhuanEarth.ALCHEMY_FURNACE.get();
            case TALISMAN_TABLE -> XuanhuanEarth.TALISMAN_TABLE.get();
            case SPIRIT_ARRAY_CORE -> XuanhuanEarth.SPIRIT_ARRAY_CORE.get();
        };
    }

    private static Component line(String key, ChatFormatting color) {
        return Component.translatable(key).withStyle(color);
    }
}
