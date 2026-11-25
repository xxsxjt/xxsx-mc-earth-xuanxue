package com.xxsx.earth.xuanxue;

/** Declared energy inputs of a spell graph. Sources are not pipeline nodes. */
public enum SpellSource {
    LOCAL_FIELD("field", "energy"),
    CHARGED_TALISMAN("talisman", "energy"),
    SPIRIT_STONE("spirit_stone", "energy"),
    PLAYER_MANA("mana", "control", "energy");

    private final String role;
    private final java.util.List<String> outputPorts;

    SpellSource(String role, String... outputPorts) {
        this.role = role;
        this.outputPorts = java.util.List.of(outputPorts);
    }

    public String role() {
        return role;
    }

    public java.util.List<String> outputPorts() {
        return outputPorts;
    }

    public boolean feeds(String port) {
        return outputPorts.contains(port);
    }

    public String translationKey() {
        return "spell_source.earth_xuanxue." + name().toLowerCase();
    }
}
