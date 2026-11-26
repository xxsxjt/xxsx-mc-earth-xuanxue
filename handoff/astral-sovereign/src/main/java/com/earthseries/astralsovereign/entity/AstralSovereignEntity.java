package com.earthseries.astralsovereign.entity;

import java.util.List;
import java.util.UUID;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Astral Sovereign (镇岳星君) - Two-Phase Oriental Heavy War-General Boss.
 * Phase 1 (HP > 50%): Halberd Cleave + 6-Sword Barrage.
 * Phase 2 (HP <= 50%): Enlarged Triple Halo + 8 Uniform Orbiting Flying Swords + Astral Judgment Burst.
 */
public class AstralSovereignEntity extends Monster {
    private static final EntityDataAccessor<Boolean> DATA_PHASE2 =
            SynchedEntityData.defineId(AstralSovereignEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> DATA_ATTACK_TICKS =
            SynchedEntityData.defineId(AstralSovereignEntity.class, EntityDataSerializers.INT);

    private final ServerBossEvent bossEvent = new ServerBossEvent(
            UUID.randomUUID(),
            Component.translatable("entity.astral_sovereign.astral_sovereign"),
            BossEvent.BossBarColor.BLUE,
            BossEvent.BossBarOverlay.NOTCHED_10
    );

    private int swordVolleyCooldown = 80;
    private int ultimateCooldown = 220;

    public AstralSovereignEntity(EntityType<? extends Monster> type, Level level) {
        super(type, level);
        this.xpReward = 250;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 450.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.30D)
                .add(Attributes.ATTACK_DAMAGE, 14.0D)
                .add(Attributes.ARMOR, 12.0D)
                .add(Attributes.ARMOR_TOUGHNESS, 6.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.85D)
                .add(Attributes.FOLLOW_RANGE, 48.0D);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_PHASE2, false);
        builder.define(DATA_ATTACK_TICKS, 0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.15D, true));
        this.goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, 0.75D));
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 24.0F));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    public boolean isPhase2() {
        return this.entityData.get(DATA_PHASE2);
    }

    public int getAttackTicks() {
        return this.entityData.get(DATA_ATTACK_TICKS);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level() instanceof ServerLevel serverLevel) {
            this.bossEvent.setProgress(this.getHealth() / this.getMaxHealth());

            int atk = getAttackTicks();
            if (atk > 0) {
                this.entityData.set(DATA_ATTACK_TICKS, atk - 1);
            }

            // Automatic Phase 2 Awakening at 50% HP
            if (!isPhase2() && this.getHealth() <= this.getMaxHealth() * 0.5F) {
                enterPhase2(serverLevel);
            }

            LivingEntity target = this.getTarget();
            if (target != null && target.isAlive()) {
                if (--swordVolleyCooldown <= 0) {
                    performFlyingSwordVolley(serverLevel, target);
                    swordVolleyCooldown = isPhase2() ? 70 : 120;
                }
                if (isPhase2() && --ultimateCooldown <= 0) {
                    performAstralJudgment(serverLevel);
                    ultimateCooldown = 260;
                }
            }
        }
    }

    private void enterPhase2(ServerLevel level) {
        this.entityData.set(DATA_PHASE2, true);
        this.bossEvent.setColor(BossEvent.BossBarColor.PURPLE);
        this.bossEvent.setName(Component.translatable("entity.astral_sovereign.astral_sovereign.phase2"));
        this.playSound(SoundEvents.WITHER_SPAWN, 1.6F, 0.9F);
        level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, getX(), getY() + 1.8, getZ(), 60, 2.2, 1.8, 2.2, 0.12);
        level.sendParticles(ParticleTypes.END_ROD, getX(), getY() + 2.0, getZ(), 40, 2.5, 2.0, 2.5, 0.08);
    }

    private void performFlyingSwordVolley(ServerLevel level, LivingEntity target) {
        Vec3 start = this.getEyePosition();
        Vec3 dir = target.getEyePosition().subtract(start).normalize();
        double dist = Math.min(18.0D, start.distanceTo(target.getEyePosition()));
        for (int i = 1; i <= (int) dist; i++) {
            Vec3 p = start.add(dir.scale(i));
            level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, p.x, p.y, p.z, 3, 0.25, 0.25, 0.25, 0.01);
        }
        target.hurtServer(level, this.damageSources().mobAttack(this), isPhase2() ? 10.0F : 7.0F);
        this.playSound(SoundEvents.TRIDENT_THROW.value(), 1.2F, 1.3F);
    }

    private void performAstralJudgment(ServerLevel level) {
        // Bagua ground ring particle burst + AoE shockwave
        for (int i = 0; i < 36; i++) {
            double rad = Math.toRadians(i * 10);
            double px = getX() + Math.cos(rad) * 5.0;
            double pz = getZ() + Math.sin(rad) * 5.0;
            level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, px, getY() + 0.3, pz, 2, 0.1, 0.8, 0.1, 0.05);
        }
        AABB aoe = this.getBoundingBox().inflate(5.5D, 2.5D, 5.5D);
        List<LivingEntity> victims = level.getEntitiesOfClass(LivingEntity.class, aoe, e -> e != this && e.isAlive());
        for (LivingEntity v : victims) {
            v.hurtServer(level, this.damageSources().mobAttack(this), 16.0F);
            Vec3 push = v.position().subtract(this.position()).normalize();
            v.push(push.x * 1.1D, 0.55D, push.z * 1.1D);
        }
        this.playSound(SoundEvents.GENERIC_EXPLODE.value(), 1.5F, 0.85F);
    }

    @Override
    public boolean doHurtTarget(ServerLevel level, Entity target) {
        this.entityData.set(DATA_ATTACK_TICKS, 16);
        level.sendParticles(ParticleTypes.SWEEP_ATTACK, target.getX(), target.getY() + 1.2, target.getZ(), 2, 0.4, 0.2, 0.4, 0.0);
        return super.doHurtTarget(level, target);
    }

    @Override
    public void startSeenByPlayer(ServerPlayer player) {
        super.startSeenByPlayer(player);
        this.bossEvent.addPlayer(player);
    }

    @Override
    public void stopSeenByPlayer(ServerPlayer player) {
        super.stopSeenByPlayer(player);
        this.bossEvent.removePlayer(player);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.WARDEN_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.IRON_GOLEM_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.WITHER_DEATH;
    }
}
