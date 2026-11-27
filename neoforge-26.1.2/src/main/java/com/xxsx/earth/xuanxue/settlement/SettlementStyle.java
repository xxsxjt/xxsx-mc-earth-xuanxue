package com.xxsx.earth.xuanxue.settlement;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Material and shape vocabulary shared by every settlement builder.
 *
 * <p>Each site rolls a {@link Palette} and a {@link Layout} from its own seed, so two
 * spirit spring villages in the same world do not look identical. The palette covers the
 * floor, walls, roof, posts, lighting and accent so a whole settlement reads as one
 * building tradition rather than a pile of unrelated blocks.</p>
 */
public final class SettlementStyle {
    private SettlementStyle() {
    }

    public record Palette(String id, BlockState floor, BlockState wall, BlockState roof,
                          BlockState post, BlockState light, BlockState accent,
                          BlockState path) {
    }

    public enum Layout {
        /** Four buildings around a central plaza. */
        CROSS,
        /** Buildings strung along one street. */
        STREET,
        /** One large hall plus two wings. */
        HALL,
        /** Ring of small huts around a well. */
        RING
    }

    private static final Palette[] TEMPERATE = {
            new Palette("spruce",
                    Blocks.SPRUCE_PLANKS.defaultBlockState(),
                    Blocks.STRIPPED_SPRUCE_LOG.defaultBlockState(),
                    Blocks.SPRUCE_SLAB.defaultBlockState(),
                    Blocks.SPRUCE_FENCE.defaultBlockState(),
                    Blocks.LANTERN.defaultBlockState(),
                    Blocks.FLOWERING_AZALEA.defaultBlockState(),
                    Blocks.COARSE_DIRT.defaultBlockState()),
            new Palette("birch",
                    Blocks.BIRCH_PLANKS.defaultBlockState(),
                    Blocks.STRIPPED_BIRCH_LOG.defaultBlockState(),
                    Blocks.BIRCH_SLAB.defaultBlockState(),
                    Blocks.BIRCH_FENCE.defaultBlockState(),
                    Blocks.LANTERN.defaultBlockState(),
                    Blocks.OAK_LEAVES.defaultBlockState(),
                    Blocks.PODZOL.defaultBlockState()),
            new Palette("mossy",
                    Blocks.MOSSY_STONE_BRICKS.defaultBlockState(),
                    Blocks.MOSSY_COBBLESTONE.defaultBlockState(),
                    Blocks.DARK_OAK_SLAB.defaultBlockState(),
                    Blocks.DARK_OAK_FENCE.defaultBlockState(),
                    Blocks.SOUL_LANTERN.defaultBlockState(),
                    Blocks.MOSS_BLOCK.defaultBlockState(),
                    Blocks.MOSSY_STONE_BRICKS.defaultBlockState()),
    };

    private static final Palette[] HIGHLAND = {
            new Palette("deepslate",
                    Blocks.POLISHED_DEEPSLATE.defaultBlockState(),
                    Blocks.DEEPSLATE_BRICKS.defaultBlockState(),
                    Blocks.DEEPSLATE_TILE_SLAB.defaultBlockState(),
                    Blocks.DARK_OAK_FENCE.defaultBlockState(),
                    Blocks.LANTERN.defaultBlockState(),
                    Blocks.CHISELED_DEEPSLATE.defaultBlockState(),
                    Blocks.GRAVEL.defaultBlockState()),
            new Palette("stone",
                    Blocks.STONE_BRICKS.defaultBlockState(),
                    Blocks.CHISELED_STONE_BRICKS.defaultBlockState(),
                    Blocks.STONE_BRICK_SLAB.defaultBlockState(),
                    Blocks.DARK_OAK_FENCE.defaultBlockState(),
                    Blocks.LANTERN.defaultBlockState(),
                    Blocks.CRACKED_STONE_BRICKS.defaultBlockState(),
                    Blocks.STONE_BRICKS.defaultBlockState()),
    };

    private static final Palette[] FOREST = {
            new Palette("dark_oak",
                    Blocks.DARK_OAK_PLANKS.defaultBlockState(),
                    Blocks.STRIPPED_DARK_OAK_LOG.defaultBlockState(),
                    Blocks.DARK_OAK_SLAB.defaultBlockState(),
                    Blocks.DARK_OAK_FENCE.defaultBlockState(),
                    Blocks.LANTERN.defaultBlockState(),
                    Blocks.AZALEA_LEAVES.defaultBlockState(),
                    Blocks.ROOTED_DIRT.defaultBlockState()),
            new Palette("bamboo",
                    Blocks.BAMBOO_PLANKS.defaultBlockState(),
                    Blocks.BAMBOO_BLOCK.defaultBlockState(),
                    Blocks.BAMBOO_SLAB.defaultBlockState(),
                    Blocks.BAMBOO_FENCE.defaultBlockState(),
                    Blocks.LANTERN.defaultBlockState(),
                    Blocks.OAK_LEAVES.defaultBlockState(),
                    Blocks.MUD_BRICKS.defaultBlockState()),
    };

    private static final Palette[] DESERT = {
            new Palette("sandstone",
                    Blocks.SMOOTH_SANDSTONE.defaultBlockState(),
                    Blocks.CUT_SANDSTONE.defaultBlockState(),
                    Blocks.SANDSTONE_SLAB.defaultBlockState(),
                    Blocks.ACACIA_FENCE.defaultBlockState(),
                    Blocks.LANTERN.defaultBlockState(),
                    Blocks.CHISELED_SANDSTONE.defaultBlockState(),
                    Blocks.SAND.defaultBlockState()),
    };

    public static Palette paletteFor(Theme theme, RandomSource random) {
        Palette[] pool = switch (theme) {
            case TEMPERATE -> TEMPERATE;
            case HIGHLAND -> HIGHLAND;
            case FOREST -> FOREST;
            case DESERT -> DESERT;
        };
        return pool[random.nextInt(pool.length)];
    }

    public static Layout layoutFor(RandomSource random) {
        Layout[] values = Layout.values();
        return values[random.nextInt(values.length)];
    }

    /** The three climates the settlement types are built in. */
    public enum Theme {
        TEMPERATE,
        HIGHLAND,
        FOREST,
        DESERT
    }

    /**
     * Places a hollow-roofed hut: posts on the corners, walls on three sides, a door gap
     * facing the given direction, and a slab roof that overhangs by one block.
     */
    public static void hut(WorldGenLevel level, BlockPos center, Palette palette, int radius,
                           int height, boolean openFront) {
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                level.setBlock(center.offset(x, -1, z), palette.floor(), 2);
            }
        }
        for (int x : new int[]{-radius, radius}) {
            for (int z : new int[]{-radius, radius}) {
                for (int y = 0; y < height; y++) {
                    level.setBlock(center.offset(x, y, z), palette.post(), 2);
                }
            }
        }
        for (int x = -radius + 1; x <= radius - 1; x++) {
            for (int z = -radius + 1; z <= radius - 1; z++) {
                boolean edge = x == -radius + 1 || x == radius - 1
                        || z == -radius + 1 || z == radius - 1;
                if (!edge) {
                    continue;
                }
                if (openFront && z == radius - 1 && Math.abs(x) <= 0) {
                    continue;
                }
                level.setBlock(center.offset(x, 0, z), palette.wall(), 2);
                if (height >= 3) {
                    level.setBlock(center.offset(x, height - 1, z), palette.wall(), 2);
                }
            }
        }
        for (int x = -radius - 1; x <= radius + 1; x++) {
            for (int z = -radius - 1; z <= radius + 1; z++) {
                level.setBlock(center.offset(x, height, z), palette.roof(), 2);
            }
        }
        level.setBlock(center.offset(0, height - 1, 0), palette.light(), 2);
    }

    /** A one-block-wide path connecting two points along an L shape. */
    public static void path(WorldGenLevel level, BlockPos from, BlockPos to, BlockState state) {
        int x = from.getX();
        int z = from.getZ();
        while (x != to.getX()) {
            level.setBlock(new BlockPos(x, from.getY() - 1, z), state, 2);
            x += Integer.signum(to.getX() - x);
        }
        while (z != to.getZ()) {
            level.setBlock(new BlockPos(x, from.getY() - 1, z), state, 2);
            z += Integer.signum(to.getZ() - z);
        }
    }

    /** Four lantern posts at the corners of a plaza. */
    public static void lanternRing(WorldGenLevel level, BlockPos center, int radius, Palette palette) {
        for (int x : new int[]{-radius, radius}) {
            for (int z : new int[]{-radius, radius}) {
                for (int y = 0; y < 2; y++) {
                    level.setBlock(center.offset(x, y, z), palette.post(), 2);
                }
                level.setBlock(center.offset(x, 2, z), palette.light(), 2);
            }
        }
    }

    /** A decorative planted bed using the palette accent. */
    public static void plantBed(WorldGenLevel level, BlockPos center, int radius, Palette palette) {
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                if (Math.abs(x) + Math.abs(z) > radius) {
                    continue;
                }
                level.setBlock(center.offset(x, -1, z), palette.path(), 2);
                if ((x + z) % 2 == 0) {
                    level.setBlock(center.offset(x, 0, z), palette.accent(), 2);
                }
            }
        }
    }

    /** Copies a block only if the target is air, used for scattered props. */
    public static void placeIfAir(WorldGenLevel level, BlockPos pos, Block block) {
        if (level.isEmptyBlock(pos)) {
            level.setBlock(pos, block.defaultBlockState(), 2);
        }
    }
}
