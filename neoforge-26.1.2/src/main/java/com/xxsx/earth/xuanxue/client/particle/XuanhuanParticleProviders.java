package com.xxsx.earth.xuanxue.client.particle;

import com.xxsx.earth.xuanxue.particle.XuanhuanParticles;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;

/** Binds every custom particle type to its client provider. */
public final class XuanhuanParticleProviders {
    private XuanhuanParticleProviders() {
    }

    public static void register(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(XuanhuanParticles.QI_MOTE.get(), QiMoteParticle::provider);
        event.registerSpriteSet(XuanhuanParticles.SPIRIT_FLAME.get(), QiMoteParticle::warmProvider);
        event.registerSpriteSet(XuanhuanParticles.BLOOD_MOTE.get(), QiMoteParticle::bloodProvider);
        event.registerSpriteSet(XuanhuanParticles.QI_RING.get(), QiRingParticle::qiProvider);
        event.registerSpriteSet(XuanhuanParticles.SEAL_RING.get(), QiRingParticle::sealProvider);
        event.registerSpriteSet(XuanhuanParticles.WIND_RING.get(), QiRingParticle::windProvider);
        event.registerSpriteSet(XuanhuanParticles.QI_SPARK.get(), QiSparkParticle::qiProvider);
        event.registerSpriteSet(XuanhuanParticles.EMBER_SPARK.get(), QiSparkParticle::emberProvider);
        event.registerSpriteSet(XuanhuanParticles.RUNE_GLYPH_A.get(), RuneGlyphParticle::goldProvider);
        event.registerSpriteSet(XuanhuanParticles.RUNE_GLYPH_B.get(), RuneGlyphParticle::jadeProvider);
        event.registerSpriteSet(XuanhuanParticles.SPIRIT_SMOKE.get(), SpiritSmokeParticle::spiritProvider);
        event.registerSpriteSet(XuanhuanParticles.ALCHEMY_SMOKE.get(), SpiritSmokeParticle::alchemyProvider);
        event.registerSpriteSet(XuanhuanParticles.SPIRIT_LEAF.get(), SpiritLeafParticle::greenProvider);
        event.registerSpriteSet(XuanhuanParticles.EMBER_LEAF.get(), SpiritLeafParticle::emberProvider);
    }
}
