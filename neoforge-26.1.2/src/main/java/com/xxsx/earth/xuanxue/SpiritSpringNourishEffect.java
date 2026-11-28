package com.xxsx.earth.xuanxue;

import com.xxsx.earth.xuanxue.particle.XuanhuanParticles;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

/**
 * Beneficial status effect granted when soaking in Spirit Spring water matching the cultivator's realm:
 * accelerates ArcanaPower Qi/mana recovery and active cultivation experience gain.
 */
public final class SpiritSpringNourishEffect extends MobEffect {
    public SpiritSpringNourishEffect() {
        super(MobEffectCategory.BENEFICIAL, 0x3CE8D4);
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return duration % 20 == 0;
    }

    @Override
    public boolean applyEffectTick(ServerLevel level, LivingEntity entity, int amplifier) {
        if (entity instanceof Player player) {
            double maxMana = ArcanaPower.getMaxMana(player);
            double curMana = ArcanaPower.getCurrentMana(player);
            double regen = 1.6D + amplifier * 0.95D;
            if (curMana < maxMana) {
                ArcanaPower.setCurrentMana(player, Math.min(maxMana, curMana + regen));
            }
            if ((entity.tickCount % 40) == 0 && (ArcanaPower.getRealm(player) > 0 || ArcanaPower.getCultivationLevel(player) > 0)) {
                CultivationFocus focus = ArcanaPower.getCultivationFocus(player);
                ArcanaPower.addCultivationExperience(player, focus, 1 + Math.min(3, amplifier));
            }
        }
        level.sendParticles(XuanhuanParticles.QI_MOTE.get(),
                entity.getX(), entity.getY() + 0.9D, entity.getZ(),
                2, 0.35D, 0.35D, 0.35D, 0.02D);
        return true;
    }
}
