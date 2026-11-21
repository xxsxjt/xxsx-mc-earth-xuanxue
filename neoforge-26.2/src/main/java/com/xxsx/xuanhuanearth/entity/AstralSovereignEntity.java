package com.xxsx.xuanhuanearth.entity;

import com.xxsx.xuanhuanearth.particle.XuanhuanParticles;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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
import net.minecraft.util.Mth;
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
 * Astral Sovereign (镇岳星君) two-phase boss entity integrated into {@code xuanhuan_earth}.
 *
 * <p>Combat kit:
 * <ul>
 *   <li>3-step Halberd Melee Combo: Horizontal Starlight Sweep, Overhead Sky-Splitter Cleave,
 *       and Astral Dragon Piercing Thrust — each with distinct full-body animations and arc VFX.</li>
 *   <li>Short-lived Homing Flying Swords ({@link AstralFlyingSwordEntity}): ambient sword waves
 *       plus the Sword Array Empower skill ({@code ACTION_SWORD_ARRAY_BUFF}) that surges sword
 *       count and boosts sword attack power for 6 seconds with golden-cyan aura visuals.</li>
 *   <li>Eight-Wastes Astral Judgment ({@code ACTION_ULTIMATE_JUDGMENT}): 20-tick levitating Bagua
 *       charge followed by a halberd slam emitting a 360-degree expanding circular shockwave
 *       (never reusing forward slash arcs).</li>
 * </ul></p>
 */
public class AstralSovereignEntity extends Monster {
    public static final double SWORD_SUMMON_MAX_RANGE = 22.0D;
    public static final int ULTIMATE_TELEGRAPH_DURATION = 20;
    public static final int ULTIMATE_SHOCKWAVE_DURATION = 12;
    public static final double ULTIMATE_MAX_RADIUS = 12.0D;
    public static final int SWORD_BUFF_DURATION = 120;

    public static final int ACTION_NONE = 0;
    public static final int ACTION_COMBO_SWEEP = 1;
    public static final int ACTION_COMBO_OVERHEAD = 2;
    public static final int ACTION_COMBO_THRUST = 3;
    public static final int ACTION_SWORD_ARRAY_BUFF = 4;
    public static final int ACTION_ULTIMATE_JUDGMENT = 5;
    public static final int ACTION_PHASE_TRANSITION = 6;
    public static final int PHASE_TRANSITION_DURATION = 68;

    private static final EntityDataAccessor<Boolean> DATA_PHASE2 =
            SynchedEntityData.defineId(AstralSovereignEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> DATA_ACTION_TYPE =
            SynchedEntityData.defineId(AstralSovereignEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_ACTION_TICK =
            SynchedEntityData.defineId(AstralSovereignEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_ACTION_DURATION =
            SynchedEntityData.defineId(AstralSovereignEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_SWORD_BUFF_TICKS =
            SynchedEntityData.defineId(AstralSovereignEntity.class, EntityDataSerializers.INT);

    private final ServerBossEvent bossEvent = new ServerBossEvent(
            java.util.UUID.randomUUID(),
            Component.translatable("entity.xuanhuan_earth.astral_sovereign"),
            BossEvent.BossBarColor.BLUE,
            BossEvent.BossBarOverlay.NOTCHED_10
    );

    private int ambientSwordCooldown = 65;
    private int swordBuffCooldown = 160;
    private int buffedWaveCooldown = 0;
    private int ultimateCooldown = 240;
    private int meleeComboIndex = 0;
    private int shockwaveTicksRemaining = 0;
    private final Set<UUID> ultimateHitIds = new HashSet<>();

    public AstralSovereignEntity(EntityType<? extends Monster> type, Level level) {
        super(type, level);
        this.xpReward = 250;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 450.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.28D)
                .add(Attributes.ATTACK_DAMAGE, 14.0D)
                .add(Attributes.ARMOR, 10.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.85D)
                .add(Attributes.FOLLOW_RANGE, 48.0D);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_PHASE2, false);
        builder.define(DATA_ACTION_TYPE, ACTION_NONE);
        builder.define(DATA_ACTION_TICK, 0);
        builder.define(DATA_ACTION_DURATION, 1);
        builder.define(DATA_SWORD_BUFF_TICKS, 0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.15D, true));
        this.goalSelector.addGoal(2, new WaterAvoidingRandomStrollGoal(this, 0.7D));
        this.goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 16.0F));
        this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    public boolean isPhase2() {
        return this.entityData.get(DATA_PHASE2);
    }

    public int getActionType() {
        return this.entityData.get(DATA_ACTION_TYPE);
    }

    public int getActionTick() {
        return this.entityData.get(DATA_ACTION_TICK);
    }

    public int getActionDuration() {
        return Math.max(1, this.entityData.get(DATA_ACTION_DURATION));
    }

    public float getActionProgress() {
        if (getActionType() == ACTION_NONE) {
            return 0.0F;
        }
        return Mth.clamp((float) getActionTick() / (float) getActionDuration(), 0.0F, 1.0F);
    }

    public int getSwordBuffTicks() {
        return this.entityData.get(DATA_SWORD_BUFF_TICKS);
    }

    public boolean isSwordBuffActive() {
        return getSwordBuffTicks() > 0;
    }

    private void startAction(int actionType, int durationTicks) {
        this.entityData.set(DATA_ACTION_TYPE, actionType);
        this.entityData.set(DATA_ACTION_TICK, 0);
        this.entityData.set(DATA_ACTION_DURATION, Math.max(1, durationTicks));
    }

    private void triggerPhase2(ServerLevel serverLevel) {
        this.entityData.set(DATA_PHASE2, true);
        syncBossBarAppearance();
        this.shockwaveTicksRemaining = 0;
        this.setDeltaMovement(Vec3.ZERO);
        // Enter 68-tick (3.4s) Phase 1 -> Phase 2 Awakening transformation sequence
        startAction(ACTION_PHASE_TRANSITION, PHASE_TRANSITION_DURATION);
        this.playSound(SoundEvents.BEACON_ACTIVATE, 1.5F, 0.72F);
        this.playSound(SoundEvents.WITHER_SPAWN, 1.15F, 1.05F);
        serverLevel.sendParticles(XuanhuanParticles.SEAL_RING.get(),
                this.getX(), this.getY() + 0.15, this.getZ(), 1, 0, 0, 0, 0);
    }

    private void tickPhaseTransitionVfx(ServerLevel serverLevel, int tick) {
        this.setDeltaMovement(Vec3.ZERO);
        this.getNavigation().stop();
        if (tick <= 22) {
            // Stage 1: Kneel & pull converging starlight inward toward chest core
            double r = 4.6D * (1.0D - (tick / 24.0D));
            for (int i = 0; i < 6; i++) {
                double a = (Math.PI * 2.0 * i / 6.0) + tick * 0.28;
                serverLevel.sendParticles(XuanhuanParticles.QI_SPARK.get(),
                        this.getX() + Math.cos(a) * r,
                        this.getY() + 0.8 + (tick * 0.06),
                        this.getZ() + Math.sin(a) * r,
                        1, 0.04, 0.04, 0.04, 0.02);
            }
            if (tick == 12) {
                serverLevel.sendParticles(XuanhuanParticles.SEAL_RING.get(),
                        this.getX(), this.getY() + 0.15, this.getZ(), 1, 0, 0, 0, 0);
            }
        } else if (tick <= 42) {
            // Stage 2: Sky-piercing golden-cyan celestial beam pillar & armor unfurl
            for (int y = 0; y < 8; y++) {
                serverLevel.sendParticles(y % 2 == 0 ? ParticleTypes.END_ROD : ParticleTypes.SOUL_FIRE_FLAME,
                        this.getX(), this.getY() + 0.6 + y * 0.9, this.getZ(),
                        3, 0.45, 0.25, 0.45, 0.05);
            }
            if (tick == 24) {
                this.playSound(SoundEvents.TRIDENT_THUNDER.value(), 1.35F, 0.85F);
                serverLevel.sendParticles(XuanhuanParticles.QI_RING.get(),
                        this.getX(), this.getY() + 2.0, this.getZ(), 1, 0, 0, 0, 0);
            }
        } else if (tick == 43) {
            // Stage 3: Awakening 360-degree shockwave burst + safe radial knockback
            this.playSound(SoundEvents.GENERIC_EXPLODE.value(), 1.25F, 0.82F);
            serverLevel.sendParticles(XuanhuanParticles.SEAL_RING.get(),
                    this.getX(), this.getY() + 0.2, this.getZ(), 1, 0, 0, 0, 0);
            serverLevel.sendParticles(XuanhuanParticles.QI_RING.get(),
                    this.getX(), this.getY() + 1.8, this.getZ(), 1, 0, 0, 0, 0);
            for (int i = 0; i < 32; i++) {
                double a = (Math.PI * 2.0 * i) / 32.0;
                serverLevel.sendParticles(i % 2 == 0 ? ParticleTypes.END_ROD : XuanhuanParticles.EMBER_SPARK.get(),
                        this.getX() + Math.cos(a) * 3.8,
                        this.getY() + 1.6,
                        this.getZ() + Math.sin(a) * 3.8,
                        2, 0.15, 0.2, 0.15, 0.12);
            }
            AABB area = this.getBoundingBox().inflate(7.5D, 3.5D, 7.5D);
            for (LivingEntity target : serverLevel.getEntitiesOfClass(LivingEntity.class, area,
                    e -> e != this && e.isAlive() && !this.isAlliedTo(e))) {
                double dx = target.getX() - this.getX();
                double dz = target.getZ() - this.getZ();
                double len = Math.sqrt(dx * dx + dz * dz);
                if (len > 1.0E-4D) {
                    target.push((dx / len) * 1.15D, 0.35D, (dz / len) * 1.15D);
                }
            }
        } else if (tick == PHASE_TRANSITION_DURATION - 4) {
            // Stage 4: Transition completes -> unleash awakened Phase 2 flying sword aura
            this.entityData.set(DATA_SWORD_BUFF_TICKS, SWORD_BUFF_DURATION);
            this.buffedWaveCooldown = 12;
        }
    }

    private void syncBossBarAppearance() {
        if (this.hasCustomName()) {
            this.bossEvent.setName(this.getDisplayName());
        } else if (isPhase2()) {
            this.bossEvent.setName(Component.translatable("entity.xuanhuan_earth.astral_sovereign.phase2"));
        } else {
            this.bossEvent.setName(Component.translatable("entity.xuanhuan_earth.astral_sovereign"));
        }
        this.bossEvent.setColor(isPhase2() ? BossEvent.BossBarColor.PURPLE : BossEvent.BossBarColor.BLUE);
        this.bossEvent.setProgress(this.getHealth() / this.getMaxHealth());
    }

    @Override
    protected void customServerAiStep(ServerLevel serverLevel) {
        super.customServerAiStep(serverLevel);
        this.bossEvent.setProgress(this.getHealth() / this.getMaxHealth());

        if (!isPhase2() && this.getHealth() <= this.getMaxHealth() * 0.5F) {
            triggerPhase2(serverLevel);
        }

        // Advance active full-body action state
        int currentAction = getActionType();
        if (currentAction != ACTION_NONE) {
            int nextTick = getActionTick() + 1;
            int duration = getActionDuration();
            if (currentAction == ACTION_PHASE_TRANSITION) {
                tickPhaseTransitionVfx(serverLevel, nextTick);
            } else if (currentAction == ACTION_ULTIMATE_JUDGMENT) {
                if (nextTick <= ULTIMATE_TELEGRAPH_DURATION) {
                    emitUltimateTelegraph(serverLevel, nextTick);
                    this.setDeltaMovement(this.getDeltaMovement().multiply(0.45D, 1.0D, 0.45D));
                }
                if (nextTick == ULTIMATE_TELEGRAPH_DURATION) {
                    beginUltimateCircularShockwave(serverLevel);
                }
            }
            if (nextTick >= duration) {
                this.entityData.set(DATA_ACTION_TYPE, ACTION_NONE);
                this.entityData.set(DATA_ACTION_TICK, 0);
            } else {
                this.entityData.set(DATA_ACTION_TICK, nextTick);
            }
        }

        // Expand active 360-degree circular shockwave tick-by-tick
        if (this.shockwaveTicksRemaining > 0) {
            int stepIndex = ULTIMATE_SHOCKWAVE_DURATION - this.shockwaveTicksRemaining;
            advanceUltimateCircularShockwave(serverLevel, stepIndex);
            this.shockwaveTicksRemaining--;
        }

        // Active Sword Array Empower buff (Phase 2 exclusive): aura VFX + frequent waves of empowered short-lived homing swords
        int buffTicks = getSwordBuffTicks();
        if (buffTicks > 0) {
            if (!isPhase2()) {
                this.entityData.set(DATA_SWORD_BUFF_TICKS, 0);
            } else {
                this.entityData.set(DATA_SWORD_BUFF_TICKS, buffTicks - 1);
                if (buffTicks % 4 == 0) {
                    emitSwordBuffAura(serverLevel);
                }
                LivingEntity target = this.getTarget();
                if (target != null && target.isAlive() && canSummonFlyingSwordsAt(target)) {
                    if (--this.buffedWaveCooldown <= 0) {
                        spawnFlyingSwordSwarm(serverLevel, target, 5, true);
                        this.buffedWaveCooldown = 16;
                    }
                }
            }
        }

        LivingEntity target = this.getTarget();
        if (target == null || !target.isAlive()) {
            return;
        }

        double distSqr = this.distanceToSqr(target);

        // Ultimate: Eight-Wastes Astral Judgment (Circular Shockwave)
        if (--this.ultimateCooldown <= 0 && getActionType() == ACTION_NONE && distSqr < 144.0D) {
            this.ultimateHitIds.clear();
            startAction(ACTION_ULTIMATE_JUDGMENT, ULTIMATE_TELEGRAPH_DURATION + ULTIMATE_SHOCKWAVE_DURATION + 8);
            this.playSound(SoundEvents.BEACON_POWER_SELECT, 1.25F, 0.72F);
            this.ultimateCooldown = isPhase2() ? 220 : 300;
            return;
        }

        // Phase 1 has NO flying swords; all flying sword skills & waves unlock strictly in Phase 2 (万剑法相)
        if (isPhase2()) {
            // Skill: Sword Array Empower (boosts sword attack power & spawns abundant short-lived homing swords)
            if (--this.swordBuffCooldown <= 0 && getActionType() == ACTION_NONE && !isSwordBuffActive()
                    && canSummonFlyingSwordsAt(target)) {
                activateSwordArrayBuff(serverLevel);
                this.swordBuffCooldown = 210;
                return;
            }

            // Ambient short-lived homing flying swords (small wave outside buff window)
            if (!isSwordBuffActive() && --this.ambientSwordCooldown <= 0 && canSummonFlyingSwordsAt(target)) {
                spawnFlyingSwordSwarm(serverLevel, target, 3, false);
                this.ambientSwordCooldown = 68;
            }
        }
    }

    private boolean canSummonFlyingSwordsAt(LivingEntity target) {
        if (!isPhase2()) {
            return false;
        }
        double maxDistSqr = SWORD_SUMMON_MAX_RANGE * SWORD_SUMMON_MAX_RANGE;
        return this.distanceToSqr(target) <= maxDistSqr && this.getSensing().hasLineOfSight(target);
    }

    private void activateSwordArrayBuff(ServerLevel serverLevel) {
        if (!isPhase2()) {
            return;
        }
        this.entityData.set(DATA_SWORD_BUFF_TICKS, SWORD_BUFF_DURATION);
        this.buffedWaveCooldown = 10;
        startAction(ACTION_SWORD_ARRAY_BUFF, 26);

        this.playSound(SoundEvents.ENCHANTMENT_TABLE_USE, 1.3F, 1.2F);
        this.playSound(SoundEvents.TRIDENT_RIPTIDE_3.value(), 0.9F, 1.35F);

        serverLevel.sendParticles(XuanhuanParticles.QI_RING.get(),
                this.getX(), this.getY() + 2.1, this.getZ(), 1, 0, 0, 0, 0);
        serverLevel.sendParticles(XuanhuanParticles.SEAL_RING.get(),
                this.getX(), this.getY() + 0.2, this.getZ(), 1, 0, 0, 0, 0);
        for (int i = 0; i < 16; i++) {
            double ang = (Math.PI * 2.0 * i) / 16.0;
            double rx = this.getX() + Math.cos(ang) * 2.4;
            double rz = this.getZ() + Math.sin(ang) * 2.4;
            serverLevel.sendParticles(i % 2 == 0 ? XuanhuanParticles.RUNE_GLYPH_A.get() : XuanhuanParticles.EMBER_SPARK.get(),
                    rx, this.getY() + 2.2, rz, 1, 0.1, 0.15, 0.1, 0.03);
        }
    }

    private void emitSwordBuffAura(ServerLevel serverLevel) {
        double angle = (this.tickCount * 0.35) % (Math.PI * 2.0);
        double radius = 3.2;
        for (int i = 0; i < 2; i++) {
            double a = angle + i * Math.PI;
            serverLevel.sendParticles(XuanhuanParticles.EMBER_SPARK.get(),
                    this.getX() + Math.cos(a) * radius,
                    this.getY() + 1.8 + Math.sin(this.tickCount * 0.25) * 0.5,
                    this.getZ() + Math.sin(a) * radius,
                    1, 0.08, 0.08, 0.08, 0.02);
        }
        if (this.tickCount % 16 == 0) {
            serverLevel.sendParticles(XuanhuanParticles.QI_RING.get(),
                    this.getX(), this.getY() + 2.0, this.getZ(), 1, 0, 0, 0, 0);
        }
    }

    private void spawnFlyingSwordSwarm(ServerLevel serverLevel, LivingEntity target, int count, boolean empowered) {
        float swordDamage = empowered
                ? (isPhase2() ? AstralFlyingSwordEntity.EMPOWERED_DAMAGE_P2 : AstralFlyingSwordEntity.EMPOWERED_DAMAGE_P1)
                : AstralFlyingSwordEntity.BASE_DAMAGE;

        double baseYaw = Math.toRadians(this.getYRot());
        Vec3 forward = new Vec3(-Math.sin(baseYaw), 0.0, Math.cos(baseYaw)).normalize();
        Vec3 right = new Vec3(forward.z, 0.0, -forward.x).normalize();

        for (int i = 0; i < count; i++) {
            double arcAngle = (Math.PI * 2.0 * i) / count + (this.tickCount * 0.17);
            double spreadRadius = empowered ? 3.85 : 3.15;
            double heightOffset = 2.05 + Math.sin(arcAngle) * 0.95;
            Vec3 ringOffset = right.scale(Math.cos(arcAngle) * spreadRadius)
                    .add(forward.scale(-0.65 + Math.sin(arcAngle) * 0.55));
            Vec3 spawnPos = new Vec3(this.getX(), this.getY() + heightOffset, this.getZ()).add(ringOffset);
            Vec3 outwardDir = ringOffset.add(0.0, 0.25, 0.0).normalize();

            AstralFlyingSwordEntity.summon(
                    serverLevel,
                    this,
                    target,
                    spawnPos,
                    outwardDir,
                    swordDamage,
                    empowered,
                    8 + (i * 2)
            );
            serverLevel.sendParticles(empowered ? XuanhuanParticles.EMBER_SPARK.get() : XuanhuanParticles.QI_SPARK.get(),
                    spawnPos.x, spawnPos.y, spawnPos.z, 5, 0.15, 0.15, 0.15, 0.05);
        }
    }

    private void emitUltimateTelegraph(ServerLevel serverLevel, int tick) {
        if (tick == 1 || tick == 10 || tick == 18) {
            serverLevel.sendParticles(XuanhuanParticles.SEAL_RING.get(),
                    this.getX(), this.getY() + 0.14, this.getZ(), 1, 0, 0, 0, 0);
            serverLevel.sendParticles(XuanhuanParticles.QI_RING.get(),
                    this.getX(), this.getY() + 0.45, this.getZ(), 1, 0, 0, 0, 0);
        }
        double radius = 2.2 + (tick / (double) ULTIMATE_TELEGRAPH_DURATION) * 3.8;
        for (int i = 0; i < 8; i++) {
            double angle = (Math.PI * 2.0 * i / 8.0) + tick * 0.18;
            double px = this.getX() + Math.cos(angle) * radius;
            double pz = this.getZ() + Math.sin(angle) * radius;
            serverLevel.sendParticles(
                    i % 2 == 0 ? XuanhuanParticles.RUNE_GLYPH_B.get() : ParticleTypes.END_ROD,
                    px, this.getY() + 0.35 + (tick * 0.08), pz, 1, 0.05, 0.08, 0.05, 0.01);
        }
    }

    private void beginUltimateCircularShockwave(ServerLevel serverLevel) {
        this.shockwaveTicksRemaining = ULTIMATE_SHOCKWAVE_DURATION;
        this.playSound(SoundEvents.GENERIC_EXPLODE.value(), 1.45F, 0.65F);
        this.playSound(SoundEvents.TRIDENT_THUNDER.value(), 1.15F, 0.9F);
        serverLevel.sendParticles(XuanhuanParticles.SEAL_RING.get(),
                this.getX(), this.getY() + 0.15, this.getZ(), 2, 0.2, 0.05, 0.2, 0.0);
        serverLevel.sendParticles(ParticleTypes.EXPLOSION_EMITTER,
                this.getX(), this.getY() + 0.6, this.getZ(), 1, 0, 0, 0, 0);
    }

    /**
     * Pure 360-degree radial circular shockwave (expanding ring from 1.5m to 12m over 12 ticks).
     */
    private void advanceUltimateCircularShockwave(ServerLevel serverLevel, int stepIndex) {
        double progress = (stepIndex + 1.0) / (double) ULTIMATE_SHOCKWAVE_DURATION;
        double waveRadius = 1.5 + progress * (ULTIMATE_MAX_RADIUS - 1.5);
        int points = 32 + stepIndex * 4;

        for (int i = 0; i < points; i++) {
            double angle = (Math.PI * 2.0 * i) / points;
            double cos = Math.cos(angle);
            double sin = Math.sin(angle);
            double px = this.getX() + cos * waveRadius;
            double pz = this.getZ() + sin * waveRadius;

            serverLevel.sendParticles(ParticleTypes.SOUL_FIRE_FLAME,
                    px, this.getY() + 0.28, pz, 1, 0.04, 0.12, 0.04, 0.02);
            if (i % 2 == 0) {
                serverLevel.sendParticles(XuanhuanParticles.QI_SPARK.get(),
                        px, this.getY() + 0.55, pz, 1, 0.05, 0.18, 0.05, 0.04);
            }
            if (i % 4 == 0) {
                serverLevel.sendParticles(ParticleTypes.END_ROD,
                        px, this.getY() + 0.95, pz, 1, 0.06, 0.35, 0.06, 0.05);
            }
        }
        if (stepIndex % 3 == 0) {
            serverLevel.sendParticles(XuanhuanParticles.QI_RING.get(),
                    this.getX(), this.getY() + 0.22, this.getZ(), 1, 0, 0, 0, 0);
        }

        // Apply wave-front damage & safe radial knockback to targets caught by the expanding ring
        double innerSq = Math.max(0.0, waveRadius - 2.4) * Math.max(0.0, waveRadius - 2.4);
        double outerSq = (waveRadius + 1.1) * (waveRadius + 1.1);
        AABB box = this.getBoundingBox().inflate(waveRadius + 1.1, 3.8, waveRadius + 1.1);
        List<LivingEntity> victims = serverLevel.getEntitiesOfClass(LivingEntity.class, box,
                e -> e != this && e.isAlive() && !this.ultimateHitIds.contains(e.getUUID()));
        float dmg = isPhase2() ? 18.0F : 14.0F;
        for (LivingEntity victim : victims) {
            double dx = victim.getX() - this.getX();
            double dz = victim.getZ() - this.getZ();
            double horizSq = dx * dx + dz * dz;
            if (horizSq >= innerSq && horizSq <= outerSq) {
                this.ultimateHitIds.add(victim.getUUID());
                victim.hurtServer(serverLevel, this.damageSources().mobAttack(this), dmg);
                double len = Math.sqrt(horizSq);
                Vec3 push = len > 1.0E-4
                        ? new Vec3(dx / len, 0.0, dz / len)
                        : new Vec3(0.0, 0.0, 1.0);
                victim.push(push.x * 1.15, 0.55, push.z * 1.15);
                victim.hurtMarked = true;
            }
        }
    }

    /**
     * 3-step Halberd Melee Combo with distinct animations, damage profiles, and rich starlight arc VFX.
     */
    @Override
    public boolean doHurtTarget(ServerLevel serverLevel, Entity target) {
        int combo = this.meleeComboIndex % 3;
        this.meleeComboIndex++;

        if (combo == 0) {
            startAction(ACTION_COMBO_SWEEP, 18);
        } else if (combo == 1) {
            startAction(ACTION_COMBO_OVERHEAD, 22);
        } else {
            startAction(ACTION_COMBO_THRUST, 20);
        }

        boolean hit = super.doHurtTarget(serverLevel, target);
        emitMeleeComboVfxAndCleave(serverLevel, combo, target);
        return hit;
    }

    private void emitMeleeComboVfxAndCleave(ServerLevel serverLevel, int combo, Entity primaryTarget) {
        Vec3 forward = this.getLookAngle();
        Vec3 horizForward = new Vec3(forward.x, 0.0, forward.z);
        if (horizForward.lengthSqr() < 1.0E-5) {
            horizForward = new Vec3(0.0, 0.0, 1.0);
        } else {
            horizForward = horizForward.normalize();
        }
        Vec3 right = new Vec3(-horizForward.z, 0.0, horizForward.x);
        double baseY = this.getY() + 1.85;

        if (combo == 0) {
            // Combo 1: Wide 140-degree Horizontal Starlight Crescent Sweep
            this.playSound(SoundEvents.PLAYER_ATTACK_SWEEP, 1.35F, 0.78F);
            for (int i = -10; i <= 10; i++) {
                double ang = Math.toRadians(i * 7.0);
                Vec3 dir = horizForward.scale(Math.cos(ang)).add(right.scale(Math.sin(ang)));
                for (double r : new double[]{2.4, 3.6, 4.4}) {
                    Vec3 p = new Vec3(this.getX(), baseY, this.getZ()).add(dir.scale(r));
                    serverLevel.sendParticles(r > 4.0 ? ParticleTypes.END_ROD : XuanhuanParticles.QI_SPARK.get(),
                            p.x, p.y, p.z, 1, 0.04, 0.06, 0.04, 0.02);
                }
                if (i % 4 == 0) {
                    Vec3 p = new Vec3(this.getX(), baseY, this.getZ()).add(dir.scale(3.4));
                    serverLevel.sendParticles(ParticleTypes.SWEEP_ATTACK, p.x, p.y, p.z, 1, 0, 0, 0, 0);
                }
            }
            // Cleave bonus damage to secondary targets inside the horizontal arc
            AABB arcBox = this.getBoundingBox().inflate(4.4, 1.8, 4.4);
            for (LivingEntity extra : serverLevel.getEntitiesOfClass(LivingEntity.class, arcBox,
                    e -> e != this && e != primaryTarget && e.isAlive())) {
                Vec3 toExtra = extra.position().subtract(this.position());
                Vec3 flat = new Vec3(toExtra.x, 0.0, toExtra.z);
                if (flat.lengthSqr() > 1.0E-4 && flat.normalize().dot(horizForward) > 0.25) {
                    extra.hurtServer(serverLevel, this.damageSources().mobAttack(this), 9.0F);
                }
            }
        } else if (combo == 1) {
            // Combo 2: Overhead Sky-Splitter Vertical Crescent + Ground Impact Wave
            this.playSound(SoundEvents.TRIDENT_THUNDER.value(), 0.7F, 1.25F);
            for (int i = 0; i <= 14; i++) {
                double t = i / 14.0;
                double pitchAng = Math.toRadians(75.0 - t * 115.0);
                double r = 3.5;
                Vec3 p = new Vec3(this.getX(), this.getY() + 2.2, this.getZ())
                        .add(horizForward.scale(Math.cos(pitchAng) * r))
                        .add(0.0, Math.sin(pitchAng) * r, 0.0);
                serverLevel.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, p.x, p.y, p.z, 1, 0.05, 0.05, 0.05, 0.02);
                serverLevel.sendParticles(XuanhuanParticles.EMBER_SPARK.get(), p.x, p.y, p.z, 1, 0.06, 0.06, 0.06, 0.03);
            }
            Vec3 impactPos = new Vec3(this.getX(), this.getY() + 0.15, this.getZ()).add(horizForward.scale(3.2));
            serverLevel.sendParticles(XuanhuanParticles.SEAL_RING.get(),
                    impactPos.x, impactPos.y, impactPos.z, 1, 0, 0, 0, 0);
            if (primaryTarget instanceof LivingEntity le && le.isAlive()) {
                le.hurtServer(serverLevel, this.damageSources().mobAttack(this), 4.0F);
            }
        } else {
            // Combo 3: Astral Dragon Spiral Piercing Thrust + Upward Flick
            this.playSound(SoundEvents.TRIDENT_THROW.value(), 1.2F, 0.85F);
            for (int step = 1; step <= 14; step++) {
                double dist = step * 0.42;
                double spiral = step * 0.7;
                double rad = 0.55 * (1.0 - step / 18.0);
                Vec3 center = new Vec3(this.getX(), baseY - 0.1, this.getZ()).add(horizForward.scale(dist));
                Vec3 spiralOffset = right.scale(Math.cos(spiral) * rad).add(0.0, Math.sin(spiral) * rad, 0.0);
                serverLevel.sendParticles(ParticleTypes.END_ROD,
                        center.x + spiralOffset.x, center.y + spiralOffset.y, center.z + spiralOffset.z,
                        1, 0.02, 0.02, 0.02, 0.01);
                serverLevel.sendParticles(XuanhuanParticles.QI_SPARK.get(),
                        center.x - spiralOffset.x, center.y - spiralOffset.y, center.z - spiralOffset.z,
                        1, 0.02, 0.02, 0.02, 0.02);
            }
            Vec3 tipPos = new Vec3(this.getX(), baseY, this.getZ()).add(horizForward.scale(4.6));
            serverLevel.sendParticles(XuanhuanParticles.QI_RING.get(),
                    tipPos.x, tipPos.y, tipPos.z, 1, 0, 0, 0, 0);
        }
    }

    @Override
    public void setCustomName(@Nullable Component name) {
        super.setCustomName(name);
        syncBossBarAppearance();
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putBoolean("Phase2", isPhase2());
        output.putInt("AmbientSwordCooldown", this.ambientSwordCooldown);
        output.putInt("SwordBuffCooldown", this.swordBuffCooldown);
        output.putInt("SwordBuffTicks", getSwordBuffTicks());
        output.putInt("UltimateCooldown", this.ultimateCooldown);
        output.putInt("MeleeComboIndex", this.meleeComboIndex);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.entityData.set(DATA_PHASE2, input.getBooleanOr("Phase2", false));
        this.ambientSwordCooldown = Math.max(0, input.getIntOr("AmbientSwordCooldown", 65));
        this.swordBuffCooldown = Math.max(0, input.getIntOr("SwordBuffCooldown", 160));
        this.entityData.set(DATA_SWORD_BUFF_TICKS, Math.max(0, input.getIntOr("SwordBuffTicks", 0)));
        this.ultimateCooldown = Math.max(0, input.getIntOr("UltimateCooldown", 240));
        this.meleeComboIndex = Math.max(0, input.getIntOr("MeleeComboIndex", 0));
        this.entityData.set(DATA_ACTION_TYPE, ACTION_NONE);
        this.entityData.set(DATA_ACTION_TICK, 0);
        this.shockwaveTicksRemaining = 0;
        syncBossBarAppearance();
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false;
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
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.IRON_GOLEM_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.WITHER_DEATH;
    }
}
