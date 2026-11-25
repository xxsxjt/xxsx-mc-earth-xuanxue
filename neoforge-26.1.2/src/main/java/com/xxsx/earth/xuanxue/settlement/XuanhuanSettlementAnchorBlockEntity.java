package com.xxsx.earth.xuanxue.settlement;

import com.xxsx.earth.xuanxue.XuanhuanEarth;
import com.xxsx.earth.xuanxue.entity.ContractableSpiritBeastEntity;
import com.xxsx.earth.xuanxue.entity.CultivationSettlerEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import java.util.ArrayList;
import java.util.List;

/**
 * Settlement anchor: records how this site was generated and populates it once.
 *
 * <p>Layout and palette are stored so a site keeps its identity across reloads, and the
 * resident roster is derived from the layout rather than being a fixed list per type. That
 * is what stops every market from containing the same three people in the same three spots.</p>
 */
public final class XuanhuanSettlementAnchorBlockEntity extends BlockEntity {
    private String settlementType = XuanhuanSettlementFeature.Type.WANDERING_MARKET.id();
    private int layout = 0;
    private String palette = "spruce";
    private boolean initialized;
    private int warmupTicks;
    private int[][] spawnOffsets = new int[0][];

    public XuanhuanSettlementAnchorBlockEntity(BlockPos pos, BlockState state) {
        super(XuanhuanEarth.SETTLEMENT_ANCHOR_BLOCK_ENTITY.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state,
                                  XuanhuanSettlementAnchorBlockEntity anchor) {
        if (anchor.initialized || !(level instanceof ServerLevel serverLevel)) {
            return;
        }
        if (++anchor.warmupTicks < 40) {
            return;
        }
        anchor.spawnSettlementResidents(serverLevel, pos);
        anchor.initialized = true;
        anchor.setChanged();
    }

    private void spawnSettlementResidents(ServerLevel level, BlockPos pos) {
        XuanhuanSettlementFeature.Type type = XuanhuanSettlementFeature.Type.byId(settlementType);
        SettlementStyle.Layout shape = SettlementStyle.Layout.values()[
                Math.max(0, Math.min(SettlementStyle.Layout.values().length - 1, layout))];
        RandomSource random = level.getRandom();

        List<CultivationSettlerEntity.Role> roster = new ArrayList<>(rosterFor(type));
        // Shuffle so the same role does not always stand in the same corner.
        for (int i = roster.size() - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            CultivationSettlerEntity.Role tmp = roster.get(i);
            roster.set(i, roster.get(j));
            roster.set(j, tmp);
        }

        List<BlockPos> spots = recordedSpots(pos, shape, spawnOffsets);
        for (int i = 0; i < Math.min(roster.size(), spots.size()); i++) {
            spawnResident(level, spots.get(i), pos, roster.get(i));
        }

        if (type == XuanhuanSettlementFeature.Type.SPIRIT_SPRING_VILLAGE
                || type == XuanhuanSettlementFeature.Type.SECT_OUTPOST
                || type == XuanhuanSettlementFeature.Type.SPIRIT_BEAST_KEEPER_CAMP
                || type == XuanhuanSettlementFeature.Type.MOUNTAIN_SPIRIT_SHRINE) {
            // A fixed corner can be water, a wall or a paved tile, so search outward instead
            // of trusting one hard-coded offset.
            spawnBeastNear(level, pos, randomBeast(random));
        }
        XuanhuanEarth.LOGGER.info("Initialized xuanhuan settlement {} layout={} palette={} residents={} at {}",
                settlementType, shape, palette, roster.size(), pos);
    }

    private static List<CultivationSettlerEntity.Role> rosterFor(XuanhuanSettlementFeature.Type type) {
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

    /**
     * Where residents stand. Prefers the offsets the feature recorded at generation time,
     * because only the builder knows where it actually put the buildings.
     */
    private static List<BlockPos> recordedSpots(BlockPos pos, SettlementStyle.Layout shape, int[][] spawnOffsets) {
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
    }

    private static EntityType<? extends ContractableSpiritBeastEntity> randomBeast(RandomSource random) {
        return switch (random.nextInt(5)) {
            case 0 -> XuanhuanEarth.SPIRIT_FOX.get();
            case 1 -> XuanhuanEarth.WIND_WOLF.get();
            case 2 -> XuanhuanEarth.STONEHORN_GOAT.get();
            case 3 -> XuanhuanEarth.CRYSTAL_TURTLE.get();
            default -> XuanhuanEarth.EMBER_CRANE.get();
        };
    }

    private static void spawnResident(ServerLevel level, BlockPos pos, BlockPos settlementAnchor,
                                      CultivationSettlerEntity.Role role) {
        CultivationSettlerEntity resident = XuanhuanEarth.CULTIVATION_SETTLER.get()
                .create(level, EntitySpawnReason.STRUCTURE);
        if (resident == null) {
            return;
        }
        BlockPos spawnPos = findOpenSpawn(level, pos);
        resident.setRole(role);
        resident.getPersistentData().putString("EarthOnMinecraftResidentRole",
                "earth_xuanxue:" + role.name().toLowerCase(java.util.Locale.ROOT));
        resident.getPersistentData().putString("EarthOnMinecraftSettlementProfile",
                "earth_core:cultivation_settlement");
        resident.getPersistentData().putInt("EarthOnMinecraftSettlementAnchorX", settlementAnchor.getX());
        resident.getPersistentData().putInt("EarthOnMinecraftSettlementAnchorY", settlementAnchor.getY());
        resident.getPersistentData().putInt("EarthOnMinecraftSettlementAnchorZ", settlementAnchor.getZ());
        resident.snapTo(spawnPos.getX() + 0.5D, spawnPos.getY(), spawnPos.getZ() + 0.5D,
                level.getRandom().nextFloat() * 360.0F, 0.0F);
        resident.setPersistenceRequired();
        level.addFreshEntity(resident);
    }

    private static void spawnBeastNear(ServerLevel level, BlockPos anchor,
                                       EntityType<? extends ContractableSpiritBeastEntity> type) {
        // Ring search outward from the anchor so the beast lands on open ground.
        for (int radius = 2; radius <= 6; radius++) {
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    if (Math.max(Math.abs(dx), Math.abs(dz)) != radius) {
                        continue;
                    }
                    BlockPos candidate = findOpenSpawn(level, anchor.offset(dx, 1, dz));
                    if (!level.isEmptyBlock(candidate)
                            || level.isEmptyBlock(candidate.below())) {
                        continue;
                    }
                    ContractableSpiritBeastEntity beast = type.create(level, EntitySpawnReason.STRUCTURE);
                    if (beast == null) {
                        return;
                    }
                    beast.snapTo(candidate.getX() + 0.5D, candidate.getY(), candidate.getZ() + 0.5D,
                            level.getRandom().nextFloat() * 360.0F, 0.0F);
                    beast.setPersistenceRequired();
                    level.addFreshEntity(beast);
                    return;
                }
            }
        }
    }

    private static BlockPos findOpenSpawn(ServerLevel level, BlockPos preferred) {
        int[] verticalOffsets = {-1, 0, 1, -2};
        for (int verticalOffset : verticalOffsets) {
            for (int radius = 0; radius <= 3; radius++) {
                for (int x = -radius; x <= radius; x++) {
                    for (int z = -radius; z <= radius; z++) {
                        if (Math.max(Math.abs(x), Math.abs(z)) != radius) {
                            continue;
                        }
                        BlockPos candidate = preferred.offset(x, verticalOffset, z);
                        if (!level.isEmptyBlock(candidate.below())
                                && level.isEmptyBlock(candidate)
                                && level.isEmptyBlock(candidate.above())) {
                            return candidate;
                        }
                    }
                }
            }
        }
        return preferred;
    }

    public void setSpawnOffsets(int[][] offsets) {
        this.spawnOffsets = offsets == null ? new int[0][] : offsets;
    }

    public void configure(String settlementType, int layout, String palette) {
        this.settlementType = XuanhuanSettlementFeature.Type.byId(settlementType).id();
        this.layout = Math.max(0, layout);
        this.palette = palette == null || palette.isBlank() ? "spruce" : palette;
        this.initialized = false;
        this.warmupTicks = 0;
        setChanged();
    }

    public String settlementType() {
        return settlementType;
    }

    public int layout() {
        return layout;
    }

    public String palette() {
        return palette;
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putString("SettlementType", settlementType);
        output.putInt("Layout", layout);
        output.putString("Palette", palette);
        output.putBoolean("Initialized", initialized);
        output.putInt("WarmupTicks", warmupTicks);
        // Flattened as x,y,z triples so the roster survives a reload.
        int[] flat = new int[spawnOffsets.length * 3];
        for (int i = 0; i < spawnOffsets.length; i++) {
            flat[i * 3] = spawnOffsets[i][0];
            flat[i * 3 + 1] = spawnOffsets[i][1];
            flat[i * 3 + 2] = spawnOffsets[i][2];
        }
        output.putIntArray("SpawnOffsets", flat);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        settlementType = XuanhuanSettlementFeature.Type.byId(
                input.getStringOr("SettlementType", XuanhuanSettlementFeature.Type.WANDERING_MARKET.id())).id();
        layout = input.getIntOr("Layout", 0);
        palette = input.getStringOr("Palette", "spruce");
        initialized = input.getBooleanOr("Initialized", false);
        warmupTicks = input.getIntOr("WarmupTicks", 0);
        int[] flat = input.getIntArray("SpawnOffsets").orElse(new int[0]);
        spawnOffsets = new int[flat.length / 3][3];
        for (int i = 0; i + 2 < flat.length; i += 3) {
            spawnOffsets[i / 3] = new int[]{flat[i], flat[i + 1], flat[i + 2]};
        }
    }
}
