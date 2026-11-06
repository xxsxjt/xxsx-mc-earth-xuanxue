package com.xxsx.xuanhuanearth;

import net.minecraft.world.effect.MobEffects;

/**
 * The pill catalogue.
 *
 * <p>Every pill is one row here: what it restores, which route it advances, and what it
 * costs in materials at the furnace. Keeping them together makes the alchemy ladder readable
 * as a whole instead of being scattered across item classes and recipe tables.</p>
 */
public final class XuanhuanPills {
    /** Fast, cheap field recovery: the everyday pill. */
    public static final XuanhuanPillItem.Pill QI_RECOVERY = new XuanhuanPillItem.Pill(
            "qi_recovery_pill", 35.0D, CultivationFocus.CIRCULATION, 6, 4.0D, 0.16D,
            new net.minecraft.world.effect.MobEffectInstance[0]);

    /** Slower but stronger; also repairs the body. */
    public static final XuanhuanPillItem.Pill FOUNDATION = new XuanhuanPillItem.Pill(
            "foundation_pill", 70.0D, CultivationFocus.CIRCULATION, 18, 10.0D, 0.40D,
            new net.minecraft.world.effect.MobEffectInstance[]{
                    XuanhuanPillItem.Pill.regeneration(20, 0)
            });

    /** Body tempering in a pill: damage resistance and absorption. */
    public static final XuanhuanPillItem.Pill IRONBODY = new XuanhuanPillItem.Pill(
            "ironbody_pill", 25.0D, CultivationFocus.BODY_TEMPERING, 22, 12.0D, 0.55D,
            new net.minecraft.world.effect.MobEffectInstance[]{
                    XuanhuanPillItem.Pill.effect(MobEffects.RESISTANCE, 90, 0),
                    XuanhuanPillItem.Pill.effect(MobEffects.ABSORPTION, 90, 0)
            });

    /** Breath route: water breathing plus a large air refill. */
    public static final XuanhuanPillItem.Pill CLOUDBREATH = new XuanhuanPillItem.Pill(
            "cloudbreath_pill", 25.0D, CultivationFocus.FETAL_BREATH, 22, 8.0D, 0.20D,
            new net.minecraft.world.effect.MobEffectInstance[]{
                    XuanhuanPillItem.Pill.effect(MobEffects.WATER_BREATHING, 180, 0)
            });

    /** Fasting route: suppresses hunger and heals slowly. */
    public static final XuanhuanPillItem.Pill GRAINFAST = new XuanhuanPillItem.Pill(
            "grainfast_pill", 20.0D, CultivationFocus.BIGU, 22, 8.0D, 0.25D,
            new net.minecraft.world.effect.MobEffectInstance[]{
                    XuanhuanPillItem.Pill.effect(MobEffects.SATURATION, 10, 0)
            });

    /** Rare breakthrough pill: big mana, big experience, short regeneration. */
    public static final XuanhuanPillItem.Pill BREAKTHROUGH = new XuanhuanPillItem.Pill(
            "breakthrough_pill", 120.0D, CultivationFocus.CIRCULATION, 45, 20.0D, 0.80D,
            new net.minecraft.world.effect.MobEffectInstance[]{
                    XuanhuanPillItem.Pill.regeneration(30, 1),
                    XuanhuanPillItem.Pill.effect(MobEffects.ABSORPTION, 120, 1)
            });

    /** Night vision and speed: a scout's pill. */
    public static final XuanhuanPillItem.Pill NIGHTSIGHT = new XuanhuanPillItem.Pill(
            "nightsight_pill", 30.0D, null, 0, 5.0D, 0.10D,
            new net.minecraft.world.effect.MobEffectInstance[]{
                    XuanhuanPillItem.Pill.effect(MobEffects.NIGHT_VISION, 240, 0),
                    XuanhuanPillItem.Pill.effect(MobEffects.SPEED, 120, 0)
            });

    /** X-031 Marrow pill: body tempering xp +120, requires body_tempering_level >= 1. */
    public static final XuanhuanPillItem.Pill MARROW = new XuanhuanPillItem.Pill(
            "marrow_pill", 0.0D, CultivationFocus.BODY_TEMPERING, 120, 6.0D, 0.30D,
            new net.minecraft.world.effect.MobEffectInstance[]{
                    XuanhuanPillItem.Pill.effect(MobEffects.RESISTANCE, 60, 0)
            });

    /** X-032 Breath pill: fetal breath xp +120, requires fetal_breath_level >= 1. */
    public static final XuanhuanPillItem.Pill BREATH = new XuanhuanPillItem.Pill(
            "breath_pill", 0.0D, CultivationFocus.FETAL_BREATH, 120, 5.0D, 0.20D,
            new net.minecraft.world.effect.MobEffectInstance[]{
                    XuanhuanPillItem.Pill.effect(MobEffects.WATER_BREATHING, 90, 0)
            });

    /** X-033 Fast pill: hunger maintained 1200 ticks, requires bigu_level >= 1. */
    public static final XuanhuanPillItem.Pill FAST = new XuanhuanPillItem.Pill(
            "fast_pill", 0.0D, CultivationFocus.BIGU, 0, 4.0D, 0.10D,
            new net.minecraft.world.effect.MobEffectInstance[]{
                    XuanhuanPillItem.Pill.effect(MobEffects.SATURATION, 60, 0)
            });

    /** X-035 Detox pill: clears poison, weakness, nausea; fails if none active. */
    public static final XuanhuanPillItem.Pill DETOX = new XuanhuanPillItem.Pill(
            "detox_pill", 0.0D, null, 0, 2.0D, 0.10D,
            new net.minecraft.world.effect.MobEffectInstance[0]);

    /** X-040 Failed pill: stack 64, inflicts 100 ticks poison. */
    public static final XuanhuanPillItem.Pill FAILED = new XuanhuanPillItem.Pill(
            "failed_pill", 0.0D, null, 0, 0.0D, 0.0D,
            new net.minecraft.world.effect.MobEffectInstance[]{
                    XuanhuanPillItem.Pill.effect(MobEffects.POISON, 5, 0)
            });

    /** X-041 Nourish Qi pill: restores 30 MP + 120t gentle regeneration. */
    public static final XuanhuanPillItem.Pill NOURISH_QI = new XuanhuanPillItem.Pill(
            "nourish_qi_pill", 30.0D, null, 0, 3.0D, 0.12D,
            new net.minecraft.world.effect.MobEffectInstance[]{
                    XuanhuanPillItem.Pill.regeneration(6, 0)
            });

    /** X-041 Calm Spirit pill: clears nausea/weakness and grants 600t immunity. */
    public static final XuanhuanPillItem.Pill CALM_SPIRIT = new XuanhuanPillItem.Pill(
            "calm_spirit_pill", 10.0D, null, 0, 4.0D, 0.10D,
            new net.minecraft.world.effect.MobEffectInstance[0]);

    /** X-041 Clear Heat pill: reduces toxic debuff tier by 1. */
    public static final XuanhuanPillItem.Pill CLEAR_HEAT = new XuanhuanPillItem.Pill(
            "clear_heat_pill", 5.0D, null, 0, 2.0D, 0.10D,
            new net.minecraft.world.effect.MobEffectInstance[]{
                    XuanhuanPillItem.Pill.effect(MobEffects.FIRE_RESISTANCE, 15, 0)
            });

    /** X-041 Invigorate Blood pill: regeneration I for 100 ticks. */
    public static final XuanhuanPillItem.Pill INVIGORATE_BLOOD = new XuanhuanPillItem.Pill(
            "invigorate_blood_pill", 5.0D, null, 0, 3.0D, 0.25D,
            new net.minecraft.world.effect.MobEffectInstance[]{
                    XuanhuanPillItem.Pill.regeneration(5, 0)
            });

    /** X-041 Resolve Toxin pill: clears poison and grants 300t poison immunity. */
    public static final XuanhuanPillItem.Pill RESOLVE_TOXIN = new XuanhuanPillItem.Pill(
            "resolve_toxin_pill", 5.0D, null, 0, 2.0D, 0.15D,
            new net.minecraft.world.effect.MobEffectInstance[0]);

    public static final XuanhuanPillItem.Pill[] ALL = {
            QI_RECOVERY, FOUNDATION, IRONBODY, CLOUDBREATH, GRAINFAST, BREAKTHROUGH, NIGHTSIGHT,
            MARROW, BREATH, FAST, DETOX, FAILED,
            NOURISH_QI, CALM_SPIRIT, CLEAR_HEAT, INVIGORATE_BLOOD, RESOLVE_TOXIN
    };

    private XuanhuanPills() {
    }
}
