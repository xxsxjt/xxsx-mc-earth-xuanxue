package com.xxsx.xuanhuanearth;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

import java.util.function.Consumer;

/** X-001 Uncharged raw spirit stone matrix (stack 16, matrix_grade 1..3). */
public class SpiritStoneRawItem extends GuidedXuanhuanItem {
    public SpiritStoneRawItem(Properties properties, String hintKey) {
        super(properties, hintKey);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display,
                                Consumer<Component> lines, TooltipFlag flag) {
        super.appendHoverText(stack, context, display, lines, flag);
        SpiritStoneComponent state = SpiritStoneComponent.read(stack);
        lines.accept(Component.translatable("tooltip.xuanhuan_earth.spirit_stone_raw.grade", state.matrixGrade())
                .withStyle(ChatFormatting.AQUA));
    }
}
