package com.xxsx.earth.xuanxue;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;

public final class Spirituality {
    private Spirituality() {
    }

    public static int measure(Level level, BlockPos pos) {
        return reading(level, pos).value();
    }

    public static ArcanaChunkField.Reading reading(Level level, BlockPos pos) {
        return ArcanaChunkField.read(level, pos);
    }

    /** Draws an explicit AEU quantity out of the local field ledger. */
    public static void consumeField(Level level, BlockPos pos, double aeu) {
        ArcanaChunkField.consumeField(level, pos, aeu);
    }

    /** Draws a dimensionless field-level quantity; used by stations that budget in levels. */
    public static void consumeFieldLevels(Level level, BlockPos pos, double levels) {
        ArcanaChunkField.consumeFieldLevels(level, pos, levels);
    }

    public static Component gradeName(int value) {
        if (value >= 75) {
            return Component.translatable("spirituality.earth_xuanxue.grade.rich");
        }
        if (value >= 50) {
            return Component.translatable("spirituality.earth_xuanxue.grade.active");
        }
        if (value >= 25) {
            return Component.translatable("spirituality.earth_xuanxue.grade.thin");
        }
        return Component.translatable("spirituality.earth_xuanxue.grade.exhausted");
    }
}
