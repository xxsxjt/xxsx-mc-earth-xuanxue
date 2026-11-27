package com.xxsx.earth.xuanxue;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.function.Consumer;

public class QiGuidingManualItem extends Item {
    public static final List<Identifier> DEFAULT_NODES = List.of(
            XuanhuanLoreIntegration.K_XUA_01_QI_GUIDING,
            XuanhuanLoreIntegration.K_XUA_02_CIRCULATION);

    public QiGuidingManualItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        return XuanhuanLoreIntegration.useManual(level, player, hand, player.getItemInHand(hand), DEFAULT_NODES);
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity user) {
        return XuanhuanLoreIntegration.manualStudyUseDuration();
    }

    @Override
    public ItemUseAnimation getUseAnimation(ItemStack stack) {
        return ItemUseAnimation.NONE;
    }

    @Override
    public boolean releaseUsing(ItemStack stack, Level level, LivingEntity entity, int remainingTime) {
        if (entity instanceof ServerPlayer serverPlayer) {
            XuanhuanLoreIntegration.cancelStudy(serverPlayer);
        }
        return false;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> lines, TooltipFlag flag) {
        lines.accept(Component.translatable("tooltip.earth_xuanxue.qi_guiding_manual").withStyle(ChatFormatting.GOLD));
        lines.accept(Component.translatable("tooltip.earth_xuanxue.qi_guiding_manual.use")
                .withStyle(ChatFormatting.GRAY));
    }
}
