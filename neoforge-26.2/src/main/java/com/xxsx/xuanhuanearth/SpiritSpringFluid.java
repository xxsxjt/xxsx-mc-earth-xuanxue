package com.xxsx.xuanhuanearth;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.neoforged.neoforge.common.SoundActions;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;
import net.neoforged.neoforge.fluids.FluidType;

/**
 * Real spiritual spring fluid: translucent glowing aqua liquid that NEVER forms infinite sources
 * ({@link #canConvertToSource} always returns false). One source block equals 1 bucket or 3 glass bottles.
 */
public abstract class SpiritSpringFluid extends BaseFlowingFluid {
    protected SpiritSpringFluid(Properties properties) {
        super(properties);
    }

    public static FluidType.Properties fluidTypeProperties() {
        return FluidType.Properties.create()
                .descriptionId("block.xuanhuan_earth.spirit_spring")
                .canConvertToSource(false)
                .canSwim(true)
                .canDrown(false)
                .canPushEntity(true)
                .canExtinguish(true)
                .supportsBoating(true)
                .canHydrate(true)
                .lightLevel(6)
                .density(950)
                .viscosity(900)
                .temperature(295)
                .sound(SoundActions.BUCKET_FILL, SoundEvents.BUCKET_FILL)
                .sound(SoundActions.BUCKET_EMPTY, SoundEvents.BUCKET_EMPTY)
                .sound(SoundActions.FLUID_VAPORIZE, SoundEvents.FIRE_EXTINGUISH);
    }

    public static Properties fluidProperties() {
        return new Properties(
                XuanhuanEarth.SPIRIT_SPRING_FLUID_TYPE,
                XuanhuanEarth.SPIRIT_SPRING_FLUID,
                XuanhuanEarth.SPIRIT_SPRING_FLOWING
        )
                .slopeFindDistance(3)
                .levelDecreasePerBlock(2)
                .tickRate(6)
                .explosionResistance(100.0F)
                .bucket(XuanhuanEarth.SPIRIT_SPRING_BUCKET)
                .block(XuanhuanEarth.SPIRIT_SPRING_BLOCK);
    }

    @Override
    protected boolean canConvertToSource(ServerLevel level) {
        return false;
    }

    @Override
    public boolean canConvertToSource(FluidState state, ServerLevel level, BlockPos pos) {
        return false;
    }

    @Override
    protected int getDropOff(LevelReader level) {
        return 2;
    }

    public static final class Source extends SpiritSpringFluid {
        public Source(Properties properties) {
            super(properties);
        }

        @Override
        public int getAmount(FluidState state) {
            return 8;
        }

        @Override
        public boolean isSource(FluidState state) {
            return true;
        }
    }

    public static final class Flowing extends SpiritSpringFluid {
        public Flowing(Properties properties) {
            super(properties);
        }

        @Override
        protected void createFluidStateDefinition(StateDefinition.Builder<Fluid, FluidState> builder) {
            super.createFluidStateDefinition(builder);
            builder.add(LEVEL);
        }

        @Override
        public int getAmount(FluidState state) {
            return state.getValue(LEVEL);
        }

        @Override
        public boolean isSource(FluidState state) {
            return false;
        }
    }
}
