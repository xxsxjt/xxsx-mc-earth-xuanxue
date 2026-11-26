package com.xxsx.xuanhuanearth.entity;

import com.xxsx.earthlore.api.LoreApi;
import com.xxsx.xuanhuanearth.TalismanComponent;
import com.xxsx.xuanhuanearth.XuanhuanEarth;
import com.xxsx.xuanhuanearth.XuanhuanEffects;
import com.xxsx.xuanhuanearth.particle.XuanhuanParticles;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.stats.Stats;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.LookAtTradingPlayerGoal;
import net.minecraft.world.entity.ai.goal.TradeWithPlayerGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.npc.villager.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * Cultivation world humanoids with tiered ranks (Wandering Merchant, Spirit Spring Keeper,
 * Sect Steward, Outer Disciple, Sect Elder), randomized elemental affinities, randomized
 * 3D spirit weapons, and distinct multi-move combat kits.
 */
public final class CultivationSettlerEntity extends AbstractVillager {
    private static final EntityDataAccessor<Integer> DATA_ROLE = SynchedEntityData.defineId(
            CultivationSettlerEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_COMBAT_POSE = SynchedEntityData.defineId(
            CultivationSettlerEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_ELEMENT = SynchedEntityData.defineId(
            CultivationSettlerEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_WEAPON = SynchedEntityData.defineId(
            CultivationSettlerEntity.class, EntityDataSerializers.INT);

    public static final int POSE_IDLE = 0;
    public static final int POSE_MELEE = 1;
    public static final int POSE_CAST = 2;
    public static final int POSE_BURST = 3;

    @Nullable
    private LivingEntity combatTarget;
    private int aggroTicks;
    private int attackCooldown;
    private int specialCooldown;
    private int evadeCooldown;
    private int poseTicks;
    private boolean initializedTraits;
    private boolean elderSecondWindUsed;

    public CultivationSettlerEntity(EntityType<? extends CultivationSettlerEntity> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 36.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.31D)
                .add(Attributes.FOLLOW_RANGE, 28.0D)
                .add(Attributes.ATTACK_DAMAGE, 6.0D)
                .add(Attributes.ARMOR, 4.0D)
                .add(Attributes.STEP_HEIGHT, 1.1D);
    }

    private void ensureRandomizedTraits() {
        if (this.initializedTraits || this.level().isClientSide()) {
            return;
        }
        this.initializedTraits = true;
        // If spawned via spawn egg with default role=0, occasionally diversify into Outer Disciple / Elder too
        if (getRole() == Role.WANDERING_MERCHANT && this.tickCount <= 2) {
            int roll = this.random.nextInt(100);
            if (roll < 28) {
                setRole(Role.OUTER_DISCIPLE);
            } else if (roll < 42) {
                setRole(Role.SECT_ELDER);
            } else if (roll < 62) {
                setRole(Role.SECT_STEWARD);
            } else if (roll < 80) {
                setRole(Role.SPIRIT_SPRING_KEEPER);
            }
        }
        setElementAffinity(ElementAffinity.randomForRole(getRole(), this.random));
        setWeaponStyle(WeaponStyle.randomForRole(getRole(), this.random));
        applyRoleAttributes(true);
    }

    public void applyRoleAttributes(boolean refillHealth) {
        Role role = getRole();
        setAttr(Attributes.MAX_HEALTH, role.maxHealth());
        setAttr(Attributes.ARMOR, role.armor());
        setAttr(Attributes.ATTACK_DAMAGE, role.attackDamage());
        setAttr(Attributes.MOVEMENT_SPEED, role.moveSpeed());
        setAttr(Attributes.KNOCKBACK_RESISTANCE, role.knockbackResistance());
        if (refillHealth) {
            this.setHealth((float) role.maxHealth());
        } else if (this.getHealth() > this.getMaxHealth()) {
            this.setHealth(this.getMaxHealth());
        }
    }

    private void setAttr(net.minecraft.core.Holder<net.minecraft.world.entity.ai.attributes.Attribute> attr, double val) {
        AttributeInstance inst = this.getAttribute(attr);
        if (inst != null) {
            inst.setBaseValue(val);
        }
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (!this.level().isClientSide() && !this.initializedTraits) {
            ensureRandomizedTraits();
        }
        if (this.attackCooldown > 0) this.attackCooldown--;
        if (this.specialCooldown > 0) this.specialCooldown--;
        if (this.evadeCooldown > 0) this.evadeCooldown--;
        if (this.aggroTicks > 0) this.aggroTicks--;
        if (this.poseTicks > 0) {
            this.poseTicks--;
            if (this.poseTicks == 0 && !this.level().isClientSide()) {
                setCombatPose(POSE_IDLE);
            }
        }

        if (this.level() instanceof ServerLevel serverLevel) {
            if (this.combatTarget == null || !this.combatTarget.isAlive()) {
                double scanRange = getRole() == Role.SECT_ELDER ? 16.0D : 11.0D;
                Monster nearbyMonster = serverLevel.getEntitiesOfClass(Monster.class,
                        this.getBoundingBox().inflate(scanRange),
                        m -> m.isAlive() && (m.getTarget() == this || getRole().proactivelyGuards()))
                        .stream().findFirst().orElse(null);
                if (nearbyMonster != null) {
                    this.combatTarget = nearbyMonster;
                    this.aggroTicks = 280;
                }
            }

            if (this.combatTarget != null && this.combatTarget.isAlive() && this.aggroTicks > 0
                    && this.distanceToSqr(this.combatTarget) < 784.0D) {
                tickCultivatorCombat(serverLevel, this.combatTarget);
            } else if (this.aggroTicks <= 0 || (this.combatTarget != null && !this.combatTarget.isAlive())) {
                this.combatTarget = null;
                if (getCombatPose() != POSE_IDLE) {
                    setCombatPose(POSE_IDLE);
                }
            }
        }
    }

    private void tickCultivatorCombat(ServerLevel serverLevel, LivingEntity target) {
        this.getLookControl().setLookAt(target, 32.0F, 32.0F);
        Role role = getRole();
        WeaponStyle weapon = getWeaponStyle();
        ElementAffinity elem = getElementAffinity();
        double distSqr = this.distanceToSqr(target);
        double meleeRange = weapon.meleeReach();
        double idealDist = weapon == WeaponStyle.DAOIST_STAFF ? 7.5D : (role == Role.SECT_ELDER ? 5.0D : meleeRange - 0.6D);

        // Tactical footwork: elders & staff casters kite or dash; melee disciples close in and hop obstacles
        if (distSqr > (idealDist + 1.5D) * (idealDist + 1.5D)) {
            this.getNavigation().moveTo(target, role == Role.SECT_ELDER ? 1.15D : 0.96D);
            if (this.horizontalCollision && this.onGround()) {
                this.getJumpControl().jump();
            }
        } else if (weapon == WeaponStyle.DAOIST_STAFF && distSqr < 14.0D && this.evadeCooldown <= 0) {
            performSpiritStep(serverLevel, target, false);
        } else {
            this.getNavigation().stop();
        }

        // Elder Second Wind below 50% HP: protective Qi barrier + heal + ring knockback
        if (role == Role.SECT_ELDER && !this.elderSecondWindUsed && this.getHealth() <= this.getMaxHealth() * 0.5F) {
            this.elderSecondWindUsed = true;
            this.heal(this.getMaxHealth() * 0.25F);
            this.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 140, 1, true, false, true));
            this.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 160, 1, true, false, true));
            setCombatPose(POSE_BURST);
            this.poseTicks = 22;
            XuanhuanEffects.groundRing(serverLevel, this.position(), 1.6F);
            serverLevel.sendParticles(elem.particle(), this.getX(), this.getY() + 1.1D, this.getZ(), 24, 0.8D, 0.6D, 0.8D, 0.08D);
            this.playSound(SoundEvents.BEACON_ACTIVATE, 1.1F, 1.25F);
        }

        // Role-specific signature arts (on independent specialCooldown)
        if (this.specialCooldown <= 0 && distSqr <= 324.0D) {
            if (performRoleSignatureArt(serverLevel, target, role, elem)) {
                return;
            }
        }

        if (this.attackCooldown > 0) {
            return;
        }

        // Primary weapon strike vs Elemental ranged spell
        if (distSqr <= meleeRange * meleeRange) {
            performWeaponStrike(serverLevel, target, role, weapon, elem);
        } else if (distSqr <= 324.0D) {
            performElementalSpell(serverLevel, target, role, weapon, elem);
        }
    }

    private void performWeaponStrike(ServerLevel level, LivingEntity target, Role role, WeaponStyle weapon, ElementAffinity elem) {
        this.swing(InteractionHand.MAIN_HAND);
        setCombatPose(POSE_MELEE);
        this.poseTicks = 13;

        float dmg = (float) role.attackDamage() * weapon.meleeDamageMul();
        target.hurtServer(level, level.damageSources().mobAttack(this), dmg);
        elem.applyOnHitEffect(level, this, target, role == Role.SECT_ELDER ? 1 : 0);

        if (weapon == WeaponStyle.IRON_FAN) {
            // Sweeping fan gust cleaves & pushes slightly
            Vec3 push = target.position().subtract(this.position());
            double len = Math.max(0.05D, push.horizontalDistance());
            target.push((push.x / len) * 0.55D, 0.22D, (push.z / len) * 0.55D);
            target.hurtMarked = true;
            level.sendParticles(ParticleTypes.SWEEP_ATTACK, target.getX(), target.getY(0.5D), target.getZ(), 2, 0.25D, 0.1D, 0.25D, 0.0D);
        } else if (weapon == WeaponStyle.JADE_SPEAR) {
            // Piercing spear thrust particles
            level.sendParticles(ParticleTypes.CRIT, target.getX(), target.getY(0.55D), target.getZ(), 10, 0.25D, 0.25D, 0.25D, 0.10D);
        } else {
            level.sendParticles(ParticleTypes.SWEEP_ATTACK, target.getX(), target.getY(0.5D), target.getZ(), 1, 0.0D, 0.0D, 0.0D, 0.0D);
        }
        level.sendParticles(elem.particle(), target.getX(), target.getY(0.5D), target.getZ(), 6, 0.25D, 0.25D, 0.25D, 0.05D);
        this.playSound(SoundEvents.PLAYER_ATTACK_SWEEP, 0.9F, weapon == WeaponStyle.JADE_SPEAR ? 1.35F : 1.05F);
        this.attackCooldown = Math.max(14, role.baseAttackCooldown() - weapon.meleeHasteBonus());
    }

    private void performElementalSpell(ServerLevel level, LivingEntity target, Role role, WeaponStyle weapon, ElementAffinity elem) {
        this.swing(InteractionHand.MAIN_HAND);
        setCombatPose(POSE_CAST);
        this.poseTicks = 18;

        Vec3 start = this.position().add(0.0D, this.getBbHeight() * 0.68D, 0.0D);
        Vec3 end = target.position().add(0.0D, target.getBbHeight() * 0.52D, 0.0D);
        spawnSpellBeam(level, start, end, elem.particle(), elem.secondaryParticle());

        float spellDmg = role.spellDamage() * weapon.spellDamageMul();
        if (!target.isBlocking() || role == Role.SECT_ELDER) {
            target.hurtServer(level, level.damageSources().indirectMagic(this, this), spellDmg);
            elem.applyOnHitEffect(level, this, target, role == Role.SECT_ELDER ? 1 : 0);
        }
        this.playSound(elem.castSound(), 0.95F, 1.15F);
        this.attackCooldown = Math.max(20, role.baseCastCooldown() - weapon.castHasteBonus());
    }

    private boolean performRoleSignatureArt(ServerLevel level, LivingEntity target, Role role, ElementAffinity elem) {
        switch (role) {
            case SECT_ELDER -> {
                // Elder alternates between Flying Sword Summon (2-3 swords) and Elemental Domain Burst!
                setCombatPose(POSE_BURST);
                this.poseTicks = 24;
                if (this.random.nextBoolean()) {
                    int count = 2 + this.random.nextInt(2);
                    for (int i = 0; i < count; i++) {
                        double angle = (Math.PI * 2.0D * i) / count + this.random.nextDouble() * 0.4D;
                        Vec3 spawn = this.position().add(Math.cos(angle) * 1.4D, 1.9D + (i % 2) * 0.35D, Math.sin(angle) * 1.4D);
                        Vec3 dir = target.position().add(0, target.getBbHeight() * 0.5D, 0).subtract(spawn);
                        Vec3 initVel = dir.lengthSqr() > 0.01D ? dir.normalize().scale(0.65D) : new Vec3(0, 0.2D, 0.65D);
                        AstralFlyingSwordEntity.summon(level, this, target, spawn, initVel, 4.5F, false, 6 + i * 3);
                    }
                    XuanhuanEffects.groundRing(level, this.position(), 1.25F);
                    this.playSound(SoundEvents.ENCHANTMENT_TABLE_USE, 1.1F, 1.35F);
                    this.specialCooldown = 115;
                } else {
                    // Elemental Domain Burst at target position (3.5m AoE)
                    Vec3 center = target.position();
                    XuanhuanEffects.groundRing(level, center, 1.4F);
                    level.sendParticles(elem.particle(), center.x, center.y + 0.5D, center.z, 26, 1.4D, 0.7D, 1.4D, 0.08D);
                    level.sendParticles(elem.secondaryParticle(), center.x, center.y + 1.0D, center.z, 16, 1.1D, 0.9D, 1.1D, 0.05D);
                    AABB box = new AABB(center.x - 3.6D, center.y - 1.5D, center.z - 3.6D, center.x + 3.6D, center.y + 3.0D, center.z + 3.6D);
                    for (LivingEntity victim : level.getEntitiesOfClass(LivingEntity.class, box,
                            e -> e != this && e.isAlive() && !(e instanceof CultivationSettlerEntity))) {
                        victim.hurtServer(level, level.damageSources().indirectMagic(this, this), 8.5F);
                        elem.applyOnHitEffect(level, this, victim, 1);
                    }
                    this.playSound(elem.castSound(), 1.25F, 0.85F);
                    this.specialCooldown = 105;
                }
                this.attackCooldown = 24;
                return true;
            }
            case SECT_STEWARD -> {
                // Triple fan of elemental Qi blades
                setCombatPose(POSE_CAST);
                this.poseTicks = 20;
                Vec3 start = this.position().add(0.0D, 1.25D, 0.0D);
                Vec3 baseDir = target.position().add(0.0D, target.getBbHeight() * 0.5D, 0.0D).subtract(start);
                double len = Math.max(0.1D, baseDir.length());
                Vec3 norm = baseDir.scale(1.0D / len);
                for (int i = -1; i <= 1; i++) {
                    Vec3 rotated = norm.yRot(i * 0.16F);
                    spawnSpellBeam(level, start, start.add(rotated.scale(Math.min(12.0D, len + 2.0D))), elem.particle(), elem.secondaryParticle());
                }
                target.hurtServer(level, level.damageSources().indirectMagic(this, this), 6.5F);
                elem.applyOnHitEffect(level, this, target, 0);
                this.playSound(SoundEvents.TRIDENT_THROW.value(), 0.95F, 1.25F);
                this.specialCooldown = 95;
                this.attackCooldown = 26;
                return true;
            }
            case SPIRIT_SPRING_KEEPER -> {
                // Spirit Spring Rejuvenation: heals self & nearby allied cultivators + slows target
                if (this.getHealth() < this.getMaxHealth() * 0.85F || this.random.nextFloat() < 0.45F) {
                    setCombatPose(POSE_BURST);
                    this.poseTicks = 20;
                    for (CultivationSettlerEntity ally : level.getEntitiesOfClass(CultivationSettlerEntity.class, this.getBoundingBox().inflate(10.0D))) {
                        if (ally.isAlive()) {
                            ally.heal(5.0F);
                            ally.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 80, 0, true, false, true));
                            level.sendParticles(ParticleTypes.HAPPY_VILLAGER, ally.getX(), ally.getY() + 1.2D, ally.getZ(), 6, 0.35D, 0.35D, 0.35D, 0.03D);
                        }
                    }
                    target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 70, 1));
                    XuanhuanEffects.groundRing(level, this.position(), 1.1F);
                    this.playSound(SoundEvents.AMETHYST_BLOCK_CHIME, 1.0F, 1.4F);
                    this.specialCooldown = 120;
                    this.attackCooldown = 24;
                    return true;
                }
                return false;
            }
            case OUTER_DISCIPLE -> {
                // Nimble forward sword/spear lunge strike when at mid-range (4..9 blocks)
                double d2 = this.distanceToSqr(target);
                if (d2 >= 16.0D && d2 <= 81.0D) {
                    performSpiritStep(level, target, true);
                    this.specialCooldown = 80;
                    this.attackCooldown = 14;
                    return true;
                }
                return false;
            }
            case WANDERING_MERCHANT -> {
                // Smoke talisman evade + elemental talisman toss when enemy gets close
                if (this.distanceToSqr(target) < 20.0D) {
                    performSpiritStep(level, target, false);
                    target.hurtServer(level, level.damageSources().indirectMagic(this, this), 4.5F);
                    elem.applyOnHitEffect(level, this, target, 0);
                    this.specialCooldown = 85;
                    this.attackCooldown = 22;
                    return true;
                }
                return false;
            }
        }
        return false;
    }

    private void performSpiritStep(ServerLevel level, LivingEntity target, boolean towardTarget) {
        Vec3 diff = target.position().subtract(this.position());
        double horiz = Math.max(0.05D, diff.horizontalDistance());
        double sign = towardTarget ? 1.0D : -1.0D;
        double speed = towardTarget ? 0.78D : 0.65D;
        level.sendParticles(ParticleTypes.CLOUD, this.getX(), this.getY() + 0.6D, this.getZ(), 10, 0.35D, 0.35D, 0.35D, 0.04D);
        this.setDeltaMovement((diff.x / horiz) * sign * speed, 0.32D, (diff.z / horiz) * sign * speed);
        this.needsSync = true;
        this.evadeCooldown = 55;
        this.playSound(SoundEvents.BREEZE_WIND_CHARGE_BURST.value(), 0.75F, 1.35F);
    }

    private static void spawnSpellBeam(ServerLevel level, Vec3 start, Vec3 end, ParticleOptions p1, ParticleOptions p2) {
        Vec3 dir = end.subtract(start);
        double len = dir.length();
        if (len <= 0.1D) return;
        Vec3 step = dir.scale(0.55D / len);
        int steps = Math.min(34, (int) (len / 0.55D));
        for (int i = 1; i <= steps; i++) {
            Vec3 p = start.add(step.scale(i));
            level.sendParticles(p1, p.x, p.y, p.z, 1, 0.03D, 0.03D, 0.03D, 0.01D);
            if (i % 2 == 0) {
                level.sendParticles(p2, p.x, p.y, p.z, 1, 0.03D, 0.03D, 0.03D, 0.01D);
            }
        }
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        boolean hurt = super.hurtServer(level, source, amount);
        if (hurt && this.isAlive()) {
            Entity attackerEntity = source.getEntity();
            if (attackerEntity instanceof LivingEntity attacker && attacker != this
                    && !(attacker instanceof CultivationSettlerEntity)) {
                this.combatTarget = attacker;
                this.aggroTicks = getRole() == Role.SECT_ELDER ? 600 : 420;
                if (this.attackCooldown <= 0) {
                    this.attackCooldown = 8;
                }
                int resAmp = getRole() == Role.SECT_ELDER ? 1 : 0;
                this.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 80, resAmp, true, false, true));
                if (getRole() == Role.SECT_ELDER || getRole() == Role.SECT_STEWARD) {
                    this.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 100, resAmp, true, false, true));
                }
                XuanhuanEffects.talismanActivation(level, this.position());
                this.playSound(SoundEvents.AMETHYST_BLOCK_CHIME, 0.9F, 1.15F);
                double callRange = getRole() == Role.SECT_ELDER ? 24.0D : 16.0D;
                for (CultivationSettlerEntity ally : level.getEntitiesOfClass(
                        CultivationSettlerEntity.class, this.getBoundingBox().inflate(callRange))) {
                    if (ally != this && ally.isAlive() && ally.combatTarget == null) {
                        ally.combatTarget = attacker;
                        ally.aggroTicks = 320;
                        ally.attackCooldown = 12;
                    }
                }
            }
        }
        return hurt;
    }

    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource damageSource, boolean recentlyHit) {
        super.dropCustomDeathLoot(level, damageSource, recentlyHit);
        this.spawnAtLocation(level, new ItemStack(XuanhuanEarth.SPIRIT_STONE_RAW.get(), 1 + this.random.nextInt(2)));
        this.spawnAtLocation(level, new ItemStack(XuanhuanEarth.SPIRIT_CRYSTAL_SHARD.get(), 1 + this.random.nextInt(2)));
        this.spawnAtLocation(level, new ItemStack(XuanhuanEarth.SPIRIT_GRASS.get(), 1 + this.random.nextInt(3)));
        if (this.random.nextBoolean()) {
            this.spawnAtLocation(level, TalismanComponent.drawn(new ItemStack(XuanhuanEarth.BASIC_TALISMAN.get())));
        } else {
            this.spawnAtLocation(level, new ItemStack(XuanhuanEarth.TALISMAN_PAPER.get(), 1 + this.random.nextInt(2)));
        }
        if (this.random.nextFloat() < (getRole() == Role.SECT_ELDER ? 0.85F : 0.35F)) {
            ItemStack manualDrop = switch (this.random.nextInt(4)) {
                case 0 -> new ItemStack(XuanhuanEarth.QI_GUIDING_MANUAL.get());
                case 1 -> new ItemStack(XuanhuanEarth.BIGU_MANUAL.get());
                case 2 -> new ItemStack(XuanhuanEarth.BODY_TEMPERING_MANUAL.get());
                default -> new ItemStack(XuanhuanEarth.FETAL_BREATH_MANUAL.get());
            };
            this.spawnAtLocation(level, manualDrop);
        }
        if (getRole() == Role.SECT_ELDER) {
            this.spawnAtLocation(level, new ItemStack(XuanhuanEarth.SPIRIT_IRON_BLANK.get(), 2));
            this.spawnAtLocation(level, new ItemStack(XuanhuanEarth.QI_RECOVERY_PILL.get(), 2));
        } else if (getRole() == Role.SECT_STEWARD) {
            this.spawnAtLocation(level, new ItemStack(XuanhuanEarth.SPIRIT_IRON_BLANK.get(), 1));
            if (this.random.nextBoolean()) {
                this.spawnAtLocation(level, new ItemStack(XuanhuanEarth.QI_RECOVERY_PILL.get(), 1));
            }
        } else if (getRole() == Role.SPIRIT_SPRING_KEEPER && this.random.nextBoolean()) {
            this.spawnAtLocation(level, new ItemStack(XuanhuanEarth.QI_RECOVERY_PILL.get(), 1));
        }
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_ROLE, Role.WANDERING_MERCHANT.id());
        builder.define(DATA_COMBAT_POSE, POSE_IDLE);
        builder.define(DATA_ELEMENT, ElementAffinity.METAL.id());
        builder.define(DATA_WEAPON, WeaponStyle.SPIRIT_SWORD.id());
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putInt("CultivationRole", getRole().id());
        output.putInt("ElementAffinity", getElementAffinity().id());
        output.putInt("WeaponStyle", getWeaponStyle().id());
        output.putBoolean("TraitsInitialized", this.initializedTraits);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        setRole(Role.byId(input.getIntOr("CultivationRole", 0)));
        setElementAffinity(ElementAffinity.byId(input.getIntOr("ElementAffinity", 0)));
        setWeaponStyle(WeaponStyle.byId(input.getIntOr("WeaponStyle", 0)));
        this.initializedTraits = input.getBooleanOr("TraitsInitialized", false);
        applyRoleAttributes(false);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new TradeWithPlayerGoal(this));
        this.goalSelector.addGoal(1, new LookAtTradingPlayerGoal(this));
        this.goalSelector.addGoal(2, new AvoidEntityGoal<>(this, Monster.class, 9.0F, 0.7D, 0.8D) {
            @Override
            public boolean canUse() {
                return !isInCombat() && !getRole().proactivelyGuards() && super.canUse();
            }

            @Override
            public boolean canContinueToUse() {
                return !isInCombat() && !getRole().proactivelyGuards() && super.canContinueToUse();
            }
        });
        this.goalSelector.addGoal(7, new WaterAvoidingRandomStrollGoal(this, 0.62D) {
            @Override
            public boolean canUse() {
                return !isInCombat() && super.canUse();
            }

            @Override
            public boolean canContinueToUse() {
                return !isInCombat() && super.canContinueToUse();
            }
        });
        this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 8.0F));
    }

    public boolean isInCombat() {
        return this.aggroTicks > 0 && this.combatTarget != null && this.combatTarget.isAlive();
    }

    public int getCombatPose() {
        return this.entityData.get(DATA_COMBAT_POSE);
    }

    public void setCombatPose(int pose) {
        this.entityData.set(DATA_COMBAT_POSE, pose);
    }

    public ElementAffinity getElementAffinity() {
        return ElementAffinity.byId(this.entityData.get(DATA_ELEMENT));
    }

    public void setElementAffinity(ElementAffinity element) {
        this.entityData.set(DATA_ELEMENT, element.id());
    }

    public WeaponStyle getWeaponStyle() {
        return WeaponStyle.byId(this.entityData.get(DATA_WEAPON));
    }

    public void setWeaponStyle(WeaponStyle weapon) {
        this.entityData.set(DATA_WEAPON, weapon.id());
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (!this.isAlive() || this.isTrading() || this.isBaby()) {
            return super.mobInteract(player, hand);
        }
        if (hand == InteractionHand.MAIN_HAND) {
            player.awardStat(Stats.TALKED_TO_VILLAGER);
        }
        if (!this.level().isClientSide()) {
            if (this.getOffers().isEmpty()) {
                return InteractionResult.CONSUME;
            }
            this.setTradingPlayer(player);
            this.openTradingScreen(player, getTypeName(), 1);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    protected void updateTrades(ServerLevel level) {
        MerchantOffers offers = this.getOffers();
        switch (getRole()) {
            case WANDERING_MERCHANT -> {
                add(offers, Items.EMERALD, 2, new ItemStack(XuanhuanEarth.SPIRIT_GRASS.get(), 3), 12, 2);
                add(offers, Items.EMERALD, 3, new ItemStack(XuanhuanEarth.TALISMAN_PAPER.get(), 2), 10, 3);
                add(offers, Items.AMETHYST_SHARD, 4, new ItemStack(XuanhuanEarth.SPIRIT_CRYSTAL_SHARD.get()), 8, 4);
            }
            case SPIRIT_SPRING_KEEPER -> {
                add(offers, Items.EMERALD, 2, new ItemStack(XuanhuanEarth.SPIRIT_SPRING_BOTTLE.get(), 2), 12, 2);
                add(offers, Items.EMERALD, 4, new ItemStack(XuanhuanEarth.QI_RECOVERY_PILL.get()), 8, 5);
                add(offers, Items.GOLD_INGOT, 3, new ItemStack(XuanhuanEarth.SPIRIT_BEAST_SEAL.get()), 6, 6);
            }
            case SECT_STEWARD -> {
                add(offers, Items.EMERALD, 5, new ItemStack(XuanhuanEarth.BASIC_TALISMAN.get()), 8, 5);
                add(offers, Items.EMERALD, 8, new ItemStack(XuanhuanEarth.QI_GUIDING_MANUAL.get()), 4, 8);
                add(offers, Items.IRON_INGOT, 6, new ItemStack(XuanhuanEarth.SPIRIT_IRON_BLANK.get()), 8, 5);
            }
            case OUTER_DISCIPLE -> {
                add(offers, Items.EMERALD, 2, new ItemStack(XuanhuanEarth.SPIRIT_GRASS.get(), 2), 10, 2);
                add(offers, Items.EMERALD, 4, new ItemStack(XuanhuanEarth.BASIC_TALISMAN.get()), 6, 4);
            }
            case SECT_ELDER -> {
                add(offers, Items.EMERALD, 8, new ItemStack(XuanhuanEarth.QI_RECOVERY_PILL.get(), 2), 8, 6);
                add(offers, Items.EMERALD, 12, new ItemStack(XuanhuanEarth.BODY_TEMPERING_MANUAL.get()), 4, 10);
                add(offers, Items.DIAMOND, 2, new ItemStack(XuanhuanEarth.SPIRIT_IRON_BLANK.get(), 2), 6, 8);
            }
        }
        offers.addAll(LoreApi.teachingOffers(level, getType()));
    }

    private static void add(MerchantOffers offers, net.minecraft.world.level.ItemLike cost, int count,
                            ItemStack result, int maxUses, int xp) {
        offers.add(new MerchantOffer(new ItemCost(cost, count), result, maxUses, xp, 0.05F));
    }

    @Override
    protected void rewardTradeXp(MerchantOffer offer) {
        if (offer.shouldRewardExp()) {
            this.level().addFreshEntity(new ExperienceOrb(this.level(), getX(), getY() + 0.5D, getZ(),
                    3 + this.random.nextInt(4)));
        }
    }

    @Override
    public boolean showProgressBar() {
        return false;
    }

    @Override
    public @Nullable AgeableMob getBreedOffspring(ServerLevel level, AgeableMob partner) {
        CultivationSettlerEntity child = XuanhuanEarth.CULTIVATION_SETTLER.get()
                .create(level, EntitySpawnReason.BREEDING);
        if (child != null) {
            child.setRole(Role.OUTER_DISCIPLE);
        }
        return child;
    }

    @Override
    protected Component getTypeName() {
        return Component.translatable(getRole().translationKey());
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return this.isTrading() ? SoundEvents.VILLAGER_TRADE : SoundEvents.VILLAGER_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.VILLAGER_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.VILLAGER_DEATH;
    }

    public Role getRole() {
        return Role.byId(this.entityData.get(DATA_ROLE));
    }

    public void setRole(Role role) {
        this.entityData.set(DATA_ROLE, role.id());
        this.offers = null;
        if (!this.level().isClientSide()) {
            applyRoleAttributes(true);
        }
    }

    public enum Role {
        WANDERING_MERCHANT(0, "entity.xuanhuan_earth.cultivation_settler.wandering_merchant", 28.0D, 3.0D, 5.0D, 4.5F, 0.30D, 0.05D, 26, 36, false),
        SPIRIT_SPRING_KEEPER(1, "entity.xuanhuan_earth.cultivation_settler.spirit_spring_keeper", 38.0D, 5.0D, 5.5D, 5.5F, 0.29D, 0.15D, 24, 32, true),
        SECT_STEWARD(2, "entity.xuanhuan_earth.cultivation_settler.sect_steward", 52.0D, 7.0D, 7.5D, 6.5F, 0.31D, 0.25D, 22, 30, true),
        OUTER_DISCIPLE(3, "entity.xuanhuan_earth.cultivation_settler.outer_disciple", 32.0D, 4.0D, 6.0D, 4.5F, 0.33D, 0.10D, 20, 34, true),
        SECT_ELDER(4, "entity.xuanhuan_earth.cultivation_settler.sect_elder", 96.0D, 12.0D, 11.0D, 9.0F, 0.33D, 0.55D, 18, 24, true);

        private final int id;
        private final String translationKey;
        private final double maxHealth;
        private final double armor;
        private final double attackDamage;
        private final float spellDamage;
        private final double moveSpeed;
        private final double knockbackResistance;
        private final int baseAttackCooldown;
        private final int baseCastCooldown;
        private final boolean proactivelyGuards;

        Role(int id, String translationKey, double maxHealth, double armor, double attackDamage, float spellDamage,
             double moveSpeed, double knockbackResistance, int baseAttackCooldown, int baseCastCooldown, boolean proactivelyGuards) {
            this.id = id;
            this.translationKey = translationKey;
            this.maxHealth = maxHealth;
            this.armor = armor;
            this.attackDamage = attackDamage;
            this.spellDamage = spellDamage;
            this.moveSpeed = moveSpeed;
            this.knockbackResistance = knockbackResistance;
            this.baseAttackCooldown = baseAttackCooldown;
            this.baseCastCooldown = baseCastCooldown;
            this.proactivelyGuards = proactivelyGuards;
        }

        public int id() { return id; }
        public String translationKey() { return translationKey; }
        public double maxHealth() { return maxHealth; }
        public double armor() { return armor; }
        public double attackDamage() { return attackDamage; }
        public float spellDamage() { return spellDamage; }
        public double moveSpeed() { return moveSpeed; }
        public double knockbackResistance() { return knockbackResistance; }
        public int baseAttackCooldown() { return baseAttackCooldown; }
        public int baseCastCooldown() { return baseCastCooldown; }
        public boolean proactivelyGuards() { return proactivelyGuards; }

        public static Role byId(int id) {
            for (Role role : values()) {
                if (role.id == id) return role;
            }
            return WANDERING_MERCHANT;
        }
    }

    public enum ElementAffinity {
        METAL(0, 245, 212, 112),
        WOOD(1, 118, 232, 152),
        WATER(2, 112, 224, 255),
        FIRE(3, 255, 132, 72),
        THUNDER(4, 198, 138, 255);

        private final int id;
        private final int r;
        private final int g;
        private final int b;

        ElementAffinity(int id, int r, int g, int b) {
            this.id = id;
            this.r = r;
            this.g = g;
            this.b = b;
        }

        public int id() { return id; }
        public int r() { return r; }
        public int g() { return g; }
        public int b() { return b; }

        public ParticleOptions particle() {
            return switch (this) {
                case METAL -> ParticleTypes.END_ROD;
                case WOOD -> ParticleTypes.HAPPY_VILLAGER;
                case WATER -> ParticleTypes.SNOWFLAKE;
                case FIRE -> ParticleTypes.FLAME;
                case THUNDER -> ParticleTypes.ELECTRIC_SPARK;
            };
        }

        public ParticleOptions secondaryParticle() {
            return switch (this) {
                case METAL -> XuanhuanParticles.QI_SPARK.get();
                case WOOD -> XuanhuanParticles.SPIRIT_LEAF.get();
                case WATER -> ParticleTypes.SOUL_FIRE_FLAME;
                case FIRE -> XuanhuanParticles.EMBER_SPARK.get();
                case THUNDER -> ParticleTypes.WITCH;
            };
        }

        public SoundEvent castSound() {
            return switch (this) {
                case METAL -> SoundEvents.AMETHYST_BLOCK_CHIME;
                case WOOD -> SoundEvents.AMETHYST_BLOCK_RESONATE;
                case WATER -> SoundEvents.GLASS_BREAK;
                case FIRE -> SoundEvents.FIRECHARGE_USE;
                case THUNDER -> SoundEvents.TRIDENT_THUNDER.value();
            };
        }

        public void applyOnHitEffect(ServerLevel level, CultivationSettlerEntity caster, LivingEntity target, int amp) {
            switch (this) {
                case METAL -> target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 60 + amp * 40, amp));
                case WOOD -> {
                    target.addEffect(new MobEffectInstance(MobEffects.POISON, 65 + amp * 35, amp));
                    target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 50 + amp * 30, 0));
                    caster.heal(1.2F + amp * 1.0F);
                }
                case WATER -> target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 80 + amp * 40, 1 + amp));
                case FIRE -> target.igniteForSeconds(2.5F + amp * 2.0F);
                case THUNDER -> {
                    target.addEffect(new MobEffectInstance(MobEffects.GLOWING, 80, 0));
                    target.hurtServer(level, level.damageSources().magic(), 1.8F + amp * 1.5F);
                }
            }
        }

        public static ElementAffinity randomForRole(Role role, net.minecraft.util.RandomSource random) {
            if (role == Role.SPIRIT_SPRING_KEEPER) {
                return random.nextBoolean() ? WATER : WOOD;
            }
            ElementAffinity[] all = values();
            return all[random.nextInt(all.length)];
        }

        public static ElementAffinity byId(int id) {
            for (ElementAffinity e : values()) {
                if (e.id == id) return e;
            }
            return METAL;
        }
    }

    public enum WeaponStyle {
        SPIRIT_SWORD(0, 3.4D, 1.05F, 1.00F, 2, 0),
        JADE_SPEAR(1, 4.3D, 1.15F, 0.90F, 0, 0),
        DAOIST_STAFF(2, 3.0D, 0.85F, 1.25F, 0, 6),
        IRON_FAN(3, 3.5D, 0.95F, 1.12F, 1, 3);

        private final int id;
        private final double meleeReach;
        private final float meleeDamageMul;
        private final float spellDamageMul;
        private final int meleeHasteBonus;
        private final int castHasteBonus;

        WeaponStyle(int id, double meleeReach, float meleeDamageMul, float spellDamageMul, int meleeHasteBonus, int castHasteBonus) {
            this.id = id;
            this.meleeReach = meleeReach;
            this.meleeDamageMul = meleeDamageMul;
            this.spellDamageMul = spellDamageMul;
            this.meleeHasteBonus = meleeHasteBonus;
            this.castHasteBonus = castHasteBonus;
        }

        public int id() { return id; }
        public double meleeReach() { return meleeReach; }
        public float meleeDamageMul() { return meleeDamageMul; }
        public float spellDamageMul() { return spellDamageMul; }
        public int meleeHasteBonus() { return meleeHasteBonus; }
        public int castHasteBonus() { return castHasteBonus; }

        public static WeaponStyle randomForRole(Role role, net.minecraft.util.RandomSource random) {
            return switch (role) {
                case WANDERING_MERCHANT -> random.nextBoolean() ? DAOIST_STAFF : IRON_FAN;
                case SPIRIT_SPRING_KEEPER -> random.nextBoolean() ? DAOIST_STAFF : SPIRIT_SWORD;
                case OUTER_DISCIPLE -> random.nextBoolean() ? SPIRIT_SWORD : JADE_SPEAR;
                case SECT_STEWARD, SECT_ELDER -> values()[random.nextInt(values().length)];
            };
        }

        public static WeaponStyle byId(int id) {
            for (WeaponStyle w : values()) {
                if (w.id == id) return w;
            }
            return SPIRIT_SWORD;
        }
    }
}
