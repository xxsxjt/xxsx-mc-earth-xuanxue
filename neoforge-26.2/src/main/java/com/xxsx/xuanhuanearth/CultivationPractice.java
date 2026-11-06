package com.xxsx.xuanhuanearth;

import com.xxsx.xuanhuanearth.particle.XuanhuanParticles;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.phys.Vec3;

public final class CultivationPractice {
    public enum Support {
        FREE(0, 0.72D, 0.72D, "free"),
        CUSHION(16, 1.0D, 1.0D, "cushion");

        private final int fieldBonus;
        private final double efficiency;
        private final double recoveryScale;
        private final String translationSuffix;

        Support(int fieldBonus, double efficiency, double recoveryScale, String translationSuffix) {
            this.fieldBonus = fieldBonus;
            this.efficiency = efficiency;
            this.recoveryScale = recoveryScale;
            this.translationSuffix = translationSuffix;
        }

        int fieldBonus() {
            return fieldBonus;
        }

        double efficiency() {
            return efficiency;
        }

        double recoveryScale() {
            return recoveryScale;
        }

        int effectDuration(int baseTicks) {
            return Math.max(20, (int) Math.round(baseTicks * recoveryScale));
        }
    }

    private CultivationPractice() {
    }

    public static boolean perform(ServerLevel level, BlockPos pos, ServerPlayer player,
                                  Support support, boolean quietOutput) {
        if (!XuanhuanLoreIntegration.knowsCirculation(player)) {
            if (!quietOutput) {
                player.sendSystemMessage(Component.translatable("lore.earth_lore.fail.lore_locked")
                        .withStyle(ChatFormatting.RED));
            }
            return false;
        }
        CultivationFocus rawSelected = CultivationFocus.byId(
                ArcanaPower.data(player).getIntOr(ArcanaPower.CULTIVATION_FOCUS, 0));
        if (!XuanhuanLoreIntegration.knowsFocus(player, rawSelected)) {
            if (!quietOutput) {
                player.sendSystemMessage(Component.translatable("lore.earth_lore.fail.lore_locked")
                        .withStyle(ChatFormatting.RED));
            }
            return false;
        }
        if (ArcanaPower.getCultivationLevel(player) <= 0) {
            if (!quietOutput) {
                player.sendSystemMessage(Component.translatable(
                        "message.xuanhuan_earth.practice.requires_qi_guiding")
                        .withStyle(ChatFormatting.RED));
            }
            return false;
        }
        long cooldown = ArcanaPower.getQiMeditationCooldownTicks(player, level);
        if (cooldown > 0L) {
            if (!quietOutput && cooldown > 20L) {
                player.sendSystemMessage(Component.translatable("message.xuanhuan_earth.qi_manual.cooldown",
                        (cooldown + 19L) / 20L).withStyle(ChatFormatting.YELLOW));
            }
            return false;
        }

        CultivationFocus focus = ArcanaPower.getCultivationFocus(player);
        ArcanaChunkField.Reading reading = Spirituality.reading(level, pos);
        int focusedQi = Math.min(100, reading.value() + support.fieldBonus());
        double focusScale = switch (focus) {
            case CIRCULATION -> 1.0D;
            case BODY_TEMPERING -> 0.72D;
            case FETAL_BREATH -> 0.82D;
            case BIGU -> 0.64D;
        };
        double processedQi = Math.max(1.0D, focusedQi * focusScale * support.efficiency());
        SpellExecutor.CirculationCommit circulation = SpellExecutor.commitCirculation(
                level, player, reading, processedQi);
        if (!circulation.accepted()) {
            if (!quietOutput) {
                player.sendSystemMessage(Component.translatable(
                        "message.xuanhuan_earth.practice.plan_rejected",
                        circulation.errorCode(), circulation.message())
                        .withStyle(ChatFormatting.YELLOW));
            }
            return false;
        }
        double restored = circulation.manaRestored();
        int gainedXp = Math.max(2, (int) Math.round((3.0D + focusedQi * 0.12D) * support.efficiency()));
        if (support == Support.CUSHION && ArcanaChunkField.isAcupoint(level, pos)) {
            gainedXp += 1;
        }
        ArcanaPower.ProgressResult progress = ArcanaPower.addCultivationExperience(player, focus, gainedXp);
        ArcanaPower.startQiMeditationCooldown(player, level);
        CultivationNetwork.broadcastVisual(player, CultivationVisualAction.MEDITATION);

        double recoveryScale = support.recoveryScale();
        EarthHumanCompat.RecoveryReport report;
        switch (focus) {
            case BODY_TEMPERING -> {
                player.addEffect(new MobEffectInstance(MobEffects.REGENERATION,
                        support.effectDuration(140), 0, true, false, true));
                player.addEffect(new MobEffectInstance(MobEffects.RESISTANCE,
                        support.effectDuration(120), 0, true, false, true));
                report = EarthHumanCompat.recoverCore(player,
                        (5.5D + restored * 0.08D) * recoveryScale,
                        (0.55D + restored * 0.018D) * recoveryScale);
            }
            case FETAL_BREATH -> {
                player.addEffect(new MobEffectInstance(MobEffects.WATER_BREATHING,
                        support.effectDuration(140), 0, true, false, true));
                int airCap = player.getMaxAirSupply() + (int) Math.round(ArcanaPower.getBreathCapacityBonus(player));
                player.setAirSupply(Math.min(airCap,
                        player.getAirSupply() + (int) Math.round(120.0D * recoveryScale)));
                report = EarthHumanCompat.recoverBreath(player,
                        (4.0D + restored * 0.06D) * recoveryScale,
                        (0.32D + restored * 0.012D) * recoveryScale);
            }
            case BIGU -> {
                player.addEffect(new MobEffectInstance(MobEffects.REGENERATION,
                        support.effectDuration(80), 0, true, false, true));
                var food = player.getFoodData();
                food.setFoodLevel(Math.min(20, food.getFoodLevel() + 1));
                food.setSaturation(Math.min(6.0F,
                        food.getSaturationLevel() + (float) (0.5D * recoveryScale)));
                report = EarthHumanCompat.recoverCore(player,
                        (4.5D + restored * 0.05D) * recoveryScale,
                        (0.20D + restored * 0.008D) * recoveryScale);
            }
            default -> {
                player.addEffect(new MobEffectInstance(MobEffects.REGENERATION,
                        support.effectDuration(100), 0, true, false, true));
                report = EarthHumanCompat.recoverCore(player,
                        (3.5D + restored * 0.06D) * recoveryScale,
                        (0.30D + restored * 0.012D) * recoveryScale);
            }
        }

        if (!quietOutput) {
            player.sendSystemMessage(Component.translatable(
                    "message.xuanhuan_earth.practice.used." + support.translationSuffix)
                    .withStyle(ChatFormatting.GREEN));
            player.sendSystemMessage(Component.translatable("message.xuanhuan_earth.practice.result",
                    Spirituality.gradeName(reading.value()),
                    reading.value(),
                    ArcanaPower.format(restored),
                    progress.gainedXp(),
                    progress.level(),
                    progress.atCap() ? "MAX" : progress.xp() + "/" + progress.xpNeeded(),
                    ArcanaPower.format(report.fatigueReduced()),
                    ArcanaPower.format(report.bodyHealed()),
                    Math.round(support.efficiency() * 100.0D)).withStyle(ChatFormatting.AQUA));
        }
        if (progress.leveledUp()) {
            player.sendSystemMessage(Component.translatable(
                    "message.xuanhuan_earth.practice.level_up",
                    Component.translatable(focus.titleKey()), progress.level())
                    .withStyle(ChatFormatting.GOLD));
        }
        XuanhuanJourney.complete(player, XuanhuanJourney.Milestone.PRACTICE);
        emitPractice(level, pos, focus, support);
        return true;
    }

    public static void emitFocusChange(ServerLevel level, BlockPos pos, CultivationFocus focus, boolean supported) {
        Vec3 center = Vec3.atCenterOf(pos);
        XuanhuanEffects.groundRing(level, center, supported ? 1.1F : 0.8F);
        XuanhuanEffects.runeCluster(level, center, supported ? 3 : 2, 0.75D,
                focus == CultivationFocus.CIRCULATION || focus == CultivationFocus.FETAL_BREATH);
        level.sendParticles(switch (focus) {
                    case CIRCULATION -> XuanhuanParticles.QI_SPARK.get();
                    case BODY_TEMPERING -> XuanhuanParticles.EMBER_SPARK.get();
                    case FETAL_BREATH -> XuanhuanParticles.SPIRIT_SMOKE.get();
                    case BIGU -> XuanhuanParticles.SPIRIT_LEAF.get();
                },
                center.x, center.y + (supported ? 0.55D : 1.05D), center.z,
                supported ? 10 : 6, 0.32D, supported ? 0.24D : 0.4D, 0.32D, 0.02D);
    }

    private static void emitPractice(ServerLevel level, BlockPos pos, CultivationFocus focus, Support support) {
        boolean supported = support == Support.CUSHION;
        Vec3 center = Vec3.atCenterOf(pos);
        XuanhuanEffects.meditationCycle(level, pos, focus, supported);
        switch (focus) {
            case BODY_TEMPERING -> level.sendParticles(XuanhuanParticles.SPIRIT_FLAME.get(),
                    center.x, center.y + 0.8D, center.z, supported ? 10 : 6,
                    0.3D, 0.25D, 0.3D, 0.015D);
            case FETAL_BREATH -> level.sendParticles(XuanhuanParticles.SPIRIT_SMOKE.get(),
                    center.x, center.y + 1.1D, center.z, supported ? 8 : 5,
                    0.35D, 0.2D, 0.35D, 0.01D);
            case BIGU -> XuanhuanEffects.leafFall(level, center.add(0.0D, 1.2D, 0.0D),
                    supported ? 8 : 5, false);
            default -> {
            }
        }
    }
}
