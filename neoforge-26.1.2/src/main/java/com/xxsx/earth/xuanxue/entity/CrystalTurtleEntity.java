package com.xxsx.earth.xuanxue.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.level.Level;

public final class CrystalTurtleEntity extends ContractableSpiritBeastEntity {
    public CrystalTurtleEntity(EntityType<? extends CrystalTurtleEntity> type, Level level) {
        super(type, level, Kind.CRYSTAL_TURTLE);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return ContractableSpiritBeastEntity.createAttributes(34.0D, 0.22D, 4.0D, 10.0D, 0.65D);
    }
}
