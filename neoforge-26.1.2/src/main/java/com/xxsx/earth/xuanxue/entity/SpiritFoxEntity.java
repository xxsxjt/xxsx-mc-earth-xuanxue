package com.xxsx.earth.xuanxue.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.level.Level;

public final class SpiritFoxEntity extends ContractableSpiritBeastEntity {
    public SpiritFoxEntity(EntityType<? extends SpiritFoxEntity> type, Level level) {
        super(type, level, Kind.SPIRIT_FOX);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return ContractableSpiritBeastEntity.createAttributes(14.0D, 0.34D, 3.0D, 1.0D, 0.05D);
    }
}
