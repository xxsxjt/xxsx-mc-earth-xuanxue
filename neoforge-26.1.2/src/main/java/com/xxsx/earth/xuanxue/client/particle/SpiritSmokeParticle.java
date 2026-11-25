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
 * A soft, swelling plume used for furnaces, springs and spirit smoke.
 *
 * <p>Vanilla smoke is grey and drifts upward fast. Xuanhuan smoke should billow outward
 * slowly, tint per source and grow as it fades so alchemy reads as vapour rather than soot.</p>
 */
public final class SpiritSmokeParticle extends SingleQuadParticle {
    private final SpriteSet sprites;
    private final float baseSize;
    private final float grow;
    private final float riseSpeed;
    private final float swirlPhase;

    private SpiritSmokeParticle(ClientLevel level, double x, double y, double z, SpriteSet sprites,
                                float red, float green, float blue, float scale, float grow,
                                float riseSpeed, int lifetime) {
        super(level, x, y, z, sprites.first());
        this.sprites = sprites;
        this.hasPhysics = false;
        this.gravity = -0.006F;
        this.friction = 0.965F;
        this.lifetime = lifetime;
        this.baseSize = 0.16F * scale;
        this.quadSize = this.baseSize;
        this.grow = grow;
        this.riseSpeed = riseSpeed;
        this.swirlPhase = random.nextFloat() * Mth.TWO_PI;
        this.xd = (random.nextDouble() - 0.5D) * 0.008D;
        this.yd = riseSpeed;
        this.zd = (random.nextDouble() - 0.5D) * 0.008D;
        this.roll = random.nextFloat() * Mth.TWO_PI;
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
        if (this.removed) {
            return;
        }
        float progress = (float) this.age / this.lifetime;
        this.quadSize = this.baseSize * (1.0F + progress * this.grow);
        this.roll += 0.012F;
        this.xd += Math.sin(this.age * 0.12F + this.swirlPhase) * 0.0012D;
        this.zd += Math.cos(this.age * 0.11F + this.swirlPhase) * 0.0012D;
        float alpha = progress < 0.18F ? progress / 0.18F : 1.0F - (progress - 0.18F) / 0.82F;
        this.setAlpha(Mth.clamp(alpha, 0.0F, 1.0F) * 0.72F);
        this.setSpriteFromAge(this.sprites);
    }

    public static ParticleProvider<SimpleParticleType> spiritProvider(SpriteSet sprites) {
        return (type, level, x, y, z, xa, ya, za, random) -> new SpiritSmokeParticle(
                level, x, y, z, sprites, 0.84F, 0.94F, 0.96F, 1.0F, 1.5F, 0.012F,
                30 + random.nextInt(16));
    }

    public static ParticleProvider<SimpleParticleType> alchemyProvider(SpriteSet sprites) {
        return (type, level, x, y, z, xa, ya, za, random) -> new SpiritSmokeParticle(
                level, x, y, z, sprites, 0.86F, 0.66F, 0.52F, 1.15F, 1.9F, 0.016F,
                34 + random.nextInt(18));
    }
}
