package com.xxsx.earth.xuanxue;

import com.xxsx.earth.xuanxue.particle.XuanhuanParticles;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

/**
 * Harmful status effect triggered when a non-cultivator or low-realm cultivator soaks in
 * overly concentrated Spirit Spring liquid: meridians suffer Qi backlash (magic damage + slowness/weakness/nausea).
 */
public final class QiOverloadEffect extends MobEffect {
    public QiOverloadEffect() {
        super(MobEffectCategory.HARMFUL, 0xC83A64);
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return duration % 25 == 0;
    }

    @Override
    public boolean applyEffectTick(ServerLevel level, LivingEntity entity, int amplifier) {
        if (entity instanceof Player player && (player.isCreative() || player.isSpectator())) {
            return true;
        }
        float dmg = 1.0F + Math.min(3, amplifier) * 0.65F;
        entity.hurtServer(level, level.damageSources().magic(), dmg);
        entity.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 50, Math.min(2, amplifier), false, false, true));
        entity.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 50, 0, false, false, true));
        if (amplifier >= 1) {
            entity.addEffect(new MobEffectInstance(MobEffects.NAUSEA, 70, 0, false, false, true));
        }
        level.sendParticles(XuanhuanParticles.QI_SPARK.get(),
                entity.getX(), entity.getY() + 0.85D, entity.getZ(),
                4, 0.3D, 0.3D, 0.3D, 0.04D);
        return true;
    }
}
