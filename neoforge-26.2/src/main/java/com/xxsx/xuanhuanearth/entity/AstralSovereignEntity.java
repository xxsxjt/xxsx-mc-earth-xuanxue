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
    public static final double ULTIMATE_MAX_RADIUS = 15.5D;
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
        // Stage 1 (ticks 1..30): High Aerial Ascent — converging golden spirit tethers rising from 8 octagon talismans
        if (tick <= 30) {
            double progress = tick / 30.0D;
            double liftY = this.getY() + 0.8D + progress * 3.6D;
            for (int i = 0; i < 8; i++) {
                double a = (Math.PI * 2.0 * i / 8.0) + tick * 0.08;
                double r = 5.0D * (1.0D - progress * 0.72D);
                serverLevel.sendParticles(i % 2 == 0 ? XuanhuanParticles.QI_SPARK.get() : ParticleTypes.END_ROD,
                        this.getX() + Math.cos(a) * r,
                        liftY,
                        this.getZ() + Math.sin(a) * r,
                        1, 0.05, 0.08, 0.05, 0.02);
            }
            if (tick == 10) {
                this.playSound(SoundEvents.ELYTRA_FLYING, 1.15F, 0.92F);
                serverLevel.sendParticles(XuanhuanParticles.SEAL_RING.get(),
                        this.getX(), this.getY() + 0.15, this.getZ(), 1, 0, 0, 0, 0);
            }
        } else if (tick <= 48) {
            // Stage 2 (ticks 31..48): Apex Mid-Air Cocoon Shatter (tick 34) & Phase 2 Celestial Awakening!
            double apexY = this.getY() + 4.3D;
            if (tick == 34) {
                this.playSound(SoundEvents.TRIDENT_THUNDER.value(), 1.55F, 0.78F);
                this.playSound(SoundEvents.GLASS_BREAK, 1.4F, 0.65F);
                serverLevel.sendParticles(XuanhuanParticles.QI_RING.get(),
                        this.getX(), apexY, this.getZ(), 1, 0, 0, 0, 0);
                for (int i = 0; i < 28; i++) {
                    double a = (Math.PI * 2.0 * i) / 28.0;
                    double elev = ((i % 5) - 2) * 0.35;
                    serverLevel.sendParticles(i % 2 == 0 ? ParticleTypes.TOTEM_OF_UNDYING : ParticleTypes.WITCH,
                            this.getX() + Math.cos(a) * 1.4,
                            apexY + elev,
                            this.getZ() + Math.sin(a) * 1.4,
                            2, Math.cos(a) * 0.45, elev * 0.25, Math.sin(a) * 0.45, 0.12);
                }
            } else if (tick % 3 == 0) {
                double a = tick * 0.55;
                serverLevel.sendParticles(ParticleTypes.TOTEM_OF_UNDYING,
                        this.getX() + Math.cos(a) * 1.8, apexY, this.getZ() + Math.sin(a) * 1.8,
                        2, 0.08, 0.12, 0.08, 0.03);
            }
        } else if (tick < 54) {
            // Stage 3a (ticks 49..53): Blazing Meteor Dive trail downward from apex to earth
            double diveFrac = (tick - 48) / 6.0D;
            double py = this.getY() + 4.2D * (1.0D - diveFrac);
            serverLevel.sendParticles(ParticleTypes.FLAME,
                    this.getX(), py, this.getZ(), 10, 0.35, 0.45, 0.35, 0.05);
            serverLevel.sendParticles(XuanhuanParticles.EMBER_SPARK.get(),
                    this.getX(), py, this.getZ(), 8, 0.45, 0.45, 0.45, 0.08);
        } else if (tick == 54) {
            // Stage 3b (tick 54): CATACLYSMIC GROUND-SLAM IMPACT SHOCKWAVE (13-block 360° blast!)
            this.playSound(SoundEvents.GENERIC_EXPLODE.value(), 1.9F, 0.58F);
            this.playSound(SoundEvents.TRIDENT_THUNDER.value(), 1.65F, 0.62F);
            serverLevel.sendParticles(ParticleTypes.EXPLOSION_EMITTER,
                    this.getX(), this.getY() + 0.6, this.getZ(), 2, 0.8, 0.3, 0.8, 0.0);
            serverLevel.sendParticles(XuanhuanParticles.SEAL_RING.get(),
                    this.getX(), this.getY() + 0.18, this.getZ(), 1, 0, 0, 0, 0);
            serverLevel.sendParticles(XuanhuanParticles.QI_RING.get(),
                    this.getX(), this.getY() + 0.9, this.getZ(), 1, 0, 0, 0, 0);
            for (int ray = 0; ray < 8; ray++) {
                double ang = (Math.PI * 2.0 * ray) / 8.0;
                double cx = Math.cos(ang), cz = Math.sin(ang);
                for (int step = 2; step <= 12; step += 2) {
                    serverLevel.sendParticles(XuanhuanParticles.EMBER_SPARK.get(),
                            this.getX() + cx * step, this.getY() + 0.35, this.getZ() + cz * step,
                            4, 0.25, 0.45, 0.25, 0.08);
                    serverLevel.sendParticles(ParticleTypes.SOUL_FIRE_FLAME,
                            this.getX() + cx * step, this.getY() + 0.2, this.getZ() + cz * step,
                            2, 0.2, 0.25, 0.2, 0.04);
                }
            }
            AABB area = this.getBoundingBox().inflate(13.0D, 4.5D, 13.0D);
            for (LivingEntity target : serverLevel.getEntitiesOfClass(LivingEntity.class, area,
                    e -> e != this && e.isAlive() && !this.isAlliedTo(e))) {
                double dx = target.getX() - this.getX();
                double dz = target.getZ() - this.getZ();
                double dist = Math.sqrt(dx * dx + dz * dz);
                if (dist <= 13.0D) {
                    target.hurtServer(serverLevel, this.damageSources().mobAttack(this), 14.0F);
                    if (dist > 1.0E-4D) {
                        double strength = 1.65D * Math.max(0.45D, 1.0D - dist / 15.0D);
                        target.push((dx / dist) * strength, 0.62D, (dz / dist) * strength);
                        target.hurtMarked = true;
                    }
                }
            }
        } else if (tick == 58 || tick == 62) {
            double r = tick == 58 ? 7.5D : 11.5D;
            for (int i = 0; i < 24; i++) {
                double a = (Math.PI * 2.0 * i) / 24.0;
                serverLevel.sendParticles(i % 2 == 0 ? XuanhuanParticles.EMBER_SPARK.get() : ParticleTypes.END_ROD,
                        this.getX() + Math.cos(a) * r, this.getY() + 0.45, this.getZ() + Math.sin(a) * r,
                        2, 0.15, 0.25, 0.15, 0.06);
            }
        } else if (tick == PHASE_TRANSITION_DURATION - 3) {
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
                this.getNavigation().stop();
                if (nextTick <= ULTIMATE_TELEGRAPH_DURATION) {
                    emitUltimateTelegraph(serverLevel, nextTick);
                    this.setDeltaMovement(this.getDeltaMovement().multiply(0.30D, 1.0D, 0.30D));
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
        double ringRadius = 4.5 + (tick / (double) ULTIMATE_TELEGRAPH_DURATION) * 11.0;
        for (int i = 0; i < 20; i++) {
            double a = (Math.PI * 2.0 * i / 20.0) + tick * 0.14;
            serverLevel.sendParticles(i % 2 == 0 ? XuanhuanParticles.QI_SPARK.get() : ParticleTypes.END_ROD,
                    this.getX() + Math.cos(a) * ringRadius,
                    this.getY() + 0.35 + (tick % 4) * 0.18,
                    this.getZ() + Math.sin(a) * ringRadius,
                    1, 0.04, 0.08, 0.04, 0.02);
        }
        // Plunging fiery astral meteors rain onto the 15.5-block arena during telegraph
        if (tick % 4 == 0) {
            int meteorIdx = tick / 4;
            for (int k = 0; k < 2; k++) {
                double ma = ((meteorIdx * 2 + k) * Math.PI * 2.0) / 8.0 + 0.35;
                double mr = 5.5 + ((meteorIdx + k) % 3) * 3.2;
                double mx = this.getX() + Math.cos(ma) * mr;
                double mz = this.getZ() + Math.sin(ma) * mr;
                for (int s = 0; s < 8; s++) {
                    double frac = s / 7.0;
                    serverLevel.sendParticles(s < 4 ? ParticleTypes.FLAME : XuanhuanParticles.EMBER_SPARK.get(),
                            mx + Math.cos(ma) * frac * 3.0, this.getY() + 0.4 + frac * 11.0, mz + Math.sin(ma) * frac * 3.0,
                            3, 0.25, 0.25, 0.25, 0.04);
                }
                serverLevel.sendParticles(ParticleTypes.EXPLOSION, mx, this.getY() + 0.5, mz, 2, 0.6, 0.3, 0.6, 0.0);
                AABB meteorBox = new AABB(mx - 3.0, this.getY() - 1.0, mz - 3.0, mx + 3.0, this.getY() + 3.5, mz + 3.0);
                for (LivingEntity le : serverLevel.getEntitiesOfClass(LivingEntity.class, meteorBox,
                        e -> e != this && e.isAlive() && !this.isAlliedTo(e))) {
                    le.hurtServer(serverLevel, this.damageSources().mobAttack(this), isPhase2() ? 10.0F : 7.5F);
                }
            }
            this.playSound(SoundEvents.FIRECHARGE_USE, 1.4F, 0.72F);
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
        int active = getActionType();
        if (active == ACTION_PHASE_TRANSITION
                || active == ACTION_ULTIMATE_JUDGMENT
                || active == ACTION_SWORD_ARRAY_BUFF) {
            return false;
        }
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
        boolean p2 = isPhase2();

        if (combo == 0) {
            // Combo 1: P1 6.2m cold-silver arc -> P2 8.5m 180° Twin-Moon Golden-Crimson Sweep + 2 Chasing Swords!
            double sweepRadius = p2 ? 8.5D : 6.2D;
            double maxDot = p2 ? -0.10D : 0.15D;
            int arcPts = p2 ? 24 : 14;
            for (int i = 0; i < arcPts; i++) {
                double frac = (double) i / (arcPts - 1);
                double ang = (frac - 0.5) * (p2 ? Math.PI * 1.05 : Math.PI * 0.78);
                double ca = Math.cos(ang), sa = Math.sin(ang);
                Vec3 dir = horizForward.scale(ca).add(right.scale(sa));
                serverLevel.sendParticles(p2 ? XuanhuanParticles.EMBER_SPARK.get() : ParticleTypes.END_ROD,
                        this.getX() + dir.x * (sweepRadius * 0.72), baseY + 0.12, this.getZ() + dir.z * (sweepRadius * 0.72),
                        2, 0.12, 0.10, 0.12, 0.03);
                if (p2) {
                    serverLevel.sendParticles(ParticleTypes.FLAME,
                            this.getX() + dir.x * (sweepRadius * 0.92), baseY + 0.45, this.getZ() + dir.z * (sweepRadius * 0.92),
                            1, 0.1, 0.1, 0.1, 0.02);
                }
            }
            AABB box = this.getBoundingBox().inflate(sweepRadius, 2.5D, sweepRadius);
            for (LivingEntity le : serverLevel.getEntitiesOfClass(LivingEntity.class, box,
                    e -> e != this && e != primaryTarget && e.isAlive() && !this.isAlliedTo(e))) {
                Vec3 to = new Vec3(le.getX() - this.getX(), 0.0, le.getZ() - this.getZ());
                double dist = to.length();
                if (dist <= sweepRadius && dist > 0.01 && to.normalize().dot(horizForward) >= maxDot) {
                    le.hurtServer(serverLevel, this.damageSources().mobAttack(this), p2 ? 12.5F : 8.5F);
                    le.push(to.x / dist * (p2 ? 1.15 : 0.75), p2 ? 0.42 : 0.25, to.z / dist * (p2 ? 1.15 : 0.75));
                    le.hurtMarked = true;
                }
            }
            if (p2 && primaryTarget instanceof LivingEntity living && canSummonFlyingSwordsAt(living)) {
                spawnFlyingSwordSwarm(serverLevel, living, 2, true);
            }
            this.playSound(SoundEvents.PLAYER_ATTACK_SWEEP, 1.35F, p2 ? 0.78F : 0.95F);
        } else if (combo == 1) {
            // Combo 2: P1 7.0m single fissure -> P2 9.5m TRIPLE-LANE Branching Fan Fissures (0°, ±28°)!
            double laneLen = p2 ? 9.5D : 7.0D;
            double[] laneAngles = p2 ? new double[]{0.0, 0.48, -0.48} : new double[]{0.0};
            for (double la : laneAngles) {
                Vec3 lDir = horizForward.scale(Math.cos(la)).add(right.scale(Math.sin(la)));
                for (int step = 2; step <= (int) laneLen; step++) {
                    double px = this.getX() + lDir.x * step;
                    double pz = this.getZ() + lDir.z * step;
                    serverLevel.sendParticles(p2 ? XuanhuanParticles.EMBER_SPARK.get() : XuanhuanParticles.QI_SPARK.get(),
                            px, this.getY() + 0.35, pz, 3, 0.22, 0.55, 0.22, 0.05);
                    if (step % 2 == 0) {
                        serverLevel.sendParticles(ParticleTypes.SOUL_FIRE_FLAME,
                                px, this.getY() + 0.18, pz, 2, 0.18, 0.35, 0.18, 0.03);
                    }
                    AABB segBox = new AABB(px - 1.35, this.getY() - 0.5, pz - 1.35, px + 1.35, this.getY() + 2.8, pz + 1.35);
                    for (LivingEntity le : serverLevel.getEntitiesOfClass(LivingEntity.class, segBox,
                            e -> e != this && e.isAlive() && !this.isAlliedTo(e))) {
                        le.hurtServer(serverLevel, this.damageSources().mobAttack(this), p2 ? 11.5F : 8.5F);
                        le.push(lDir.x * 0.65, p2 ? 0.58 : 0.38, lDir.z * 0.65);
                        le.hurtMarked = true;
                    }
                }
            }
            this.playSound(SoundEvents.TRIDENT_THUNDER.value(), 1.25F, p2 ? 0.85F : 1.08F);
        } else {
            // Combo 3: P1 8.5m cyan lance -> P2 12.5m Golden-Crimson Dragon Lance + 4.5m Tip Star-Nova Detonation!
            double lanceLen = p2 ? 12.5D : 8.5D;
            for (int step = 1; step <= (int) lanceLen; step++) {
                double px = this.getX() + horizForward.x * step;
                double pz = this.getZ() + horizForward.z * step;
                serverLevel.sendParticles(p2 ? XuanhuanParticles.EMBER_SPARK.get() : ParticleTypes.END_ROD,
                        px, baseY, pz, p2 ? 3 : 2, 0.18, 0.18, 0.18, 0.04);
                AABB segBox = new AABB(px - 1.4, baseY - 1.4, pz - 1.4, px + 1.4, baseY + 1.6, pz + 1.4);
                for (LivingEntity le : serverLevel.getEntitiesOfClass(LivingEntity.class, segBox,
                        e -> e != this && e.isAlive() && !this.isAlliedTo(e))) {
                    le.hurtServer(serverLevel, this.damageSources().mobAttack(this), p2 ? 12.0F : 9.0F);
                    le.push(horizForward.x * 0.95, 0.32, horizForward.z * 0.95);
                    le.hurtMarked = true;
                }
            }
            if (p2) {
                double tx = this.getX() + horizForward.x * lanceLen;
                double tz = this.getZ() + horizForward.z * lanceLen;
                serverLevel.sendParticles(ParticleTypes.EXPLOSION_EMITTER, tx, baseY, tz, 1, 0.4, 0.4, 0.4, 0.0);
                serverLevel.sendParticles(XuanhuanParticles.QI_RING.get(), tx, baseY, tz, 1, 0, 0, 0, 0);
                AABB novaBox = new AABB(tx - 4.5, this.getY() - 1.0, tz - 4.5, tx + 4.5, this.getY() + 3.5, tz + 4.5);
                for (LivingEntity le : serverLevel.getEntitiesOfClass(LivingEntity.class, novaBox,
                        e -> e != this && e.isAlive() && !this.isAlliedTo(e))) {
                    le.hurtServer(serverLevel, this.damageSources().mobAttack(this), 13.0F);
                    le.push(horizForward.x * 1.1, 0.45, horizForward.z * 1.1);
                    le.hurtMarked = true;
                }
            }
            this.playSound(SoundEvents.TRIDENT_THROW.value(), 1.35F, p2 ? 0.72F : 0.95F);
        }
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
