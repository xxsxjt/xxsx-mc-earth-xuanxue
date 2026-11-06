package com.xxsx.xuanhuanearth;

import com.xxsx.earthlore.api.LoreApi;
import com.xxsx.earthlore.api.StudyResult;
import com.xxsx.earthlore.data.LoreText;
import com.xxsx.earthlore.registry.LoreContent;
import com.xxsx.earthlore.study.LiteratureItem;
import com.xxsx.earthlore.study.StudyService;
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

        public LoreText defaultLoreText() {
            return XuanhuanLoreIntegration.manualLoreText(List.of(nodeId()));
        }
    }

    private final Type type;

    public CultivationManualItem(Properties properties, Type type) {
        super(properties);
        this.type = type;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!stack.has(LoreContent.LORE_TEXT.get())) {
            stack.set(LoreContent.LORE_TEXT.get(), type.defaultLoreText());
        }
        if (level.isClientSide()) {
            if (!LiteratureItem.clientCanStudy.test(player, stack)) {
                return InteractionResult.FAIL;
            }
            player.startUsingItem(hand);
            return InteractionResult.CONSUME;
        }
        if (player instanceof ServerPlayer serverPlayer) {
            StudyResult result = LoreApi.beginStudy(serverPlayer, stack);
            if (!result.started()) {
                serverPlayer.sendOverlayMessage(StudyService.failMessage(result));
                return InteractionResult.FAIL;
            }
            player.startUsingItem(hand);
            return InteractionResult.CONSUME;
        }
        return InteractionResult.SUCCESS_SERVER;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity user) {
        return LiteratureItem.USE_DURATION;
    }

    @Override
    public ItemUseAnimation getUseAnimation(ItemStack stack) {
        return ItemUseAnimation.NONE;
    }

    @Override
    public boolean releaseUsing(ItemStack stack, Level level, LivingEntity entity, int remainingTime) {
        if (entity instanceof ServerPlayer serverPlayer) {
            StudyService.cancel(serverPlayer);
        }
        return false;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> lines, TooltipFlag flag) {
        lines.accept(Component.translatable("tooltip.xuanhuan_earth." + type.id).withStyle(ChatFormatting.GOLD));
        lines.accept(Component.translatable("tooltip.xuanhuan_earth." + type.id + ".use").withStyle(ChatFormatting.GRAY));
    }

    static void sendAdaptationStatus(Player player) {
        player.sendSystemMessage(Component.translatable(
                "message.xuanhuan_earth.cultivation_manual.body_status",
                Math.round(ArcanaPower.getExhaustionReduction(player) * 100.0D),
                Math.round(ArcanaPower.getCombatDamageReduction(player) * 100.0D),
                Math.round((ArcanaPower.getBreathMultiplier(player) - 1.0D) * 100.0D),
                ArcanaPower.format(ArcanaPower.getMaxMana(player)))
                .withStyle(ChatFormatting.DARK_AQUA));
    }
}
