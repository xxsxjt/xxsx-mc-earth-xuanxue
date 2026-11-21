package com.xxsx.xuanhuanearth.entity;

import com.xxsx.xuanhuanearth.XuanhuanEarth;
import com.xxsx.xuanhuanearth.particle.XuanhuanParticles;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Short-lived homing flying sword conjured by the Astral Sovereign (星辰飞剑).
 *
 * <p>Lifecycle: brief hover/aim telegraph -> smooth homing pursuit toward its locked target.
 * Like a Ghast fireball, players can strike the sword in melee to deflect it away (and even
 * counter-hit the Boss if aimed back); however, after a short recoil window the deflected
 * sword re-engages homing and arcs back toward its quarry until its short lifespan expires.
 * Impacts use {@code mobProjectile} damage so raised shields natively block and shatter it.</p>
 */
public class AstralFlyingSwordEntity extends Projectile {
    public static final int BASE_LIFETIME_TICKS = 76;
    public static final int EMPOWERED_LIFETIME_TICKS = 96;
    public static final int DEFLECT_RECOIL_TICKS = 14;
    public static final float BASE_DAMAGE = 5.0F;
    public static final float EMPOWERED_DAMAGE_P1 = 8.5F;
    public static final float EMPOWERED_DAMAGE_P2 = 9.5F;

    private static final EntityDataAccessor<Boolean> DATA_EMPOWERED =
            SynchedEntityData.defineId(AstralFlyingSwordEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DATA_RETURNING =
            SynchedEntityData.defineId(AstralFlyingSwordEntity.class, EntityDataSerializers.BOOLEAN);

    private int lifeTicks = 0;
    private int maxLifetimeTicks = BASE_LIFETIME_TICKS;
    private int launchDelayTicks = 10;
    private int deflectRecoilTicks = 0;
    private int deflectCount = 0;
    private float damage = BASE_DAMAGE;
    @Nullable
    private UUID targetUuid;
    @Nullable
    private UUID originalOwnerUuid;

    public AstralFlyingSwordEntity(EntityType<? extends AstralFlyingSwordEntity> type, Level level) {
        super(type, level);
        this.setNoGravity(true);
    }

    public static AstralFlyingSwordEntity summon(
            ServerLevel level,
            LivingEntity owner,
            LivingEntity target,
            Vec3 spawnPos,
            Vec3 initialDir,
            float damage,
            boolean empowered,
            int launchDelayTicks
    ) {
        AstralFlyingSwordEntity sword = new AstralFlyingSwordEntity(XuanhuanEarth.ASTRAL_FLYING_SWORD.get(), level);
        sword.setOwner(owner);
        sword.originalOwnerUuid = owner.getUUID();
        sword.targetUuid = target.getUUID();
        sword.damage = damage;
        sword.setEmpowered(empowered);
        sword.maxLifetimeTicks = empowered ? EMPOWERED_LIFETIME_TICKS : BASE_LIFETIME_TICKS;
        sword.launchDelayTicks = Math.max(4, launchDelayTicks);
        sword.setPos(spawnPos.x, spawnPos.y, spawnPos.z);
        Vec3 dir = initialDir.lengthSqr() > 1.0E-6 ? initialDir.normalize() : new Vec3(0.0, 0.0, 1.0);
        sword.setDeltaMovement(dir.scale(0.18));
        sword.syncRotationToVelocity(dir);
        level.addFreshEntity(sword);
        return sword;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DATA_EMPOWERED, false);
        builder.define(DATA_RETURNING, false);
    }

    public boolean isEmpowered() {
        return this.entityData.get(DATA_EMPOWERED);
    }

    public void setEmpowered(boolean empowered) {
        this.entityData.set(DATA_EMPOWERED, empowered);
    }

    public boolean isReturningAfterDeflect() {
        return this.entityData.get(DATA_RETURNING);
    }

    @Override
    public boolean isPickable() {
        return true;
    }

    @Override
    public float getPickRadius() {
        return 0.65F;
    }

    /**
     * Ghast-fireball-like melee deflection: hitting the flying sword knocks it backward
     * along the striker's gaze (potentially striking the Boss on the recoil), then after
     * {@link #DEFLECT_RECOIL_TICKS} ticks it automatically curves back to re-home on its target.
     */
    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        if (this.isInvulnerableToBase(source)) {
            return false;
        }
        Entity striker = source.getEntity();
        if (striker == null || (this.originalOwnerUuid != null && striker.getUUID().equals(this.originalOwnerUuid))) {
            return false;
        }
        return deflectByEntity(level, striker, striker.getLookAngle());
    }

    public boolean deflectByEntity(ServerLevel level, Entity striker, Vec3 pushDirection) {
        this.markHurt();
        this.deflectCount++;
        this.deflectRecoilTicks = DEFLECT_RECOIL_TICKS;
        this.entityData.set(DATA_RETURNING, false);
        // Ensure a deflected sword has just enough remaining lifetime to arc back cleanly, while staying short-lived
        int minRemaining = DEFLECT_RECOIL_TICKS + 34;
        if (this.maxLifetimeTicks - this.lifeTicks < minRemaining) {
            this.maxLifetimeTicks = Math.min(this.lifeTicks + minRemaining, EMPOWERED_LIFETIME_TICKS + 36);
        }
        if (this.targetUuid == null && striker instanceof LivingEntity) {
            this.targetUuid = striker.getUUID();
        }

        Vec3 dir = pushDirection.lengthSqr() > 1.0E-5
                ? pushDirection.normalize()
                : this.position().subtract(striker.position()).normalize();

        // Assist counter-aiming back toward the Boss if striker is looking roughly toward the Boss
        Entity originalOwner = findOriginalOwner(level);
        if (originalOwner != null && originalOwner.isAlive()) {
            Vec3 toBoss = originalOwner.getEyePosition().subtract(this.position());
            if (toBoss.lengthSqr() > 1.0E-4) {
                Vec3 bossDir = toBoss.normalize();
                if (bossDir.dot(dir) > 0.68) {
                    dir = dir.scale(0.35).add(bossDir.scale(0.65)).normalize();
                }
            }
        }

        this.setDeltaMovement(dir.scale(1.32));
        this.syncRotationToVelocity(dir);

        level.playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.ANVIL_LAND, this.getSoundSource(), 0.55F, 1.75F);
        level.playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.TRIDENT_HIT_GROUND, this.getSoundSource(), 0.85F, 1.45F);
        level.sendParticles(ParticleTypes.CRIT, this.getX(), this.getY(), this.getZ(),
                10, 0.25, 0.25, 0.25, 0.16);
        level.sendParticles(XuanhuanParticles.QI_SPARK.get(), this.getX(), this.getY(), this.getZ(),
                8, 0.2, 0.2, 0.2, 0.12);
        return true;
    }

    @Override
    public void tick() {
        super.tick();
        this.lifeTicks++;

        if (this.level() instanceof ServerLevel serverLevel) {
            if (this.lifeTicks >= this.maxLifetimeTicks) {
                shatterParticles(serverLevel, false);
                this.discard();
                return;
            }

            LivingEntity target = resolveTarget(serverLevel);
            if (this.deflectRecoilTicks > 0) {
                this.deflectRecoilTicks--;
                // Slow slightly during recoil before dramatic boomerang turn-back
                this.setDeltaMovement(this.getDeltaMovement().scale(0.95));
                if (this.deflectRecoilTicks == 0) {
                    this.entityData.set(DATA_RETURNING, true);
                    serverLevel.playSound(null, this.getX(), this.getY(), this.getZ(),
                            SoundEvents.TRIDENT_THROW.value(), this.getSoundSource(), 0.7F, 1.55F);
                    serverLevel.sendParticles(XuanhuanParticles.QI_RING.get(),
                            this.getX(), this.getY(), this.getZ(), 1, 0, 0, 0, 0);
                }
            } else if (this.lifeTicks <= this.launchDelayTicks) {
                // Brief poised hover & aim lock right after conjuration
                this.setDeltaMovement(this.getDeltaMovement().scale(0.86));
                if (target != null && target.isAlive()) {
                    Vec3 aim = target.position().add(0.0, target.getBbHeight() * 0.55, 0.0).subtract(this.position());
                    if (aim.lengthSqr() > 1.0E-5) {
                        this.syncRotationToVelocity(aim.normalize());
                    }
                    if (this.lifeTicks == this.launchDelayTicks) {
                        double launchSpeed = isEmpowered() ? 0.96 : 0.80;
                        this.setDeltaMovement(aim.normalize().scale(launchSpeed));
                        serverLevel.playSound(null, this.getX(), this.getY(), this.getZ(),
                                SoundEvents.TRIDENT_THROW.value(), this.getSoundSource(), 0.55F,
                                isEmpowered() ? 1.45F : 1.25F);
                    }
                }
            } else {
                // Homing pursuit (both normal flight and post-deflect boomerang return)
                double desiredSpeed = isEmpowered() ? 1.02 : 0.84;
                if (this.isReturningAfterDeflect()) {
                    desiredSpeed *= 1.12;
                }
                Vec3 vel = this.getDeltaMovement();
                if (target != null && target.isAlive()) {
                    Vec3 toTarget = target.position().add(0.0, target.getBbHeight() * 0.55, 0.0)
                            .subtract(this.position());
                    if (toTarget.lengthSqr() > 1.0E-5) {
                        Vec3 desiredDir = toTarget.normalize();
                        Vec3 curDir = vel.lengthSqr() > 1.0E-5 ? vel.normalize() : desiredDir;
                        double turnRate = this.isReturningAfterDeflect() ? 0.28 : (isEmpowered() ? 0.21 : 0.16);
                        Vec3 steered = curDir.lerp(desiredDir, turnRate).normalize();
                        vel = steered.scale(desiredSpeed);
                        this.setDeltaMovement(vel);
                    }
                } else if (vel.lengthSqr() < 0.04) {
                    this.setDeltaMovement(Vec3.directionFromRotation(this.getXRot(), this.getYRot()).scale(desiredSpeed));
                }
            }

            HitResult hit = ProjectileUtil.getHitResultOnMoveVector(this, this::canHitEntity);
            if (hit.getType() != HitResult.Type.MISS) {
                this.onHit(hit);
            }

            if (this.isAlive()) {
                spawnTrailParticles(serverLevel);
            }
        }

        Vec3 delta = this.getDeltaMovement();
        this.setPos(this.getX() + delta.x, this.getY() + delta.y, this.getZ() + delta.z);
        if (delta.lengthSqr() > 1.0E-4) {
            this.syncRotationToVelocity(delta);
        }
    }

    @Override
    protected boolean canHitEntity(Entity entity) {
        if (!super.canHitEntity(entity)) {
            return false;
        }
        // Only allow hitting the original Boss during the deflected recoil window!
        if (this.originalOwnerUuid != null && entity.getUUID().equals(this.originalOwnerUuid)) {
            return this.deflectRecoilTicks > 0;
        }
        // Ignore the entity that just deflected us during the first 4 ticks of recoil
        if (this.deflectRecoilTicks > DEFLECT_RECOIL_TICKS - 4 && this.targetUuid != null
                && entity.getUUID().equals(this.targetUuid)) {
            return false;
        }
        return true;
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        if (!(this.level() instanceof ServerLevel serverLevel)) {
            return;
        }
        Entity victim = result.getEntity();

        // Counter-hit on the Astral Sovereign if deflected directly back into the Boss
        if (this.originalOwnerUuid != null && victim.getUUID().equals(this.originalOwnerUuid)) {
            float counterDamage = this.damage * 1.4F;
            victim.hurtServer(serverLevel, this.damageSources().magic(), counterDamage);
            serverLevel.playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.TRIDENT_THUNDER.value(), this.getSoundSource(), 0.7F, 1.5F);
            shatterParticles(serverLevel, true);
            this.discard();
            return;
        }

        LivingEntity ownerLiving = findOriginalOwner(serverLevel) instanceof LivingEntity le ? le : null;
        DamageSource source = this.damageSources().mobProjectile(this, ownerLiving);

        if (victim instanceof LivingEntity livingTarget) {
            boolean wasBlocking = livingTarget.isBlocking();
            boolean hurtApplied = livingTarget.hurtServer(serverLevel, source, this.damage);
            if (wasBlocking && !hurtApplied) {
                // Clean shield block feedback: sparks, shield clang, shatter the flying sword
                serverLevel.playSound(null, livingTarget.getX(), livingTarget.getY(), livingTarget.getZ(),
                        SoundEvents.SHIELD_BLOCK.value(), this.getSoundSource(), 1.0F, 1.1F);
                serverLevel.sendParticles(ParticleTypes.CRIT, this.getX(), this.getY(), this.getZ(),
                        12, 0.28, 0.28, 0.28, 0.14);
                serverLevel.sendParticles(XuanhuanParticles.QI_SPARK.get(), this.getX(), this.getY(), this.getZ(),
                        10, 0.25, 0.25, 0.25, 0.1);
                this.discard();
                return;
            }
        } else {
            victim.hurtServer(serverLevel, source, this.damage);
        }

        serverLevel.playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.TRIDENT_HIT, this.getSoundSource(), 0.85F, isEmpowered() ? 1.35F : 1.15F);
        shatterParticles(serverLevel, isEmpowered());
        this.discard();
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);
        if (this.level() instanceof ServerLevel serverLevel) {
            serverLevel.playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.TRIDENT_HIT_GROUND, this.getSoundSource(), 0.65F, 1.35F);
            shatterParticles(serverLevel, false);
        }
        this.discard();
    }

    private void spawnTrailParticles(ServerLevel level) {
        if (isEmpowered() || isReturningAfterDeflect()) {
            level.sendParticles(XuanhuanParticles.EMBER_SPARK.get(),
                    this.getX(), this.getY(), this.getZ(), 1, 0.06, 0.06, 0.06, 0.01);
            level.sendParticles(ParticleTypes.END_ROD,
                    this.getX(), this.getY(), this.getZ(), 1, 0.04, 0.04, 0.04, 0.01);
        } else if (this.lifeTicks % 2 == 0) {
            level.sendParticles(XuanhuanParticles.QI_SPARK.get(),
                    this.getX(), this.getY(), this.getZ(), 1, 0.05, 0.05, 0.05, 0.01);
        }
    }

    private void shatterParticles(ServerLevel level, boolean intense) {
        level.sendParticles(XuanhuanParticles.QI_SPARK.get(),
                this.getX(), this.getY(), this.getZ(), intense ? 14 : 7, 0.25, 0.25, 0.25, 0.12);
        level.sendParticles(intense ? ParticleTypes.SOUL_FIRE_FLAME : ParticleTypes.END_ROD,
                this.getX(), this.getY(), this.getZ(), intense ? 8 : 4, 0.2, 0.2, 0.2, 0.08);
    }

    private void syncRotationToVelocity(Vec3 dir) {
        double horiz = Math.sqrt(dir.x * dir.x + dir.z * dir.z);
        float yRot = (float) (Mth.atan2(dir.x, dir.z) * Mth.RAD_TO_DEG);
        float xRot = (float) (Mth.atan2(dir.y, horiz) * Mth.RAD_TO_DEG);
        this.setYRot(yRot);
        this.setXRot(xRot);
        this.yRotO = yRot;
        this.xRotO = xRot;
    }

    @Nullable
    private LivingEntity resolveTarget(ServerLevel level) {
        if (this.targetUuid != null) {
            Entity e = level.getEntity(this.targetUuid);
            if (e instanceof LivingEntity living && living.isAlive()) {
                return living;
            }
        }
        // Fallback: re-acquire Boss's current target if original target vanished
        Entity owner = findOriginalOwner(level);
        if (owner instanceof AstralSovereignEntity boss && boss.getTarget() != null && boss.getTarget().isAlive()) {
            this.targetUuid = boss.getTarget().getUUID();
            return boss.getTarget();
        }
        return null;
    }

    @Nullable
    private Entity findOriginalOwner(ServerLevel level) {
        if (this.originalOwnerUuid != null) {
            Entity e = level.getEntity(this.originalOwnerUuid);
            if (e != null) return e;
        }
        return this.getOwner();
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putInt("LifeTicks", this.lifeTicks);
        output.putInt("MaxLifetimeTicks", this.maxLifetimeTicks);
        output.putInt("LaunchDelayTicks", this.launchDelayTicks);
        output.putInt("DeflectRecoilTicks", this.deflectRecoilTicks);
        output.putInt("DeflectCount", this.deflectCount);
        output.putFloat("Damage", this.damage);
        output.putBoolean("Empowered", isEmpowered());
        output.putBoolean("Returning", isReturningAfterDeflect());
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.lifeTicks = Math.max(0, input.getIntOr("LifeTicks", 0));
        this.maxLifetimeTicks = Math.max(20, input.getIntOr("MaxLifetimeTicks", BASE_LIFETIME_TICKS));
        this.launchDelayTicks = Math.max(0, input.getIntOr("LaunchDelayTicks", 10));
        this.deflectRecoilTicks = Math.max(0, input.getIntOr("DeflectRecoilTicks", 0));
        this.deflectCount = Math.max(0, input.getIntOr("DeflectCount", 0));
        this.damage = Math.max(1.0F, input.getFloatOr("Damage", BASE_DAMAGE));
        setEmpowered(input.getBooleanOr("Empowered", false));
        this.entityData.set(DATA_RETURNING, input.getBooleanOr("Returning", false));
    }
}
