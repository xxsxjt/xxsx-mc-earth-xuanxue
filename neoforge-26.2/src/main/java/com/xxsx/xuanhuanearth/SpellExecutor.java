package com.xxsx.xuanhuanearth;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;

/**
 * Server-authoritative planning and commit for built-in spell graphs.
 *
 * <p>A plan never mutates state. Commit re-reads the live state, re-validates, and only
 * then pays the costs that the graph actually names. AEU and MP are separate fields of
 * {@link Plan}; they are not interchangeable.</p>
 */
public final class SpellExecutor {
    private SpellExecutor() {
    }

    public static Plan plan(ServerPlayer player, SpellGraph graph) {
        return plan(player, graph, InteractionHand.MAIN_HAND);
    }

    public static Plan plan(ServerPlayer player, SpellGraph graph, InteractionHand hand) {
        SpellGraph resolved = resolve(graph);
        SpellGraph.Validation validation = resolved.validate();
        if (!validation.valid()) {
            return Plan.reject(validation.errorCode(), validation.message());
        }
        boolean builtIn = resolved.id().equals(SpellCatalog.WARD_TALISMAN.id())
                || resolved.id().equals(SpellCatalog.CIRCULATION.id());
        if (!builtIn && player != null && !XuanhuanLoreIntegration.knowsSpellGraphs(player)) {
            return Plan.reject("lore_locked", "spell_graphs knowledge is required for custom spell graphs");
        }
        if (resolved.id().equals(SpellCatalog.WARD_TALISMAN.id())) {
            ItemStack talisman = player.getItemInHand(hand);
            if (!talisman.is(XuanhuanEarth.BASIC_TALISMAN.get())) {
                return Plan.reject("missing_talisman", "a charged talisman must be held");
            }
            if (!TalismanComponent.isSealed(talisman)) {
                return Plan.reject("talisman_empty", "the talisman carries no sealed field charge");
            }
            double manaCost = resolved.manaCost();
            if (!ArcanaPower.canSpendMana(player, manaCost)) {
                return Plan.reject("mana_low", "not enough mana to excite the talisman");
            }
            return Plan.ok(resolved, 0.0D, manaCost);
        }
        if (resolved.id().equals(SpellCatalog.CIRCULATION.id())) {
            if (ArcanaPower.getCultivationLevel(player) <= 0) {
                return Plan.reject("requires_technique", "learn the qi guiding method first");
            }
            double absorption = ArcanaPower.getQiAbsorptionRate(player);
            if (absorption <= 0.0D) {
                return Plan.reject("no_absorption", "the current route cannot absorb ambient qi");
            }
            return Plan.ok(resolved, resolved.energyCostAeu(), 0.0D);
        }
        return Plan.reject("unknown_graph", "no executor is registered for " + resolved.id());
    }

    /** Prefers a server-loaded data graph, falling back to the built-in catalog. */
    private static SpellGraph resolve(SpellGraph graph) {
        if (graph == null) {
            throw new IllegalArgumentException("spell graph is required");
        }
        SpellGraph loaded = SpellGraphLoader.find(graph.id());
        return loaded == null ? graph : loaded;
    }

    public static boolean commitTalisman(ServerLevel level, ServerPlayer player) {
        return commitTalisman(level, player, InteractionHand.MAIN_HAND);
    }

    public static boolean commitTalisman(ServerLevel level, ServerPlayer player, InteractionHand hand) {
        Plan plan = plan(player, SpellCatalog.wardTalisman(), hand);
        if (!plan.accepted() || !ArcanaPower.trySpendMana(player, plan.manaCost())) {
            return false;
        }
        if (!player.getAbilities().instabuild) {
            player.getItemInHand(hand).shrink(1);
        }
        XuanhuanJourney.complete(player, XuanhuanJourney.Milestone.ARTIFACT_USE);
        if (level != null) {
            CultivationNetwork.broadcastVisual(player, CultivationVisualAction.TALISMAN);
            XuanhuanEffects.talismanActivation(level, player.position());
        }
        return true;
    }

    /**
     * Commits one circulation cycle. {@code ambientLevels} is how much local field the
     * practitioner exposes themselves to; only the coupled fraction is actually drawn,
     * so the field is debited for the energy that was absorbed, not for the raw exposure.
     * The graph's own AEU overhead is paid on top of that.
     */
    public static CirculationCommit commitCirculation(ServerLevel level, ServerPlayer player,
                                                      ArcanaChunkField.Reading reading,
                                                      double ambientLevels) {
        Plan plan = plan(player, SpellCatalog.circulation());
        if (!plan.accepted()) {
            return CirculationCommit.reject(plan.errorCode(), plan.message());
        }
        double overheadField = SpiritFieldLedger.fieldCostOfAeu(plan.aeuCost());
        double usable = SpiritFieldLedger.availableField(reading.value(), reading.depletion());
        double ambient = Double.isFinite(ambientLevels) ? Math.max(0.0D, ambientLevels) : 0.0D;
        double absorption = ArcanaPower.getQiAbsorptionRate(player);
        double absorbedField = SpiritFieldLedger.meditationFieldCost(ambient, absorption);
        double totalField = overheadField + absorbedField;
        if (totalField <= 0.0001D) {
            return CirculationCommit.reject("empty_graph", "circulation did not request any field energy");
        }
        if (usable + 0.0001D < totalField) {
            return CirculationCommit.reject("field_low", "the local qi field cannot support this cycle");
        }
        double manaGain = SpiritFieldLedger.meditationMana(ambient, absorption);
        if (SpiritFieldLedger.isNegligible(manaGain)) {
            return CirculationCommit.reject("no_gain", "this cycle would not restore any mana");
        }
        double before = ArcanaPower.getCurrentMana(player);
        ArcanaPower.setCurrentMana(player, before + manaGain);
        double restored = ArcanaPower.getCurrentMana(player) - before;
        if (SpiritFieldLedger.isNegligible(restored)) {
            return CirculationCommit.reject("mana_full", "the mana pool is already full");
        }
        double aeuDrawn = totalField * SpiritFieldLedger.AEU_PER_FIELD_UNIT;
        Spirituality.consumeFieldLevels(level, player.blockPosition(), totalField);
        return CirculationCommit.ok(aeuDrawn, totalField, restored);
    }

    public record Plan(boolean accepted, SpellGraph graph, double aeuCost, double manaCost,
                       String errorCode, String message) {
        public static Plan ok(SpellGraph graph, double aeuCost, double manaCost) {
            return new Plan(true, graph, aeuCost, manaCost, "", "");
        }

        public static Plan reject(String code, String message) {
            return new Plan(false, null, 0.0D, 0.0D, code, message);
        }
    }

    public record CirculationCommit(boolean accepted, double aeuDrawn, double fieldCost,
                                    double manaRestored, String errorCode, String message) {
        public static CirculationCommit ok(double aeuDrawn, double fieldCost, double manaRestored) {
            return new CirculationCommit(true, aeuDrawn, fieldCost, manaRestored, "", "");
        }

        public static CirculationCommit reject(String code, String message) {
            return new CirculationCommit(false, 0.0D, 0.0D, 0.0D, code, message);
        }
    }
}
