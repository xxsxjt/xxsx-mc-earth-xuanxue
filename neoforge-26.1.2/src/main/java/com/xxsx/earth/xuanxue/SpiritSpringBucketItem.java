package com.xxsx.earth.xuanxue;

import com.xxsx.earth.core.api.v1.EarthCoreApi;
import com.xxsx.earth.core.component.SolutionComposition;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import org.jetbrains.annotations.Nullable;

/**
 * Bucket of Spirit Spring fluid carrying EarthCore SolutionComposition so pouring back into the
 * world preserves exact Qi concentration (25% / 50% / 75% / 100%).
 */
public final class SpiritSpringBucketItem extends BucketItem {
    public SpiritSpringBucketItem(Fluid content, Properties properties) {
        super(content, properties);
    }

    @Override
    public void checkExtraContent(@Nullable LivingEntity entity, Level level, ItemStack stack, BlockPos pos) {
        super.checkExtraContent(entity, level, stack, pos);
        if (level.isClientSide()) {
            return;
        }
        BlockState placed = level.getBlockState(pos);
        if (placed.getBlock() instanceof SpiritSpringFluidBlock) {
            int conc = SpiritSpringFluidBlock.MAX_CONCENTRATION;
            var opt = EarthCoreApi.fluids().readComposition(stack);
            if (opt.isPresent()) {
                int permille = opt.get().concentrationOf(SpiritSpringFluidBlock.SOLUTE_SPIRIT_QI);
                conc = SpiritSpringFluidBlock.concentrationFromPermille(permille);
            }
            if (placed.getValue(SpiritSpringFluidBlock.CONCENTRATION) != conc) {
                level.setBlock(pos, placed.setValue(SpiritSpringFluidBlock.CONCENTRATION, conc), Block.UPDATE_ALL);
            }
            level.scheduleTick(pos, placed.getBlock(), 10);
        }
    }

    public static ItemStack withConcentration(int concentration) {
        ItemStack stack = new ItemStack(XuanhuanEarth.SPIRIT_SPRING_BUCKET.get());
        int clamped = Math.max(1, Math.min(SpiritSpringFluidBlock.MAX_CONCENTRATION, concentration));
        EarthCoreApi.fluids().writeComposition(stack, SolutionComposition.singleSolute(
                "minecraft:water",
                SpiritSpringFluidBlock.SOLUTE_SPIRIT_QI,
                clamped * 250,
                1000,
                295
        ));
        return stack;
    }
}
