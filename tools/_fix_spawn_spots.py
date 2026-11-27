import io

base = (r'D:\Codex\WORKSPACES\Projects\mc-agent\earth-series\earth_xuanxue\neoforge-26.1.2\src\main\java'
        r'\com\xxsx\earth\xuanxue\settlement')

# --- Feature: record where residents should stand, per layout, at generation time.
p = base + r'\XuanhuanSettlementFeature.java'
s = io.open(p, encoding='utf-8').read()

old = '''        level.setBlock(center, XuanhuanEarth.SETTLEMENT_ANCHOR.get().defaultBlockState(), FLAGS);
        if (level.getBlockEntity(center) instanceof XuanhuanSettlementAnchorBlockEntity anchor) {
            anchor.configure(type.id(), layout.ordinal(), palette.id());
        }
        return true;'''

new = '''        level.setBlock(center, XuanhuanEarth.SETTLEMENT_ANCHOR.get().defaultBlockState(), FLAGS);
        if (level.getBlockEntity(center) instanceof XuanhuanSettlementAnchorBlockEntity anchor) {
            anchor.configure(type.id(), layout.ordinal(), palette.id());
            // Record the real standing spots for this layout. The anchor cannot derive them
            // from the layout enum alone, because each builder places its buildings
            // differently, and a mismatch spawns residents inside walls.
            anchor.setSpawnOffsets(spawnOffsets(layout));
        }
        return true;'''

if old in s:
    s = s.replace(old, new, 1)
    print('configure extended')
else:
    print('MISS configure')

# --- Add the offset table. Kept next to the builders so it can be checked against them.
anchor = '    private static void pave(WorldGenLevel level, BlockPos center, int radius,'

table = '''    /**
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

'''

if anchor in s:
    s = s.replace(anchor, table + anchor, 1)
    print('offset table added')
else:
    print('MISS anchor')

io.open(p, 'w', encoding='utf-8', newline='\n').write(s)

# --- Anchor entity: store and use the recorded offsets.
p2 = base + r'\XuanhuanSettlementAnchorBlockEntity.java'
s2 = io.open(p2, encoding='utf-8').read()

old_spots = '''        List<BlockPos> spots = spawnSpots(pos, shape);'''

new_spots = '''        List<BlockPos> spots = recordedSpots(pos, shape);'''

if old_spots in s2:
    s2 = s2.replace(old_spots, new_spots, 1)
    print('spot call swapped')
else:
    print('MISS spot call')

old_method = '''    /** Where residents stand depends on how the site was laid out. */
    private static List<BlockPos> spawnSpots(BlockPos pos, SettlementStyle.Layout shape) {
        return switch (shape) {
            case CROSS -> List.of(
                    pos.offset(-4, 1, -3), pos.offset(4, 1, -3),
                    pos.offset(-4, 1, 3), pos.offset(4, 1, 3));
            case STREET -> List.of(
                    pos.offset(-5, 1, -3), pos.offset(0, 1, -3), pos.offset(5, 1, -3));
            case HALL -> List.of(
                    pos.offset(0, 1, -2), pos.offset(-5, 1, 3), pos.offset(5, 1, 3));
            case RING -> List.of(
                    pos.offset(-4, 1, 0), pos.offset(4, 1, 0), pos.offset(0, 1, -4));
        };
    }'''

new_method = '''    /**
     * Where residents stand. Prefers the offsets the feature recorded at generation time,
     * because only the builder knows where it actually put the buildings.
     */
    private static List<BlockPos> recordedSpots(BlockPos pos, SettlementStyle.Layout shape) {
        if (spawnOffsets.length > 0) {
            List<BlockPos> spots = new ArrayList<>(spawnOffsets.length);
            for (int[] offset : spawnOffsets) {
                spots.add(pos.offset(offset[0], offset[1], offset[2]));
            }
            return spots;
        }
        return switch (shape) {
            case CROSS -> List.of(
                    pos.offset(-4, 1, -3), pos.offset(4, 1, -3),
                    pos.offset(-4, 1, 3), pos.offset(4, 1, 3));
            case STREET -> List.of(
                    pos.offset(-4, 1, -3), pos.offset(0, 1, -3), pos.offset(4, 1, -3));
            case HALL -> List.of(
                    pos.offset(0, 1, -3), pos.offset(-5, 1, 3), pos.offset(5, 1, 3));
            case RING -> List.of(
                    pos.offset(-4, 1, 0), pos.offset(4, 1, 0), pos.offset(0, 1, -4));
        };
    }'''

if old_method in s2:
    s2 = s2.replace(old_method, new_method, 1)
    print('method replaced')
else:
    print('MISS method')

# Field + accessor + persistence.
old_fields = '''    private boolean initialized;
    private int warmupTicks;'''

new_fields = '''    private boolean initialized;
    private int warmupTicks;
    private int[][] spawnOffsets = new int[0][];'''

if old_fields in s2:
    s2 = s2.replace(old_fields, new_fields, 1)
    print('field added')
else:
    print('MISS field')

old_conf = '''    public void configure(String settlementType, int layout, String palette) {'''

new_conf = '''    public void setSpawnOffsets(int[][] offsets) {
        this.spawnOffsets = offsets == null ? new int[0][] : offsets;
    }

    public void configure(String settlementType, int layout, String palette) {'''

if old_conf in s2:
    s2 = s2.replace(old_conf, new_conf, 1)
    print('setter added')
else:
    print('MISS setter')

io.open(p2, 'w', encoding='utf-8', newline='\n').write(s2)
