package com.xxsx.earth.xuanxue;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.VegetationBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Dedicated Spirit Herb plant growing naturally on Spirit Soil terraces in cultivation settlements
 * (replaces the placeholder vanilla fern). Harvests into Spirit Herb & Spirit Grass.
 */
public final class SpiritHerbBlock extends VegetationBlock {
    public static final MapCodec<SpiritHerbBlock> CODEC = simpleCodec(SpiritHerbBlock::new);
    private static final VoxelShape SHAPE = Block.box(2.0D, 0.0D, 2.0D, 14.0D, 13.0D, 14.0D);

    public SpiritHerbBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends VegetationBlock> codec() {
        return CODEC;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
        return state.is(XuanhuanEarth.SPIRIT_SOIL.get()) || state.is(BlockTags.DIRT);
    }
}
