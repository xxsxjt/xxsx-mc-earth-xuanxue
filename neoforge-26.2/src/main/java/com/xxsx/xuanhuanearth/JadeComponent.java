package com.xxsx.xuanhuanearth;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;

/**
 * X-042 Jade container state for jade_slip, jade_ward_amulet, and jade_array_plate.
 * Nephrite capacity: 48 AEU (capture 0.85, release 0.90); Jadeite capacity: 36 AEU (capture 0.80, release 0.95).
 */
public record JadeComponent(String manualId, double chargeAeu, double stability, BlockPos linkedArray, int radiusBonus) {
    public static final double NEPHRITE_CAPACITY_AEU = 48.0D;
    public static final double JADEITE_CAPACITY_AEU = 36.0D;
    public static final double NEPHRITE_CAPTURE = 0.85D;
    public static final double NEPHRITE_RELEASE = 0.90D;
    public static final double JADEITE_CAPTURE = 0.80D;
    public static final double JADEITE_RELEASE = 0.95D;

    public static final JadeComponent EMPTY = new JadeComponent("", 0.0D, 1.0D, BlockPos.ZERO, 0);

    public static final Codec<JadeComponent> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.string(0, 128).optionalFieldOf("manual_id", "").forGetter(JadeComponent::manualId),
            Codec.doubleRange(0.0D, NEPHRITE_CAPACITY_AEU).optionalFieldOf("charge_aeu", 0.0D).forGetter(JadeComponent::chargeAeu),
            Codec.doubleRange(0.0D, 1.0D).optionalFieldOf("stability", 1.0D).forGetter(JadeComponent::stability),
            BlockPos.CODEC.optionalFieldOf("linked_array", BlockPos.ZERO).forGetter(JadeComponent::linkedArray),
            Codec.intRange(0, 2).optionalFieldOf("radius_bonus", 0).forGetter(JadeComponent::radiusBonus)
    ).apply(instance, JadeComponent::new));

    public JadeComponent {
        manualId = manualId == null ? "" : manualId;
        chargeAeu = Double.isFinite(chargeAeu) ? Math.max(0.0D, Math.min(NEPHRITE_CAPACITY_AEU, chargeAeu)) : 0.0D;
        stability = Double.isFinite(stability) ? Math.max(0.0D, Math.min(1.0D, stability)) : 1.0D;
        linkedArray = linkedArray == null ? BlockPos.ZERO : linkedArray;
        radiusBonus = Math.max(0, Math.min(2, radiusBonus));
    }

    public static JadeComponent read(ItemStack stack) {
        JadeComponent val = stack.get(XuanhuanEarth.JADE_COMPONENT.get());
        return val == null ? EMPTY : val;
    }

    public JadeComponent withCharge(double nextCharge, double nextStability) {
        return new JadeComponent(manualId, nextCharge, nextStability, linkedArray, radiusBonus);
    }

    public JadeComponent withManual(String id) {
        return new JadeComponent(id, chargeAeu, stability, linkedArray, radiusBonus);
    }

    public JadeComponent withArray(BlockPos pos, int bonus) {
        return new JadeComponent(manualId, chargeAeu, stability, pos, bonus);
    }
}
