package com.xxsx.xuanhuanearth;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;

import java.util.function.Consumer;

/** X-062 Array Rune Stone: carries one of the four formation runes (power, guard, flow, bind). */
public class ArrayRuneStoneItem extends GuidedXuanhuanItem {
    public ArrayRuneStoneItem(Properties properties, String hintKey) {
        super(properties, hintKey);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (player.isShiftKeyDown()) {
            if (!level.isClientSide()) {
                ItemStack stack = player.getItemInHand(hand);
                RuneStoneComponent.Rune next = RuneStoneComponent.read(stack).rune().next();
                stack.set(XuanhuanEarth.RUNE_STONE_COMPONENT.get(), new RuneStoneComponent(next));
                player.sendSystemMessage(Component.translatable(
                        "message.xuanhuan_earth.array_rune_stone.cycled",
                        Component.translatable("rune.xuanhuan_earth." + next.id()))
                        .withStyle(ChatFormatting.AQUA));
            }
            return level.isClientSide() ? InteractionResult.SUCCESS : InteractionResult.SUCCESS_SERVER;
        }
        return super.use(level, player, hand);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display,
                                Consumer<Component> lines, TooltipFlag flag) {
        super.appendHoverText(stack, context, display, lines, flag);
        RuneStoneComponent comp = RuneStoneComponent.read(stack);
        lines.accept(Component.translatable("tooltip.xuanhuan_earth.array_rune_stone.rune",
                Component.translatable("rune.xuanhuan_earth." + comp.rune().id()))
                .withStyle(ChatFormatting.GOLD));
    }
}
