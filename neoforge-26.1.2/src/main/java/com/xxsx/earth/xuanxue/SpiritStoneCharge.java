package com.xxsx.earth.xuanxue;

/**
 * Server-authoritative energy accounting for a spirit stone.
 *
 * <p>The stored charge is an arcane field state, never a chemical composition of the
 * quartz-feldspar-muscovite matrix. Every argument and result of this class is AEU, the
 * game's arcane energy unit, so the block field ledger and the item component always
 * describe the same transaction. Callers convert field levels with
 * {@link SpiritFieldLedger#extractableAeu} first.</p>
 */
public final class SpiritStoneCharge {
    public static final int SCHEMA_VERSION = 1;
    public static final double CAPACITY_AEU = 120.0D;
    public static final double CAPTURE_EFFICIENCY = 0.80D;
    public static final double RELEASE_EFFICIENCY = 0.90D;
    public static final double AEU_PER_MANA = 10.0D;
    private static final double EPSILON = 0.000001D;

    private SpiritStoneCharge() {
    }

    public static double clampCharge(double value) {
        if (!Double.isFinite(value)) {
            return 0.0D;
        }
        return Math.max(0.0D, Math.min(CAPACITY_AEU, value));
    }

    public static double remainingCapacity(double charge) {
        return Math.max(0.0D, CAPACITY_AEU - clampCharge(charge));
    }

    /**
     * Draws up to {@code aeuBudget} AEU out of the local field into the stone.
     *
     * <p>{@code fieldCoupling} is local field availability in 0-1, derived by the caller
     * from {@link SpiritFieldLedger#availableField}. Coupling is quadratic, so a thin
     * chunk delivers a small fraction of the batch. The stone only ever takes the AEU it
     * can still store divided by capture efficiency, so the field debit is always exactly
     * the number the stone records as input.</p>
     */
    public static ChargeResult capture(double currentCharge, double fieldCoupling, double aeuBudget) {
        double stored = clampCharge(currentCharge);
        if (!Double.isFinite(fieldCoupling) || fieldCoupling <= 0.0D
                || !Double.isFinite(aeuBudget) || aeuBudget <= 0.0D) {
            return new ChargeResult(stored, 0.0D, 0.0D, 0.0D);
        }
        double coupling = Math.min(1.0D, fieldCoupling);
        double requested = aeuBudget * coupling * coupling;
        double capturableAeu = remainingCapacity(stored) / CAPTURE_EFFICIENCY;
        double aeuSpent = Math.min(requested, capturableAeu);
        if (aeuSpent <= EPSILON) {
            return new ChargeResult(stored, 0.0D, 0.0D, 0.0D);
        }
        double gain = aeuSpent * CAPTURE_EFFICIENCY;
        double after = clampCharge(stored + gain);
        gain = after - stored;
        double loss = aeuSpent - gain;
        return new ChargeResult(after, aeuSpent, gain, loss);
    }

    public static ReleaseResult release(double currentCharge, double requestedMana) {
        if (!Double.isFinite(requestedMana) || requestedMana <= 0.0D) {
            return new ReleaseResult(clampCharge(currentCharge), 0.0D, 0.0D, 0.0D);
        }
        double stored = clampCharge(currentCharge);
        double neededAeu = requestedMana * AEU_PER_MANA / RELEASE_EFFICIENCY;
        double spent = Math.min(stored, neededAeu);
        double mana = spent * RELEASE_EFFICIENCY / AEU_PER_MANA;
        double after = clampCharge(stored - spent);
        double loss = Math.max(0.0D, neededAeu - spent);
        return new ReleaseResult(after, spent, mana, loss);
    }

    public static String grade(double charge) {
        double clamped = clampCharge(charge);
        if (clamped <= EPSILON) {
            return "empty";
        }
        if (clamped < CAPACITY_AEU * 0.5D) {
            return "low";
        }
        if (clamped < CAPACITY_AEU - EPSILON) {
            return "steady";
        }
        return "full";
    }

    public record ChargeResult(double charge, double fieldSpent, double gained, double loss) {
    }

    public record ReleaseResult(double charge, double aeuSpent, double manaRestored, double shortfall) {
    }
}
