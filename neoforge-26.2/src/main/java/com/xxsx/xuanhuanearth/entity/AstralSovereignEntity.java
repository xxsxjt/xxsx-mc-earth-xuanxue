package com.xxsx.xuanhuanearth.entity;

import com.xxsx.xuanhuanearth.particle.XuanhuanParticles;
import java.util.List;
import java.util.UUID;
import javax.annotation.Nullable;
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
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Astral Sovereign (镇岳星君) — Dual-Phase Cultivation Boss for {@code xuanhuan_earth}.
 *
 * <ul>
 *   <li>Phase 1 (100%–50% HP): Fangtian Halberd melee cleave + line-of-sight bounded Flying Sword Volley.</li>
 *   <li>Phase 2 (&le; 50% HP): 万剑法相 — awakens 3rd star halo &amp; 8 orbiting flying swords, faster volleys,
 *       and telegraphed Bagua Astral Judgment shockwave.</li>
 * </ul>
 */
public class AstralSovereignEntity extends Monster {
    public static final double MAX_HEALTH = 450.0D;
    public static final double SWORD_VOLLEY_MAX_RANGE = 18.0D;
    public static final int JUDGMENT_TELEGRAPH_TICKS = 20;
    public static final double JUDGMENT_RADIUS = 5.5D;

    private static final EntityDataAccessor<Boolean> DATA_PHASE2 =
            SynchedEntityData.defineId(AstralSovereignEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> DATA_ATTACK_TICKS =
            SynchedEntityData.defineId(AstralSovereignEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_CAST_TICKS =
            SynchedEntityData.defineId(AstralSovereignEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_TELEGRAPH_TICKS =
            SynchedEntityData.defineId(AstralSovereignEntity.class, EntityDataSerializers.INT);

    private final ServerBossEvent bossEvent = new ServerBossEvent(
            UUID.randomUUID(),
            Component.translatable("entity.xuanhuan_earth.astral_sovereign"),
            BossEvent.BossBarColor.BLUE,
            BossEvent.BossBarOverlay.NOTCHED_10);

    private int swordVolleyCooldown = 80;
    private int ultimateCooldown = 220;

    public AstralSovereignEntity(EntityType<? extends Monster> type, Level level) {
        super(type, level);
        this.xpReward = 250;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, MAX_HEALTH)
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
        builder.define(DATA_CAST_TICKS, 0);
        builder.define(DATA_TELEGRAPH_TICKS, 0);
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putBoolean("Phase2", isPhase2());
        output.putInt("SwordVolleyCooldown", this.swordVolleyCooldown);
        output.putInt("UltimateCooldown", this.ultimateCooldown);
        output.putInt("TelegraphTicks", getTelegraphTicks());
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        boolean savedPhase2 = input.getBooleanOr("Phase2", false);
        this.entityData.set(DATA_PHASE2, savedPhase2);
        this.swordVolleyCooldown = Math.max(0, input.getIntOr("SwordVolleyCooldown", 80));
        this.ultimateCooldown = Math.max(0, input.getIntOr("UltimateCooldown", 220));
        this.entityData.set(DATA_TELEGRAPH_TICKS, Math.max(0, input.getIntOr("TelegraphTicks", 0)));
        syncBossBarAppearance();
    }

    @Override
    public void setCustomName(@Nullable Component name) {
        super.setCustomName(name);
        syncBossBarAppearance();
    }

    private void syncBossBarAppearance() {
        if (isPhase2()) {
            this.bossEvent.setColor(BossEvent.BossBarColor.PURPLE);
            this.bossEvent.setName(this.hasCustomName()
                    ? this.getDisplayName()
                    : Component.translatable("entity.xuanhuan_earth.astral_sovereign.phase2"));
        } else {
            this.bossEvent.setColor(BossEvent.BossBarColor.BLUE);
            this.bossEvent.setName(this.hasCustomName()
                    ? this.getDisplayName()
                    : Component.translatable("entity.xuanhuan_earth.astral_sovereign"));
        }
        if (this.getMaxHealth() > 0.0F) {
            this.bossEvent.setProgress(Math.clamp(this.getHealth() / this.getMaxHealth(), 0.0F, 1.0F));
        }
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false;
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

    public int getCastTicks() {
        return this.entityData.get(DATA_CAST_TICKS);
    }

    public int getTelegraphTicks() {
        return this.entityData.get(DATA_TELEGRAPH_TICKS);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level() instanceof ServerLevel serverLevel) {
            this.bossEvent.setProgress(Math.clamp(this.getHealth() / this.getMaxHealth(), 0.0F, 1.0F));

            decrementSyncedTimer(DATA_ATTACK_TICKS);
            decrementSyncedTimer(DATA_CAST_TICKS);

            int telegraph = getTelegraphTicks();
            if (telegraph > 0) {
                emitBaguaTelegraphRing(serverLevel, telegraph);
                if (telegraph == 1) {
                    this.entityData.set(DATA_TELEGRAPH_TICKS, 0);
                    detonateAstralJudgment(serverLevel);
                } else {
                    this.entityData.set(DATA_TELEGRAPH_TICKS, telegraph - 1);
                }
            }

            if (!isPhase2() && this.getHealth() <= this.getMaxHealth() * 0.5F) {
                enterPhase2(serverLevel);
            }

            LivingEntity target = this.getTarget();
            if (target != null && target.isAlive() && getTelegraphTicks() == 0) {
                if (swordVolleyCooldown > 0) {
                    swordVolleyCooldown--;
                }
                if (swordVolleyCooldown <= 0 && canStrikeWithSwordVolley(target)) {
                    performFlyingSwordVolley(serverLevel, target);
                    swordVolleyCooldown = isPhase2() ? 70 : 120;
                }

                if (isPhase2()) {
                    if (ultimateCooldown > 0) {
                        ultimateCooldown--;
                    }
                    if (ultimateCooldown <= 0 && this.distanceToSqr(target) <= (JUDGMENT_RADIUS + 3.5D) * (JUDGMENT_RADIUS + 3.5D)) {
                        beginAstralJudgmentTelegraph(serverLevel);
                        ultimateCooldown = 260;
                    }
                }
            }
        }
    }

    private void decrementSyncedTimer(EntityDataAccessor<Integer> accessor) {
        int val = this.entityData.get(accessor);
        if (val > 0) {
            this.entityData.set(accessor, val - 1);
        }
    }

    private boolean canStrikeWithSwordVolley(LivingEntity target) {
        double maxDistSqr = SWORD_VOLLEY_MAX_RANGE * SWORD_VOLLEY_MAX_RANGE;
        return this.distanceToSqr(target) <= maxDistSqr && this.getSensing().hasLineOfSight(target);
    }

    private void enterPhase2(ServerLevel level) {
        this.entityData.set(DATA_PHASE2, true);
        syncBossBarAppearance();
        this.playSound(SoundEvents.WITHER_SPAWN, 1.5F, 0.92F);
        level.sendParticles(XuanhuanParticles.QI_RING.get(), getX(), getY() + 1.8, getZ(), 12, 1.8, 1.2, 1.8, 0.04);
        level.sendParticles(XuanhuanParticles.RUNE_GLYPH_A.get(), getX(), getY() + 2.2, getZ(), 24, 2.2, 1.6, 2.2, 0.05);
        level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, getX(), getY() + 1.8, getZ(), 50, 2.2, 1.8, 2.2, 0.12);
        level.sendParticles(ParticleTypes.END_ROD, getX(), getY() + 2.0, getZ(), 32, 2.4, 2.0, 2.4, 0.08);
    }

    private void performFlyingSwordVolley(ServerLevel level, LivingEntity target) {
        this.entityData.set(DATA_CAST_TICKS, 12);
        Vec3 start = this.getEyePosition();
        Vec3 end = target.getEyePosition();
        double rawDist = start.distanceTo(end);
        double dist = Math.min(SWORD_VOLLEY_MAX_RANGE, rawDist);
        if (dist > 0.25D) {
            Vec3 dir = end.subtract(start).normalize();
            int steps = Math.max(1, (int) Math.ceil(dist));
            for (int i = 1; i <= steps; i++) {
                Vec3 p = start.add(dir.scale(i));
                level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, p.x, p.y, p.z, 2, 0.22, 0.22, 0.22, 0.01);
                level.sendParticles(XuanhuanParticles.QI_SPARK.get(), p.x, p.y, p.z, 1, 0.18, 0.18, 0.18, 0.02);
            }
        }
        target.hurtServer(level, this.damageSources().mobAttack(this), isPhase2() ? 10.0F : 7.0F);
        this.playSound(SoundEvents.TRIDENT_THROW.value(), 1.2F, 1.3F);
    }

    private void beginAstralJudgmentTelegraph(ServerLevel level) {
        this.entityData.set(DATA_TELEGRAPH_TICKS, JUDGMENT_TELEGRAPH_TICKS);
        this.playSound(SoundEvents.BEACON_ACTIVATE, 1.4F, 1.25F);
        level.sendParticles(XuanhuanParticles.SEAL_RING.get(), getX(), getY() + 0.25, getZ(), 8, 2.4, 0.15, 2.4, 0.02);
    }

    private void emitBaguaTelegraphRing(ServerLevel level, int remainingTicks) {
        if (remainingTicks % 4 != 0) {
            return;
        }
        for (int i = 0; i < 24; i++) {
            double rad = Math.toRadians(i * 15.0D + remainingTicks * 6.0D);
            double px = getX() + Math.cos(rad) * JUDGMENT_RADIUS;
            double pz = getZ() + Math.sin(rad) * JUDGMENT_RADIUS;
            level.sendParticles(XuanhuanParticles.RUNE_GLYPH_B.get(), px, getY() + 0.25, pz, 1, 0.08, 0.15, 0.08, 0.01);
        }
    }

    private void detonateAstralJudgment(ServerLevel level) {
        for (int i = 0; i < 36; i++) {
            double rad = Math.toRadians(i * 10.0D);
            double px = getX() + Math.cos(rad) * JUDGMENT_RADIUS;
            double pz = getZ() + Math.sin(rad) * JUDGMENT_RADIUS;
            level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, px, getY() + 0.35, pz, 2, 0.1, 0.8, 0.1, 0.05);
            if (i % 3 == 0) {
                level.sendParticles(XuanhuanParticles.QI_RING.get(), px, getY() + 0.5, pz, 1, 0.1, 0.4, 0.1, 0.03);
            }
        }
        AABB aoe = this.getBoundingBox().inflate(JUDGMENT_RADIUS, 2.5D, JUDGMENT_RADIUS);
        List<LivingEntity> victims = level.getEntitiesOfClass(
                LivingEntity.class, aoe, e -> e != this && e.isAlive());
        for (LivingEntity victim : victims) {
            victim.hurtServer(level, this.damageSources().mobAttack(this), 16.0F);
            Vec3 delta = victim.position().subtract(this.position());
            Vec3 horizontal = new Vec3(delta.x, 0.0D, delta.z);
            Vec3 push = horizontal.lengthSqr() > 1.0E-4D ? horizontal.normalize() : Vec3.ZERO;
            victim.push(push.x * 1.1D, 0.55D, push.z * 1.1D);
        }
        this.playSound(SoundEvents.GENERIC_EXPLODE.value(), 1.5F, 0.85F);
    }

    @Override
    public boolean doHurtTarget(ServerLevel level, Entity target) {
        this.entityData.set(DATA_ATTACK_TICKS, 16);
        level.sendParticles(ParticleTypes.SWEEP_ATTACK, target.getX(), target.getY() + 1.2, target.getZ(), 2, 0.4, 0.2, 0.4, 0.0);
        level.sendParticles(XuanhuanParticles.QI_SPARK.get(), target.getX(), target.getY() + 1.1, target.getZ(), 6, 0.35, 0.3, 0.35, 0.04);
        return super.doHurtTarget(level, target);
    }

    @Override
    public void startSeenByPlayer(ServerPlayer player) {
        super.startSeenByPlayer(player);
        syncBossBarAppearance();
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
