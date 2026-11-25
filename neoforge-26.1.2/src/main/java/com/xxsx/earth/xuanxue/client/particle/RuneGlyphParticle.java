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
 * A hanging rune glyph that slowly turns in place.
 *
 * <p>Glyphs are the readable part of a formation: they should persist long enough to be
 * seen, stay level with the ground and pulse rather than fly away. Nothing in vanilla
 * provides a long-lived, non-moving, rotating decal, so this particle owns that role.</p>
 */
public final class RuneGlyphParticle extends SingleQuadParticle {
    private final SpriteSet sprites;
    private final float baseSize;
    private final float spin;
    private final float bobPhase;
    private final int fadeIn;

    private RuneGlyphParticle(ClientLevel level, double x, double y, double z, SpriteSet sprites,
                              float red, float green, float blue, float scale, int lifetime) {
        super(level, x, y, z, sprites.first());
        this.sprites = sprites;
        this.hasPhysics = false;
        this.gravity = 0.0F;
        this.friction = 1.0F;
        this.lifetime = lifetime;
        this.fadeIn = Math.max(1, lifetime / 6);
        this.baseSize = 0.26F * scale;
        this.quadSize = this.baseSize;
        this.spin = (random.nextFloat() < 0.5F ? -1.0F : 1.0F) * (0.012F + random.nextFloat() * 0.014F);
        this.bobPhase = random.nextFloat() * Mth.TWO_PI;
        this.roll = random.nextFloat() * Mth.TWO_PI;
        this.setColor(red, green, blue);
        this.setSpriteFromAge(sprites);
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
        this.roll += this.spin;
        this.y += Math.sin(this.age * 0.14F + this.bobPhase) * 0.0016D;
        float alpha;
        if (this.age < this.fadeIn) {
            alpha = (float) this.age / this.fadeIn;
        } else {
            float remaining = 1.0F - (float) (this.age - this.fadeIn) / Math.max(1, this.lifetime - this.fadeIn);
            alpha = Mth.clamp(remaining, 0.0F, 1.0F);
        }
        float pulse = 0.85F + 0.15F * Mth.sin(this.age * 0.3F + this.bobPhase);
        this.setAlpha(alpha * pulse);
        this.quadSize = this.baseSize * (0.9F + pulse * 0.2F);
        this.setSpriteFromAge(this.sprites);
    }

    public static ParticleProvider<SimpleParticleType> goldProvider(SpriteSet sprites) {
        return (type, level, x, y, z, xa, ya, za, random) -> new RuneGlyphParticle(
                level, x, y, z, sprites, 1.0F, 0.87F, 0.55F, 1.0F, 34 + random.nextInt(14));
    }

    public static ParticleProvider<SimpleParticleType> jadeProvider(SpriteSet sprites) {
        return (type, level, x, y, z, xa, ya, za, random) -> new RuneGlyphParticle(
                level, x, y, z, sprites, 0.58F, 0.98F, 0.92F, 0.95F, 30 + random.nextInt(12));
    }
}
