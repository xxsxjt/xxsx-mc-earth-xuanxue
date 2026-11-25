package com.xxsx.earth.xuanxue;

import java.util.Set;

/** Whitelisted pipeline nodes. Energy sources are declared separately in {@link SpellSource}. */
public enum SpellNode {
    FIELD_FILTER("filter", "energy", Set.of("energy")),
    CONTAMINATION_FILTER("filter", "energy", Set.of("energy")),
    BREATH_CONVERTER("converter", "energy", Set.of("energy")),
    CIRCULATION_SHAPER("shaper", "control", Set.of("energy")),
    SIGNATURE_FILTER("filter", "energy", Set.of("energy")),
    PULSE_GATE("gate", "control", Set.of("energy")),
    MANA_RETURN("effect", "target", Set.of("control")),
    BODY_TARGET("target", "target", Set.of("control")),
    SELF_TARGET("target", "target", Set.of("control")),
    WARD_EFFECT("effect", "target", Set.of("target")),
    EXHAUST_SINK("sink", "none", Set.of("energy", "target")),
    FIELD_DEPLETION_SINK("sink", "none", Set.of("energy", "target"));

    private final String role;
    private final String outputPort;
    private final Set<String> allowedInputPorts;

    SpellNode(String role, String outputPort, Set<String> allowedInputPorts) {
        this.role = role;
        this.outputPort = outputPort;
        this.allowedInputPorts = allowedInputPorts;
    }

    public String role() {
        return role;
    }

    public String outputPort() {
        return outputPort;
    }

    public boolean acceptsInput(String port) {
        return allowedInputPorts.contains(port);
    }

    public boolean isSink() {
        return "sink".equals(role);
    }

    public boolean isEffect() {
        return this == WARD_EFFECT || this == MANA_RETURN;
    }

    public String translationKey() {
        return "spell_node.earth_xuanxue." + name().toLowerCase();
    }
}
