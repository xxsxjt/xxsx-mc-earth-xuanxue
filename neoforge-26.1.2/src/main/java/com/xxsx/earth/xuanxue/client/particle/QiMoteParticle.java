package com.xxsx.earth.xuanxue.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;

/**
 * A slow, drifting mote of ambient qi.
 *
 * <p>It rises, curls sideways on a sine path, and fades out. The sprite set cycles so the
 * mote reads as a living spark rather than a static dot. Vanilla has nothing that keeps a
 * soft, non-additive glow this long without gravity, so this is a dedicated particle.</p>
 */
public final class QiMoteParticle extends SingleQuadParticle {
    private final SpriteSet sprites;
    private final float spin;
    private final float driftPhase;
    private final float baseSize;

    private QiMoteParticle(ClientLevel level, double x, double y, double z,
                           double xa, double ya, double za, SpriteSet sprites,
                           float red, float green, float blue, float scale) {
        super(level, x, y, z, 0.0D, 0.0D, 0.0D, sprites.first());
        this.sprites = sprites;
        this.hasPhysics = false;
        this.friction = 0.985F;
        this.gravity = -0.004F;
        this.lifetime = 26 + random.nextInt(22);
        this.baseSize = 0.09F * scale;
        this.quadSize = this.baseSize;
        this.spin = (random.nextFloat() - 0.5F) * 0.06F;
        this.roll = random.nextFloat() * Mth.TWO_PI;
        this.driftPhase = random.nextFloat() * Mth.TWO_PI;
        this.xd = xa * 0.12D + (random.nextDouble() - 0.5D) * 0.012D;
        this.yd = ya * 0.10D + 0.012D + random.nextDouble() * 0.010D;
        this.zd = za * 0.12D + (random.nextDouble() - 0.5D) * 0.012D;
        this.setColor(red, green, blue);
        this.setSpriteFromAge(sprites);
    }

    @Override
    public SingleQuadParticle.Layer getLayer() {
        return SingleQuadParticle.Layer.TRANSLUCENT;
    }

    @Override
    public void tick() {
        this.oRoll = this.roll;
        super.tick();
        if (!this.removed) {
            float age = (float) this.age / this.lifetime;
            double curl = Math.sin(this.age * 0.22F + this.driftPhase) * 0.006D;
            this.xd += curl;
            this.zd += Math.cos(this.age * 0.18F + this.driftPhase) * 0.006D;
            this.roll += this.spin;
            this.setSpriteFromAge(this.sprites);
            float fade = age < 0.2F ? age / 0.2F : 1.0F - (age - 0.2F) / 0.8F;
            this.setAlpha(Mth.clamp(fade, 0.0F, 1.0F) * 0.92F);
            this.quadSize = this.baseSize * (1.0F + age * 0.55F);
        }
    }

    public static ParticleProvider<SimpleParticleType> provider(SpriteSet sprites) {
        return (type, level, x, y, z, xa, ya, za, random) -> new QiMoteParticle(
                level, x, y, z, xa, ya, za, sprites, 0.55F, 0.98F, 0.95F, 1.0F);
    }

    public static ParticleProvider<SimpleParticleType> warmProvider(SpriteSet sprites) {
        return (type, level, x, y, z, xa, ya, za, random) -> new QiMoteParticle(
                level, x, y, z, xa, ya, za, sprites, 1.0F, 0.83F, 0.52F, 0.92F);
    }

    public static ParticleProvider<SimpleParticleType> bloodProvider(SpriteSet sprites) {
        return (type, level, x, y, z, xa, ya, za, random) -> new QiMoteParticle(
                level, x, y, z, xa, ya, za, sprites, 0.95F, 0.45F, 0.55F, 0.8F);
    }
}
