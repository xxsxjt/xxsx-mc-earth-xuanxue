package com.xxsx.earth.xuanxue.particle;

import com.xxsx.earth.xuanxue.XuanhuanEarth;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Custom xuanhuan particle types.
 *
 * <p>Vanilla particles cannot express a qi mote that drifts along a field line, a
 * rotating seal ring, or a rune glyph that lingers. These are registered as plain
 * {@link SimpleParticleType}s; all motion and tinting lives in the client providers so
 * the server only ever sends a type plus a position.</p>
 */
public final class XuanhuanParticles {
    public static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES =
            DeferredRegister.create(Registries.PARTICLE_TYPE, XuanhuanEarth.MODID);

    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> QI_MOTE =
            PARTICLE_TYPES.register("qi_mote", () -> new SimpleParticleType(false));
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> SPIRIT_FLAME =
            PARTICLE_TYPES.register("spirit_flame", () -> new SimpleParticleType(false));
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> BLOOD_MOTE =
            PARTICLE_TYPES.register("blood_mote", () -> new SimpleParticleType(false));
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> QI_RING =
            PARTICLE_TYPES.register("qi_ring", () -> new SimpleParticleType(true));
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> SEAL_RING =
            PARTICLE_TYPES.register("seal_ring", () -> new SimpleParticleType(true));
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> WIND_RING =
            PARTICLE_TYPES.register("wind_ring", () -> new SimpleParticleType(true));
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> QI_SPARK =
            PARTICLE_TYPES.register("qi_spark", () -> new SimpleParticleType(false));
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> EMBER_SPARK =
            PARTICLE_TYPES.register("ember_spark", () -> new SimpleParticleType(false));
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> RUNE_GLYPH_A =
            PARTICLE_TYPES.register("rune_glyph_a", () -> new SimpleParticleType(true));
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> RUNE_GLYPH_B =
            PARTICLE_TYPES.register("rune_glyph_b", () -> new SimpleParticleType(true));
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> SPIRIT_SMOKE =
            PARTICLE_TYPES.register("spirit_smoke", () -> new SimpleParticleType(false));
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> ALCHEMY_SMOKE =
            PARTICLE_TYPES.register("alchemy_smoke", () -> new SimpleParticleType(false));
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> SPIRIT_LEAF =
            PARTICLE_TYPES.register("spirit_leaf", () -> new SimpleParticleType(false));
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> EMBER_LEAF =
            PARTICLE_TYPES.register("ember_leaf", () -> new SimpleParticleType(false));

    private XuanhuanParticles() {
    }
}
