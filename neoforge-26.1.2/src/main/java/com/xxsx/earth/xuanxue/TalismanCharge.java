package com.xxsx.earth.xuanxue;

/**
 * Stored field energy sealed into a talisman when it is drawn.
 *
 * <p>This is separate from the caster's mana: the paper carries the bulk charge, and
 * mana only pays the excitation cost at the moment of use. These constants are the
 * single source of truth for both the crafting machine and the trigger.</p>
 */
public final class TalismanCharge {
    public static final int SCHEMA_VERSION = 1;
    public static final double CRAFTED_CHARGE_AEU = 24.0D;
    public static final double CAPACITY_AEU = 24.0D;
    private static final double EPSILON = 0.000001D;

    private TalismanCharge() {
    }

    public static double clamp(double value) {
        if (!Double.isFinite(value)) {
            return 0.0D;
        }
        return Math.max(0.0D, Math.min(CAPACITY_AEU, value));
    }

    public static boolean isCharged(double value) {
        return clamp(value) > EPSILON;
    }
}
