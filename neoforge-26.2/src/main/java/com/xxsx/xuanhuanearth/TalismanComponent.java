package com.xxsx.xuanhuanearth;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.item.ItemStack;

/** Persistent sealed charge of a drawn talisman. */
public record TalismanComponent(double chargeAeu, int schemaVersion) {
    public static final int CURRENT_SCHEMA = TalismanCharge.SCHEMA_VERSION;
    public static final Codec<TalismanComponent> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.DOUBLE.fieldOf("charge_aeu").forGetter(TalismanComponent::chargeAeu),
            Codec.INT.optionalFieldOf("schema_version", CURRENT_SCHEMA).forGetter(TalismanComponent::schemaVersion)
    ).apply(instance, TalismanComponent::new));
    public static final TalismanComponent EMPTY = new TalismanComponent(0.0D, CURRENT_SCHEMA);

    public TalismanComponent {
        chargeAeu = TalismanCharge.clamp(chargeAeu);
        schemaVersion = Math.max(1, schemaVersion);
    }

    public static TalismanComponent read(ItemStack stack) {
        TalismanComponent value = stack.get(XuanhuanEarth.TALISMAN_CHARGE);
        return value == null ? EMPTY : value;
    }

    /** A talisman is only usable when the stack still carries sealed field charge. */
    public static boolean isSealed(ItemStack stack) {
        boolean isTalisman = stack.is(XuanhuanEarth.BASIC_TALISMAN.get())
                || stack.is(XuanhuanEarth.FIRE_TALISMAN.get())
                || stack.is(XuanhuanEarth.WIND_TALISMAN.get())
                || stack.is(XuanhuanEarth.SEAL_TALISMAN.get());
        return isTalisman
                && TalismanCharge.isCharged(read(stack).chargeAeu());
    }

    /** Returns a fresh stack that carries a freshly drawn sealed charge. */
    public static ItemStack drawn(ItemStack stack) {
        stack.set(XuanhuanEarth.TALISMAN_CHARGE,
                new TalismanComponent(TalismanCharge.CRAFTED_CHARGE_AEU, CURRENT_SCHEMA));
        return stack;
    }
}
