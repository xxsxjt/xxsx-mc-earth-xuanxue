package com.xxsx.earth.xuanxue;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.item.ItemStack;

/** Persistent charge pool of a spirit iron implement. */
public record ImplementComponent(double charge, int schemaVersion) {
    public static final int CURRENT_SCHEMA = 1;
    public static final Codec<ImplementComponent> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.DOUBLE.fieldOf("charge").forGetter(ImplementComponent::charge),
            Codec.INT.optionalFieldOf("schema_version", CURRENT_SCHEMA)
                    .forGetter(ImplementComponent::schemaVersion)
    ).apply(instance, ImplementComponent::new));
    public static final ImplementComponent EMPTY = new ImplementComponent(0.0D, CURRENT_SCHEMA);

    public ImplementComponent {
        charge = !Double.isFinite(charge) ? 0.0D : Math.max(0.0D, charge);
        schemaVersion = Math.max(1, schemaVersion);
    }

    public static ImplementComponent read(ItemStack stack) {
        ImplementComponent value = stack.get(XuanhuanEarth.IMPLEMENT_CHARGE);
        return value == null ? EMPTY : value;
    }

    public ImplementComponent withCharge(double value) {
        return new ImplementComponent(value, CURRENT_SCHEMA);
    }
}
