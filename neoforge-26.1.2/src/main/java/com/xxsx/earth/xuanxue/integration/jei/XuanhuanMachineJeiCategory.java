package com.xxsx.earth.xuanxue.integration.jei;

import com.xxsx.earth.xuanxue.XuanhuanMachineBlock;
import com.xxsx.earth.xuanxue.ArcanaPower;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.recipe.types.IRecipeType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.ItemLike;

public class XuanhuanMachineJeiCategory implements IRecipeCategory<XuanhuanMachineBlock.Recipe> {
    private final IDrawableStatic background;
    private final IDrawable icon;
    private final XuanhuanMachineBlock.Kind kind;
    private final IRecipeType<XuanhuanMachineBlock.Recipe> recipeType;

    public XuanhuanMachineJeiCategory(IGuiHelper guiHelper, XuanhuanMachineBlock.Kind kind,
                                      IRecipeType<XuanhuanMachineBlock.Recipe> recipeType, ItemLike iconItem) {
        this.background = guiHelper.createBlankDrawable(168, 72);
        this.icon = guiHelper.createDrawableItemLike(iconItem);
        this.kind = kind;
        this.recipeType = recipeType;
    }

    @Override
    public IRecipeType<XuanhuanMachineBlock.Recipe> getRecipeType() {
        return recipeType;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("jei.earth_xuanxue.machine", Component.translatable(kind.displayNameKey()));
    }

    @Override
    public int getWidth() {
        return background.getWidth();
    }

    @Override
    public int getHeight() {
        return background.getHeight();
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, XuanhuanMachineBlock.Recipe recipe, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, 4, 24).addItemStacks(recipe.primaryStacks()).setStandardSlotBackground();
        builder.addSlot(RecipeIngredientRole.INPUT, 30, 24).addItemStacks(recipe.catalystStacks()).setStandardSlotBackground();
        int x = 92;
        int index = 0;
        for (var stack : recipe.outputStacks()) {
            builder.addSlot(RecipeIngredientRole.OUTPUT, x + index * 24, 24).add(stack).setOutputSlotBackground();
            index++;
        }
    }

    @Override
    public void draw(XuanhuanMachineBlock.Recipe recipe, IRecipeSlotsView slots, GuiGraphicsExtractor g, double mouseX, double mouseY) {
        var font = Minecraft.getInstance().font;
        g.text(font, Component.translatable(kind.displayNameKey()), 4, 2, 0xFFD9B3FF);
        g.text(font, "+", 23, 29, 0xFFE8DFAF);
        g.text(font, Component.translatable("jei.earth_xuanxue.process." + kind.blockId()), 58, 29, 0xFFE8DFAF);
        g.text(font, ">", 78, 29, 0xFFE8DFAF);
        g.text(font, Component.translatable("jei.earth_xuanxue.field", recipe.minField(),
                ArcanaPower.format(recipe.displayedFieldDraw())), 4, 50, 0xFF80D0C8);
        String note = Component.translatable(recipe.noteKey()).getString();
        if (font.width(note) > 116) {
            note = font.plainSubstrByWidth(note, 113) + "...";
        }
        g.text(font, note, 44, 60, 0xFFFFF2CC);
    }
}
