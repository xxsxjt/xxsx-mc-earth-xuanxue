package com.xxsx.xuanhuanearth;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
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

public class QiRecoveryPillItem extends GuidedXuanhuanItem {
    public static final int COOLDOWN_TICKS = 200;

    public QiRecoveryPillItem(Properties properties, String hintKey) {
        super(properties, hintKey);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.getCooldowns().isOnCooldown(stack)) {
            return InteractionResult.FAIL;
        }
        if (!level.isClientSide()) {
            double before = ArcanaPower.getCurrentMana(player);
            if (before >= ArcanaPower.getMaxMana(player) - 0.0001D && !EarthHumanCompat.canRecover(player)) {
                player.sendSystemMessage(Component.translatable(
                        "message.xuanhuan_earth.qi_pill.full",
                        ArcanaPower.format(before),
                        ArcanaPower.format(ArcanaPower.getMaxMana(player))).withStyle(ChatFormatting.YELLOW));
                return InteractionResult.FAIL;
            }
            PillComponent pillComp = PillComponent.read(stack, "qi_recovery_pill");
            double mult = pillComp.grade().durationMultiplier();
            ArcanaPower.setCurrentMana(player, before + 40.0D * mult);
            double restored = ArcanaPower.getCurrentMana(player) - before;
            double nauseaChance = pillComp.grade().nauseaChance();
            if (nauseaChance > 0.0D && level.getRandom().nextDouble() < nauseaChance) {
                player.addEffect(new MobEffectInstance(MobEffects.NAUSEA, 120, 0, false, true, true));
            }
            player.getCooldowns().addCooldown(stack, COOLDOWN_TICKS);
            if (player instanceof ServerPlayer serverPlayer) {
                CultivationNetwork.broadcastVisual(serverPlayer, CultivationVisualAction.RECOVERY);
            }
            EarthHumanCompat.RecoveryReport report = player instanceof ServerPlayer serverPlayer
                    ? EarthHumanCompat.recoverCore(serverPlayer, 4.0D, 0.16D)
                    : new EarthHumanCompat.RecoveryReport(0.0D, 0.0D);
            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }
            if (player instanceof ServerPlayer serverPlayer) {
                XuanhuanJourney.complete(serverPlayer, XuanhuanJourney.Milestone.ARTIFACT_USE);
            }
            player.sendSystemMessage(Component.translatable("message.xuanhuan_earth.qi_pill.used",
                    ArcanaPower.format(restored),
                    ArcanaPower.format(ArcanaPower.getCurrentMana(player)),
                    ArcanaPower.format(ArcanaPower.getMaxMana(player))).withStyle(ChatFormatting.AQUA));
            if (report.changed()) {
                player.sendSystemMessage(Component.translatable("message.xuanhuan_earth.human_recovery",
                        ArcanaPower.format(report.fatigueReduced()),
                        ArcanaPower.format(report.bodyHealed())).withStyle(ChatFormatting.GREEN));
            }
        }
        return level.isClientSide() ? InteractionResult.SUCCESS : InteractionResult.SUCCESS_SERVER;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display,
                                Consumer<Component> lines, TooltipFlag flag) {
        super.appendHoverText(stack, context, display, lines, flag);
        PillComponent comp = PillComponent.read(stack, "qi_recovery_pill");
        lines.accept(Component.translatable("tooltip.xuanhuan_earth.pill.grade",
                Component.translatable("grade.earth_lore." + comp.grade().serialized()),
                Math.round(comp.purity() * 100.0D)).withStyle(ChatFormatting.GOLD));
    }
}
