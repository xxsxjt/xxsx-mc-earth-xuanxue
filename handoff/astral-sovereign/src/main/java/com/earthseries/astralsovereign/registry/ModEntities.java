package com.earthseries.astralsovereign.registry;

import com.earthseries.astralsovereign.AstralSovereignMod;
import com.earthseries.astralsovereign.entity.AstralSovereignEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModEntities {
    public static final DeferredRegister.Entities ENTITY_TYPES =
            DeferredRegister.createEntities(AstralSovereignMod.MOD_ID);

    public static final DeferredHolder<EntityType<?>, EntityType<AstralSovereignEntity>> ASTRAL_SOVEREIGN =
            ENTITY_TYPES.registerEntityType(
                    "astral_sovereign",
                    AstralSovereignEntity::new,
                    MobCategory.MONSTER,
                    builder -> builder
                            .sized(1.4F, 3.6F)
                            .eyeHeight(3.05F)
                            .fireImmune()
                            .clientTrackingRange(12)
            );
}
