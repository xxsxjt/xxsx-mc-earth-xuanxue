package com.xxsx.xuanhuanearth;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.item.ItemStack;

import java.util.Locale;

/** X-062 Array rune stone pattern component (power, guard, flow, bind). */
public record RuneStoneComponent(Rune rune) {
    public enum Rune {
        POWER("power"),
        GUARD("guard"),
        FLOW("flow"),
        BIND("bind");

        private final String id;

        Rune(String id) {
            this.id = id;
        }

        public String id() {
            return id;
        }

        public Rune next() {
            return values()[(ordinal() + 1) % values().length];
        }

        public static Rune byId(String raw) {
            if (raw == null) {
                return FLOW;
            }
            String norm = raw.trim().toLowerCase(Locale.ROOT);
            for (Rune r : values()) {
                if (r.id.equals(norm)) {
                    return r;
                }
            }
            return FLOW;
        }
    }

    public static final RuneStoneComponent DEFAULT = new RuneStoneComponent(Rune.FLOW);
    public static final Codec<RuneStoneComponent> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.xmap(Rune::byId, Rune::id).optionalFieldOf("rune", Rune.FLOW).forGetter(RuneStoneComponent::rune)
    ).apply(instance, RuneStoneComponent::new));

    public static RuneStoneComponent read(ItemStack stack) {
        RuneStoneComponent val = stack.get(XuanhuanEarth.RUNE_STONE_COMPONENT.get());
        return val == null ? DEFAULT : val;
    }
}
