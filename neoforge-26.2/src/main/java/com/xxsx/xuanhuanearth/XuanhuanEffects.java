package com.xxsx.xuanhuanearth;

import com.xxsx.xuanhuanearth.entity.ContractableSpiritBeastEntity;
import com.xxsx.xuanhuanearth.particle.XuanhuanParticles;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * Server-side effect choreography.
 *
 * <p>Every ritual in the mod shares a small vocabulary: a rising column of qi, a ground
 * ring, orbiting sparks, a rune that hangs in place, and a smoke plume. Keeping the shape
 * here means a talisman, an array and a furnace all read as the same world, while still
 * differing in colour and density.</p>
 */
public final class XuanhuanEffects {
    private XuanhuanEffects() {
    }

    /** Rising qi motes from a point, used by meditation and circulation. */
    public static void qiRise(ServerLevel level, Vec3 origin, double radius, int count, double speed) {
        RandomSource random = level.getRandom();
        for (int i = 0; i < count; i++) {
            double angle = random.nextDouble() * Mth.TWO_PI;
            double r = radius * Math.sqrt(random.nextDouble());
            double x = origin.x + Math.cos(angle) * r;
            double z = origin.z + Math.sin(angle) * r;
            double y = origin.y + random.nextDouble() * 0.15D;
            level.sendParticles(XuanhuanParticles.QI_MOTE.get(), x, y, z, 1,
                    0.0D, speed, 0.0D, 0.0D);
        }
    }

    /** Ground-anchored ring that expands and fades. */
    public static void groundRing(ServerLevel level, Vec3 origin, float scale) {
        level.sendParticles(XuanhuanParticles.QI_RING.get(), origin.x, origin.y + 0.05D, origin.z,
                1, 0.0D, 0.0D, 0.0D, 0.0D);
        if (scale > 1.0F) {
            for (int i = 0; i < 4; i++) {
                double angle = i * Mth.HALF_PI;
                level.sendParticles(XuanhuanParticles.QI_RING.get(),
                        origin.x + Math.cos(angle) * scale * 0.5D,
                        origin.y + 0.05D,
                        origin.z + Math.sin(angle) * scale * 0.5D,
                        1, 0.0D, 0.0D, 0.0D, 0.0D);
            }
        }
    }

    /** Sparks thrown outward in a horizontal fan. */
    public static void sparkBurst(ServerLevel level, Vec3 origin, int count, double speed) {
        RandomSource random = level.getRandom();
        for (int i = 0; i < count; i++) {
            double angle = random.nextDouble() * Mth.TWO_PI;
            double horizontal = speed * (0.45D + random.nextDouble() * 0.55D);
            level.sendParticles(XuanhuanParticles.QI_SPARK.get(), origin.x, origin.y, origin.z, 1,
                    Math.cos(angle) * horizontal, 0.06D + random.nextDouble() * 0.08D,
                    Math.sin(angle) * horizontal, 0.0D);
        }
    }

    /** Hanging rune glyphs above a point. */
    public static void runeCluster(ServerLevel level, Vec3 origin, int count, double radius,
                                   boolean jade) {
        RandomSource random = level.getRandom();
        ParticleOptions glyph = jade ? XuanhuanParticles.RUNE_GLYPH_B.get()
                : XuanhuanParticles.RUNE_GLYPH_A.get();
        for (int i = 0; i < count; i++) {
            double angle = Mth.TWO_PI * i / count + random.nextDouble() * 0.4D;
            double r = radius * (0.75D + random.nextDouble() * 0.35D);
            level.sendParticles(glyph,
                    origin.x + Math.cos(angle) * r,
                    origin.y + 0.35D + random.nextDouble() * 0.5D,
                    origin.z + Math.sin(angle) * r,
                    1, 0.0D, 0.0D, 0.0D, 0.0D);
        }
    }

    /** Slow vapour plume from a block or machine. */
    public static void smokePlume(ServerLevel level, Vec3 origin, boolean alchemy, int count) {
        ParticleOptions smoke = alchemy ? XuanhuanParticles.ALCHEMY_SMOKE.get()
                : XuanhuanParticles.SPIRIT_SMOKE.get();
        level.sendParticles(smoke, origin.x, origin.y, origin.z, count,
                0.22D, 0.05D, 0.22D, 0.01D);
    }

    /** Tumbling leaves or feathers. */
    public static void leafFall(ServerLevel level, Vec3 origin, int count, boolean ember) {
        ParticleOptions leaf = ember ? XuanhuanParticles.EMBER_LEAF.get()
                : XuanhuanParticles.SPIRIT_LEAF.get();
        level.sendParticles(leaf, origin.x, origin.y, origin.z, count,
                0.45D, 0.25D, 0.45D, 0.01D);
    }

    /** A complete meditation cycle: ring, motes, glyphs and a chime. */
    public static void meditationCycle(ServerLevel level, BlockPos pos, CultivationFocus focus,
                                       boolean supported) {
        Vec3 center = Vec3.atCenterOf(pos);
        groundRing(level, center, supported ? 1.35F : 1.0F);
        qiRise(level, center, supported ? 1.05D : 0.8D, supported ? 16 : 10, 0.02D);
        runeCluster(level, center, supported ? 4 : 2, supported ? 1.15D : 0.9D,
                focus == CultivationFocus.CIRCULATION || focus == CultivationFocus.FETAL_BREATH);
        sparkBurst(level, center.add(0.0D, 0.6D, 0.0D), supported ? 10 : 6, 0.06D);
        level.playSound(null, pos, SoundEvent.createVariableRangeEvent(
                net.minecraft.resources.Identifier.fromNamespaceAndPath("minecraft",
                        supported ? "block.amethyst_block.resonate" : "block.amethyst_block.chime")),
                SoundSource.PLAYERS, supported ? 0.8F : 0.5F, 0.9F + focus.id() * 0.05F);
    }

    /** Skill activation flash around the caster. */
    public static void skillBurst(ServerLevel level, Vec3 origin, CultivationFocus focus) {
        groundRing(level, origin, 1.2F);
        sparkBurst(level, origin.add(0.0D, 1.0D, 0.0D), 18, 0.12D);
        switch (focus) {
            case CIRCULATION -> qiRise(level, origin, 0.9D, 12, 0.03D);
            case BODY_TEMPERING -> level.sendParticles(XuanhuanParticles.EMBER_SPARK.get(),
                    origin.x, origin.y + 1.0D, origin.z, 14, 0.35D, 0.4D, 0.35D, 0.02D);
            case FETAL_BREATH -> level.sendParticles(XuanhuanParticles.SPIRIT_SMOKE.get(),
                    origin.x, origin.y + 1.1D, origin.z, 10, 0.4D, 0.2D, 0.4D, 0.01D);
            case BIGU -> leafFall(level, origin.add(0.0D, 1.2D, 0.0D), 10, false);
        }
    }

    /** Talisman activation: seal ring, gold glyphs and a brief flash. */
    public static void talismanActivation(ServerLevel level, Vec3 origin) {
        level.sendParticles(XuanhuanParticles.SEAL_RING.get(), origin.x, origin.y + 0.1D, origin.z,
                1, 0.0D, 0.0D, 0.0D, 0.0D);
        runeCluster(level, origin, 5, 0.85D, false);
        level.sendParticles(ParticleTypes.END_ROD, origin.x, origin.y + 1.0D, origin.z, 6,
                0.2D, 0.25D, 0.2D, 0.02D);
        sparkBurst(level, origin.add(0.0D, 1.0D, 0.0D), 14, 0.1D);
        level.playSound(null, BlockPos.containing(origin),
                net.minecraft.sounds.SoundEvents.AMETHYST_BLOCK_CHIME,
                SoundSource.PLAYERS, 0.75F, 1.2F);
    }

    /** Spirit stone charge draw. */
    public static void stoneCharge(ServerLevel level, Vec3 origin) {
        groundRing(level, origin, 0.85F);
        qiRise(level, origin, 0.7D, 12, 0.025D);
        level.sendParticles(XuanhuanParticles.QI_SPARK.get(), origin.x, origin.y + 1.0D, origin.z,
                8, 0.3D, 0.25D, 0.3D, 0.02D);
        level.playSound(null, BlockPos.containing(origin),
                net.minecraft.sounds.SoundEvents.AMETHYST_BLOCK_RESONATE,
                SoundSource.PLAYERS, 0.65F, 1.05F);
    }

    /** Spirit stone release. */
    public static void stoneRelease(ServerLevel level, Vec3 origin) {
        level.sendParticles(XuanhuanParticles.SPIRIT_FLAME.get(), origin.x, origin.y + 0.9D, origin.z,
                14, 0.35D, 0.4D, 0.35D, 0.02D);
        groundRing(level, origin, 1.0F);
        level.playSound(null, BlockPos.containing(origin),
                net.minecraft.sounds.SoundEvents.AMETHYST_BLOCK_CHIME,
                SoundSource.PLAYERS, 0.75F, 1.15F);
    }

    /** Ambient aura for a tame spirit beast. */
    public static void beastAura(Level level, Vec3 origin, ContractableSpiritBeastEntity.Kind kind) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }
        switch (kind) {
            case SPIRIT_FOX -> serverLevel.sendParticles(XuanhuanParticles.QI_MOTE.get(),
                    origin.x, origin.y, origin.z, 2, 0.28D, 0.2D, 0.28D, 0.01D);
            case WIND_WOLF -> serverLevel.sendParticles(XuanhuanParticles.WIND_RING.get(),
                    origin.x, origin.y + 0.05D, origin.z, 1, 0.0D, 0.0D, 0.0D, 0.0D);
            case STONEHORN_GOAT -> serverLevel.sendParticles(XuanhuanParticles.QI_SPARK.get(),
                    origin.x, origin.y + 0.5D, origin.z, 2, 0.3D, 0.15D, 0.3D, 0.01D);
            case CRYSTAL_TURTLE -> serverLevel.sendParticles(XuanhuanParticles.RUNE_GLYPH_B.get(),
                    origin.x, origin.y + 0.35D, origin.z, 1, 0.0D, 0.0D, 0.0D, 0.0D);
            case EMBER_CRANE -> serverLevel.sendParticles(XuanhuanParticles.EMBER_LEAF.get(),
                    origin.x, origin.y + 0.6D, origin.z, 2, 0.25D, 0.2D, 0.25D, 0.01D);
        }
    }

    /** Implement cast: a colour-coded burst matching the implement's element. */
    public static void implementCast(ServerLevel level, Vec3 origin,
                                     SpiritImplementItem.Implement kind) {
        groundRing(level, origin, 1.3F);
        BlockPos soundPos = BlockPos.containing(origin);
        switch (kind) {
            case WIND_FAN -> {
                level.sendParticles(XuanhuanParticles.WIND_RING.get(),
                        origin.x, origin.y + 0.4D, origin.z, 1, 0.0D, 0.0D, 0.0D, 0.0D);
                sparkBurst(level, origin.add(0.0D, 0.8D, 0.0D), 16, 0.14D);
                level.playSound(null, soundPos,
                        net.minecraft.sounds.SoundEvents.BREEZE_WIND_CHARGE_BURST.value(),
                        SoundSource.PLAYERS, 0.85F, 1.15F);
            }
            case STONE_SEAL -> {
                runeCluster(level, origin, 4, 1.0D, false);
                level.sendParticles(XuanhuanParticles.EMBER_SPARK.get(),
                        origin.x, origin.y + 0.6D, origin.z, 12, 0.4D, 0.3D, 0.4D, 0.02D);
                level.playSound(null, soundPos,
                        net.minecraft.sounds.SoundEvents.ANVIL_LAND,
                        SoundSource.PLAYERS, 0.55F, 0.85F);
            }
            case GATHER_ORB -> {
                qiRise(level, origin, 1.1D, 18, 0.03D);
                level.playSound(null, soundPos,
                        net.minecraft.sounds.SoundEvents.AMETHYST_BLOCK_RESONATE,
                        SoundSource.PLAYERS, 0.85F, 1.1F);
            }
            case LANTERN_ROD -> {
                runeCluster(level, origin, 3, 0.9D, true);
                level.sendParticles(XuanhuanParticles.QI_SPARK.get(),
                        origin.x, origin.y + 1.2D, origin.z, 10, 0.5D, 0.4D, 0.5D, 0.02D);
                level.playSound(null, soundPos,
                        net.minecraft.sounds.SoundEvents.BEACON_ACTIVATE,
                        SoundSource.PLAYERS, 0.65F, 1.35F);
            }
        }
    }

    /** Pill swallowed: a small upward puff plus a jade ring at the feet. */
    public static void pillBurst(ServerLevel level, Vec3 origin) {
        groundRing(level, origin, 0.8F);
        qiRise(level, origin, 0.5D, 8, 0.02D);
        level.sendParticles(XuanhuanParticles.QI_SPARK.get(), origin.x, origin.y + 0.9D, origin.z,
                6, 0.25D, 0.2D, 0.25D, 0.02D);
    }

    /** Realm breakthrough: a large, slow, multi-stage effect. */
    public static void realmBreakthrough(ServerLevel level, Vec3 origin) {
        for (int i = 0; i < 3; i++) {
            groundRing(level, origin, 1.2F + i * 0.7F);
        }
        qiRise(level, origin, 1.6D, 30, 0.05D);
        runeCluster(level, origin, 8, 1.6D, true);
        sparkBurst(level, origin.add(0.0D, 1.2D, 0.0D), 28, 0.18D);
        level.sendParticles(XuanhuanParticles.SPIRIT_FLAME.get(),
                origin.x, origin.y + 1.0D, origin.z, 20, 0.7D, 0.9D, 0.7D, 0.03D);
    }
}
