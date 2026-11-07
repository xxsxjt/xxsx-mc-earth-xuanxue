package com.xxsx.xuanhuanearth;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.level.block.BreakBlockEvent;

public final class ArcanaEvents {
    private static final String ARCANA_PREFIX = "earth_arcana.";

    private ArcanaEvents() {
    }

    public static void onBlockBreak(BreakBlockEvent event) {
        if (!(event.getLevel() instanceof ServerLevel level)) {
            return;
        }
        BlockPos pos = event.getPos();
        BlockState state = event.getState();
        if (state.is(Blocks.FERN) || state.is(Blocks.LARGE_FERN) || state.is(Blocks.SHORT_GRASS) || state.is(Blocks.TALL_GRASS)) {
            boolean onSpiritSoil = level.getBlockState(pos.below()).is(XuanhuanEarth.SPIRIT_SOIL.get());
            int field = Spirituality.measure(level, pos);
            if (onSpiritSoil || (field >= 40 && level.getRandom().nextFloat() < 0.14F)) {
                Block.popResource(level, pos, new ItemStack(
                        onSpiritSoil && level.getRandom().nextBoolean()
                                ? XuanhuanEarth.SPIRIT_HERB.get()
                                : XuanhuanEarth.SPIRIT_GRASS.get()));
            }
        } else if (state.is(BlockTags.REDSTONE_ORES) && level.getRandom().nextFloat() < 0.25F) {
            Block.popResource(level, pos, new ItemStack(XuanhuanEarth.CINNABAR_SPIRIT_INK.get()));
        } else if (state.is(BlockTags.EMERALD_ORES) && level.getRandom().nextFloat() < 0.35F) {
            Block.popResource(level, pos, new ItemStack(XuanhuanEarth.SPIRIT_CRYSTAL_SHARD.get()));
        }
    }

    public static void onPlayerClone(PlayerEvent.Clone event) {
        CompoundTag source = event.getOriginal().getPersistentData();
        CompoundTag target = event.getEntity().getPersistentData();
        ArcanaPower.migrateLegacyData(source);
        ArcanaPower.migrateLegacyData(target);
        for (String key : source.keySet()) {
            if (!key.startsWith(ARCANA_PREFIX)) {
                continue;
            }
            Tag value = source.get(key);
            if (value != null) {
                target.put(key, value.copy());
            }
        }
    }
}
