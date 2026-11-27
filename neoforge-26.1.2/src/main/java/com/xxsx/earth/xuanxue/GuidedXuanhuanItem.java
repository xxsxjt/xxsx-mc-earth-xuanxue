package com.xxsx.earth.xuanxue;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

import java.util.function.Consumer;

public class GuidedXuanhuanItem extends Item {
    private final String hintKey;

    public GuidedXuanhuanItem(Properties properties, String hintKey) {
        super(properties);
        this.hintKey = hintKey;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> lines, TooltipFlag flag) {
        lines.accept(Component.translatable(hintKey).withStyle(ChatFormatting.GOLD));
        lines.accept(Component.translatable("tooltip.earth_xuanxue.material.route").withStyle(ChatFormatting.GRAY));
    }
}
