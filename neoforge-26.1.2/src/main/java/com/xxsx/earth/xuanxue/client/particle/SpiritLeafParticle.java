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
 * A tumbling leaf or feather shard.
 *
 * <p>It falls slowly, sways and rotates around its own axis. Used for spirit grass growth
 * and crane feather trails, where the read is "organic debris carried on qi" rather than
 * "spark".</p>
 */
public final class SpiritLeafParticle extends SingleQuadParticle {
    private final SpriteSet sprites;
    private final float swayPhase;
    private final float swayAmount;
    private final float tumble;

    private SpiritLeafParticle(ClientLevel level, double x, double y, double z, SpriteSet sprites,
                               float red, float green, float blue, float scale, int lifetime) {
        super(level, x, y, z, sprites.first());
        this.sprites = sprites;
        this.hasPhysics = false;
        this.gravity = 0.02F;
        this.friction = 0.99F;
        this.lifetime = lifetime;
        this.quadSize = 0.13F * scale;
        this.swayPhase = random.nextFloat() * Mth.TWO_PI;
        this.swayAmount = 0.004F + random.nextFloat() * 0.004F;
        this.tumble = (random.nextFloat() < 0.5F ? -1.0F : 1.0F) * (0.05F + random.nextFloat() * 0.05F);
        this.yd = -0.004D - random.nextDouble() * 0.006D;
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
        this.xd += Math.sin(this.age * 0.16F + this.swayPhase) * this.swayAmount;
        this.zd += Math.cos(this.age * 0.13F + this.swayPhase) * this.swayAmount;
        this.roll += this.tumble;
        float progress = (float) this.age / this.lifetime;
        float alpha = progress < 0.15F ? progress / 0.15F : 1.0F - (progress - 0.15F) / 0.85F;
        this.setAlpha(Mth.clamp(alpha, 0.0F, 1.0F) * 0.9F);
        this.setSpriteFromAge(this.sprites);
    }

    public static ParticleProvider<SimpleParticleType> greenProvider(SpriteSet sprites) {
        return (type, level, x, y, z, xa, ya, za, random) -> new SpiritLeafParticle(
                level, x, y, z, sprites, 0.62F, 0.95F, 0.68F, 1.0F, 40 + random.nextInt(22));
    }

    public static ParticleProvider<SimpleParticleType> emberProvider(SpriteSet sprites) {
        return (type, level, x, y, z, xa, ya, za, random) -> new SpiritLeafParticle(
                level, x, y, z, sprites, 1.0F, 0.72F, 0.58F, 1.1F, 30 + random.nextInt(16));
    }
}
