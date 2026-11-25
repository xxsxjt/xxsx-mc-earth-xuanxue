package com.xxsx.earth.xuanxue;

/**
 * Single accounting path from qi field level to stored or spent arcane energy.
 *
 * <p>The chunk field tracks a dimensionless 0-100 level plus depletion, not joules.
 * This class performs the one conversion both meditation and spirit stones must use,
 * so a field draw never enters the item ledger as if it were already stored charge.</p>
 */
public final class SpiritFieldLedger {
    public static final double FIELD_MAX = 100.0D;
    /** Game axiom: one usable field level of qi carries 10 AEU of extractable work. */
    public static final double AEU_PER_FIELD_UNIT = 10.0D;
    private static final double EPSILON = 0.000001D;

    private SpiritFieldLedger() {
    }

    public static double availableField(double level, double depletion) {
        double readable = Double.isFinite(level) ? Math.max(0.0D, Math.min(FIELD_MAX, level)) : 0.0D;
        double saturated = Double.isFinite(depletion) ? Math.max(0.0D, Math.min(45.0D, depletion)) : 0.0D;
        return Math.max(0.0D, readable - saturated);
    }

    /** Converts a usable field level into the AEU it can deliver in one action. */
    public static double extractableAeu(double usableField, double coupling) {
        if (!Double.isFinite(usableField) || usableField <= 0.0D
                || !Double.isFinite(coupling) || coupling <= 0.0D) {
            return 0.0D;
        }
        return Math.min(FIELD_MAX, usableField) * AEU_PER_FIELD_UNIT * Math.min(1.0D, coupling);
    }

    /** Field level consumed by drawing the given AEU out of the local field. */
    public static double fieldCostOfAeu(double aeu) {
        if (!Double.isFinite(aeu) || aeu <= 0.0D) {
            return 0.0D;
        }
        return aeu / AEU_PER_FIELD_UNIT;
    }

    /**
     * Mana returned by one meditation cycle after the shared release efficiency.
     * Passive absorption and passive generation are already folded into the caller's
     * absorption fraction, so this is the only conversion applied.
     */
    public static double meditationMana(double processedField, double absorption) {
        double aeu = absorbedAeu(processedField, absorption);
        return aeu * SpiritStoneCharge.RELEASE_EFFICIENCY / SpiritStoneCharge.AEU_PER_MANA;
    }

    /** AEU actually drawn out of the field by exposing it with the given coupling. */
    public static double absorbedAeu(double processedField, double absorption) {
        return extractableAeu(processedField, absorption);
    }

    /**
     * Field levels debited for a meditation cycle. Only the absorbed energy leaves the
     * chunk, so the debit is the absorbed AEU converted back to levels; exposing more qi
     * than the practitioner can couple to must not deplete the chunk at full exposure.
     */
    public static double meditationFieldCost(double processedField, double absorption) {
        return fieldCostOfAeu(absorbedAeu(processedField, absorption));
    }

    public static boolean isNegligible(double value) {
        return !Double.isFinite(value) || value <= EPSILON;
    }
}
