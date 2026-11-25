package com.xxsx.earth.xuanxue;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.block.Block;

import java.util.function.Consumer;

public class GuidedXuanhuanBlockItem extends BlockItem {
    private final String hintKey;

    public GuidedXuanhuanBlockItem(Block block, Properties properties, String hintKey) {
        super(block, properties);
        this.hintKey = hintKey;
    }

    @Override
    public Component getName(ItemStack stack) {
        return Component.translatable(getBlock().getDescriptionId());
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> lines, TooltipFlag flag) {
        lines.accept(Component.translatable(hintKey).withStyle(ChatFormatting.GOLD));
        lines.accept(Component.translatable("tooltip.earth_xuanxue.block.use").withStyle(ChatFormatting.GRAY));
    }
}
