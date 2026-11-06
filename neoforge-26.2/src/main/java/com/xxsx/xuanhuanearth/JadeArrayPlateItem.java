package com.xxsx.xuanhuanearth;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

import java.util.function.Consumer;

/**
 * X-042 Jade Array Plate: jadeite formation plate adding +0..2 scan radius bonus to a linked spirit array core.
 */
public class JadeArrayPlateItem extends GuidedXuanhuanItem {
    public JadeArrayPlateItem(Properties properties, String hintKey) {
        super(properties, hintKey);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        if (level.getBlockState(pos).is(XuanhuanEarth.SPIRIT_ARRAY_CORE.get())) {
            if (!level.isClientSide() && context.getPlayer() != null) {
                ItemStack stack = context.getItemInHand();
                JadeComponent jade = JadeComponent.read(stack);
                int bonus = jade.radiusBonus() <= 0 ? 2 : jade.radiusBonus();
                stack.set(XuanhuanEarth.JADE_COMPONENT.get(), jade.withArray(pos, bonus));
                if (level.getBlockEntity(pos) instanceof XuanhuanMachineBlockEntity machine) {
                    machine.setRadiusBonus(bonus);
                }
                context.getPlayer().sendSystemMessage(Component.translatable(
                        "message.xuanhuan_earth.jade_plate.linked", pos.toShortString(), bonus)
                        .withStyle(ChatFormatting.AQUA));
            }
            return level.isClientSide() ? InteractionResult.SUCCESS : InteractionResult.SUCCESS_SERVER;
        }
        return super.useOn(context);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        ItemStack stack = player.getItemInHand(hand);
        JadeComponent jade = JadeComponent.read(stack);
        int next = (jade.radiusBonus() + 1) % 3;
        stack.set(XuanhuanEarth.JADE_COMPONENT.get(), jade.withArray(jade.linkedArray(), next));
        if (!jade.linkedArray().equals(BlockPos.ZERO)
                && level.isLoaded(jade.linkedArray())
                && level.getBlockEntity(jade.linkedArray()) instanceof XuanhuanMachineBlockEntity machine) {
            machine.setRadiusBonus(next);
        }
        player.sendSystemMessage(Component.translatable("message.xuanhuan_earth.jade_plate.radius", next)
                .withStyle(ChatFormatting.GOLD));
        return InteractionResult.SUCCESS_SERVER;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display,
                                Consumer<Component> lines, TooltipFlag flag) {
        super.appendHoverText(stack, context, display, lines, flag);
        JadeComponent jade = JadeComponent.read(stack);
        lines.accept(Component.translatable("tooltip.xuanhuan_earth.jade_plate.status",
                jade.linkedArray().equals(BlockPos.ZERO) ? "-" : jade.linkedArray().toShortString(),
                jade.radiusBonus()).withStyle(ChatFormatting.GRAY));
    }
}
