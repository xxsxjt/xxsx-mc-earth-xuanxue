package com.xxsx.xuanhuanearth;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.Locale;

public final class ArcanaPower {
    private static final String ARCANA_PREFIX = "earth_arcana.";
    private static final String LEGACY_ARCANA_PREFIX = "earth_online_arcana.";
    public static final String CURRENT_MANA = "earth_arcana.current_mana";
    public static final String BASE_MANA = "earth_arcana.base_mana";
    public static final String XUANHUAN_MANA_BONUS = "earth_arcana.xuanhuan_mana_bonus";
    public static final String MAGIC_MANA_BONUS = "earth_arcana.magic_mana_bonus";
    public static final String EQUIPMENT_MANA_BONUS = "earth_arcana.equipment_mana_bonus";
    public static final String TEMPORARY_MANA_BONUS = "earth_arcana.temporary_mana_bonus";
    public static final String QI_ABSORPTION_RATE = "earth_arcana.qi_absorption_rate";
    public static final String MAGIC_ATTUNEMENT_RATE = "earth_arcana.magic_attunement_rate";
    public static final String CULTIVATION_LEVEL = "earth_arcana.cultivation_level";
    public static final String REALM = "earth_arcana.realm";
    public static final String REALM_MANA_BONUS = "earth_arcana.realm_mana_bonus";
    public static final String MAGIC_RESEARCH_LEVEL = "earth_arcana.magic_research_level";
    public static final String FASTING_FOOD_BONUS = "earth_arcana.fasting_food_bonus";
    public static final String BREATH_CAPACITY_BONUS = "earth_arcana.breath_capacity_bonus";
    public static final String ENDURANCE_BONUS = "earth_arcana.endurance_bonus";
    public static final String BODY_TEMPERING_BONUS = "earth_arcana.body_tempering_bonus";
    public static final String BIGU_LEVEL = "earth_arcana.bigu_level";
    public static final String BODY_TEMPERING_LEVEL = "earth_arcana.body_tempering_level";
    public static final String FETAL_BREATH_LEVEL = "earth_arcana.fetal_breath_level";
    public static final String CULTIVATION_XP = "earth_arcana.cultivation_xp";
    public static final String BIGU_XP = "earth_arcana.bigu_xp";
    public static final String BODY_TEMPERING_XP = "earth_arcana.body_tempering_xp";
    public static final String FETAL_BREATH_XP = "earth_arcana.fetal_breath_xp";
    public static final String QI_MEDITATION_COOLDOWN_UNTIL = "earth_arcana.qi_meditation_cooldown_until";
    public static final String XUANHUAN_SKILL_COOLDOWN_UNTIL = "earth_arcana.xuanhuan_skill_cooldown_until";
    public static final String CULTIVATION_FOCUS = "earth_arcana.cultivation_focus";
    public static final String DIVINATION_DAY = "earth_arcana.divination_day";

    private static final double DEFAULT_BASE_MANA = 20.0D;
    private static final double MAX_REASONABLE_MANA = 1_000_000.0D;
    public static final int QI_MEDITATION_COOLDOWN_TICKS = 20 * 18;
    public static final int XUANHUAN_SKILL_COOLDOWN_TICKS = 20 * 12;
    public static final int MAX_CULTIVATION_LEVEL = 10;

    private ArcanaPower() {
    }

    public static double getBaseMana(Player player) {
        return Math.max(0.0D, data(player).getDoubleOr(BASE_MANA, DEFAULT_BASE_MANA));
    }

    public static double getXuanhuanBonus(Player player) {
        return Math.max(0.0D, data(player).getDoubleOr(XUANHUAN_MANA_BONUS, 0.0D));
    }

    public static double getMagicBonus(Player player) {
        return Math.max(0.0D, data(player).getDoubleOr(MAGIC_MANA_BONUS, 0.0D));
    }

    public static double getMaxMana(Player player) {
        CompoundTag tag = data(player);
        double max = getBaseMana(player)
                + getXuanhuanBonus(player)
                + getMagicBonus(player)
                + Math.max(0.0D, tag.getDoubleOr(EQUIPMENT_MANA_BONUS, 0.0D))
                + Math.max(0.0D, tag.getDoubleOr(TEMPORARY_MANA_BONUS, 0.0D));
        return clamp(max, 0.0D, MAX_REASONABLE_MANA);
    }

    public static double getCurrentMana(Player player) {
        double max = getMaxMana(player);
        return clamp(data(player).getDoubleOr(CURRENT_MANA, max), 0.0D, max);
    }

    public static void setCurrentMana(Player player, double value) {
        data(player).putDouble(CURRENT_MANA, clamp(value, 0.0D, getMaxMana(player)));
    }

    public static boolean trySpendMana(Player player, double amount) {
        double cost = Math.max(0.0D, amount);
        double current = getCurrentMana(player);
        if (current + 0.0001D < cost) {
            return false;
        }
        setCurrentMana(player, current - cost);
        return true;
    }

    public static boolean canSpendMana(Player player, double amount) {
        double cost = Math.max(0.0D, amount);
        return getCurrentMana(player) + 0.0001D >= cost;
    }

    public static int getCultivationLevel(Player player) {
        return Math.max(0, data(player).getIntOr(CULTIVATION_LEVEL, 0));
    }

    public static int getRealm(Player player) {
        return Math.max(0, Math.min(RealmBreakthrough.MAX_REALM,
                data(player).getIntOr(REALM, 0)));
    }

    public static void setRealm(Player player, int realm) {
        data(player).putInt(REALM, Math.max(0, Math.min(RealmBreakthrough.MAX_REALM, realm)));
    }

    /** Adds to the permanent realm contribution without touching the magic or equipment pools. */
    public static void addXuanhuanManaBonus(Player player, double amount) {
        if (!Double.isFinite(amount) || amount <= 0.0D) {
            return;
        }
        CompoundTag tag = data(player);
        tag.putDouble(XUANHUAN_MANA_BONUS,
                Math.max(0.0D, tag.getDoubleOr(XUANHUAN_MANA_BONUS, 0.0D)) + amount);
    }

    public static double getQiAbsorptionRate(Player player) {
        return Math.max(0.0D, data(player).getDoubleOr(QI_ABSORPTION_RATE, 0.0D));
    }

    public static int getBiguLevel(Player player) {
        return Math.max(0, data(player).getIntOr(BIGU_LEVEL, 0));
    }

    public static int getBodyTemperingLevel(Player player) {
        return Math.max(0, data(player).getIntOr(BODY_TEMPERING_LEVEL, 0));
    }

    public static int getFetalBreathLevel(Player player) {
        return Math.max(0, data(player).getIntOr(FETAL_BREATH_LEVEL, 0));
    }

    public static int getFocusLevel(Player player, CultivationFocus focus) {
        return switch (focus) {
            case CIRCULATION -> getCultivationLevel(player);
            case BODY_TEMPERING -> getBodyTemperingLevel(player);
            case FETAL_BREATH -> getFetalBreathLevel(player);
            case BIGU -> getBiguLevel(player);
        };
    }

    public static int getFocusXp(Player player, CultivationFocus focus) {
        return Math.max(0, data(player).getIntOr(xpKey(focus), 0));
    }

    public static int getFocusXpNeeded(Player player, CultivationFocus focus) {
        return xpNeededForLevel(getFocusLevel(player, focus));
    }

    public static ProgressResult addCultivationExperience(Player player, CultivationFocus focus, int amount) {
        if (player instanceof net.minecraft.server.level.ServerPlayer serverPlayer
                && !XuanhuanLoreIntegration.knowsFocus(serverPlayer, focus)) {
            int cur = getFocusLevel(player, focus);
            return new ProgressResult(cur, cur, getFocusXp(player, focus),
                    getFocusXpNeeded(player, focus), 0, false, cur >= MAX_CULTIVATION_LEVEL);
        }
        int previousLevel = getFocusLevel(player, focus);
        if (previousLevel <= 0 || amount <= 0 || previousLevel >= MAX_CULTIVATION_LEVEL) {
            return new ProgressResult(previousLevel, previousLevel, getFocusXp(player, focus),
                    getFocusXpNeeded(player, focus), 0, false, previousLevel >= MAX_CULTIVATION_LEVEL);
        }

        CompoundTag tag = data(player);
        int level = previousLevel;
        int xp = getFocusXp(player, focus) + amount;
        double manaBefore = getCurrentMana(player);
        while (level < MAX_CULTIVATION_LEVEL) {
            int needed = xpNeededForLevel(level);
            if (xp < needed) {
                break;
            }
            xp -= needed;
            level++;
            applyLevelReward(tag, focus);
        }
        if (level >= MAX_CULTIVATION_LEVEL) {
            xp = 0;
        }
        tag.putInt(levelKey(focus), level);
        tag.putInt(xpKey(focus), xp);
        if (level > previousLevel) {
            setCurrentMana(player, manaBefore + 4.0D * (level - previousLevel));
        }
        return new ProgressResult(previousLevel, level, xp, xpNeededForLevel(level), amount,
                level > previousLevel, level >= MAX_CULTIVATION_LEVEL);
    }

    public static double getFastingFoodBonus(Player player) {
        return Math.max(0.0D, data(player).getDoubleOr(FASTING_FOOD_BONUS, 0.0D));
    }

    public static double getBreathCapacityBonus(Player player) {
        return Math.max(0.0D, data(player).getDoubleOr(BREATH_CAPACITY_BONUS, 0.0D));
    }

    public static double getEnduranceBonus(Player player) {
        return Math.max(0.0D, data(player).getDoubleOr(ENDURANCE_BONUS, 0.0D));
    }

    public static double getBodyTemperingBonus(Player player) {
        return Math.max(0.0D, data(player).getDoubleOr(BODY_TEMPERING_BONUS, 0.0D));
    }

    public static double getExhaustionReduction(Player player) {
        return ArcanaBalance.exhaustionReduction(getEnduranceBonus(player), getFastingFoodBonus(player));
    }

    public static double getCombatDamageReduction(Player player) {
        return ArcanaBalance.combatDamageReduction(getBodyTemperingBonus(player));
    }

    public static double getBreathMultiplier(Player player) {
        return ArcanaBalance.breathMultiplier(getBreathCapacityBonus(player));
    }

    public static CultivationFocus getCultivationFocus(Player player) {
        CultivationFocus focus = CultivationFocus.byId(data(player).getIntOr(CULTIVATION_FOCUS, 0));
        return focus.isUnlocked(player) ? focus : CultivationFocus.CIRCULATION;
    }

    public static boolean setCultivationFocus(Player player, CultivationFocus focus) {
        if (!focus.isUnlocked(player) || getCultivationFocus(player) == focus) {
            return false;
        }
        data(player).putInt(CULTIVATION_FOCUS, focus.id());
        return true;
    }

    public static int getCultivationFocusMask(Player player) {
        int mask = 0;
        for (CultivationFocus focus : CultivationFocus.values()) {
            if (focus.isUnlocked(player)) {
                mask |= 1 << focus.id();
            }
        }
        return mask;
    }

    public static boolean learnQiGuiding(Player player) {
        CompoundTag tag = data(player);
        if (getCultivationLevel(player) > 0) {
            return false;
        }
        tag.putInt(CULTIVATION_LEVEL, 1);
        tag.putInt(CULTIVATION_XP, 0);
        add(tag, XUANHUAN_MANA_BONUS, 30.0D);
        add(tag, QI_ABSORPTION_RATE, 0.08D);
        add(tag, ENDURANCE_BONUS, 0.04D);
        setCurrentMana(player, getMaxMana(player));
        return true;
    }

    public static boolean learnBigu(Player player) {
        CompoundTag tag = data(player);
        if (getCultivationLevel(player) <= 0 || getBiguLevel(player) > 0) {
            return false;
        }
        tag.putInt(BIGU_LEVEL, 1);
        tag.putInt(BIGU_XP, 0);
        add(tag, FASTING_FOOD_BONUS, 0.60D);
        add(tag, ENDURANCE_BONUS, 0.10D);
        add(tag, XUANHUAN_MANA_BONUS, 10.0D);
        setCurrentMana(player, getMaxMana(player));
        return true;
    }

    public static boolean learnBodyTempering(Player player) {
        CompoundTag tag = data(player);
        if (getCultivationLevel(player) <= 0 || getBodyTemperingLevel(player) > 0) {
            return false;
        }
        tag.putInt(BODY_TEMPERING_LEVEL, 1);
        tag.putInt(BODY_TEMPERING_XP, 0);
        add(tag, BODY_TEMPERING_BONUS, 0.45D);
        add(tag, ENDURANCE_BONUS, 0.22D);
        add(tag, BREATH_CAPACITY_BONUS, 60.0D);
        add(tag, XUANHUAN_MANA_BONUS, 15.0D);
        setCurrentMana(player, getMaxMana(player));
        return true;
    }

    public static boolean learnFetalBreath(Player player) {
        CompoundTag tag = data(player);
        if (getCultivationLevel(player) <= 0 || getFetalBreathLevel(player) > 0) {
            return false;
        }
        tag.putInt(FETAL_BREATH_LEVEL, 1);
        tag.putInt(FETAL_BREATH_XP, 0);
        add(tag, BREATH_CAPACITY_BONUS, 180.0D);
        add(tag, ENDURANCE_BONUS, 0.08D);
        add(tag, XUANHUAN_MANA_BONUS, 12.0D);
        setCurrentMana(player, getMaxMana(player));
        return true;
    }

    public static boolean learnBodyTemperingFromLore(Player player) {
        CompoundTag tag = data(player);
        if (getBodyTemperingLevel(player) > 0) {
            return false;
        }
        tag.putInt(BODY_TEMPERING_LEVEL, 1);
        tag.putInt(BODY_TEMPERING_XP, 0);
        add(tag, BODY_TEMPERING_BONUS, 0.45D);
        add(tag, ENDURANCE_BONUS, 0.22D);
        add(tag, BREATH_CAPACITY_BONUS, 60.0D);
        add(tag, XUANHUAN_MANA_BONUS, 15.0D);
        setCurrentMana(player, getMaxMana(player));
        return true;
    }

    public static boolean learnFetalBreathFromLore(Player player) {
        CompoundTag tag = data(player);
        if (getFetalBreathLevel(player) > 0) {
            return false;
        }
        tag.putInt(FETAL_BREATH_LEVEL, 1);
        tag.putInt(FETAL_BREATH_XP, 0);
        add(tag, BREATH_CAPACITY_BONUS, 180.0D);
        add(tag, ENDURANCE_BONUS, 0.08D);
        add(tag, XUANHUAN_MANA_BONUS, 12.0D);
        setCurrentMana(player, getMaxMana(player));
        return true;
    }

    public static boolean learnBiguFromLore(Player player) {
        CompoundTag tag = data(player);
        if (getBiguLevel(player) > 0) {
            return false;
        }
        tag.putInt(BIGU_LEVEL, 1);
        tag.putInt(BIGU_XP, 0);
        add(tag, FASTING_FOOD_BONUS, 0.60D);
        add(tag, ENDURANCE_BONUS, 0.10D);
        add(tag, XUANHUAN_MANA_BONUS, 10.0D);
        setCurrentMana(player, getMaxMana(player));
        return true;
    }

    public static long getDivinationDay(Player player) {
        return data(player).getLongOr(DIVINATION_DAY, -1L);
    }

    public static void setDivinationDay(Player player, long day) {
        data(player).putLong(DIVINATION_DAY, day);
    }

    public static long getQiMeditationCooldownTicks(Player player, Level level) {
        long until = data(player).getLongOr(QI_MEDITATION_COOLDOWN_UNTIL, 0L);
        return Math.max(0L, until - level.getGameTime());
    }

    public static void startQiMeditationCooldown(Player player, Level level) {
        data(player).putLong(QI_MEDITATION_COOLDOWN_UNTIL, level.getGameTime() + QI_MEDITATION_COOLDOWN_TICKS);
    }

    public static long getSkillCooldownTicks(Player player, Level level) {
        long until = data(player).getLongOr(XUANHUAN_SKILL_COOLDOWN_UNTIL, 0L);
        return Math.max(0L, until - level.getGameTime());
    }

    public static void startSkillCooldown(Player player, Level level) {
        data(player).putLong(XUANHUAN_SKILL_COOLDOWN_UNTIL,
                level.getGameTime() + XUANHUAN_SKILL_COOLDOWN_TICKS);
    }

    public static String format(double value) {
        if (Math.abs(value - Math.rint(value)) < 0.05D) {
            return Integer.toString((int) Math.rint(value));
        }
        return String.format(Locale.ROOT, "%.1f", value);
    }

    static CompoundTag data(Player player) {
        CompoundTag data = player.getPersistentData();
        migrateLegacyData(data);
        return data;
    }

    static void migrateLegacyData(CompoundTag data) {
        for (String legacyKey : new ArrayList<>(data.keySet())) {
            if (!legacyKey.startsWith(LEGACY_ARCANA_PREFIX)) {
                continue;
            }
            String currentKey = ARCANA_PREFIX + legacyKey.substring(LEGACY_ARCANA_PREFIX.length());
            var value = data.get(legacyKey);
            if (value != null && data.get(currentKey) == null) {
                data.put(currentKey, value.copy());
            }
            data.remove(legacyKey);
        }
    }

    private static void add(CompoundTag tag, String key, double amount) {
        tag.putDouble(key, Math.max(0.0D, tag.getDoubleOr(key, 0.0D) + amount));
    }

    private static int xpNeededForLevel(int level) {
        return ArcanaBalance.xpNeededForLevel(level, MAX_CULTIVATION_LEVEL);
    }

    private static String levelKey(CultivationFocus focus) {
        return switch (focus) {
            case CIRCULATION -> CULTIVATION_LEVEL;
            case BODY_TEMPERING -> BODY_TEMPERING_LEVEL;
            case FETAL_BREATH -> FETAL_BREATH_LEVEL;
            case BIGU -> BIGU_LEVEL;
        };
    }

    private static String xpKey(CultivationFocus focus) {
        return switch (focus) {
            case CIRCULATION -> CULTIVATION_XP;
            case BODY_TEMPERING -> BODY_TEMPERING_XP;
            case FETAL_BREATH -> FETAL_BREATH_XP;
            case BIGU -> BIGU_XP;
        };
    }

    private static void applyLevelReward(CompoundTag tag, CultivationFocus focus) {
        switch (focus) {
            case CIRCULATION -> {
                add(tag, XUANHUAN_MANA_BONUS, 6.0D);
                add(tag, QI_ABSORPTION_RATE, 0.012D);
                add(tag, ENDURANCE_BONUS, 0.01D);
            }
            case BODY_TEMPERING -> {
                add(tag, XUANHUAN_MANA_BONUS, 4.0D);
                add(tag, BODY_TEMPERING_BONUS, 0.09D);
                add(tag, ENDURANCE_BONUS, 0.04D);
            }
            case FETAL_BREATH -> {
                add(tag, XUANHUAN_MANA_BONUS, 4.0D);
                add(tag, BREATH_CAPACITY_BONUS, 30.0D);
                add(tag, ENDURANCE_BONUS, 0.02D);
            }
            case BIGU -> {
                add(tag, XUANHUAN_MANA_BONUS, 3.0D);
                add(tag, FASTING_FOOD_BONUS, 0.12D);
                add(tag, ENDURANCE_BONUS, 0.025D);
            }
        }
    }

    private static double clamp(double value, double min, double max) {
        return ArcanaBalance.clamp(value, min, max);
    }

    public record ProgressResult(int previousLevel, int level, int xp, int xpNeeded, int gainedXp,
                                 boolean leveledUp, boolean atCap) {
    }
}
