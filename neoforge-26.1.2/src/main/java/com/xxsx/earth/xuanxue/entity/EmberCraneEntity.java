package com.xxsx.earth.xuanxue.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.level.Level;

public final class EmberCraneEntity extends ContractableSpiritBeastEntity {
    public EmberCraneEntity(EntityType<? extends EmberCraneEntity> type, Level level) {
        super(type, level, Kind.EMBER_CRANE);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return ContractableSpiritBeastEntity.createAttributes(18.0D, 0.32D, 4.0D, 1.0D, 0.05D);
    }
}
