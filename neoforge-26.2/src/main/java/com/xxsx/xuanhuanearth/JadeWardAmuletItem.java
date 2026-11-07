package com.xxsx.xuanhuanearth;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;

import java.util.function.Consumer;

/**
 * X-042 Jade Ward Amulet: nephrite vessel (48 AEU, 0.5x stability decay).
 * Right-click captures field; sneak+right-click spends 8 AEU for ward protection.
 */
public class JadeWardAmuletItem extends GuidedXuanhuanItem {
    private static final double WARD_COST_AEU = 8.0D;

    public JadeWardAmuletItem(Properties properties, String hintKey) {
        super(properties, hintKey);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        ItemStack stack = player.getItemInHand(hand);
        JadeComponent jade = JadeComponent.read(stack);
        if (player.isShiftKeyDown()) {
            if (jade.chargeAeu() < WARD_COST_AEU) {
                player.sendSystemMessage(Component.translatable("message.xuanhuan_earth.jade_ward.low_charge")
                        .withStyle(ChatFormatting.YELLOW));
                return InteractionResult.SUCCESS_SERVER;
            }
            stack.set(XuanhuanEarth.JADE_COMPONENT.get(),
                    jade.withCharge(jade.chargeAeu() - WARD_COST_AEU, jade.stability()));
            player.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 200, 0, true, false, true));
            player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 200, 0, true, false, true));
            player.swing(hand, true);
            if (player instanceof ServerPlayer serverPlayer) {
                CultivationNetwork.broadcastVisual(serverPlayer, CultivationVisualAction.TALISMAN);
                XuanhuanJourney.complete(serverPlayer, XuanhuanJourney.Milestone.ARTIFACT_USE);
            }
            if (level instanceof ServerLevel serverLevel) {
                XuanhuanEffects.talismanActivation(serverLevel, player.position());
            }
            player.sendSystemMessage(Component.translatable("message.xuanhuan_earth.jade_ward.activated",
                    ArcanaPower.format(jade.chargeAeu() - WARD_COST_AEU),
                    ArcanaPower.format(jade.stability()))
                    .withStyle(ChatFormatting.AQUA));
            return InteractionResult.SUCCESS_SERVER;
        }
        ArcanaChunkField.Reading reading = Spirituality.reading(level, player.blockPosition());
        double usable = SpiritFieldLedger.availableField(reading.value(), reading.depletion());
        double remaining = JadeComponent.NEPHRITE_CAPACITY_AEU - jade.chargeAeu();
        if (usable <= 0.5D) {
            player.sendSystemMessage(Component.translatable("message.xuanhuan_earth.jade_slip.no_field")
                    .withStyle(ChatFormatting.YELLOW));
            return InteractionResult.SUCCESS_SERVER;
        }
        double requested = 16.0D * Math.min(1.0D, usable / SpiritFieldLedger.FIELD_MAX);
        double capturable = remaining / JadeComponent.NEPHRITE_CAPTURE;
        double spent = Math.min(requested, capturable);
        double rejected = Math.max(0.0D, requested - spent);
        double nextStability = Math.max(0.0D, jade.stability() - 0.01D * rejected);
        double after = Math.min(JadeComponent.NEPHRITE_CAPACITY_AEU, jade.chargeAeu() + spent * JadeComponent.NEPHRITE_CAPTURE);
        stack.set(XuanhuanEarth.JADE_COMPONENT.get(), jade.withCharge(after, nextStability));
        if (spent > 0.0D) {
            Spirituality.consumeField(level, player.blockPosition(), spent);
            player.swing(hand, true);
            if (player instanceof ServerPlayer serverPlayer) {
                CultivationNetwork.broadcastVisual(serverPlayer, CultivationVisualAction.RECOVERY);
            }
            if (level instanceof ServerLevel serverLevel) {
                XuanhuanEffects.stoneCharge(serverLevel, player.position());
            }
        }
        player.sendSystemMessage(Component.translatable("message.xuanhuan_earth.jade_slip.charged",
                ArcanaPower.format(after), ArcanaPower.format(JadeComponent.NEPHRITE_CAPACITY_AEU))
                .withStyle(ChatFormatting.AQUA));
        return InteractionResult.SUCCESS_SERVER;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display,
                                Consumer<Component> lines, TooltipFlag flag) {
        super.appendHoverText(stack, context, display, lines, flag);
        JadeComponent jade = JadeComponent.read(stack);
        lines.accept(Component.translatable("tooltip.xuanhuan_earth.jade.charge",
                ArcanaPower.format(jade.chargeAeu()),
                ArcanaPower.format(JadeComponent.NEPHRITE_CAPACITY_AEU)).withStyle(ChatFormatting.AQUA));
    }
}
