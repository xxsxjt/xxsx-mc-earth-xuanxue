package com.earthseries.astralsovereign.item;

import java.util.List;
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
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Fangtian Astral Halberd (方天星辰戟) - Boss Signature Drop.
 * Right-click unleashes a sweeping Astral Cleave Energy Wave along the player's gaze.
 */
public class AstralHalberdItem extends Item {
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

            // Sweep glowing cyan energy arc forward up to 10 blocks
            for (int step = 1; step <= 10; step++) {
                Vec3 center = origin.add(look.scale(step));
                serverLevel.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, center.x, center.y, center.z, 6, 0.7, 0.25, 0.7, 0.02);
                serverLevel.sendParticles(ParticleTypes.END_ROD, center.x, center.y, center.z, 2, 0.5, 0.2, 0.5, 0.01);

                AABB box = new AABB(center.x - 1.6, center.y - 1.2, center.z - 1.6, center.x + 1.6, center.y + 1.2, center.z + 1.6);
                List<LivingEntity> targets = serverLevel.getEntitiesOfClass(LivingEntity.class, box, e -> e != player && e.isAlive());
                for (LivingEntity target : targets) {
                    target.hurtServer(serverLevel, player.damageSources().playerAttack(player), 12.0F);
                    target.knockback(0.6D, -look.x, -look.z);
                }
            }

            serverLevel.playSound(null, player.blockPosition(), SoundEvents.TRIDENT_THUNDER.value(), SoundSource.PLAYERS, 0.9F, 1.35F);
            player.getCooldowns().addCooldown(stack, 60);
            stack.hurtAndBreak(2, player, hand.asEquipmentSlot());
        }
        player.swing(hand);
        return InteractionResult.SUCCESS;
    }
}
