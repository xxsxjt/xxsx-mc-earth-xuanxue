package com.xxsx.xuanhuanearth.settlement;

import com.mojang.serialization.Codec;
import com.xxsx.xuanhuanearth.TalismanComponent;
import com.xxsx.xuanhuanearth.XuanhuanEarth;
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

        level.setBlock(center, XuanhuanEarth.SETTLEMENT_ANCHOR.get().defaultBlockState(), FLAGS);
        if (level.getBlockEntity(center) instanceof XuanhuanSettlementAnchorBlockEntity anchor) {
            anchor.configure(type.id(), layout.ordinal(), palette.id());
            // Record the real standing spots for this layout. The anchor cannot derive them
            // from the layout enum alone, because each builder places its buildings
            // differently, and a mismatch spawns residents inside walls.
            anchor.setSpawnOffsets(spawnOffsets(layout));
        }
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
        return (maxSampleY - minSampleY) <= 9;
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
                    level.setBlock(center.offset(x, 0, z),
                            XuanhuanEarth.SPIRIT_SOIL.get().defaultBlockState(), FLAGS);
                } else if ((x * z) % 5 == 0) {
                    level.setBlock(center.offset(x, 0, z), palette.accent(), FLAGS);
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
        int minFoundationY = Math.max(level.getMinY() + 1, center.getY() - 28);
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                boolean edge = Math.abs(x) == radius || Math.abs(z) == radius;
                BlockState topState = edge ? palette.wall() : palette.floor();
                level.setBlock(center.offset(x, -1, z), topState, FLAGS);
                BlockState pillarState = edge ? palette.wall() : palette.floor();
                for (int y = -2; center.getY() + y >= minFoundationY; y--) {
                    BlockPos supportPos = center.offset(x, y, z);
                    BlockState existing = level.getBlockState(supportPos);
                    if (isFoliageOrReplaceable(existing) || !existing.getFluidState().isEmpty()) {
                        level.setBlock(supportPos, pillarState, FLAGS);
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
                    level.setBlock(center.offset(x, -1, z), Blocks.WATER.defaultBlockState(), FLAGS);
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
                    level.setBlock(pos.offset(x, 0, z), Blocks.WATER.defaultBlockState(), FLAGS);
                    level.setBlock(pos.offset(x, 1, z), Blocks.AIR.defaultBlockState(), FLAGS);
                } else {
                    level.setBlock(pos.offset(x, 0, z),
                            XuanhuanEarth.SPIRIT_SOIL.get().defaultBlockState(), FLAGS);
                    level.setBlock(pos.offset(x, 1, z), Blocks.FERN.defaultBlockState(), FLAGS);
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
                Identifier.fromNamespaceAndPath("earth_on_minecraft", "rice"));
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
                Identifier.fromNamespaceAndPath("earth_on_minecraft", "settlement_board"));
        if (board != null && board != Blocks.AIR) {
            level.setBlock(pos, board.defaultBlockState(), FLAGS);
        }
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
