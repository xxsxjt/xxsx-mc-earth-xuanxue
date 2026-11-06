package com.xxsx.xuanhuanearth;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class ArcanaChunkField extends SavedData {
    private static final long REFRESH_INTERVAL_TICKS = 600L;
    private static final double MAX_DEPLETION = 45.0D;
    private static final double DEPLETION_RECOVERY_PER_TICK = 0.003D;
    private static final int SCAN_RADIUS_XZ = 8;
    private static final int SCAN_RADIUS_Y = 5;
    private static final int MAX_SOURCE_CACHE_ENTRIES = 256;

    private static final Codec<ArcanaChunkField> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.unboundedMap(Codec.STRING, DepletionState.CODEC)
                    .optionalFieldOf("chunks", Map.<String, DepletionState>of())
                    .forGetter(data -> data.depletedChunks),
            Codec.unboundedMap(Codec.STRING, BlockPos.CODEC)
                    .optionalFieldOf("acupoints", Map.<String, BlockPos>of())
                    .forGetter(data -> data.acupoints)
    ).apply(instance, ArcanaChunkField::new));
    private static final SavedDataType<ArcanaChunkField> TYPE = new SavedDataType<>(
            XuanhuanEarth.id("arcana_chunk_field"), ArcanaChunkField::new, CODEC);

    private final Map<String, DepletionState> depletedChunks = new HashMap<>();
    private final Map<String, BlockPos> acupoints = new HashMap<>();
    private final Map<String, CachedSources> sourceCache = new LinkedHashMap<>(64, 0.75F, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<String, CachedSources> eldest) {
            return size() > MAX_SOURCE_CACHE_ENTRIES;
        }
    };
    private final Map<String, CachedVeins> veinCache = new LinkedHashMap<>(64, 0.75F, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<String, CachedVeins> eldest) {
            return size() > MAX_SOURCE_CACHE_ENTRIES;
        }
    };

    private ArcanaChunkField() {
    }

    private ArcanaChunkField(Map<String, DepletionState> states, Map<String, BlockPos> savedAcupoints) {
        states.forEach((key, state) -> {
            double depletion = clamp(state.depletion(), 0.0D, MAX_DEPLETION);
            if (depletion > 0.0D) {
                depletedChunks.put(key, new DepletionState(depletion, state.lastUpdate()));
            }
        });
        if (savedAcupoints != null) {
            acupoints.putAll(savedAcupoints);
        }
    }

    public static Reading read(Level level, BlockPos pos) {
        if (level instanceof ServerLevel serverLevel) {
            return data(serverLevel).readAt(serverLevel, pos);
        }
        return buildReading(level, pos, scanSources(level, pos), 0.0D);
    }

    /**
     * Field-side debit expressed in AEU. The chunk stores a dimensionless depletion
     * level, so the conversion happens here exactly once for every caller.
     */
    public static void consumeField(Level level, BlockPos pos, double aeu) {
        if (!Double.isFinite(aeu) || aeu <= 0.0D || !(level instanceof ServerLevel serverLevel)) {
            return;
        }
        data(serverLevel).consumeAt(serverLevel, pos, SpiritFieldLedger.fieldCostOfAeu(aeu));
    }

    /** Field-side debit already expressed in dimensionless field levels. */
    public static void consumeFieldLevels(Level level, BlockPos pos, double levels) {
        if (!Double.isFinite(levels) || levels <= 0.0D || !(level instanceof ServerLevel serverLevel)) {
            return;
        }
        data(serverLevel).consumeAt(serverLevel, pos, levels);
    }

    public static Optional<BlockPos> getAcupoint(Level level, BlockPos pos) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return Optional.empty();
        }
        return Optional.ofNullable(data(serverLevel).acupoints.get(chunkKey(serverLevel, pos)));
    }

    public static boolean isAcupoint(Level level, BlockPos pos) {
        return getAcupoint(level, pos).map(pos::equals).orElse(false);
    }

    /** X-071: Computes weighted source centroid in the chunk of origin and persists the surface acupoint. */
    public static BlockPos locateAndMarkAcupoint(ServerLevel level, BlockPos origin) {
        ChunkPos chunkPos = ChunkPos.containing(origin);
        LevelChunk chunk = level.getChunk(chunkPos.x(), chunkPos.z());
        double weightedX = 0.0D;
        double weightedZ = 0.0D;
        double totalWeight = 0.0D;

        LevelChunkSection[] sections = chunk.getSections();
        int minSectionY = level.getMinSectionY();
        for (int s = 0; s < sections.length; s++) {
            LevelChunkSection section = sections[s];
            if (section == null || section.hasOnlyAir()) {
                continue;
            }
            int baseY = (minSectionY + s) << 4;
            for (int ly = 0; ly < 16; ly++) {
                for (int lz = 0; lz < 16; lz++) {
                    for (int lx = 0; lx < 16; lx++) {
                        Block block = section.getBlockState(lx, ly, lz).getBlock();
                        int weight = 0;
                        if (block == XuanhuanEarth.SPIRIT_VEIN_NODE.get()) {
                            weight = 18;
                        } else if (block == XuanhuanEarth.SPIRIT_SPRING_STONE.get()) {
                            weight = 13;
                        } else if (block == XuanhuanEarth.SPIRIT_SOIL.get()) {
                            weight = 5;
                        }
                        if (weight > 0) {
                            weightedX += (chunkPos.getMinBlockX() + lx) * weight;
                            weightedZ += (chunkPos.getMinBlockZ() + lz) * weight;
                            totalWeight += weight;
                        }
                    }
                }
            }
        }

        int targetX;
        int targetZ;
        if (totalWeight > 0.0D) {
            targetX = clamp((int) Math.round(weightedX / totalWeight), chunkPos.getMinBlockX(), chunkPos.getMaxBlockX());
            targetZ = clamp((int) Math.round(weightedZ / totalWeight), chunkPos.getMinBlockZ(), chunkPos.getMaxBlockZ());
        } else {
            targetX = chunkPos.getMiddleBlockX();
            targetZ = chunkPos.getMiddleBlockZ();
        }
        int surfaceAbove = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, targetX, targetZ);
        int clampedY = clamp(surfaceAbove, level.getMinY() + 1, level.getMaxY() - 1);
        BlockPos acupoint = new BlockPos(targetX, clampedY, targetZ);
        ArcanaChunkField fieldData = data(level);
        String key = chunkKey(level, acupoint);
        fieldData.acupoints.put(key, acupoint);
        fieldData.sourceCache.remove(key);
        fieldData.setDirty();
        return acupoint;
    }

    /** X-070: Finds nearest spirit_vein_node in loaded chunks within radius, caching per chunk for 600 ticks. */
    public static Optional<BlockPos> findNearestSpiritVein(Level level, BlockPos origin, int radius) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return Optional.empty();
        }
        ArcanaChunkField fieldData = data(serverLevel);
        long now = serverLevel.getGameTime();
        int chunkRadius = (radius + 15) >> 4;
        ChunkPos center = ChunkPos.containing(origin);
        double maxDistSq = (double) radius * (double) radius;
        BlockPos best = null;
        double bestDistSq = Double.MAX_VALUE;

        for (int dx = -chunkRadius; dx <= chunkRadius; dx++) {
            for (int dz = -chunkRadius; dz <= chunkRadius; dz++) {
                int cx = center.x() + dx;
                int cz = center.z() + dz;
                if (!serverLevel.hasChunk(cx, cz)) {
                    continue;
                }
                ChunkPos cp = new ChunkPos(cx, cz);
                String key = serverLevel.dimension().identifier() + "|" + cp.pack();
                CachedVeins cached = fieldData.veinCache.get(key);
                if (cached == null || now < cached.lastRefresh() || now - cached.lastRefresh() >= REFRESH_INTERVAL_TICKS) {
                    cached = new CachedVeins(scanChunkVeins(serverLevel, serverLevel.getChunk(cx, cz), cp), now);
                    fieldData.veinCache.put(key, cached);
                }
                for (BlockPos veinPos : cached.positions()) {
                    double distSq = veinPos.distSqr(origin);
                    if (distSq <= maxDistSq && distSq < bestDistSq) {
                        bestDistSq = distSq;
                        best = veinPos;
                    }
                }
            }
        }
        return Optional.ofNullable(best);
    }

    private static List<BlockPos> scanChunkVeins(ServerLevel level, LevelChunk chunk, ChunkPos cp) {
        List<BlockPos> found = new ArrayList<>();
        LevelChunkSection[] sections = chunk.getSections();
        int minSectionY = level.getMinSectionY();
        Block target = XuanhuanEarth.SPIRIT_VEIN_NODE.get();
        for (int s = 0; s < sections.length; s++) {
            LevelChunkSection section = sections[s];
            if (section == null || section.hasOnlyAir()) {
                continue;
            }
            int baseY = (minSectionY + s) << 4;
            for (int ly = 0; ly < 16; ly++) {
                for (int lz = 0; lz < 16; lz++) {
                    for (int lx = 0; lx < 16; lx++) {
                        if (section.getBlockState(lx, ly, lz).getBlock() == target) {
                            found.add(new BlockPos(cp.getMinBlockX() + lx, baseY + ly, cp.getMinBlockZ() + lz));
                        }
                    }
                }
            }
        }
        found.sort(Comparator.comparingInt(BlockPos::getY));
        return List.copyOf(found);
    }

    private static ArcanaChunkField data(ServerLevel level) {
        return level.getServer().getDataStorage().computeIfAbsent(TYPE);
    }

    private Reading readAt(ServerLevel level, BlockPos pos) {
        String key = chunkKey(level, pos);
        double depletion = currentDepletion(key, level.getGameTime());
        SourceScan sources = cachedSources(level, pos, key);
        return buildReading(level, pos, sources, depletion);
    }

    /** Applies a dimensionless field-level debit; callers convert AEU first. */
    private void consumeAt(ServerLevel level, BlockPos pos, double fieldLevels) {
        String key = chunkKey(level, pos);
        long now = level.getGameTime();
        double current = currentDepletion(key, now);
        double added = Math.max(0.5D, fieldLevels * 0.35D);
        double updated = clamp(current + added, 0.0D, MAX_DEPLETION);
        depletedChunks.put(key, new DepletionState(updated, now));
        setDirty();
    }

    private double currentDepletion(String key, long now) {
        DepletionState state = depletedChunks.get(key);
        if (state == null) {
            return 0.0D;
        }
        if (now < state.lastUpdate()) {
            depletedChunks.put(key, new DepletionState(state.depletion(), now));
            setDirty();
            return state.depletion();
        }
        double recovered = clamp(
                state.depletion() - (now - state.lastUpdate()) * DEPLETION_RECOVERY_PER_TICK,
                0.0D,
                MAX_DEPLETION);
        if (recovered <= 0.0D) {
            depletedChunks.remove(key);
            setDirty();
        }
        return recovered;
    }

    private SourceScan cachedSources(ServerLevel level, BlockPos pos, String key) {
        long now = level.getGameTime();
        CachedSources cached = sourceCache.get(key);
        if (cached == null
                || now < cached.lastRefresh()
                || now - cached.lastRefresh() >= REFRESH_INTERVAL_TICKS
                || Math.abs(pos.getY() - cached.anchorY()) > SCAN_RADIUS_Y) {
            SourceScan scan = scanSources(level, pos);
            sourceCache.put(key, new CachedSources(scan, now, pos.getY()));
            return scan;
        }
        return cached.sources();
    }

    private static Reading buildReading(Level level, BlockPos pos, SourceScan scan, double depletion) {
        int base = naturalBase(level, pos, ChunkPos.containing(pos));
        int value = clamp((int) Math.round(
                base + scan.vein() + scan.spring() + scan.flora() + scan.structure() - depletion), 0, 100);
        String sourceKey = depletion >= 28.0D && value < 35
                ? "spirituality.xuanhuan_earth.source.depleted"
                : scan.mainSourceKey();
        return new Reading(value, depletion, sourceKey);
    }

    private static String chunkKey(Level level, BlockPos pos) {
        return level.dimension().identifier() + "|" + ChunkPos.containing(pos).pack();
    }

    private static int naturalBase(Level level, BlockPos pos, ChunkPos chunk) {
        String dimension = level.dimension().identifier().toString();
        int value;
        if ("minecraft:overworld".equals(dimension)) {
            value = 34;
        } else if ("minecraft:the_nether".equals(dimension)) {
            value = 18;
        } else if ("minecraft:the_end".equals(dimension)) {
            value = 12;
        } else {
            value = 22;
        }

        int y = pos.getY();
        if (y < 0) {
            value += 12;
        } else if (y > 120) {
            value += 8;
        } else if (y > 72) {
            value += 4;
        }

        long seed = level instanceof ServerLevel serverLevel ? serverLevel.getSeed() : 0L;
        long mixed = mix(seed
                ^ ((long) chunk.x() * 0x9E3779B97F4A7C15L)
                ^ ((long) chunk.z() * 0xC2B2AE3D27D4EB4FL)
                ^ dimension.hashCode());
        value += Math.floorMod(mixed, 31) - 10;
        return clamp(value, 0, 100);
    }

    private static SourceScan scanSources(Level level, BlockPos pos) {
        int vein = 0;
        int spring = 0;
        int flora = 0;
        int structure = 0;

        int minX = pos.getX() - SCAN_RADIUS_XZ;
        int maxX = pos.getX() + SCAN_RADIUS_XZ;
        int minY = pos.getY() - SCAN_RADIUS_Y;
        int maxY = pos.getY() + SCAN_RADIUS_Y;
        int minZ = pos.getZ() - SCAN_RADIUS_XZ;
        int maxZ = pos.getZ() + SCAN_RADIUS_XZ;

        for (BlockPos sample : BlockPos.betweenClosed(minX, minY, minZ, maxX, maxY, maxZ)) {
            Block block = level.getBlockState(sample).getBlock();
            if (block == XuanhuanEarth.SPIRIT_VEIN_NODE.get()) {
                vein = Math.min(36, vein + 18);
            } else if (block == XuanhuanEarth.SPIRIT_SPRING_STONE.get()) {
                spring = Math.min(26, spring + 13);
            } else if (block == XuanhuanEarth.SPIRIT_SOIL.get()) {
                flora = Math.min(20, flora + 5);
            } else if (block == XuanhuanEarth.SPIRIT_ARRAY_CORE.get()) {
                boolean formal = XuanhuanStructures.isFormalSpiritArray(level, sample);
                boolean onAcupoint = isAcupoint(level, sample);
                int arrayBonus;
                if (level.getBlockEntity(sample) instanceof XuanhuanMachineBlockEntity machine) {
                    arrayBonus = machine.arrayFieldBonus(formal, onAcupoint);
                } else {
                    arrayBonus = formal ? (onAcupoint ? 32 : 28) : 12;
                }
                structure = Math.min(36, structure + arrayBonus);
            } else if (block == XuanhuanEarth.MEDITATION_CUSHION.get()) {
                structure = Math.min(30, structure + 8);
            } else if (block == XuanhuanEarth.ALCHEMY_FURNACE.get()
                    || block == XuanhuanEarth.TALISMAN_TABLE.get()) {
                structure = Math.min(28, structure + 4);
            }
        }

        String sourceKey = "spirituality.xuanhuan_earth.source.natural";
        int strongest = 0;
        if (vein > strongest) {
            strongest = vein;
            sourceKey = "spirituality.xuanhuan_earth.source.vein";
        }
        if (spring > strongest) {
            strongest = spring;
            sourceKey = "spirituality.xuanhuan_earth.source.spring";
        }
        if (structure > strongest) {
            strongest = structure;
            sourceKey = "spirituality.xuanhuan_earth.source.array";
        }
        if (flora > strongest) {
            sourceKey = "spirituality.xuanhuan_earth.source.flora";
        }
        return new SourceScan(vein, spring, flora, structure, sourceKey);
    }

    private static long mix(long value) {
        value ^= value >>> 33;
        value *= 0xff51afd7ed558ccdL;
        value ^= value >>> 33;
        value *= 0xc4ceb9fe1a85ec53L;
        value ^= value >>> 33;
        return value;
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    public record Reading(int value, double depletion, String mainSourceKey) {
        public net.minecraft.network.chat.Component mainSource() {
            return net.minecraft.network.chat.Component.translatable(mainSourceKey);
        }
    }

    private record SourceScan(int vein, int spring, int flora, int structure, String mainSourceKey) {
    }

    private record CachedSources(SourceScan sources, long lastRefresh, int anchorY) {
    }

    private record CachedVeins(List<BlockPos> positions, long lastRefresh) {
    }

    private record DepletionState(double depletion, long lastUpdate) {
        private static final Codec<DepletionState> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.DOUBLE.fieldOf("depletion").forGetter(DepletionState::depletion),
                Codec.LONG.fieldOf("last_update").forGetter(DepletionState::lastUpdate)
        ).apply(instance, DepletionState::new));
    }
}
