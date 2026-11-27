package com.xxsx.earth.xuanxue.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.level.Level;

public final class WindWolfEntity extends ContractableSpiritBeastEntity {
    public WindWolfEntity(EntityType<? extends WindWolfEntity> type, Level level) {
        super(type, level, Kind.WIND_WOLF);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return ContractableSpiritBeastEntity.createAttributes(22.0D, 0.36D, 5.0D, 2.0D, 0.10D);
    }
}
