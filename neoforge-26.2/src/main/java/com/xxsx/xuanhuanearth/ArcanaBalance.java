package com.xxsx.xuanhuanearth;

import java.util.UUID;

final class ArcanaBalance {
    private ArcanaBalance() {
    }

    static double exhaustionReduction(double enduranceBonus, double fastingBonus) {
        return clamp(nonNegative(enduranceBonus) * 0.24D + nonNegative(fastingBonus) * 0.18D, 0.0D, 0.82D);
    }

    static double combatDamageReduction(double bodyTemperingBonus) {
        return clamp(nonNegative(bodyTemperingBonus) * 0.11D, 0.0D, 0.30D);
    }

    static double breathMultiplier(double breathCapacityBonus) {
        return 1.0D + Math.min(3.0D, nonNegative(breathCapacityBonus) / 300.0D);
    }

    static double fallDamageReduction(double enduranceBonus) {
        return Math.min(0.45D, nonNegative(enduranceBonus) * 0.14D);
    }

    static int xpNeededForLevel(int level, int maxLevel) {
        if (level <= 0 || level >= maxLevel) {
            return 0;
        }
        return 60 + level * 35 + level * level * 8;
    }

    static double clamp(double value, double min, double max) {
        if (Double.isNaN(value)) {
            return min;
        }
        return Math.max(min, Math.min(max, value));
    }

    private static double nonNegative(double value) {
        return Double.isFinite(value) && value > 0.0D ? value : 0.0D;
    }

    static int furnaceTemperature(int fieldVal, double fireFactor) {
        int clamped = Math.max(0, Math.min(100, fieldVal));
        int raw = (int) Math.floor(300.0D + 9.0D * clamped * fireFactor);
        return Math.max(300, Math.min(1200, raw));
    }

    static int luopanRating(int fieldValue, int nearestWaterDist, int northRise, boolean openSky) {
        int clampedField = Math.max(0, Math.min(100, fieldValue));
        double fieldPart = 0.55D * clampedField;
        int waterPart = nearestWaterDist <= 16 ? 15 : (nearestWaterDist <= 32 ? 8 : 0);
        int mountainPart = northRise >= 8 ? 15 : (northRise >= 4 ? 8 : 0);
        int openPart = openSky ? 15 : 0;
        double score = Math.max(0.0D, Math.min(100.0D, fieldPart + waterPart + mountainPart + openPart));
        return Math.max(1, Math.min(9, 1 + (int) Math.floor(score / 12.5D)));
    }

    static int divinationHexagram(long worldSeed, UUID playerUuid, long day) {
        long uuidHash = playerUuid.getMostSignificantBits() ^ playerUuid.getLeastSignificantBits();
        long value = worldSeed ^ uuidHash ^ day;
        value ^= value >>> 33;
        value *= 0xff51afd7ed558ccdL;
        value ^= value >>> 33;
        value *= 0xc4ceb9fe1a85ec53L;
        value ^= value >>> 33;
        return Math.floorMod(value, 64);
    }
}
