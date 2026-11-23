package com.xxsx.xuanhuanearth.entity;

import com.xxsx.xuanhuanearth.XuanhuanEarth;
import com.xxsx.xuanhuanearth.particle.XuanhuanParticles;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
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
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Phase-2 homing celestial flying sword (星辰飞剑):
 * - Pierces straight through targets, banks in a graceful sweeping curve, and re-homes repeatedly (3-4 pierces).
 * - Automatically re-acquires the nearest valid hostile target within 18 blocks if its current target dies.
 * - Supports shield blocking (shatters with metallic sparks) and Ghast-style melee deflection boomerang.
 */
public class AstralFlyingSwordEntity extends Entity {

    public static final int BASE_LIFETIME_TICKS = 115;
    public static final int EMPOWERED_LIFETIME_TICKS = 155;
    public static final int DEFLECT_RECOIL_TICKS = 14;
    public static final int PIERCE_CURVE_TICKS = 11;
    public static final int PER_TARGET_COOLDOWN_TICKS = 14;
    public static final float BASE_DAMAGE = 5.0F;
    public static final float EMPOWERED_DAMAGE_P1 = 8.5F;
    public static final float EMPOWERED_DAMAGE_P2 = 9.5F;

    private static final EntityDataAccessor<Boolean> DATA_EMPOWERED =
            SynchedEntityData.defineId(AstralFlyingSwordEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DATA_DEFLECTED =
            SynchedEntityData.defineId(AstralFlyingSwordEntity.class, EntityDataSerializers.BOOLEAN);

    private UUID ownerUuid;
    private UUID targetUuid;
    private int lifeTicks = 0;
    private int maxLifetime = BASE_LIFETIME_TICKS;
    private int launchDelay = 8;
    private int deflectRecoilTicks = 0;
    private int pierceCurveTicks = 0;
    private int pierceCount = 0;
    private int maxPierces = 3;
    private float curveSign = 1.0F;
    private float damage = BASE_DAMAGE;
    private final Map<UUID, Integer> recentHitCooldowns = new HashMap<>();

    public AstralFlyingSwordEntity(EntityType<? extends AstralFlyingSwordEntity> type, Level level) {
        super(type, level);
        this.noPhysics = false;
    }


    public static AstralFlyingSwordEntity summon(
            ServerLevel level,
            LivingEntity owner,
            LivingEntity target,
            Vec3 pos,
            Vec3 initialVel,
            float damage,
            boolean empowered,
            int launchDelay
    ) {
        AstralFlyingSwordEntity sword = new AstralFlyingSwordEntity(XuanhuanEarth.ASTRAL_FLYING_SWORD.get(), level);
        sword.setPos(pos.x, pos.y, pos.z);
        sword.configure(owner, target, damage, empowered, launchDelay);
        if (initialVel != null && initialVel.lengthSqr() > 1.0E-5) {
            sword.setDeltaMovement(initialVel);
            sword.updateRotationFromVelocity(initialVel);
        }
        level.addFreshEntity(sword);
        return sword;
    }

    public void configure(LivingEntity owner, LivingEntity target, float damage, boolean empowered, int launchDelay) {
        this.ownerUuid = owner != null ? owner.getUUID() : null;
        this.targetUuid = target != null ? target.getUUID() : null;
        this.damage = damage;
        this.launchDelay = Math.max(2, launchDelay);
        this.maxLifetime = empowered ? EMPOWERED_LIFETIME_TICKS : BASE_LIFETIME_TICKS;
        this.maxPierces = empowered ? 4 : 3;
        this.curveSign = this.level().getRandom().nextBoolean() ? 1.0F : -1.0F;
        this.entityData.set(DATA_EMPOWERED, empowered);
        this.entityData.set(DATA_DEFLECTED, false);
    }

    public boolean isEmpowered() { return this.entityData.get(DATA_EMPOWERED); }
    public boolean isDeflected() { return this.entityData.get(DATA_DEFLECTED); }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DATA_EMPOWERED, false);
        builder.define(DATA_DEFLECTED, false);
    }

    @Override
    public boolean isPickable() {
        return true;
    }

    @Override
    public float getPickRadius() {
        return 0.55F;
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        Entity attacker = source.getEntity();
        if (attacker instanceof LivingEntity living && (this.ownerUuid == null || !this.ownerUuid.equals(living.getUUID()))) {
            deflectBy(level, living, living.getLookAngle());
            return true;
        }
        return false;
    }

    public void deflectByEntity(ServerLevel level, LivingEntity deflector, Vec3 pushDir) {
        deflectBy(level, deflector, pushDir);
    }

    public void deflectBy(ServerLevel level, LivingEntity deflector, Vec3 pushDir) {
        Vec3 dir = pushDir.lengthSqr() > 1.0E-4 ? pushDir.normalize() : new Vec3(0.0, 0.2, 1.0);
        Entity owner = getOwnerEntity(level);
        if (owner != null && owner.isAlive()) {
            Vec3 toOwner = owner.getBoundingBox().getCenter().subtract(this.position());
            if (toOwner.lengthSqr() > 1.0E-4) {
                dir = dir.scale(0.35).add(toOwner.normalize().scale(0.65)).normalize();
            }
        }
        this.setDeltaMovement(dir.scale(1.12));
        this.deflectRecoilTicks = DEFLECT_RECOIL_TICKS;
        this.pierceCurveTicks = 0;
        this.entityData.set(DATA_DEFLECTED, true);
        this.playSound(SoundEvents.ANVIL_LAND, 0.65F, 1.65F);
        level.sendParticles(XuanhuanParticles.QI_RING.get(), getX(), getY(), getZ(), 1, 0, 0, 0, 0);
        level.sendParticles(ParticleTypes.CRIT, getX(), getY(), getZ(), 8, 0.2, 0.2, 0.2, 0.12);
    }

    @Override
    public void tick() {
        super.tick();
        this.lifeTicks++;

        if (this.level().isClientSide()) {
            Vec3 v = this.getDeltaMovement();
            if (v.lengthSqr() > 1.0E-5) {
                updateRotationFromVelocity(v);
            }
            if (this.tickCount % 2 == 0) {
                this.level().addParticle(
                        isEmpowered() ? XuanhuanParticles.EMBER_SPARK.get() : ParticleTypes.END_ROD,
                        getX(), getY(), getZ(), -v.x * 0.1, -v.y * 0.1, -v.z * 0.1);
            }
            return;
        }

        ServerLevel serverLevel = (ServerLevel) this.level();
        if (this.lifeTicks > this.maxLifetime || this.pierceCount >= this.maxPierces) {
            shatterWithSparks(serverLevel);
            this.discard();
            return;
        }

        // Tick down per-target pierce cooldowns
        recentHitCooldowns.entrySet().removeIf(e -> e.getValue() <= this.lifeTicks);

        // Launch hover anticipation
        if (this.launchDelay > 0) {
            this.launchDelay--;
            LivingEntity target = resolveTarget(serverLevel);
            if (target != null) {
                Vec3 aim = target.getBoundingBox().getCenter().subtract(this.position());
                if (aim.lengthSqr() > 1.0E-4) {
                    updateRotationFromVelocity(aim.normalize());
                }
            }
            this.setDeltaMovement(0.0, Mth.sin(this.lifeTicks * 0.35F) * 0.03, 0.0);
            if (this.launchDelay == 0 && target != null) {
                Vec3 aim = target.getBoundingBox().getCenter().subtract(this.position()).normalize();
                this.setDeltaMovement(aim.scale(isEmpowered() ? 1.06 : 0.90));
                this.playSound(SoundEvents.TRIDENT_THROW.value(), 0.55F, 1.55F);
            }
            return;
        }

        Vec3 vel = this.getDeltaMovement();
        float speed = isEmpowered() ? 1.08F : 0.92F;

        if (this.deflectRecoilTicks > 0) {
            this.deflectRecoilTicks--;
            Entity owner = getOwnerEntity(serverLevel);
            if (owner != null && owner.isAlive()) {
                Vec3 toOwner = owner.getBoundingBox().getCenter().subtract(this.position());
                if (toOwner.lengthSqr() > 1.0E-4) {
                    vel = vel.scale(0.84).add(toOwner.normalize().scale(0.24)).normalize().scale(1.12);
                }
            }
            if (this.deflectRecoilTicks == 0) {
                this.entityData.set(DATA_DEFLECTED, false);
            }
        } else if (this.pierceCurveTicks > 0) {
            // Sweeping figure-8 banking arc right after piercing through a target before re-locking!
            this.pierceCurveTicks--;
            Vec3 curDir = vel.lengthSqr() > 1.0E-4 ? vel.normalize() : new Vec3(0, 0, 1);
            Vec3 side = new Vec3(-curDir.z * this.curveSign, 0.24, curDir.x * this.curveSign).normalize();
            vel = curDir.scale(0.84).add(side.scale(0.24)).normalize().scale(speed * 0.96);
        } else {
            LivingEntity target = resolveTarget(serverLevel);
            if (target != null) {
                Vec3 toTarget = target.getBoundingBox().getCenter().subtract(this.position());
                if (toTarget.lengthSqr() > 1.0E-4) {
                    Vec3 desired = toTarget.normalize().scale(speed);
                    double steer = isEmpowered() ? 0.24 : 0.19;
                    vel = vel.scale(1.0 - steer).add(desired.scale(steer));
                    if (vel.lengthSqr() > 1.0E-4) {
                        vel = vel.normalize().scale(speed);
                    }
                }
            } else if (vel.lengthSqr() < 0.04) {
                vel = new Vec3(0, 0.05, speed);
            }
        }

        // Collision checks along velocity ray
        Vec3 pos = this.position();
        Vec3 nextPos = pos.add(vel);
        HitResult hit = ProjectileUtil.getHitResultOnMoveVector(this, this::canHitEntity);

        if (hit.getType() == HitResult.Type.ENTITY && hit instanceof EntityHitResult ehr) {
            Entity hitEntity = ehr.getEntity();
            if (hitEntity instanceof LivingEntity living && !recentHitCooldowns.containsKey(living.getUUID())) {
                handleLivingHit(serverLevel, living, vel);
                if (this.isRemoved()) return;
            }
        } else if (hit.getType() == HitResult.Type.BLOCK && hit instanceof BlockHitResult bhr) {
            // Ricochet cleanly off terrain instead of vanishing immediately so swords keep looping!
            Vec3 normal = Vec3.atLowerCornerOf(bhr.getDirection().getUnitVec3i());
            vel = vel.subtract(normal.scale(2.0 * vel.dot(normal))).scale(0.85);
            if (vel.y < 0.15) vel = new Vec3(vel.x, 0.28, vel.z);
            nextPos = pos.add(vel.scale(0.45));
            this.pierceCurveTicks = Math.max(this.pierceCurveTicks, 5);
        }

        this.setDeltaMovement(vel);
        this.setPos(nextPos.x, nextPos.y, nextPos.z);
        updateRotationFromVelocity(vel);
    }

    private void handleLivingHit(ServerLevel serverLevel, LivingEntity living, Vec3 vel) {
        // Shield block check: shatters sword with crisp block feedback
        if (living instanceof Player player && player.isBlocking()) {
            Vec3 look = player.getLookAngle();
            Vec3 incoming = vel.lengthSqr() > 1.0E-4 ? vel.normalize() : Vec3.ZERO;
            if (look.dot(incoming) < -0.15) {
                this.playSound(SoundEvents.SHIELD_BLOCK.value(), 1.0F, 1.15F);
                player.getUseItem().hurtAndBreak(2, player, EquipmentSlot.OFFHAND);
                shatterWithSparks(serverLevel);
                this.discard();
                return;
            }
        }

        LivingEntity owner = getOwnerEntity(serverLevel);
        boolean deflectedIntoOwner = isDeflected() && owner != null && living.getUUID().equals(owner.getUUID());
        float hitDmg = deflectedIntoOwner ? this.damage * 1.45F : this.damage;
        DamageSource ds = owner != null ? this.damageSources().mobAttack(owner) : this.damageSources().magic();
        living.hurtServer(serverLevel, ds, hitDmg);
        serverLevel.sendParticles(
                isEmpowered() ? XuanhuanParticles.EMBER_SPARK.get() : ParticleTypes.ELECTRIC_SPARK,
                living.getX(), living.getY() + living.getBbHeight() * 0.5, living.getZ(),
                8, 0.3, 0.3, 0.3, 0.14);
        this.playSound(SoundEvents.PLAYER_ATTACK_CRIT, 0.65F, 1.45F);

        if (deflectedIntoOwner) {
            shatterWithSparks(serverLevel);
            this.discard();
            return;
        }

        // Pierce straight through! Record cooldown, flip curve sign for figure-8, and keep hunting!
        this.pierceCount++;
        this.recentHitCooldowns.put(living.getUUID(), this.lifeTicks + PER_TARGET_COOLDOWN_TICKS);
        this.pierceCurveTicks = PIERCE_CURVE_TICKS;
        this.curveSign = -this.curveSign;

        if (this.pierceCount >= this.maxPierces) {
            shatterWithSparks(serverLevel);
            this.discard();
        }
    }

    private boolean canHitEntity(Entity e) {
        if (!e.isAlive() || e.isSpectator() || !(e instanceof LivingEntity)) return false;
        if (e instanceof AstralFlyingSwordEntity) return false;
        if (this.ownerUuid != null && this.ownerUuid.equals(e.getUUID())) {
            return isDeflected();
        }
        return !recentHitCooldowns.containsKey(e.getUUID());
    }

    private LivingEntity resolveTarget(ServerLevel level) {
        if (this.targetUuid != null) {
            Entity e = level.getEntity(this.targetUuid);
            if (e instanceof LivingEntity le && le.isAlive() && !le.isSpectator()) {
                return le;
            }
        }
        // Re-acquire nearest valid non-owner living enemy within 18 blocks!
        LivingEntity owner = getOwnerEntity(level);
        if (owner instanceof Mob mob && mob.getTarget() != null && mob.getTarget().isAlive()) {
            this.targetUuid = mob.getTarget().getUUID();
            return mob.getTarget();
        }
        AABB box = this.getBoundingBox().inflate(18.0);
        List<LivingEntity> candidates = level.getEntitiesOfClass(LivingEntity.class, box, e ->
                e.isAlive() && !e.isSpectator()
                        && !(e instanceof AstralSovereignEntity)
                        && (this.ownerUuid == null || !this.ownerUuid.equals(e.getUUID()))
                        && (!(e instanceof Player p) || (!p.isCreative() && !p.isSpectator()))
        );
        LivingEntity best = null;
        double bestDist = Double.MAX_VALUE;
        for (LivingEntity c : candidates) {
            double d = c.distanceToSqr(this);
            if (d < bestDist) {
                bestDist = d;
                best = c;
            }
        }
        if (best != null) {
            this.targetUuid = best.getUUID();
        }
        return best;
    }

    private LivingEntity getOwnerEntity(ServerLevel level) {
        if (this.ownerUuid == null) return null;
        Entity e = level.getEntity(this.ownerUuid);
        return e instanceof LivingEntity le ? le : null;
    }

    private void shatterWithSparks(ServerLevel level) {
        level.sendParticles(XuanhuanParticles.QI_RING.get(), getX(), getY(), getZ(), 1, 0, 0, 0, 0);
        level.sendParticles(ParticleTypes.END_ROD, getX(), getY(), getZ(), 6, 0.25, 0.25, 0.25, 0.08);
    }

    private void updateRotationFromVelocity(Vec3 v) {
        double horiz = Math.sqrt(v.x * v.x + v.z * v.z);
        this.setYRot((float) (Mth.atan2(v.x, v.z) * Mth.RAD_TO_DEG));
        this.setXRot((float) (Mth.atan2(v.y, horiz) * Mth.RAD_TO_DEG));
        this.yRotO = this.getYRot();
        this.xRotO = this.getXRot();
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        this.lifeTicks = input.getIntOr("LifeTicks", 0);
        this.maxLifetime = input.getIntOr("MaxLifetime", BASE_LIFETIME_TICKS);
        this.pierceCount = input.getIntOr("PierceCount", 0);
        this.maxPierces = input.getIntOr("MaxPierces", 3);
        this.damage = input.getFloatOr("Damage", BASE_DAMAGE);
        this.entityData.set(DATA_EMPOWERED, input.getBooleanOr("Empowered", false));
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        output.putInt("LifeTicks", this.lifeTicks);
        output.putInt("MaxLifetime", this.maxLifetime);
        output.putInt("PierceCount", this.pierceCount);
        output.putInt("MaxPierces", this.maxPierces);
        output.putFloat("Damage", this.damage);
        output.putBoolean("Empowered", isEmpowered());
    }
}
