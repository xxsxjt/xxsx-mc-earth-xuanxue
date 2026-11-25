import io

p = r'D:\Codex\WORKSPACES\Projects\mc-agent\earth-series\earth_xuanxue\neoforge-26.1.2\src\main\java\com\xxsx\earth\xuanxue\settlement\XuanhuanSettlementFeature.java'
s = io.open(p, encoding='utf-8').read()

# 1. New enum entries.
old_enum = '''    public enum Type {
        WANDERING_MARKET("wandering_cultivator_market", 8),
        SPIRIT_SPRING_VILLAGE("spirit_spring_village", 9),
        SECT_OUTPOST("sect_frontier_outpost", 10);

        private final String id;
        private final int radius;

        Type(String id, int radius) {
            this.id = id;
            this.radius = radius;
        }'''

new_enum = '''    public enum Type {
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
        }'''

if old_enum in s:
    s = s.replace(old_enum, new_enum, 1)
    print('enum extended')
else:
    print('MISS enum')

# 2. Theme mapping for the two new types.
old_theme = '''            return switch (this) {
                case SECT_OUTPOST -> SettlementStyle.Theme.HIGHLAND;
                case SPIRIT_SPRING_VILLAGE -> SettlementStyle.Theme.FOREST;
                case WANDERING_MARKET -> SettlementStyle.Theme.TEMPERATE;
            };'''

new_theme = '''            return switch (this) {
                case SECT_OUTPOST, MOUNTAIN_SPIRIT_SHRINE -> SettlementStyle.Theme.HIGHLAND;
                case SPIRIT_SPRING_VILLAGE, SPIRIT_BEAST_KEEPER_CAMP -> SettlementStyle.Theme.FOREST;
                case WANDERING_MARKET -> SettlementStyle.Theme.TEMPERATE;
            };'''

if old_theme in s:
    s = s.replace(old_theme, new_theme, 1)
    print('theme extended')
else:
    print('MISS theme')

# 3. Dispatch to the new builders.
old_dispatch = '''            case SECT_OUTPOST -> buildOutpost(level, center, radius, palette, layout, random);
        }'''

new_dispatch = '''            case SECT_OUTPOST -> buildOutpost(level, center, radius, palette, layout, random);
            case MOUNTAIN_SPIRIT_SHRINE -> buildShrine(level, center, radius, palette, layout, random);
            case SPIRIT_BEAST_KEEPER_CAMP -> buildKeeperCamp(level, center, radius, palette, layout, random);
        }'''

if old_dispatch in s:
    s = s.replace(old_dispatch, new_dispatch, 1)
    print('dispatch extended')
else:
    print('MISS dispatch')

# 4. Insert the two new builders before the "pave" helper.
anchor = '    private static void pave(WorldGenLevel level, BlockPos center, int radius,'

builders = '''    private static void buildShrine(WorldGenLevel level, BlockPos center, int radius,
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
        level.setBlock(center.offset(0, 3, 0), XuanhuanEarth.SPIRIT_ARRAY_CORE.get().defaultBlockState(), FLAGS);
        level.setBlock(center.offset(0, 2, 0), XuanhuanEarth.SPIRIT_VEIN_NODE.get().defaultBlockState(), FLAGS);
        for (int x : new int[]{-2, 2}) {
            for (int z : new int[]{-2, 2}) {
                level.setBlock(center.offset(x, 1, z), Blocks.CAMPFIRE.defaultBlockState(), FLAGS);
                level.setBlock(center.offset(x, 2, z), palette.light(), FLAGS);
            }
        }
        SettlementStyle.hut(level, center.offset(0, 0, -radius + 3), palette, 2, 3, true);
        stock(level, center.offset(0, 0, -radius + 3),
                new ItemStack(XuanhuanEarth.SPIRIT_CRYSTAL_SHARD.get(), 6),
                new ItemStack(XuanhuanEarth.SPIRIT_SPRING_BOTTLE.get(), 4));
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
                    level.setBlock(center.offset(x, 0, z),
                            XuanhuanEarth.SPIRIT_GRASS.get().defaultBlockState(), FLAGS);
                }
            }
        }
        SettlementStyle.hut(level, center.offset(-radius + 3, 0, -radius + 3), palette, 2, 3, true);
        SettlementStyle.hut(level, center.offset(radius - 3, 0, -radius + 3), palette, 2, 3, true);
        level.setBlock(center.offset(-radius + 3, 0, -radius + 3),
                XuanhuanEarth.ALCHEMY_FURNACE.get().defaultBlockState(), FLAGS);
        stock(level, center.offset(radius - 3, 0, -radius + 3),
                new ItemStack(XuanhuanEarth.SPIRIT_GRASS.get(), 16),
                new ItemStack(XuanhuanEarth.SPIRIT_BEAST_SEAL.get(), 2));
        SettlementStyle.lanternRing(level, center, radius - 1, palette);
        scatterProps(level, center, radius - 1, random);
    }

'''

if anchor in s:
    s = s.replace(anchor, builders + anchor, 1)
    print('builders inserted')
else:
    print('MISS anchor')

io.open(p, 'w', encoding='utf-8', newline='\n').write(s)
