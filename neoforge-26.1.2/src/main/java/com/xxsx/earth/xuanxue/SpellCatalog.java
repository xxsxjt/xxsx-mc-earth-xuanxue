package com.xxsx.earth.xuanxue;

import java.util.List;
import java.util.Map;

/** Built-in spell graphs. Data files may later reference these by stable id. */
public final class SpellCatalog {
    public static final SpellGraph WARD_TALISMAN = new SpellGraph("earth_xuanxue:ward_talisman",
            List.of(SpellSource.CHARGED_TALISMAN),
            List.of(
                    node(SpellNode.SIGNATURE_FILTER, "energy", 0.0D, 0.0D),
                    node(SpellNode.PULSE_GATE, "energy", 0.0D, 1.0D),
                    node(SpellNode.BODY_TARGET, "control", 0.0D, 0.0D),
                    node(SpellNode.WARD_EFFECT, "target", 0.0D, 0.0D),
                    node(SpellNode.EXHAUST_SINK, "target", 0.0D, 0.0D)
            ));
    public static final SpellGraph CIRCULATION = new SpellGraph("earth_xuanxue:circulation",
            List.of(SpellSource.LOCAL_FIELD, SpellSource.PLAYER_MANA),
            List.of(
                    node(SpellNode.CONTAMINATION_FILTER, "energy", 0.0D, 0.0D),
                    node(SpellNode.BREATH_CONVERTER, "energy", 1.0D, 0.0D),
                    node(SpellNode.CIRCULATION_SHAPER, "energy", 0.0D, 0.0D),
                    node(SpellNode.MANA_RETURN, "control", 0.0D, 0.0D),
                    node(SpellNode.FIELD_DEPLETION_SINK, "target", 0.0D, 0.0D)
            ));
    private static final Map<String, SpellGraph> BY_ID = Map.of(
            WARD_TALISMAN.id(), WARD_TALISMAN,
            CIRCULATION.id(), CIRCULATION);

    private SpellCatalog() {
    }

    public static Map<String, SpellGraph> graphs() {
        return BY_ID;
    }

    public static SpellGraph wardTalisman() {
        return WARD_TALISMAN;
    }

    public static SpellGraph circulation() {
        return CIRCULATION;
    }

    private static SpellGraph.Node node(SpellNode kind, String input, double costAeu, double costMana) {
        return new SpellGraph.Node(kind, input, costAeu, costMana);
    }
}
