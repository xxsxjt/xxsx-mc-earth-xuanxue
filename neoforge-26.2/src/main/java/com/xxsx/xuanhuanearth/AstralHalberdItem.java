package com.xxsx.xuanhuanearth;

import com.xxsx.xuanhuanearth.entity.AstralFlyingSwordEntity;
import com.xxsx.xuanhuanearth.particle.XuanhuanParticles;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Astral Fangtian Halberd (方天星辰戟) — signature weapon dropped by Astral Sovereign.
 *
 * <p>Right-clicking chains through 3 distinct starlight halberd techniques within a 4.5s window:
 * <ol>
 *   <li>Horizontal Starlight Crescent Sweep (wide 130° layered arc cleave).</li>
 *   <li>Overhead Sky-Splitter Cleave (vertical starlight blade curtain + ground seal wave).</li>
 *   <li>Astral Dragon Spiral Thrust (long-range double-helix piercing lance + shock ring).</li>
 * </ol>
 * All 3 styles respect solid block raycast collision, deduplicate hits per swing, and can
 * parry/deflect incoming {@link AstralFlyingSwordEntity} projectiles.</p>
 */
public class AstralHalberdItem extends Item {
    public static final int COOLDOWN_TICKS = 36;
    public static final int COMBO_WINDOW_TICKS = 90;
    public static final double SWEEP_MAX_RANGE = 12.0D;
    public static final float SWEEP_DAMAGE = 12.0F;
    public static final float OVERHEAD_DAMAGE = 14.5F;
    public static final float THRUST_DAMAGE = 13.0F;

    private static final Map<UUID, ComboMemory> PLAYER_COMBOS = new HashMap<>();

    private record ComboMemory(int nextStep, long expiresAtTick) {
    }

    public AstralHalberdItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        player.swing(hand, true);
        if (level instanceof ServerLevel serverLevel) {
            long now = serverLevel.getGameTime();
            ComboMemory prev = PLAYER_COMBOS.get(player.getUUID());
            int style = (prev != null && now <= prev.expiresAtTick()) ? prev.nextStep() : 0;
            PLAYER_COMBOS.put(player.getUUID(), new ComboMemory((style + 1) % 3, now + COMBO_WINDOW_TICKS));

            Vec3 look = player.getLookAngle().normalize();
            Vec3 origin = player.getEyePosition().add(0.0, -0.2, 0.0);
            double maxRange = style == 2 ? SWEEP_MAX_RANGE + 2.0D : SWEEP_MAX_RANGE;

            // Respect solid wall collisions
            Vec3 desiredEnd = origin.add(look.scale(maxRange));
            BlockHitResult blockHit = serverLevel.clip(new ClipContext(
                    origin, desiredEnd, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
            double effectiveRange = blockHit.getType() == HitResult.Type.BLOCK
                    ? Math.max(1.5D, origin.distanceTo(blockHit.getLocation()))
                    : maxRange;

            if (style == 0) {
                executeHorizontalStarSweep(serverLevel, player, origin, look, Math.min(effectiveRange, 7.5D));
            } else if (style == 1) {
                executeOverheadSkySplitter(serverLevel, player, origin, look, effectiveRange);
            } else {
                executeSpiralDragonThrust(serverLevel, player, origin, look, effectiveRange);
            }

            player.getCooldowns().addCooldown(player.getItemInHand(hand), COOLDOWN_TICKS);
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.CONSUME;
    }

    private void executeHorizontalStarSweep(
            ServerLevel level, Player player, Vec3 origin, Vec3 look, double range
    ) {
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 1.35F, 0.85F);
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.TRIDENT_THROW.value(), SoundSource.PLAYERS, 0.9F, 1.3F);

        Vec3 horiz = new Vec3(look.x, 0.0, look.z);
        horiz = horiz.lengthSqr() > 1.0E-5 ? horiz.normalize() : new Vec3(0.0, 0.0, 1.0);
        Vec3 right = new Vec3(-horiz.z, 0.0, horiz.x);

        Set<LivingEntity> alreadyHit = new HashSet<>();
        for (int rStep = 2; rStep <= (int) Math.floor(range); rStep++) {
            double rad = rStep * 0.95;
            for (int deg = -65; deg <= 65; deg += 10) {
                double a = Math.toRadians(deg);
                Vec3 dir = horiz.scale(Math.cos(a)).add(right.scale(Math.sin(a)));
                Vec3 p = origin.add(dir.scale(rad));
                level.sendParticles(rStep >= (int) range - 1 ? ParticleTypes.END_ROD : XuanhuanParticles.QI_SPARK.get(),
                        p.x, p.y, p.z, 1, 0.04, 0.06, 0.04, 0.02);
                if (deg % 30 == 0 && rStep == 4) {
                    level.sendParticles(ParticleTypes.SWEEP_ATTACK, p.x, p.y, p.z, 1, 0, 0, 0, 0);
                }
                damageAndParryInBox(level, player, p, 1.35, SWEEP_DAMAGE, look, 0.65D, alreadyHit);
            }
        }
    }

    private void executeOverheadSkySplitter(
            ServerLevel level, Player player, Vec3 origin, Vec3 look, double range
    ) {
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.TRIDENT_THUNDER.value(), SoundSource.PLAYERS, 0.85F, 1.25F);

        Set<LivingEntity> alreadyHit = new HashSet<>();
        int steps = Math.max(2, (int) Math.floor(range));
        for (int s = 1; s <= steps; s++) {
            double frac = s / (double) steps;
            double verticalArc = Math.sin(frac * Math.PI) * 1.8 - 0.35;
            Vec3 p = origin.add(look.scale(s)).add(0.0, verticalArc, 0.0);
            level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, p.x, p.y, p.z, 2, 0.08, 0.35, 0.08, 0.02);
            level.sendParticles(XuanhuanParticles.EMBER_SPARK.get(), p.x, p.y - 0.35, p.z, 2, 0.08, 0.2, 0.08, 0.03);
            damageAndParryInBox(level, player, p, 1.5, OVERHEAD_DAMAGE, look, 0.55D, alreadyHit);
        }

        // Terminal impact seal ring
        Vec3 impact = origin.add(look.scale(range));
        level.sendParticles(XuanhuanParticles.SEAL_RING.get(), impact.x, impact.y - 0.8, impact.z, 1, 0, 0, 0, 0);
        level.sendParticles(XuanhuanParticles.QI_RING.get(), impact.x, impact.y - 0.4, impact.z, 1, 0, 0, 0, 0);
    }

    private void executeSpiralDragonThrust(
            ServerLevel level, Player player, Vec3 origin, Vec3 look, double range
    ) {
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.TRIDENT_RIPTIDE_2.value(), SoundSource.PLAYERS, 1.1F, 1.25F);

        Vec3 upRef = Math.abs(look.y) > 0.92 ? new Vec3(1.0, 0.0, 0.0) : new Vec3(0.0, 1.0, 0.0);
        Vec3 right = look.cross(upRef).normalize();
        Vec3 up = right.cross(look).normalize();

        Set<LivingEntity> alreadyHit = new HashSet<>();
        int steps = Math.max(4, (int) Math.floor(range * 2.0));
        for (int s = 1; s <= steps; s++) {
            double dist = s * 0.5;
            double spiral = s * 0.75;
            double rad = 0.55 * (1.0 - (s / (double) (steps + 4)));
            Vec3 center = origin.add(look.scale(dist));
            Vec3 off = right.scale(Math.cos(spiral) * rad).add(up.scale(Math.sin(spiral) * rad));
            level.sendParticles(ParticleTypes.END_ROD,
                    center.x + off.x, center.y + off.y, center.z + off.z, 1, 0.02, 0.02, 0.02, 0.01);
            level.sendParticles(XuanhuanParticles.QI_SPARK.get(),
                    center.x - off.x, center.y - off.y, center.z - off.z, 1, 0.02, 0.02, 0.02, 0.02);
            if (s % 2 == 0) {
                damageAndParryInBox(level, player, center, 1.25, THRUST_DAMAGE, look, 0.95D, alreadyHit);
            }
        }
        Vec3 tip = origin.add(look.scale(range));
        level.sendParticles(XuanhuanParticles.QI_RING.get(), tip.x, tip.y, tip.z, 1, 0, 0, 0, 0);
    }

    private void damageAndParryInBox(
            ServerLevel level,
            Player player,
            Vec3 center,
            double radius,
            float damage,
            Vec3 look,
            double knockback,
            Set<LivingEntity> alreadyHit
    ) {
        AABB box = new AABB(
                center.x - radius, center.y - 1.2, center.z - radius,
                center.x + radius, center.y + 1.2, center.z + radius);
        List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, box,
                e -> e != player && e.isAlive());
        for (LivingEntity target : targets) {
            if (alreadyHit.add(target)) {
                target.hurtServer(level, player.damageSources().playerAttack(player), damage);
                target.knockback(knockback, -look.x, -look.z);
            }
        }
        // Halberd waves also parry incoming AstralFlyingSwordEntity projectiles!
        List<AstralFlyingSwordEntity> swords = level.getEntitiesOfClass(AstralFlyingSwordEntity.class, box,
                AstralFlyingSwordEntity::isAlive);
        for (AstralFlyingSwordEntity sword : swords) {
            sword.deflectByEntity(level, player, look);
        }
    }
}
