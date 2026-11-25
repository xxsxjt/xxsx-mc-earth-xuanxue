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
 * An expanding, spinning ring used for seal bursts and array pulses.
 *
 * <p>It deliberately has no motion: the ring is a ground-anchored effect, so it stays put
 * and grows outward while fading. A vanilla {@code end_rod} or {@code enchant} spray cannot
 * express a closed circle, and a flat billboard sprite would not read as a ring from above.</p>
 */
public final class QiRingParticle extends SingleQuadParticle {
    private final SpriteSet sprites;
    private final float startSize;
    private final float endSize;
    private final float spinSpeed;
    private final int totalLife;

    private QiRingParticle(ClientLevel level, double x, double y, double z, SpriteSet sprites,
                           float red, float green, float blue, float startSize, float endSize,
                           int lifetime, boolean flat) {
        super(level, x, y, z, sprites.first());
        this.sprites = sprites;
        this.hasPhysics = false;
        this.gravity = 0.0F;
        this.friction = 1.0F;
        this.totalLife = lifetime;
        this.lifetime = lifetime;
        this.startSize = startSize;
        this.endSize = endSize;
        this.quadSize = startSize;
        this.spinSpeed = (random.nextFloat() < 0.5F ? -1.0F : 1.0F) * (0.04F + random.nextFloat() * 0.03F);
        this.roll = random.nextFloat() * Mth.TWO_PI;
        this.setColor(red, green, blue);
        if (flat) {
            this.setSpriteFromAge(sprites);
        }
    }

    @Override
    public FacingCameraMode getFacingCameraMode() {
        return FacingCameraMode.LOOKAT_Y;
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
        float progress = 1.0F - (float) this.age / this.totalLife;
        float eased = 1.0F - progress * progress;
        this.quadSize = Mth.lerp(eased, this.startSize, this.endSize);
        this.roll += this.spinSpeed;
        this.setAlpha(Mth.clamp(progress * 1.15F, 0.0F, 1.0F));
        this.setSpriteFromAge(this.sprites);
    }

    public static ParticleProvider<SimpleParticleType> sealProvider(SpriteSet sprites) {
        return (type, level, x, y, z, xa, ya, za, random) -> new QiRingParticle(
                level, x, y, z, sprites, 1.0F, 0.86F, 0.58F, 0.22F, 1.35F, 22, true);
    }

    public static ParticleProvider<SimpleParticleType> qiProvider(SpriteSet sprites) {
        return (type, level, x, y, z, xa, ya, za, random) -> new QiRingParticle(
                level, x, y, z, sprites, 0.52F, 0.97F, 0.94F, 0.26F, 1.55F, 26, true);
    }

    public static ParticleProvider<SimpleParticleType> windProvider(SpriteSet sprites) {
        return (type, level, x, y, z, xa, ya, za, random) -> new QiRingParticle(
                level, x, y, z, sprites, 0.62F, 0.84F, 1.0F, 0.3F, 1.9F, 18, true);
    }
}
