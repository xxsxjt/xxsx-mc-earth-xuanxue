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

public class CultivationManualItem extends Item {
    public enum Type {
        BIGU("bigu_manual"),
        BODY_TEMPERING("body_tempering_manual"),
        FETAL_BREATH("fetal_breath_manual");

        private final String id;

        Type(String id) {
            this.id = id;
        }

        public Identifier nodeId() {
            return switch (this) {
                case BIGU -> XuanhuanLoreIntegration.K_XUA_05_BIGU;
                case BODY_TEMPERING -> XuanhuanLoreIntegration.K_XUA_03_BODY_TEMPERING;
                case FETAL_BREATH -> XuanhuanLoreIntegration.K_XUA_04_FETAL_BREATH;
            };
        }
    }

    private final Type type;

    public CultivationManualItem(Properties properties, Type type) {
        super(properties);
        this.type = type;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        return XuanhuanLoreIntegration.useManual(level, player, hand, player.getItemInHand(hand), List.of(type.nodeId()));
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
        lines.accept(Component.translatable("tooltip.earth_xuanxue." + type.id).withStyle(ChatFormatting.GOLD));
        lines.accept(Component.translatable("tooltip.earth_xuanxue." + type.id + ".use").withStyle(ChatFormatting.GRAY));
    }

    static void sendAdaptationStatus(Player player) {
        player.sendSystemMessage(Component.translatable(
                "message.earth_xuanxue.cultivation_manual.body_status",
                Math.round(ArcanaPower.getExhaustionReduction(player) * 100.0D),
                Math.round(ArcanaPower.getCombatDamageReduction(player) * 100.0D),
                Math.round((ArcanaPower.getBreathMultiplier(player) - 1.0D) * 100.0D),
                ArcanaPower.format(ArcanaPower.getMaxMana(player)))
                .withStyle(ChatFormatting.DARK_AQUA));
    }
}
