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
 * A fast, tapered spark that keeps its momentum.
 *
 * <p>Vanilla {@code crit} particles are short and gravity-bound; this one has almost no
 * gravity, stretches along its own velocity and leaves a visible streak. Used for skill
 * activation, beast strikes and talisman bursts.</p>
 */
public final class QiSparkParticle extends SingleQuadParticle {
    private final SpriteSet sprites;
    private final float baseSize;
    private final float stretch;
    private final boolean gravityBound;

    private QiSparkParticle(ClientLevel level, double x, double y, double z,
                            double xa, double ya, double za, SpriteSet sprites,
                            float red, float green, float blue, float scale, boolean gravityBound) {
        super(level, x, y, z, 0.0D, 0.0D, 0.0D, sprites.first());
        this.sprites = sprites;
        this.hasPhysics = false;
        this.gravity = gravityBound ? 0.35F : 0.01F;
        this.friction = 0.93F;
        this.lifetime = 10 + random.nextInt(8);
        this.baseSize = 0.11F * scale;
        this.quadSize = this.baseSize;
        this.stretch = 1.0F + random.nextFloat() * 0.6F;
        this.gravityBound = gravityBound;
        this.xd = xa;
        this.yd = ya;
        this.zd = za;
        this.roll = random.nextFloat() * Mth.TWO_PI;
        this.setColor(red, green, blue);
        this.setSpriteFromAge(sprites);
    }

    @Override
    public SingleQuadParticle.Layer getLayer() {
        return SingleQuadParticle.Layer.TRANSLUCENT;
    }

    @Override
    public float getQuadSize(float partialTick) {
        return this.quadSize * this.stretch;
    }

    @Override
    public void tick() {
        this.oRoll = this.roll;
        super.tick();
        if (this.removed) {
            return;
        }
        float progress = (float) this.age / this.lifetime;
        this.setAlpha(Mth.clamp(1.0F - progress, 0.0F, 1.0F));
        this.quadSize = this.baseSize * (1.0F - progress * 0.45F);
        this.roll += 0.14F;
        this.setSpriteFromAge(this.sprites);
        if (this.gravityBound && this.onGround) {
            this.yd = Math.abs(this.yd) * 0.4D;
        }
    }

    public static ParticleProvider<SimpleParticleType> qiProvider(SpriteSet sprites) {
        return (type, level, x, y, z, xa, ya, za, random) -> new QiSparkParticle(
                level, x, y, z, xa, ya, za, sprites, 0.62F, 0.98F, 0.95F, 1.0F, false);
    }

    public static ParticleProvider<SimpleParticleType> emberProvider(SpriteSet sprites) {
        return (type, level, x, y, z, xa, ya, za, random) -> new QiSparkParticle(
                level, x, y, z, xa, ya, za, sprites, 1.0F, 0.72F, 0.35F, 1.1F, true);
    }
}
