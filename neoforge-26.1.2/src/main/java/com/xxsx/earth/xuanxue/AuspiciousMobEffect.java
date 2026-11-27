package com.xxsx.earth.xuanxue;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/**
 * X-072 Auspicious omen effect (earth_xuanxue:auspicious):
 * +0.05 alchemy furnace pill success rate, and talisman table crafting costs 0 brush durability.
 */
public class AuspiciousMobEffect extends MobEffect {
    public AuspiciousMobEffect() {
        super(MobEffectCategory.BENEFICIAL, 0xE6B84C);
    }
}
