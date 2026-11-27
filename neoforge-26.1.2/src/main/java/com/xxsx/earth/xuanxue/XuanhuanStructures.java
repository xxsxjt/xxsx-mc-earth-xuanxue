package com.xxsx.earth.xuanxue;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Optional;

public final class XuanhuanStructures {
    private XuanhuanStructures() {
    }

    public static int tierFor(XuanhuanMachineBlock.Kind kind, Level level, BlockPos pos) {
        if (kind == XuanhuanMachineBlock.Kind.SPIRIT_ARRAY_CORE && isFormalSpiritArray(level, pos)) {
            return 1;
        }
        return 0;
    }

    public static boolean isFormalSpiritArray(Level level, BlockPos center) {
        if (!is(level, center, XuanhuanEarth.SPIRIT_ARRAY_CORE.get())) {
            return false;
        }
        return is(level, center.north(), XuanhuanEarth.SPIRIT_SOIL.get())
                && is(level, center.south(), XuanhuanEarth.SPIRIT_SOIL.get())
                && is(level, center.east(), XuanhuanEarth.SPIRIT_SOIL.get())
                && is(level, center.west(), XuanhuanEarth.SPIRIT_SOIL.get())
                && isSpiritFocus(level, center.north().east())
                && isSpiritFocus(level, center.north().west())
                && isSpiritFocus(level, center.south().east())
                && isSpiritFocus(level, center.south().west());
    }

    public static Optional<BlockPos> findFormalSpiritArrayCenter(Level level, BlockPos clicked) {
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                BlockPos center = clicked.offset(dx, 0, dz);
                if (isFormalSpiritArray(level, center)) {
                    return Optional.of(center);
                }
            }
        }
        return Optional.empty();
    }

    private static boolean isSpiritFocus(Level level, BlockPos pos) {
        Block block = level.getBlockState(pos).getBlock();
        return block == XuanhuanEarth.SPIRIT_VEIN_NODE.get()
                || block == XuanhuanEarth.SPIRIT_SPRING_STONE.get();
    }

    private static boolean is(Level level, BlockPos pos, Block block) {
        BlockState state = level.getBlockState(pos);
        return state.getBlock() == block;
    }
}
