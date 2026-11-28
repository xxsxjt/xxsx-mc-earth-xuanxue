package com.xxsx.earth.xuanxue.settlement;

import com.mojang.serialization.Codec;
import com.xxsx.earth.xuanxue.TalismanComponent;
import com.xxsx.earth.core.api.v1.EarthCoreApi;
import com.xxsx.earth.xuanxue.XuanhuanEarth;
import com.xxsx.earth.xuanxue.entity.ContractableSpiritBeastEntity;
import com.xxsx.earth.xuanxue.entity.CultivationSettlerEntity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.entity.SignText;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

import java.util.Optional;

/**
 * Generates one xuanhuan settlement.
 *
 * <p>Every site rolls its own palette, layout and prop set from the world seed plus its own
 * origin, so repeated visits to the same type do not produce the same building. The three
 * types still differ in what they contain: markets get stalls, villages get a spring and
 * terraces, outposts get walls and a gate.</p>
 */
public final class XuanhuanSettlementFeature extends Feature<NoneFeatureConfiguration> {
    private static final int FLAGS = 2;
    private final Type type;

    public XuanhuanSettlementFeature(Codec<NoneFeatureConfiguration> codec, Type type) {
        super(codec);
        this.type = type;
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        int radius = type.radius();
        BlockPos center = resolveGround(level, context.origin());
        if (center == null || !isSuitableSite(level, center, radius)) {
            return false;
        }

        // The feature RNG is seeded per placement, so two sites generated in the same tick
        // can receive the same roll. Mix the world seed with the exact origin instead so
        // every settlement gets its own palette and layout.
        long mixed = level.getSeed()
                ^ ((long) center.getX() * 0x9E3779B97F4A7C15L)
                ^ ((long) center.getY() * 0xC2B2AE3D27D4EB4FL)
                ^ ((long) center.getZ() * 0x165667B19E3779F9L)
                ^ type.ordinal();
        RandomSource random = RandomSource.create(mixed);
        SettlementStyle.Theme theme = type.themeFor(level, center);
        SettlementStyle.Palette palette = SettlementStyle.paletteFor(theme, random);
        SettlementStyle.Layout layout = SettlementStyle.layoutFor(random);

        clearSite(level, center, radius);
        switch (type) {
            case WANDERING_MARKET -> buildMarket(level, center, radius, palette, layout, random);
            case SPIRIT_SPRING_VILLAGE -> buildVillage(level, center, radius, palette, layout, random);
            case SECT_OUTPOST -> buildOutpost(level, center, radius, palette, layout, random);
            case MOUNTAIN_SPIRIT_SHRINE -> buildShrine(level, center, radius, palette, layout, random);
            case SPIRIT_BEAST_KEEPER_CAMP -> buildKeeperCamp(level, center, radius, palette, layout, random);
        }

        // Place authentic settlement signpost + optional Waystones compat (no artificial anchor block)
        BlockPos markerPos = placeSettlementSignpostAndCompat(level, center, radius, type);
        spawnSettlementResidents(level, markerPos, type, spawnOffsets(layout), random);
        return true;
    }

    private static BlockPos resolveGround(WorldGenLevel level, BlockPos start) {
        BlockPos cursor = start;
        int minY = level.getMinY() + 4;
        while (cursor.getY() > minY) {
            BlockState below = level.getBlockState(cursor.below());
            if (!below.getFluidState().isEmpty()
                    || below.is(Blocks.WATER)
                    || below.is(Blocks.LAVA)
                    || below.is(Blocks.ICE)
                    || below.is(Blocks.FROSTED_ICE)
                    || below.is(Blocks.KELP)
                    || below.is(Blocks.KELP_PLANT)
                    || below.is(Blocks.SEAGRASS)
                    || below.is(Blocks.TALL_SEAGRASS)) {
                return null;
            }
            if (isFoliageOrReplaceable(below)) {
                cursor = cursor.below();
                continue;
            }
            break;
        }
        BlockState ground = level.getBlockState(cursor.below());
        if (ground.isAir() || !ground.getFluidState().isEmpty() || isFoliageOrReplaceable(ground)) {
            return null;
        }
        return cursor;
    }

    private static boolean isFoliageOrReplaceable(BlockState state) {
        return state.isAir()
                || state.canBeReplaced()
                || state.is(BlockTags.LEAVES)
                || state.is(BlockTags.LOGS)
                || state.is(BlockTags.FLOWERS)
                || state.is(BlockTags.SAPLINGS)
                || state.is(Blocks.SNOW)
                || state.is(Blocks.VINE)
                || state.is(Blocks.BAMBOO);
    }

    private static boolean isSuitableSite(WorldGenLevel level, BlockPos center, int radius) {
        var centerBiome = level.getBiome(center);
        if (centerBiome.is(BiomeTags.IS_OCEAN) || centerBiome.is(BiomeTags.IS_RIVER)) {
            return false;
        }
        int minSampleY = center.getY();
        int maxSampleY = center.getY();
        int[][] offsets = {
                {0, 0},
                {-radius, -radius}, {radius, -radius}, {-radius, radius}, {radius, radius},
                {-radius, 0}, {radius, 0}, {0, -radius}, {0, radius}
        };
        for (int[] off : offsets) {
            BlockPos probeStart = new BlockPos(center.getX() + off[0], center.getY() + 6, center.getZ() + off[1]);
            var biome = level.getBiome(probeStart);
            if (biome.is(BiomeTags.IS_OCEAN) || biome.is(BiomeTags.IS_RIVER)) {
                return false;
            }
            BlockPos ground = resolveGround(level, probeStart);
            if (ground == null) {
                return false;
            }
            for (int dy = -1; dy <= 2; dy++) {
                BlockState check = level.getBlockState(ground.offset(0, dy, 0));
                if (!check.getFluidState().isEmpty() || check.is(Blocks.WATER) || check.is(Blocks.LAVA)) {
                    return false;
                }
            }
            minSampleY = Math.min(minSampleY, ground.getY());
            maxSampleY = Math.max(maxSampleY, ground.getY());
        }
        return (maxSampleY - minSampleY) <= 3;
    }

    private static void clearSite(WorldGenLevel level, BlockPos center, int radius) {
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                for (int y = 0; y <= 7; y++) {
                    level.setBlock(center.offset(x, y, z), Blocks.AIR.defaultBlockState(), FLAGS);
                }
            }
        }
    }

    private static void buildMarket(WorldGenLevel level, BlockPos center, int radius,
                                    SettlementStyle.Palette palette, SettlementStyle.Layout layout,
                                    RandomSource random) {
        pave(level, center, radius, palette);
        switch (layout) {
            case CROSS -> {
                stall(level, center.offset(-4, 0, -3), palette, random);
                stall(level, center.offset(4, 0, -3), palette, random);
                stall(level, center.offset(-4, 0, 3), palette, random);
                campfire(level, center.offset(0, 0, 0));
            }
            case STREET -> {
                for (int i = -2; i <= 2; i++) {
                    stall(level, center.offset(i * 4, 0, -3), palette, random);
                    stall(level, center.offset(i * 4, 0, 3), palette, random);
                }
                SettlementStyle.path(level, center.offset(-8, 1, 0), center.offset(8, 1, 0),
                        palette.path());
            }
            case HALL -> {
                SettlementStyle.hut(level, center.offset(0, 0, -3), palette, 4, 4, true);
                stall(level, center.offset(-5, 0, 3), palette, random);
                stall(level, center.offset(5, 0, 3), palette, random);
            }
            case RING -> {
                for (int i = 0; i < 5; i++) {
                    double angle = Math.PI * 2 * i / 5;
                    stall(level, center.offset((int) Math.round(Math.cos(angle) * 5), 0,
                            (int) Math.round(Math.sin(angle) * 5)), palette, random);
                }
                campfire(level, center);
            }
        }
        tradePost(level, center.offset(0, 0, -1), palette, random);
        SettlementStyle.lanternRing(level, center, radius - 1, palette);
        scatterProps(level, center, radius - 1, random);
    }

    private static void buildVillage(WorldGenLevel level, BlockPos center, int radius,
                                     SettlementStyle.Palette palette, SettlementStyle.Layout layout,
                                     RandomSource random) {
        pave(level, center, radius, palette);
        // The spring is the reason the village exists, so it is always present.
        spring(level, center, palette);
        switch (layout) {
            case CROSS -> {
                SettlementStyle.hut(level, center.offset(-4, 0, -3), palette, 2, 3, true);
                stock(level, center.offset(-4, 0, -4),
                        new ItemStack(XuanhuanEarth.SPIRIT_HERB.get(), 3 + random.nextInt(4)),
                        new ItemStack(XuanhuanEarth.QI_RECOVERY_PILL.get(), 1 + random.nextInt(2)),
                        new ItemStack(XuanhuanEarth.GRAINFAST_PILL.get(), 1 + random.nextInt(2)),
                        new ItemStack(XuanhuanEarth.BASIC_TALISMAN.get(), 2),
                        new ItemStack(XuanhuanEarth.SPIRIT_CRYSTAL_SHARD.get(), 3 + random.nextInt(4)));
                SettlementStyle.hut(level, center.offset(4, 0, -3), palette, 2, 3, true);
                stock(level, center.offset(4, 0, -4),
                        new ItemStack(XuanhuanEarth.CULTIVATION_JOURNAL.get(), 1),
                        new ItemStack(XuanhuanEarth.QI_GUIDING_MANUAL.get(), 1),
                        new ItemStack(XuanhuanEarth.SPIRIT_SPRING_BOTTLE.get(), 2 + random.nextInt(3)),
                        new ItemStack(XuanhuanEarth.SPIRIT_GRASS.get(), 6 + random.nextInt(6)));
                SettlementStyle.hut(level, center.offset(-4, 0, 3), palette, 2, 3, true);
                SettlementStyle.hut(level, center.offset(4, 0, 3), palette, 2, 3, true);
            }
            case STREET -> {
                for (int i = -1; i <= 1; i++) {
                    SettlementStyle.hut(level, center.offset(i * 5, 0, -4), palette, 2, 3, true);
                    SettlementStyle.hut(level, center.offset(i * 5, 0, 4), palette, 2, 3, true);
                }
                stock(level, center.offset(0, 0, -5),
                        new ItemStack(XuanhuanEarth.CULTIVATION_JOURNAL.get(), 1),
                        new ItemStack(XuanhuanEarth.QI_RECOVERY_PILL.get(), 2),
                        new ItemStack(XuanhuanEarth.GRAINFAST_PILL.get(), 2),
                        new ItemStack(XuanhuanEarth.BASIC_TALISMAN.get(), 2),
                        new ItemStack(XuanhuanEarth.SPIRIT_CRYSTAL_SHARD.get(), 4));
            }
            case HALL -> {
                SettlementStyle.hut(level, center.offset(0, 0, -4), palette, 4, 4, true);
                stock(level, center.offset(0, 0, -6),
                        new ItemStack(XuanhuanEarth.CULTIVATION_JOURNAL.get(), 1),
                        new ItemStack(XuanhuanEarth.QI_GUIDING_MANUAL.get(), 1),
                        new ItemStack(XuanhuanEarth.QI_RECOVERY_PILL.get(), 2),
                        new ItemStack(XuanhuanEarth.GRAINFAST_PILL.get(), 2),
                        new ItemStack(XuanhuanEarth.BASIC_TALISMAN.get(), 2),
                        new ItemStack(XuanhuanEarth.SPIRIT_CRYSTAL_SHARD.get(), 5));
                SettlementStyle.hut(level, center.offset(-5, 0, 3), palette, 2, 3, true);
                SettlementStyle.hut(level, center.offset(5, 0, 3), palette, 2, 3, true);
            }
            case RING -> {
                for (int i = 0; i < 5; i++) {
                    double angle = Math.PI * 2 * i / 5;
                    SettlementStyle.hut(level, center.offset((int) Math.round(Math.cos(angle) * 5), 0,
                            (int) Math.round(Math.sin(angle) * 5)), palette, 2, 3, true);
                }
                stock(level, center.offset(5, 0, 0),
                        new ItemStack(XuanhuanEarth.CULTIVATION_JOURNAL.get(), 1),
                        new ItemStack(XuanhuanEarth.QI_RECOVERY_PILL.get(), 2),
                        new ItemStack(XuanhuanEarth.GRAINFAST_PILL.get(), 2),
                        new ItemStack(XuanhuanEarth.BASIC_TALISMAN.get(), 2),
                        new ItemStack(XuanhuanEarth.SPIRIT_CRYSTAL_SHARD.get(), 4));
            }
        }
        terrace(level, center.offset(-radius, 0, -radius + 1), palette);
        terrace(level, center.offset(radius - 2, 0, radius - 3), palette);
        workshop(level, center.offset(radius - 3, 0, -radius + 2), palette, random);
        SettlementStyle.lanternRing(level, center, radius - 1, palette);
        scatterProps(level, center, radius - 1, random);
    }

    private static void buildOutpost(WorldGenLevel level, BlockPos center, int radius,
                                     SettlementStyle.Palette palette, SettlementStyle.Layout layout,
                                     RandomSource random) {
        pave(level, center, radius, palette);
        // A wall ring with one gate is what makes an outpost an outpost.
        wallRing(level, center, radius, palette);
        gate(level, center.offset(0, 0, radius), palette);
        switch (layout) {
            case CROSS -> {
                SettlementStyle.hut(level, center.offset(-4, 0, -3), palette, 2, 4, true);
                SettlementStyle.hut(level, center.offset(4, 0, -3), palette, 2, 4, true);
                trainingYard(level, center.offset(0, 0, 1), palette);
            }
            case STREET -> {
                SettlementStyle.hut(level, center.offset(-4, 0, -3), palette, 2, 4, true);
                SettlementStyle.hut(level, center.offset(4, 0, -3), palette, 2, 4, true);
                SettlementStyle.hut(level, center.offset(0, 0, -5), palette, 2, 4, true);
            }
            case HALL -> {
                SettlementStyle.hut(level, center.offset(0, 0, -4), palette, 4, 5, true);
                trainingYard(level, center.offset(-4, 0, 2), palette);
                trainingYard(level, center.offset(4, 0, 2), palette);
            }
            case RING -> {
                for (int i = 0; i < 4; i++) {
                    double angle = Math.PI * 2 * i / 4 + Math.PI / 4;
                    SettlementStyle.hut(level, center.offset((int) Math.round(Math.cos(angle) * 4), 0,
                            (int) Math.round(Math.sin(angle) * 4)), palette, 2, 4, true);
                }
            }
        }
        workshop(level, center.offset(-radius + 3, 0, radius - 3), palette, random);
        SettlementStyle.lanternRing(level, center, radius - 1, palette);
        scatterProps(level, center, radius - 1, random);
    }

    private static void buildShrine(WorldGenLevel level, BlockPos center, int radius,
                                    SettlementStyle.Palette palette, SettlementStyle.Layout layout,
                                    RandomSource random) {
        pave(level, center, radius, palette);
        // A stepped stone platform, an altar on top, and incense braziers at the corners.
        for (int step = 0; step < 3; step++) {
            int r = 3 - step;
            for (int x = -r; x <= r; x++) {
                for (int z = -r; z <= r; z++) {
                    level.setBlock(center.offset(x, step, z), palette.wall(), FLAGS);
                }
            }
        }
        // The altar is a complete formal array on the top step, not a lone core: a core with
        // no soil ring and no corner focus can never form, so it would be a dead block.
        formalArray(level, center.offset(0, 2, 0));
        for (int x : new int[]{-2, 2}) {
            for (int z : new int[]{-2, 2}) {
                level.setBlock(center.offset(x, 1, z), Blocks.CAMPFIRE.defaultBlockState(), FLAGS);
                level.setBlock(center.offset(x, 2, z), palette.light(), FLAGS);
            }
        }
        SettlementStyle.hut(level, center.offset(0, 0, -radius + 3), palette, 2, 3, true);
        stock(level, center.offset(0, 0, -radius + 3),
                new ItemStack(XuanhuanEarth.SPIRIT_CRYSTAL_SHARD.get(), 6),
                new ItemStack(XuanhuanEarth.SPIRIT_SPRING_BOTTLE.get(), 4),
                new ItemStack(XuanhuanEarth.QI_RECOVERY_PILL.get(), 2),
                new ItemStack(XuanhuanEarth.BASIC_TALISMAN.get(), 2),
                new ItemStack(XuanhuanEarth.CULTIVATION_JOURNAL.get(), 1));
        SettlementStyle.plantBed(level, center.offset(-radius + 2, 0, radius - 2), 2, palette);
        SettlementStyle.lanternRing(level, center, radius - 1, palette);
        scatterProps(level, center, radius - 1, random);
    }

    private static void buildKeeperCamp(WorldGenLevel level, BlockPos center, int radius,
                                        SettlementStyle.Palette palette, SettlementStyle.Layout layout,
                                        RandomSource random) {
        pave(level, center, radius, palette);
        // A pen of spirit soil and grass with a fence, plus a keeper tent and a supply shed.
        int pen = radius - 3;
        for (int x = -pen; x <= pen; x++) {
            for (int z = -pen; z <= pen; z++) {
                boolean edge = Math.abs(x) == pen || Math.abs(z) == pen;
                if (edge) {
                    level.setBlock(center.offset(x, 0, z), palette.post(), FLAGS);
                } else if ((x + z) % 3 == 0) {
                    level.setBlock(center.offset(x, -1, z),
                            XuanhuanEarth.SPIRIT_SOIL.get().defaultBlockState(), FLAGS);
                    if ((x + z) % 2 == 0) {
                        level.setBlock(center.offset(x, 0, z),
                                XuanhuanEarth.SPIRIT_HERB_PLANT.get().defaultBlockState(), FLAGS);
                    }
                }
            }
        }
        SettlementStyle.hut(level, center.offset(-radius + 3, 0, -radius + 3), palette, 2, 3, true);
        SettlementStyle.hut(level, center.offset(radius - 3, 0, -radius + 3), palette, 2, 3, true);
        level.setBlock(center.offset(-radius + 3, 0, -radius + 3),
                XuanhuanEarth.ALCHEMY_FURNACE.get().defaultBlockState(), FLAGS);
        stock(level, center.offset(radius - 3, 0, -radius + 3),
                new ItemStack(XuanhuanEarth.SPIRIT_GRASS.get(), 16),
                new ItemStack(XuanhuanEarth.SPIRIT_HERB.get(), 6),
                new ItemStack(XuanhuanEarth.SPIRIT_BEAST_SEAL.get(), 2),
                new ItemStack(XuanhuanEarth.GRAINFAST_PILL.get(), 2),
                new ItemStack(XuanhuanEarth.SPIRIT_CRYSTAL_SHARD.get(), 4));
        SettlementStyle.lanternRing(level, center, radius - 1, palette);
        scatterProps(level, center, radius - 1, random);
    }

    /**
     * Standing spots per layout, in the same order as the builders place their buildings.
     * Every entry is an open floor tile one block above the paved surface.
     */
    private static int[][] spawnOffsets(SettlementStyle.Layout layout) {
        return switch (layout) {
            case CROSS -> new int[][]{{-4, 1, -3}, {4, 1, -3}, {-4, 1, 3}, {4, 1, 3}};
            case STREET -> new int[][]{{-4, 1, -3}, {0, 1, -3}, {4, 1, -3}};
            case HALL -> new int[][]{{0, 1, -3}, {-5, 1, 3}, {5, 1, 3}};
            case RING -> new int[][]{{-4, 1, 0}, {4, 1, 0}, {0, 1, -4}};
        };
    }

    private static void pave(WorldGenLevel level, BlockPos center, int radius,
                             SettlementStyle.Palette palette) {
        // Natural earth/stone courtyard surface (wooden planks are strictly reserved for interior hut floors)
        // with shallow natural dirt/cobblestone footing (max 2 blocks deep) to blend cleanly into terrain.
        int minFoundationY = Math.max(level.getMinY() + 1, center.getY() - 3);
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                boolean edge = Math.abs(x) == radius || Math.abs(z) == radius;
                BlockState topState = edge ? palette.wall() : palette.path();
                level.setBlock(center.offset(x, -1, z), topState, FLAGS);
                BlockState naturalFoundation = edge
                        ? Blocks.COBBLESTONE.defaultBlockState()
                        : Blocks.DIRT.defaultBlockState();
                for (int y = -2; center.getY() + y >= minFoundationY; y--) {
                    BlockPos supportPos = center.offset(x, y, z);
                    BlockState existing = level.getBlockState(supportPos);
                    if (isFoliageOrReplaceable(existing) || !existing.getFluidState().isEmpty()) {
                        level.setBlock(supportPos, naturalFoundation, FLAGS);
                    } else {
                        break;
                    }
                }
            }
        }
    }

    private static void wallRing(WorldGenLevel level, BlockPos center, int radius,
                                 SettlementStyle.Palette palette) {
        for (int i = -radius; i <= radius; i++) {
            for (int y = 0; y < 3; y++) {
                BlockState state = y == 2 ? palette.roof() : palette.wall();
                level.setBlock(center.offset(i, y, -radius), state, FLAGS);
                level.setBlock(center.offset(i, y, radius), state, FLAGS);
                level.setBlock(center.offset(-radius, y, i), state, FLAGS);
                level.setBlock(center.offset(radius, y, i), state, FLAGS);
            }
        }
    }

    private static void gate(WorldGenLevel level, BlockPos pos, SettlementStyle.Palette palette) {
        for (int x = -1; x <= 1; x++) {
            for (int y = 0; y < 3; y++) {
                level.setBlock(pos.offset(x, y, 0), Blocks.AIR.defaultBlockState(), FLAGS);
            }
        }
        for (int x : new int[]{-2, 2}) {
            for (int y = 0; y < 4; y++) {
                level.setBlock(pos.offset(x, y, 0), palette.post(), FLAGS);
            }
            level.setBlock(pos.offset(x, 4, 0), palette.light(), FLAGS);
        }
        level.setBlock(pos.offset(0, 3, 0), palette.roof(), FLAGS);
    }

    private static void stall(WorldGenLevel level, BlockPos pos,
                              SettlementStyle.Palette palette, RandomSource random) {
        for (int x : new int[]{-1, 1}) {
            for (int z : new int[]{-1, 1}) {
                for (int y = 0; y < 3; y++) {
                    level.setBlock(pos.offset(x, y, z), palette.post(), FLAGS);
                }
            }
        }
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                level.setBlock(pos.offset(x, 3, z), palette.roof(), FLAGS);
            }
        }
        level.setBlock(pos.offset(0, 2, 0), palette.light(), FLAGS);
        level.setBlock(pos.offset(0, 0, 1), Blocks.BARREL.defaultBlockState(), FLAGS);
        stock(level, pos.offset(0, 0, 1),
                randomTradeStack(random),
                randomTradeStack(random),
                randomTradeStack(random));
        if (random.nextBoolean()) {
            level.setBlock(pos.offset(-1, 0, 0), Blocks.CHEST.defaultBlockState(), FLAGS);
            stock(level, pos.offset(-1, 0, 0),
                    randomTradeStack(random),
                    randomTradeStack(random),
                    new ItemStack(XuanhuanEarth.SPIRIT_CRYSTAL_SHARD.get(), 2 + random.nextInt(4)));
        }
    }

    private static ItemStack randomTradeStack(RandomSource random) {
        return switch (random.nextInt(12)) {
            case 0 -> new ItemStack(XuanhuanEarth.TALISMAN_PAPER.get(), 4 + random.nextInt(8));
            case 1 -> new ItemStack(XuanhuanEarth.CINNABAR_SPIRIT_INK.get(), 2 + random.nextInt(5));
            case 2 -> new ItemStack(XuanhuanEarth.SPIRIT_GRASS.get(), 4 + random.nextInt(10));
            case 3 -> new ItemStack(XuanhuanEarth.SPIRIT_CRYSTAL_SHARD.get(), 2 + random.nextInt(5));
            case 4 -> new ItemStack(Items.EMERALD, 1 + random.nextInt(4));
            case 5 -> new ItemStack(XuanhuanEarth.SPIRIT_HERB.get(), 2 + random.nextInt(5));
            case 6 -> new ItemStack(XuanhuanEarth.QI_RECOVERY_PILL.get(), 1 + random.nextInt(2));
            case 7 -> new ItemStack(XuanhuanEarth.NOURISH_QI_PILL.get(), 1 + random.nextInt(2));
            case 8 -> new ItemStack(XuanhuanEarth.GRAINFAST_PILL.get(), 1 + random.nextInt(2));
            case 9 -> new ItemStack(XuanhuanEarth.BASIC_TALISMAN.get(), 1 + random.nextInt(3));
            case 10 -> new ItemStack(XuanhuanEarth.SPIRIT_STONE_RAW.get(), 1 + random.nextInt(3));
            default -> new ItemStack(XuanhuanEarth.SPIRIT_SPRING_BOTTLE.get(), 1 + random.nextInt(3));
        };
    }

    private static void campfire(WorldGenLevel level, BlockPos pos) {
        level.setBlock(pos, Blocks.CAMPFIRE.defaultBlockState(), FLAGS);
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                if (Math.abs(x) + Math.abs(z) == 2) {
                    level.setBlock(pos.offset(x, 0, z), Blocks.SPRUCE_SLAB.defaultBlockState(), FLAGS);
                }
            }
        }
    }

    private static void spring(WorldGenLevel level, BlockPos center, SettlementStyle.Palette palette) {
        for (int x = -2; x <= 2; x++) {
            for (int z = -2; z <= 2; z++) {
                level.setBlock(center.offset(x, -2, z), palette.floor(), FLAGS);
                if (Math.abs(x) == 2 || Math.abs(z) == 2) {
                    level.setBlock(center.offset(x, -1, z), palette.wall(), FLAGS);
                    level.setBlock(center.offset(x, 0, z), Blocks.SMOOTH_STONE_SLAB.defaultBlockState(), FLAGS);
                } else {
                    level.setBlock(center.offset(x, -1, z), XuanhuanEarth.SPIRIT_SPRING_BLOCK.get().defaultBlockState(), FLAGS);
                    level.setBlock(center.offset(x, 0, z), Blocks.AIR.defaultBlockState(), FLAGS);
                }
            }
        }
        level.setBlock(center.offset(0, -2, 0), XuanhuanEarth.SPIRIT_SPRING_STONE.get().defaultBlockState(), FLAGS);
        level.setBlock(center.offset(0, -1, 0), XuanhuanEarth.SPIRIT_SPRING_STONE.get().defaultBlockState(), FLAGS);
        for (int x : new int[]{-2, 2}) {
            for (int z : new int[]{-2, 2}) {
                level.setBlock(center.offset(x, 0, z), palette.post(), FLAGS);
                level.setBlock(center.offset(x, 1, z), palette.light(), FLAGS);
            }
        }
        level.setBlock(center.offset(0, 0, 2), XuanhuanEarth.SPIRIT_VEIN_NODE.get().defaultBlockState(), FLAGS);
    }

    private static void terrace(WorldGenLevel level, BlockPos pos, SettlementStyle.Palette palette) {
        for (int x = 0; x < 4; x++) {
            for (int z = 0; z < 3; z++) {
                level.setBlock(pos.offset(x, -1, z), palette.floor(), FLAGS);
                boolean border = x == 0 || x == 3 || z == 0 || z == 2;
                if (border) {
                    level.setBlock(pos.offset(x, 0, z), palette.wall(), FLAGS);
                    level.setBlock(pos.offset(x, 1, z), Blocks.SMOOTH_STONE_SLAB.defaultBlockState(), FLAGS);
                } else if (x == 1 && z == 1) {
                    level.setBlock(pos.offset(x, -1, z),
                            XuanhuanEarth.SPIRIT_SPRING_STONE.get().defaultBlockState(), FLAGS);
                    level.setBlock(pos.offset(x, 0, z), XuanhuanEarth.SPIRIT_SPRING_BLOCK.get().defaultBlockState(), FLAGS);
                    level.setBlock(pos.offset(x, 1, z), Blocks.AIR.defaultBlockState(), FLAGS);
                } else {
                    level.setBlock(pos.offset(x, 0, z),
                            XuanhuanEarth.SPIRIT_SOIL.get().defaultBlockState(), FLAGS);
                    level.setBlock(pos.offset(x, 1, z), XuanhuanEarth.SPIRIT_HERB_PLANT.get().defaultBlockState(), FLAGS);
                }
            }
        }
    }

    private static void workshop(WorldGenLevel level, BlockPos pos, SettlementStyle.Palette palette,
                                 RandomSource random) {
        SettlementStyle.hut(level, pos, palette, 3, 3, true);
        switch (random.nextInt(3)) {
            case 0 -> level.setBlock(pos.offset(0, 0, 0),
                    XuanhuanEarth.ALCHEMY_FURNACE.get().defaultBlockState(), FLAGS);
            case 1 -> level.setBlock(pos.offset(0, 0, 0),
                    XuanhuanEarth.TALISMAN_TABLE.get().defaultBlockState(), FLAGS);
            default -> formalArray(level, pos.offset(0, 0, 0));
        }
        level.setBlock(pos.offset(-1, 0, 0), Blocks.CRAFTING_TABLE.defaultBlockState(), FLAGS);
        level.setBlock(pos.offset(1, 0, 0), Blocks.BARREL.defaultBlockState(), FLAGS);
        stock(level, pos.offset(1, 0, 0),
                randomTradeStack(random),
                randomTradeStack(random),
                new ItemStack(XuanhuanEarth.CULTIVATION_JOURNAL.get(), 1),
                new ItemStack(XuanhuanEarth.BASIC_TALISMAN.get(), 2),
                new ItemStack(XuanhuanEarth.SPIRIT_CRYSTAL_SHARD.get(), 3 + random.nextInt(4)));
    }

    /**
     * Builds a complete formal spirit array: core, four spirit soil arms and four corner
     * focus blocks. A lone core can never form, so placing one without its ring would leave
     * a block that looks meaningful and does nothing.
     */
    private static void formalArray(WorldGenLevel level, BlockPos core) {
        level.setBlock(core, XuanhuanEarth.SPIRIT_ARRAY_CORE.get().defaultBlockState(), FLAGS);
        BlockState soil = XuanhuanEarth.SPIRIT_SOIL.get().defaultBlockState();
        level.setBlock(core.north(), soil, FLAGS);
        level.setBlock(core.south(), soil, FLAGS);
        level.setBlock(core.east(), soil, FLAGS);
        level.setBlock(core.west(), soil, FLAGS);
        BlockState vein = XuanhuanEarth.SPIRIT_VEIN_NODE.get().defaultBlockState();
        level.setBlock(core.north().east(), vein, FLAGS);
        level.setBlock(core.north().west(), vein, FLAGS);
        level.setBlock(core.south().east(), vein, FLAGS);
        level.setBlock(core.south().west(), vein, FLAGS);
    }

    private static void trainingYard(WorldGenLevel level, BlockPos pos, SettlementStyle.Palette palette) {
        for (int x = -2; x <= 2; x++) {
            for (int z = -2; z <= 2; z++) {
                level.setBlock(pos.offset(x, -1, z), palette.path(), FLAGS);
            }
        }
        level.setBlock(pos.offset(-2, 0, -2), Blocks.TARGET.defaultBlockState(), FLAGS);
        level.setBlock(pos.offset(2, 0, -2), Blocks.TARGET.defaultBlockState(), FLAGS);
        level.setBlock(pos.offset(0, 0, 2), XuanhuanEarth.MEDITATION_CUSHION.get().defaultBlockState(), FLAGS);
    }

    private static void tradePost(WorldGenLevel level, BlockPos pos, SettlementStyle.Palette palette,
                                  RandomSource random) {
        SettlementStyle.hut(level, pos, palette, 2, 3, true);
        level.setBlock(pos.offset(0, 0, 0), Blocks.BARREL.defaultBlockState(), FLAGS);
        stock(level, pos.offset(0, 0, 0),
                randomTradeStack(random),
                randomTradeStack(random),
                new ItemStack(XuanhuanEarth.QI_GUIDING_MANUAL.get(), 1),
                new ItemStack(XuanhuanEarth.QI_RECOVERY_PILL.get(), 2),
                new ItemStack(XuanhuanEarth.BASIC_TALISMAN.get(), 2));
        placeBoard(level, pos.offset(0, 1, -1));
    }

    private static void scatterProps(WorldGenLevel level, BlockPos center, int radius,
                                     RandomSource random) {
        int props = 3 + random.nextInt(4);
        for (int i = 0; i < props; i++) {
            int x = random.nextInt(radius * 2 + 1) - radius;
            int z = random.nextInt(radius * 2 + 1) - radius;
            if (Math.abs(x) < 2 && Math.abs(z) < 2) {
                continue;
            }
            BlockPos pos = center.offset(x, 0, z);
            switch (random.nextInt(5)) {
                case 0 -> SettlementStyle.placeIfAir(level, pos, Blocks.FLOWER_POT);
                case 1 -> SettlementStyle.placeIfAir(level, pos, Blocks.HAY_BLOCK);
                case 2 -> SettlementStyle.placeIfAir(level, pos, Blocks.COMPOSTER);
                case 3 -> SettlementStyle.placeIfAir(level, pos, Blocks.LECTERN);
                default -> {
                    if (level.isEmptyBlock(pos)) {
                        level.setBlock(pos, Blocks.BARREL.defaultBlockState(), FLAGS);
                        stock(level, pos, randomTradeStack(random), randomTradeStack(random));
                    }
                }
            }
        }
    }

    private static void stock(WorldGenLevel level, BlockPos pos, ItemStack... items) {
        Container container;
        if (level.getBlockEntity(pos) instanceof Container existing) {
            container = existing;
        } else {
            level.setBlock(pos, Blocks.BARREL.defaultBlockState(), FLAGS);
            if (!(level.getBlockEntity(pos) instanceof Container placedContainer)) {
                return;
            }
            container = placedContainer;
        }
        for (int slot = 0; slot < Math.min(container.getContainerSize(), items.length); slot++) {
            container.setItem(slot, sealed(items[slot].copy()));
        }
        Optional<net.minecraft.world.item.Item> optRice = BuiltInRegistries.ITEM.getOptional(
                Identifier.fromNamespaceAndPath("earth_core", "rice"));
        if (optRice.isPresent() && items.length < container.getContainerSize()) {
            container.setItem(items.length, new ItemStack(optRice.get(), 4));
        }
    }

    /** Village stock must be usable; an unsealed talisman could never be triggered. */
    private static ItemStack sealed(ItemStack stack) {
        if (stack.is(XuanhuanEarth.BASIC_TALISMAN.get())
                || stack.is(XuanhuanEarth.FIRE_TALISMAN.get())
                || stack.is(XuanhuanEarth.WIND_TALISMAN.get())
                || stack.is(XuanhuanEarth.SEAL_TALISMAN.get())) {
            return TalismanComponent.drawn(stack);
        }
        return stack;
    }

    private static void placeBoard(WorldGenLevel level, BlockPos pos) {
        Block board = BuiltInRegistries.BLOCK.getValue(
                Identifier.fromNamespaceAndPath("earth_core", "settlement_board"));
        if (board != null && board != Blocks.AIR) {
            level.setBlock(pos, board.defaultBlockState(), FLAGS);
        }
    }

    private static BlockPos findOpenSignpostSpot(WorldGenLevel level, BlockPos center, int radius) {
        int rim = Math.max(2, radius - 2);
        int[][] candidates = new int[][]{
                {0, rim}, {2, rim}, {-2, rim},
                {rim, 0}, {-rim, 0}, {0, -rim},
                {rim - 1, rim - 1}, {-rim + 1, rim - 1},
                {2, 2}, {-2, 2}, {2, -2}, {-2, -2}
        };
        for (int[] off : candidates) {
            BlockPos pos = center.offset(off[0], 0, off[1]);
            BlockState below = level.getBlockState(pos.below());
            if (level.isEmptyBlock(pos) && level.isEmptyBlock(pos.above())
                    && !below.isAir() && below.getFluidState().isEmpty()) {
                return pos;
            }
        }
        return center.offset(0, 0, rim);
    }

    private static BlockPos findOpenWaystoneSpot(WorldGenLevel level, BlockPos signPos) {
        int[][] offsets = new int[][]{{2, 0}, {-2, 0}, {0, 2}, {0, -2}};
        for (int[] off : offsets) {
            BlockPos pos = signPos.offset(off[0], 0, off[1]);
            if (!level.ensureCanWrite(pos) || !level.ensureCanWrite(pos.above())) {
                continue;
            }
            BlockState below = level.getBlockState(pos.below());
            if (!below.isAir() && below.getFluidState().isEmpty()
                    && level.isEmptyBlock(pos) && level.isEmptyBlock(pos.above())) {
                return pos;
            }
        }
        return null;
    }

    private static BlockPos placeSettlementSignpostAndCompat(WorldGenLevel level, BlockPos center, int radius, Type type) {
        BlockPos signPos = findOpenSignpostSpot(level, center, radius);
        if (level.ensureCanWrite(signPos)) {
            level.setBlock(signPos, Blocks.SPRUCE_SIGN.defaultBlockState(), FLAGS);
            if (level.getBlockEntity(signPos) instanceof SignBlockEntity sign) {
                SignText text = SettlementSignHelper.buildSettlementSignText(sign.getFrontText(), type.displayTitle());
                SettlementSignHelper.applyFrontTextSafely(level, sign, text);
            }
        }
        // Soft-compat: if Waystones mod is installed, place a full 2-block Waystone cleanly beside the signpost
        if (EarthCoreApi.compat().isWaystonesLoaded()) {
            try {
                Block waystone = BuiltInRegistries.BLOCK.getValue(Identifier.fromNamespaceAndPath("waystones", "waystone"));
                BlockPos wsPos = findOpenWaystoneSpot(level, signPos);
                if (waystone != null && waystone != Blocks.AIR && wsPos != null) {
                    BlockState base = waystone.defaultBlockState();
                    if (base.hasProperty(BlockStateProperties.WATERLOGGED)) {
                        base = base.setValue(BlockStateProperties.WATERLOGGED, false);
                    }
                    if (base.hasProperty(BlockStateProperties.DOUBLE_BLOCK_HALF)) {
                        level.setBlock(wsPos, base.setValue(BlockStateProperties.DOUBLE_BLOCK_HALF, DoubleBlockHalf.LOWER), FLAGS);
                        level.setBlock(wsPos.above(), base.setValue(BlockStateProperties.DOUBLE_BLOCK_HALF, DoubleBlockHalf.UPPER), FLAGS);
                    } else {
                        level.setBlock(wsPos, base, FLAGS);
                    }
                    return wsPos;
                }
            } catch (RuntimeException ignored) {
                // Never allow third-party Waystones block state changes to abort worldgen
            }
        }
        return EarthCoreApi.compat().findNearestSettlementMarker(level, signPos, 8).orElse(signPos);
    }

    private static void spawnSettlementResidents(WorldGenLevel level, BlockPos markerPos, Type type,
                                                 int[][] spawnOffsets, RandomSource random) {
        List<CultivationSettlerEntity.Role> roster = new ArrayList<>(rosterFor(type));
        for (int i = roster.size() - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            CultivationSettlerEntity.Role tmp = roster.get(i);
            roster.set(i, roster.get(j));
            roster.set(j, tmp);
        }
        for (int i = 0; i < Math.min(roster.size(), spawnOffsets.length); i++) {
            int[] off = spawnOffsets[i];
            BlockPos preferred = markerPos.offset(off[0], off[1], off[2]);
            BlockPos spawnPos = findOpenSpawn(level, preferred);
            CultivationSettlerEntity resident = XuanhuanEarth.CULTIVATION_SETTLER.get()
                    .create(level.getLevel(), EntitySpawnReason.STRUCTURE);
            if (resident == null) continue;
            resident.setRole(roster.get(i));
            resident.getPersistentData().putString("EarthOnMinecraftResidentRole",
                    "earth_xuanxue:" + roster.get(i).name().toLowerCase(java.util.Locale.ROOT));
            resident.getPersistentData().putString("EarthOnMinecraftSettlementProfile",
                    "earth_core:cultivation_settlement");
            resident.getPersistentData().putInt("EarthOnMinecraftSettlementAnchorX", markerPos.getX());
            resident.getPersistentData().putInt("EarthOnMinecraftSettlementAnchorY", markerPos.getY());
            resident.getPersistentData().putInt("EarthOnMinecraftSettlementAnchorZ", markerPos.getZ());
            resident.snapTo(spawnPos.getX() + 0.5D, spawnPos.getY(), spawnPos.getZ() + 0.5D,
                    random.nextFloat() * 360.0F, 0.0F);
            resident.setPersistenceRequired();
            level.addFreshEntity(resident);
        }
        if (type != Type.WANDERING_MARKET) {
            EntityType<? extends ContractableSpiritBeastEntity> beastType = switch (random.nextInt(5)) {
                case 0 -> XuanhuanEarth.SPIRIT_FOX.get();
                case 1 -> XuanhuanEarth.WIND_WOLF.get();
                case 2 -> XuanhuanEarth.STONEHORN_GOAT.get();
                case 3 -> XuanhuanEarth.CRYSTAL_TURTLE.get();
                default -> XuanhuanEarth.EMBER_CRANE.get();
            };
            BlockPos beastPos = findOpenSpawn(level, markerPos.offset(3, 1, 2));
            ContractableSpiritBeastEntity beast = beastType.create(level.getLevel(), EntitySpawnReason.STRUCTURE);
            if (beast != null) {
                beast.snapTo(beastPos.getX() + 0.5D, beastPos.getY(), beastPos.getZ() + 0.5D,
                        random.nextFloat() * 360.0F, 0.0F);
                beast.setPersistenceRequired();
                level.addFreshEntity(beast);
            }
        }
    }

    private static List<CultivationSettlerEntity.Role> rosterFor(Type type) {
        return switch (type) {
            case WANDERING_MARKET -> List.of(
                    CultivationSettlerEntity.Role.WANDERING_MERCHANT,
                    CultivationSettlerEntity.Role.WANDERING_MERCHANT,
                    CultivationSettlerEntity.Role.SECT_STEWARD);
            case SPIRIT_SPRING_VILLAGE -> List.of(
                    CultivationSettlerEntity.Role.SPIRIT_SPRING_KEEPER,
                    CultivationSettlerEntity.Role.SPIRIT_SPRING_KEEPER,
                    CultivationSettlerEntity.Role.WANDERING_MERCHANT);
            case SECT_OUTPOST -> List.of(
                    CultivationSettlerEntity.Role.SECT_STEWARD,
                    CultivationSettlerEntity.Role.SECT_STEWARD,
                    CultivationSettlerEntity.Role.SPIRIT_SPRING_KEEPER);
            case MOUNTAIN_SPIRIT_SHRINE -> List.of(
                    CultivationSettlerEntity.Role.SPIRIT_SPRING_KEEPER,
                    CultivationSettlerEntity.Role.SECT_STEWARD,
                    CultivationSettlerEntity.Role.WANDERING_MERCHANT);
            case SPIRIT_BEAST_KEEPER_CAMP -> List.of(
                    CultivationSettlerEntity.Role.SPIRIT_SPRING_KEEPER,
                    CultivationSettlerEntity.Role.WANDERING_MERCHANT,
                    CultivationSettlerEntity.Role.WANDERING_MERCHANT);
        };
    }

    private static BlockPos findOpenSpawn(WorldGenLevel level, BlockPos preferred) {
        int[] ys = {0, 1, -1, 2};
        for (int dy : ys) {
            for (int r = 0; r <= 3; r++) {
                for (int x = -r; x <= r; x++) {
                    for (int z = -r; z <= r; z++) {
                        BlockPos c = preferred.offset(x, dy, z);
                        if (!level.isEmptyBlock(c.below()) && level.isEmptyBlock(c) && level.isEmptyBlock(c.above())) {
                            return c;
                        }
                    }
                }
            }
        }
        return preferred;
    }

    public enum Type {
        WANDERING_MARKET("wandering_cultivator_market", 8),
        SPIRIT_SPRING_VILLAGE("spirit_spring_village", 9),
        SECT_OUTPOST("sect_frontier_outpost", 10),
        MOUNTAIN_SPIRIT_SHRINE("mountain_spirit_shrine", 8),
        SPIRIT_BEAST_KEEPER_CAMP("spirit_beast_keeper_camp", 8);

        private final String id;
        private final int radius;

        Type(String id, int radius) {
            this.id = id;
            this.radius = radius;
        }

        public String id() {
            return id;
        }

        public int radius() {
            return radius;
        }

        public String displayTitle() {
            return switch (this) {
                case WANDERING_MARKET -> "「散修坊市」";
                case SPIRIT_SPRING_VILLAGE -> "「灵泉村」";
                case SECT_OUTPOST -> "「宗门前哨」";
                case MOUNTAIN_SPIRIT_SHRINE -> "「山灵古祠」";
                case SPIRIT_BEAST_KEEPER_CAMP -> "「御兽营地」";
            };
        }

        /** Climate drives which palette pool the site draws from. */
        public SettlementStyle.Theme themeFor(WorldGenLevel level, BlockPos pos) {
            var biome = level.getBiome(pos).value();
            boolean cold = biome.coldEnoughToSnow(pos, level.getSeaLevel());
            boolean dry = biome.getBaseTemperature() > 0.9F;
            if (cold) {
                return SettlementStyle.Theme.HIGHLAND;
            }
            if (dry) {
                return SettlementStyle.Theme.DESERT;
            }
            return switch (this) {
                case SECT_OUTPOST, MOUNTAIN_SPIRIT_SHRINE -> SettlementStyle.Theme.HIGHLAND;
                case SPIRIT_SPRING_VILLAGE, SPIRIT_BEAST_KEEPER_CAMP -> SettlementStyle.Theme.FOREST;
                case WANDERING_MARKET -> SettlementStyle.Theme.TEMPERATE;
            };
        }

        public static Type byId(String id) {
            for (Type type : values()) {
                if (type.id.equals(id)) {
                    return type;
                }
            }
            return WANDERING_MARKET;
        }
    }
}
