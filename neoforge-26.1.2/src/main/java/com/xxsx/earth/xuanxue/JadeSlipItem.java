package com.xxsx.earth.xuanxue;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
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

import java.util.function.Consumer;

/**
 * X-042 Jade Slip: serves as both a 48 AEU nephrite energy vessel and a Xuanxue literature carrier
 * (A-014, study_factor = 0.5, tagged earth_lore:carriers and earth_lore:custom_study).
 */
public class JadeSlipItem extends GuidedXuanhuanItem {
    private static final double BATCH_AEU = 24.0D;
    private static final double RELEASE_MANA = 8.0D;

    public JadeSlipItem(Properties properties, String hintKey) {
        super(properties, hintKey);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        InteractionResult loreUse = XuanhuanLoreIntegration.tryUseJadeSlipAsLiterature(level, player, hand, stack);
        if (loreUse != null) {
            return loreUse;
        }

        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        if (player.isShiftKeyDown()) {
            releaseMana(level, player, hand, stack);
        } else {
            captureField(level, player, hand, stack);
        }
        return InteractionResult.SUCCESS_SERVER;
    }

    private static void captureField(Level level, Player player, InteractionHand hand, ItemStack stack) {
        JadeComponent jade = JadeComponent.read(stack);
        double remaining = JadeComponent.NEPHRITE_CAPACITY_AEU - jade.chargeAeu();
        ArcanaChunkField.Reading reading = Spirituality.reading(level, player.blockPosition());
        double usable = SpiritFieldLedger.availableField(reading.value(), reading.depletion());
        if (remaining <= 0.0001D || usable <= 0.5D) {
            player.sendSystemMessage(Component.translatable("message.earth_xuanxue.jade_slip.no_field")
                    .withStyle(ChatFormatting.YELLOW));
            return;
        }
        double coupling = Math.min(1.0D, usable / SpiritFieldLedger.FIELD_MAX);
        double spent = Math.min(BATCH_AEU * coupling * coupling, remaining / JadeComponent.NEPHRITE_CAPTURE);
        if (spent <= 0.0001D) {
            player.sendSystemMessage(Component.translatable("message.earth_xuanxue.jade_slip.no_field")
                    .withStyle(ChatFormatting.YELLOW));
            return;
        }
        double gained = spent * JadeComponent.NEPHRITE_CAPTURE;
        double after = Math.min(JadeComponent.NEPHRITE_CAPACITY_AEU, jade.chargeAeu() + gained);
        stack.set(XuanhuanEarth.JADE_COMPONENT.get(), jade.withCharge(after, jade.stability()));
        Spirituality.consumeField(level, player.blockPosition(), spent);
        player.swing(hand, true);
        if (player instanceof ServerPlayer serverPlayer) {
            CultivationNetwork.broadcastVisual(serverPlayer, CultivationVisualAction.RECOVERY);
        }
        if (level instanceof ServerLevel serverLevel) {
            XuanhuanEffects.stoneCharge(serverLevel, player.position());
        }
        player.sendSystemMessage(Component.translatable("message.earth_xuanxue.jade_slip.charged",
                ArcanaPower.format(after), ArcanaPower.format(JadeComponent.NEPHRITE_CAPACITY_AEU))
                .withStyle(ChatFormatting.AQUA));
    }

    private static void releaseMana(Level level, Player player, InteractionHand hand, ItemStack stack) {
        JadeComponent jade = JadeComponent.read(stack);
        double missing = ArcanaPower.getMaxMana(player) - ArcanaPower.getCurrentMana(player);
        if (missing <= 0.0001D || jade.chargeAeu() <= 0.0001D) {
            player.sendSystemMessage(Component.translatable("message.earth_xuanxue.jade_slip.release_skip")
                    .withStyle(ChatFormatting.YELLOW));
            return;
        }
        double targetMana = Math.min(RELEASE_MANA, missing);
        double neededAeu = targetMana * SpiritStoneCharge.AEU_PER_MANA / JadeComponent.NEPHRITE_RELEASE;
        double spent = Math.min(jade.chargeAeu(), neededAeu);
        double restored = spent * JadeComponent.NEPHRITE_RELEASE / SpiritStoneCharge.AEU_PER_MANA;
        ArcanaPower.setCurrentMana(player, ArcanaPower.getCurrentMana(player) + restored);
        stack.set(XuanhuanEarth.JADE_COMPONENT.get(), jade.withCharge(jade.chargeAeu() - spent, jade.stability()));
        player.swing(hand, true);
        if (player instanceof ServerPlayer serverPlayer) {
            CultivationNetwork.broadcastVisual(serverPlayer, CultivationVisualAction.RECOVERY);
            if (level instanceof ServerLevel serverLevel) {
                XuanhuanEffects.stoneRelease(serverLevel, player.position());
            }
        }
        player.sendSystemMessage(Component.translatable("message.earth_xuanxue.jade_slip.released",
                ArcanaPower.format(restored), ArcanaPower.format(jade.chargeAeu() - spent))
                .withStyle(ChatFormatting.GOLD));
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity user) {
        return XuanhuanLoreIntegration.jadeSlipStudyUseDuration(stack);
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
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display,
                                Consumer<Component> lines, TooltipFlag flag) {
        super.appendHoverText(stack, context, display, lines, flag);
        JadeComponent jade = JadeComponent.read(stack);
        lines.accept(Component.translatable("tooltip.earth_xuanxue.jade.charge",
                ArcanaPower.format(jade.chargeAeu()),
                ArcanaPower.format(JadeComponent.NEPHRITE_CAPACITY_AEU)).withStyle(ChatFormatting.AQUA));
    }
}
