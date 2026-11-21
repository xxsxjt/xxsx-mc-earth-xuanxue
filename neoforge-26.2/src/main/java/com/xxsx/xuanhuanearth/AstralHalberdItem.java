package com.xxsx.xuanhuanearth;

import com.xxsx.xuanhuanearth.particle.XuanhuanParticles;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Fangtian Astral Halberd (方天星辰戟) — signature Boss drop of Astral Sovereign.
 *
 * <p>Right-click unleashes a sweeping Astral Cleave energy arc along the player's gaze,
 * capped by solid-block occlusion and deduplicated so each target takes damage and
 * knockback at most once per cast.</p>
 */
public class AstralHalberdItem extends Item {
    public static final double SWEEP_MAX_RANGE = 10.0D;
    public static final float SWEEP_DAMAGE = 12.0F;
    public static final int SWEEP_COOLDOWN_TICKS = 60;

    public AstralHalberdItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.getCooldowns().isOnCooldown(stack)) {
            return InteractionResult.PASS;
        }
        if (level instanceof ServerLevel serverLevel) {
            Vec3 look = player.getLookAngle().normalize();
            Vec3 origin = player.getEyePosition();
            Vec3 desiredEnd = origin.add(look.scale(SWEEP_MAX_RANGE));

            BlockHitResult blockHit = serverLevel.clip(new ClipContext(
                    origin,
                    desiredEnd,
                    ClipContext.Block.COLLIDER,
                    ClipContext.Fluid.NONE,
                    player));
            double effectiveRange = blockHit.getType() == HitResult.Type.BLOCK
                    ? origin.distanceTo(blockHit.getLocation())
                    : SWEEP_MAX_RANGE;
            int maxStep = Math.max(1, (int) Math.floor(effectiveRange));

            Set<LivingEntity> alreadyHit = new HashSet<>();
            for (int step = 1; step <= maxStep; step++) {
                Vec3 center = origin.add(look.scale(step));
                serverLevel.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, center.x, center.y, center.z, 5, 0.65, 0.25, 0.65, 0.02);
                serverLevel.sendParticles(XuanhuanParticles.QI_SPARK.get(), center.x, center.y, center.z, 3, 0.55, 0.22, 0.55, 0.03);
                serverLevel.sendParticles(ParticleTypes.END_ROD, center.x, center.y, center.z, 2, 0.45, 0.2, 0.45, 0.01);

                AABB box = new AABB(
                        center.x - 1.6D, center.y - 1.2D, center.z - 1.6D,
                        center.x + 1.6D, center.y + 1.2D, center.z + 1.6D);
                List<LivingEntity> targets = serverLevel.getEntitiesOfClass(
                        LivingEntity.class, box, e -> e != player && e.isAlive());
                for (LivingEntity target : targets) {
                    if (alreadyHit.add(target)) {
                        target.hurtServer(serverLevel, player.damageSources().playerAttack(player), SWEEP_DAMAGE);
                        target.knockback(0.6D, -look.x, -look.z);
                    }
                }
            }
            serverLevel.playSound(null, player.blockPosition(),
                    SoundEvents.TRIDENT_THUNDER.value(), SoundSource.PLAYERS, 0.9F, 1.35F);
            player.getCooldowns().addCooldown(stack, SWEEP_COOLDOWN_TICKS);
            stack.hurtAndBreak(2, player, hand.asEquipmentSlot());
        }
        player.swing(hand);
        return InteractionResult.SUCCESS;
    }
}
