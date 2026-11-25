package com.xxsx.earth.xuanxue.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.level.Level;

public final class StonehornGoatEntity extends ContractableSpiritBeastEntity {
    public StonehornGoatEntity(EntityType<? extends StonehornGoatEntity> type, Level level) {
        super(type, level, Kind.STONEHORN_GOAT);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return ContractableSpiritBeastEntity.createAttributes(30.0D, 0.27D, 6.0D, 7.0D, 0.45D);
    }
}
