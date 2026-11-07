package com.xxsx.xuanhuanearth;

import com.xxsx.xuanhuanearth.entity.ContractableSpiritBeastEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public final class CultivationSkill {
    private CultivationSkill() {
    }

    public static boolean activate(ServerLevel level, ServerPlayer player) {
        CultivationFocus focus = ArcanaPower.getCultivationFocus(player);
        if (!focus.isUnlocked(player)) {
            player.sendSystemMessage(Component.translatable(
                    "message.xuanhuan_earth.skill.requires_technique")
                    .withStyle(ChatFormatting.RED));
            return false;
        }
        long remaining = ArcanaPower.getSkillCooldownTicks(player, level);
        if (remaining > 0L) {
            player.sendSystemMessage(Component.translatable(
                    "message.xuanhuan_earth.skill.cooldown", (remaining + 19L) / 20L)
                    .withStyle(ChatFormatting.YELLOW));
            return false;
        }

        double cost = switch (focus) {
            case CIRCULATION, BIGU -> 8.0D;
            case FETAL_BREATH -> 10.0D;
            case BODY_TEMPERING -> 12.0D;
        };
        if (!ArcanaPower.trySpendMana(player, cost)) {
            player.sendSystemMessage(Component.translatable(
                    "message.xuanhuan_earth.skill.no_mana", ArcanaPower.format(cost))
                    .withStyle(ChatFormatting.RED));
            return false;
        }

        int levelValue = ArcanaPower.getFocusLevel(player, focus);
        switch (focus) {
            case CIRCULATION -> {
                player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 80 + levelValue * 8,
                        levelValue >= 7 ? 1 : 0, true, false, true));
                player.heal(2.0F + levelValue * 0.3F);
                EarthHumanCompat.treatFractureAndCleanseAir(player,
                        0.45D + levelValue * 0.06D,
                        2.5D + levelValue * 0.45D,
                        18.0D + levelValue * 2.0D,
                        0.0D,
                        6.0D + levelValue * 1.2D);
                applyCirculationSpringPulse(level, player, levelValue);
            }
            case BODY_TEMPERING -> {
                player.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 120 + levelValue * 10,
                        levelValue >= 8 ? 1 : 0, true, false, true));
                player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 140 + levelValue * 10,
                        levelValue >= 6 ? 1 : 0, true, false, true));
                applySwordQiAndRockShatter(level, player, levelValue);
            }
            case FETAL_BREATH -> {
                player.addEffect(new MobEffectInstance(MobEffects.WATER_BREATHING, 180 + levelValue * 20,
                        0, true, false, true));
                player.setAirSupply(player.getMaxAirSupply()
                        + (int) Math.round(ArcanaPower.getBreathCapacityBonus(player)));
                EarthHumanCompat.treatFractureAndCleanseAir(player, 0.0D, 1.0D, 100.0D, 0.0D, 8.0D);
                applyWindStepDash(level, player, levelValue);
            }
            case BIGU -> {
                player.getFoodData().setFoodLevel(Math.min(20,
                        player.getFoodData().getFoodLevel() + 4 + levelValue / 3));
                player.getFoodData().setSaturation(Math.min(8.0F,
                        player.getFoodData().getSaturationLevel() + 2.0F + levelValue * 0.25F));
                EarthHumanCompat.treatFractureAndCleanseAir(player, 0.0D, 1.2D, 20.0D, 25.0D + levelValue * 2.0D, 14.0D + levelValue * 1.5D);
                applyPureYangPulse(level, player, levelValue);
            }
        }

        player.swing(InteractionHand.MAIN_HAND, true);
        ArcanaPower.startSkillCooldown(player, level);
        CultivationNetwork.broadcastVisual(player, CultivationVisualAction.forFocus(focus));
        XuanhuanEffects.skillBurst(level, player.position(), focus);
        level.playSound(null, player.blockPosition(), SoundEvents.AMETHYST_BLOCK_RESONATE,
                SoundSource.PLAYERS, 0.8F, 0.9F + focus.id() * 0.08F);
        player.sendSystemMessage(Component.translatable(
                "message.xuanhuan_earth.skill.activated." + focus.path(),
                ArcanaPower.format(cost)).withStyle(ChatFormatting.AQUA));
        XuanhuanJourney.complete(player, XuanhuanJourney.Milestone.ACTIVE_SKILL);
        return true;
    }

    /**
     * 引气回春诀：释放青木灵波，催熟 5 格内农作物与草木，并震退逼近的敌对生物。
     */
    private static void applyCirculationSpringPulse(ServerLevel level, ServerPlayer player, int levelValue) {
        BlockPos center = player.blockPosition();
        int grown = 0;
        int maxGrow = 4 + levelValue / 2;
        for (BlockPos pos : BlockPos.betweenClosed(center.offset(-4, -1, -4), center.offset(4, 2, 4))) {
            if (grown >= maxGrow) {
                break;
            }
            BlockState state = level.getBlockState(pos);
            if (state.getBlock() instanceof BonemealableBlock bonemealable
                    && bonemealable.isValidBonemealTarget(level, pos, state)) {
                if (bonemealable.isBonemealSuccess(level, level.getRandom(), pos, state)) {
                    bonemealable.performBonemeal(level, level.getRandom(), pos.immutable(), state);
                    level.sendParticles(ParticleTypes.HAPPY_VILLAGER,
                            pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D,
                            4, 0.25D, 0.25D, 0.25D, 0.02D);
                    grown++;
                }
            }
        }
        AABB box = player.getBoundingBox().inflate(4.5D);
        for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, box,
                e -> e != player && e instanceof Enemy && !(e instanceof ContractableSpiritBeastEntity))) {
            Vec3 push = target.position().subtract(player.position());
            if (push.lengthSqr() > 0.001D) {
                Vec3 dir = push.normalize().scale(0.65D);
                target.push(dir.x, 0.25D, dir.z);
            }
        }
    }

    /**
     * 庚金破岩剑气：向视线方向斩出 8 格穿透剑气，伤害沿途敌人并震碎/采集前方矿石或岩石收入背包。
     */
    public static void applySwordQiAndRockShatter(ServerLevel level, ServerPlayer player, int levelValue) {
        Vec3 eye = player.getEyePosition();
        Vec3 look = player.getLookAngle().normalize();
        float damage = 7.0F + levelValue * 0.8F;
        AABB sweepBox = player.getBoundingBox().expandTowards(look.scale(8.0D)).inflate(1.25D);
        for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, sweepBox,
                e -> e != player && e.isAlive() && !(e instanceof ContractableSpiritBeastEntity))) {
            Vec3 to = target.getBoundingBox().getCenter().subtract(eye);
            double dist = to.length();
            if (dist <= 8.5D && dist > 0.001D && to.normalize().dot(look) >= 0.72D) {
                target.hurtServer(level, level.damageSources().playerAttack(player), damage);
                target.push(look.x * 0.55D, 0.22D, look.z * 0.55D);
            }
        }
        for (int step = 1; step <= 7; step++) {
            Vec3 p = eye.add(look.scale(step));
            level.sendParticles(ParticleTypes.SWEEP_ATTACK, p.x, p.y, p.z, 1, 0.0D, 0.0D, 0.0D, 0.0D);
            BlockPos hitPos = BlockPos.containing(p);
            if (tryHarvestRockOrOre(level, player, hitPos)) {
                for (BlockPos adj : BlockPos.betweenClosed(hitPos.offset(-1, -1, -1), hitPos.offset(1, 1, 1))) {
                    if (!adj.equals(hitPos) && level.getRandom().nextBoolean()) {
                        tryHarvestRockOrOre(level, player, adj.immutable());
                    }
                }
                break;
            }
        }
        level.playSound(null, player.blockPosition(), SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 0.9F, 1.15F);
    }

    /**
     * 御风步：向准星方向高速瞬步突进，重置摔落伤害，并在起点掀起灵风推开周围怪物。
     */
    private static void applyWindStepDash(ServerLevel level, ServerPlayer player, int levelValue) {
        Vec3 look = player.getLookAngle().normalize();
        double boost = 1.35D + levelValue * 0.06D;
        player.setDeltaMovement(look.x * boost, Math.max(0.42D, look.y * 0.85D + 0.32D), look.z * boost);
        player.hurtMarked = true;
        player.resetFallDistance();
        player.addEffect(new MobEffectInstance(MobEffects.SPEED, 140 + levelValue * 12, 1, true, false, true));
        level.playSound(null, player.blockPosition(), SoundEvents.BREEZE_WIND_CHARGE_BURST.value(),
                SoundSource.PLAYERS, 0.8F, 1.25F);
    }

    /**
     * 辟谷丹元震：纯阳灵气灼烧周围亡灵与敌对怪物。
     */
    private static void applyPureYangPulse(ServerLevel level, ServerPlayer player, int levelValue) {
        float damage = 5.0F + levelValue * 0.6F;
        AABB box = player.getBoundingBox().inflate(5.0D);
        for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, box,
                e -> e != player && (e instanceof Enemy || e instanceof Monster))) {
            target.igniteForSeconds(4 + levelValue / 2);
            target.hurtServer(level, level.damageSources().playerAttack(player), damage);
        }
    }

    public static boolean tryHarvestRockOrOre(ServerLevel level, ServerPlayer player, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (state.isAir() || state.getDestroySpeed(level, pos) < 0.0F || state.getDestroySpeed(level, pos) > 30.0F) {
            return false;
        }
        if (!state.is(BlockTags.MINEABLE_WITH_PICKAXE) && !state.is(BlockTags.BASE_STONE_OVERWORLD)) {
            return false;
        }
        if (level.getBlockEntity(pos) != null) {
            return false;
        }
        List<ItemStack> drops = Block.getDrops(state, level, pos, null, player, player.getMainHandItem());
        level.destroyBlock(pos, false, player);
        for (ItemStack drop : drops) {
            ItemStack copy = drop.copy();
            if (!player.getInventory().add(copy) && !copy.isEmpty()) {
                Block.popResource(level, pos, copy);
            }
        }
        return true;
    }
}
