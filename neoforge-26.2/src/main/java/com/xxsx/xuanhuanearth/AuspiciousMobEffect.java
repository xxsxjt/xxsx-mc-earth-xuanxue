package com.xxsx.xuanhuanearth;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/**
 * X-072 Auspicious omen effect (xuanhuan_earth:auspicious):
 * +0.05 alchemy furnace pill success rate, and talisman table crafting costs 0 brush durability.
 */
public class AuspiciousMobEffect extends MobEffect {
    public AuspiciousMobEffect() {
        super(MobEffectCategory.BENEFICIAL, 0xE6B84C);
    }
}
