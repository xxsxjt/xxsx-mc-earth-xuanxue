package com.xxsx.earth.xuanxue;

public final class ArcanaBalanceRegression {
    private static final double EPSILON = 0.000001D;

    private ArcanaBalanceRegression() {
    }

    public static void main(String[] args) {
        verifyMonotonicAndCapped();
        verifyInvalidValuesAreBounded();
        verifyExperienceCurve();
        verifyJourneyMask();
        verifySpiritStoneLedger();
        verifyFieldLedger();
        verifyTalismanLedger();
        verifySpellGraphs();
    }

    private static void verifyMonotonicAndCapped() {
        double previousExhaustion = 0.0D;
        double previousCombat = 0.0D;
        double previousBreath = 1.0D;
        double previousFall = 0.0D;
        for (int level = 0; level <= 100; level++) {
            double exhaustion = ArcanaBalance.exhaustionReduction(level * 0.10D, level * 0.12D);
            double combat = ArcanaBalance.combatDamageReduction(level * 0.09D);
            double breath = ArcanaBalance.breathMultiplier(level * 30.0D);
            double fall = ArcanaBalance.fallDamageReduction(level * 0.08D);
            require(exhaustion + EPSILON >= previousExhaustion, "exhaustion reduction regressed");
            require(combat + EPSILON >= previousCombat, "combat reduction regressed");
            require(breath + EPSILON >= previousBreath, "breath multiplier regressed");
            require(fall + EPSILON >= previousFall, "fall reduction regressed");
            previousExhaustion = exhaustion;
            previousCombat = combat;
            previousBreath = breath;
            previousFall = fall;
        }
        close(0.82D, ArcanaBalance.exhaustionReduction(1000.0D, 1000.0D));
        close(0.30D, ArcanaBalance.combatDamageReduction(1000.0D));
        close(4.0D, ArcanaBalance.breathMultiplier(100000.0D));
        close(0.45D, ArcanaBalance.fallDamageReduction(1000.0D));
    }

    private static void verifyInvalidValuesAreBounded() {
        close(0.0D, ArcanaBalance.exhaustionReduction(Double.NaN, Double.POSITIVE_INFINITY));
        close(0.0D, ArcanaBalance.combatDamageReduction(Double.NEGATIVE_INFINITY));
        close(1.0D, ArcanaBalance.breathMultiplier(Double.NaN));
        close(0.0D, ArcanaBalance.fallDamageReduction(-1.0D));
    }

    private static void verifyExperienceCurve() {
        int previous = 0;
        for (int level = 1; level < 10; level++) {
            int needed = ArcanaBalance.xpNeededForLevel(level, 10);
            require(needed > previous, "experience curve must increase");
            previous = needed;
        }
        require(ArcanaBalance.xpNeededForLevel(0, 10) == 0, "locked route must not require experience");
        require(ArcanaBalance.xpNeededForLevel(10, 10) == 0, "capped route must not require experience");
    }

    private static void verifyJourneyMask() {
        int total = 6;
        int mask = 0;
        for (int milestoneId = 0; milestoneId < total; milestoneId++) {
            require(JourneyProgress.nextId(mask, total) == milestoneId, "journey order changed");
            int next = JourneyProgress.apply(mask, milestoneId, total);
            require(JourneyProgress.count(next, total) == JourneyProgress.count(mask, total) + 1,
                    "journey count did not advance");
            require(JourneyProgress.apply(next, milestoneId, total) == next,
                    "journey milestone is not idempotent");
            mask = next;
        }
        require(JourneyProgress.isComplete(mask, total), "journey full mask is incomplete");
        require(JourneyProgress.nextId(mask, total) == -1, "complete journey still has a next step");
        require(JourneyProgress.count(mask, total) == total, "journey total mismatch");
        require(JourneyProgress.sanitize(mask | (1 << 20), total) == mask,
                "journey mask kept unknown bits");
    }

    private static void verifySpiritStoneLedger() {
        close(0.0D, SpiritStoneCharge.clampCharge(Double.NaN));
        close(120.0D, SpiritStoneCharge.clampCharge(99999.0D));
        SpiritStoneCharge.ChargeResult emptyField = SpiritStoneCharge.capture(0.0D, 0.0D, 24.0D);
        close(0.0D, emptyField.gained());
        close(0.0D, emptyField.fieldSpent());
        SpiritStoneCharge.ChargeResult first = SpiritStoneCharge.capture(0.0D, 1.0D, 24.0D);
        require(first.charge() > 0.0D && first.charge() <= 120.0D, "first capture must charge");
        close(first.gained(), first.charge());
        close(first.fieldSpent(), first.gained() + first.loss());
        close(24.0D * SpiritStoneCharge.CAPTURE_EFFICIENCY, first.charge());
        SpiritStoneCharge.ChargeResult thin = SpiritStoneCharge.capture(0.0D, 0.25D, 24.0D);
        require(thin.charge() < first.charge(), "a thinner field must deliver less charge");
        close(24.0D * 0.0625D * SpiritStoneCharge.CAPTURE_EFFICIENCY, thin.charge());
        SpiritStoneCharge.ChargeResult full = SpiritStoneCharge.capture(
                SpiritStoneCharge.CAPACITY_AEU, 1.0D, 24.0D);
        close(SpiritStoneCharge.CAPACITY_AEU, full.charge());
        close(0.0D, full.fieldSpent());
        SpiritStoneCharge.ChargeResult capped = SpiritStoneCharge.capture(0.0D, 1.0D, 10000.0D);
        require(capped.charge() <= SpiritStoneCharge.CAPACITY_AEU + EPSILON, "charge must never exceed capacity");
        close(SpiritStoneCharge.CAPACITY_AEU, capped.charge());
        close(SpiritStoneCharge.CAPACITY_AEU / SpiritStoneCharge.CAPTURE_EFFICIENCY, capped.fieldSpent());
        SpiritStoneCharge.ReleaseResult noCharge = SpiritStoneCharge.release(0.0D, 10.0D);
        close(0.0D, noCharge.manaRestored());
        close(0.0D, noCharge.aeuSpent());
        SpiritStoneCharge.ReleaseResult released = SpiritStoneCharge.release(120.0D, 10.0D);
        require(released.manaRestored() > 0.0D && released.manaRestored() <= 10.0D,
                "release must restore only requested mana");
        close(120.0D, released.charge() + released.aeuSpent());
        require(released.aeuSpent() >= released.manaRestored() * SpiritStoneCharge.AEU_PER_MANA - EPSILON,
                "release must pay storage and loss");
        close(120.0D, SpiritStoneCharge.CAPACITY_AEU);

        // Round trip through one full stone must never create energy: capture at 80%,
        // release at 90%, and 10 AEU is one mana.
        double roundTripMana = SpiritStoneCharge.release(SpiritStoneCharge.CAPACITY_AEU,
                SpiritStoneCharge.CAPACITY_AEU).manaRestored();
        require(roundTripMana <= SpiritStoneCharge.CAPACITY_AEU * SpiritStoneCharge.RELEASE_EFFICIENCY
                        / SpiritStoneCharge.AEU_PER_MANA + EPSILON,
                "releasing a full stone must not exceed the release efficiency");
        require(SpiritStoneCharge.CAPTURE_EFFICIENCY <= 1.0D
                        && SpiritStoneCharge.RELEASE_EFFICIENCY <= 1.0D,
                "efficiencies must stay within 0-1");
    }

    private static void verifyFieldLedger() {
        close(100.0D, SpiritFieldLedger.availableField(100.0D, 0.0D));
        close(0.0D, SpiritFieldLedger.availableField(20.0D, 45.0D));
        close(100.0D, SpiritFieldLedger.availableField(99999.0D, Double.NaN));
        close(1.0D, SpiritFieldLedger.fieldCostOfAeu(10.0D));
        close(1.25D, SpiritFieldLedger.fieldCostOfAeu(12.5D));
        close(0.0D, SpiritFieldLedger.fieldCostOfAeu(Double.NaN));
        close(800.0D, SpiritFieldLedger.extractableAeu(100.0D, 0.8D));
        close(0.0D, SpiritFieldLedger.extractableAeu(-5.0D, 1.0D));
        close(7.2D, SpiritFieldLedger.meditationMana(100.0D, 0.08D));
        require(SpiritFieldLedger.meditationMana(100.0D, 0.08D)
                        <= SpiritFieldLedger.extractableAeu(100.0D, 0.08D) * SpiritStoneCharge.RELEASE_EFFICIENCY
                        / SpiritStoneCharge.AEU_PER_MANA,
                "meditation must not return more mana than the field can supply");
        close(10.0D, SpiritFieldLedger.fieldCostOfAeu(100.0D));
        require(SpiritFieldLedger.AEU_PER_FIELD_UNIT == 10.0D, "field conversion changed without a test update");

        // Meditation only debits the chunk for the energy it actually absorbs. Exposing
        // 100 levels at 8% coupling must cost 8 levels, not the full 100.
        close(8.0D, SpiritFieldLedger.meditationFieldCost(100.0D, 0.08D));
        close(0.0D, SpiritFieldLedger.meditationFieldCost(0.0D, 1.0D));
        close(0.0D, SpiritFieldLedger.meditationFieldCost(100.0D, Double.NaN));
        require(SpiritFieldLedger.meditationFieldCost(100.0D, 1.0D)
                        >= SpiritFieldLedger.meditationFieldCost(100.0D, 0.08D),
                "stronger coupling must cost the field at least as much");
        require(SpiritFieldLedger.meditationFieldCost(100.0D, 0.08D)
                        <= SpiritFieldLedger.availableField(100.0D, 0.0D),
                "one meditation cycle must never exceed the whole chunk field");
        require(SpiritFieldLedger.meditationMana(100.0D, 0.08D)
                        <= SpiritFieldLedger.meditationFieldCost(100.0D, 0.08D)
                        * SpiritFieldLedger.AEU_PER_FIELD_UNIT * SpiritStoneCharge.RELEASE_EFFICIENCY
                        / SpiritStoneCharge.AEU_PER_MANA + EPSILON,
                "meditation must not return more mana than it debits");
    }

    private static void verifyTalismanLedger() {
        close(0.0D, TalismanCharge.clamp(Double.NaN));
        close(24.0D, TalismanCharge.clamp(99999.0D));
        close(0.0D, TalismanCharge.clamp(-3.0D));
        require(!TalismanCharge.isCharged(0.0D), "empty talisman must not be triggerable");
        require(TalismanCharge.isCharged(TalismanCharge.CRAFTED_CHARGE_AEU),
                "a freshly drawn talisman must carry charge");
        require(TalismanCharge.CRAFTED_CHARGE_AEU <= TalismanCharge.CAPACITY_AEU,
                "crafted charge must fit the talisman capacity");
        close(1.0D, SpellCatalog.wardTalisman().manaCost());
        require(SpellCatalog.wardTalisman().energyCostAeu() == 0.0D,
                "talisman excitation must not draw extra field AEU");
    }

    private static void verifySpellGraphs() {
        require(SpellCatalog.graphs().size() == 2, "spell catalog changed without a test update");
        for (SpellGraph graph : SpellCatalog.graphs().values()) {
            SpellGraph.Validation validation = graph.validate();
            require(validation.valid(), "built-in graph is invalid: " + graph.id() + " -> "
                    + validation.errorCode() + " " + validation.message());
            require(graph.energyCostAeu() >= 0.0D, "graph AEU cost must be non-negative");
            require(graph.manaCost() >= 0.0D, "graph MP cost must be non-negative");
            require(graph.energyCostAeu() + graph.manaCost() > 0.0D, "graph must not be free");
        }
        close(1.0D, SpellCatalog.wardTalisman().manaCost());
        close(0.0D, SpellCatalog.wardTalisman().energyCostAeu());
        close(1.0D, SpellCatalog.circulation().energyCostAeu());
        close(0.0D, SpellCatalog.circulation().manaCost());
        require(SpellCatalog.circulation().requires(SpellSource.LOCAL_FIELD),
                "circulation must declare the local field source");
        require(SpellCatalog.circulation().requires(SpellSource.PLAYER_MANA),
                "circulation must declare the player mana sink");
        SpellGraph badPort = new SpellGraph("earth_xuanxue:bad_port", java.util.List.of(
                SpellSource.LOCAL_FIELD),
                java.util.List.of(
                        new SpellGraph.Node(SpellNode.BODY_TARGET, "energy", 0.0D, 1.0D),
                        new SpellGraph.Node(SpellNode.WARD_EFFECT, "target", 0.0D, 0.0D),
                        new SpellGraph.Node(SpellNode.EXHAUST_SINK, "target", 0.0D, 0.0D)));
        require(!badPort.validate().valid(), "target node accepted a control input as energy");
        require("invalid_input_port".equals(badPort.validate().errorCode()), "bad input produced wrong error");
        SpellGraph noSink = new SpellGraph("earth_xuanxue:no_sink", java.util.List.of(
                SpellSource.LOCAL_FIELD),
                java.util.List.of(
                        new SpellGraph.Node(SpellNode.WARD_EFFECT, "energy", 0.0D, 0.0D)));
        require(!noSink.validate().valid(), "graph without a sink was accepted");
        SpellGraph free = new SpellGraph("earth_xuanxue:free", java.util.List.of(
                SpellSource.PLAYER_MANA),
                java.util.List.of(
                        new SpellGraph.Node(SpellNode.BODY_TARGET, "control", 0.0D, 0.0D),
                        new SpellGraph.Node(SpellNode.WARD_EFFECT, "target", 0.0D, 0.0D),
                        new SpellGraph.Node(SpellNode.EXHAUST_SINK, "target", 0.0D, 0.0D)));
        require("free_spell".equals(free.validate().errorCode()), "zero-cost graph was accepted");
    }

    private static void close(double expected, double actual) {
        require(Math.abs(expected - actual) <= EPSILON, "expected " + expected + ", got " + actual);
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
