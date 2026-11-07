package com.xxsx.xuanhuanearth.entity;

import com.xxsx.earthlore.api.LoreApi;
import com.xxsx.xuanhuanearth.TalismanComponent;
import com.xxsx.xuanhuanearth.XuanhuanEarth;
import com.xxsx.xuanhuanearth.XuanhuanEffects;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.stats.Stats;
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
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public final class CultivationSettlerEntity extends AbstractVillager {
    private static final EntityDataAccessor<Integer> DATA_ROLE = SynchedEntityData.defineId(
            CultivationSettlerEntity.class, EntityDataSerializers.INT);
    @Nullable
    private LivingEntity combatTarget;
    private int aggroTicks;
    private int attackCooldown;

    public CultivationSettlerEntity(EntityType<? extends CultivationSettlerEntity> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 30.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.42D)
                .add(Attributes.FOLLOW_RANGE, 24.0D)
                .add(Attributes.ATTACK_DAMAGE, 6.0D);
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.attackCooldown > 0) {
            this.attackCooldown--;
        }
        if (this.aggroTicks > 0) {
            this.aggroTicks--;
        }
        if (this.level() instanceof ServerLevel serverLevel) {
            if (this.combatTarget != null && this.combatTarget.isAlive() && this.aggroTicks > 0
                    && this.distanceToSqr(this.combatTarget) < 576.0D) {
                this.getLookControl().setLookAt(this.combatTarget, 30.0F, 30.0F);
                double distSqr = this.distanceToSqr(this.combatTarget);
                if (distSqr > 12.25D) {
                    this.getNavigation().moveTo(this.combatTarget, 0.78D);
                } else {
                    this.getNavigation().stop();
                }
                if (this.attackCooldown <= 0) {
                    if (distSqr <= 10.24D) {
                        this.swing(InteractionHand.MAIN_HAND);
                        float meleeDamage = 5.0F + getRole().id() * 1.5F;
                        this.combatTarget.hurtServer(serverLevel, serverLevel.damageSources().mobAttack(this), meleeDamage);
                        serverLevel.sendParticles(ParticleTypes.SWEEP_ATTACK,
                                this.combatTarget.getX(), this.combatTarget.getY(0.5D), this.combatTarget.getZ(),
                                1, 0.0D, 0.0D, 0.0D, 0.0D);
                        serverLevel.sendParticles(ParticleTypes.CRIT,
                                this.combatTarget.getX(), this.combatTarget.getY(0.5D), this.combatTarget.getZ(),
                                8, 0.25D, 0.25D, 0.25D, 0.08D);
                        this.playSound(SoundEvents.PLAYER_ATTACK_SWEEP, 0.9F, 1.1F);
                        this.attackCooldown = 24;
                    } else if (distSqr <= 256.0D) {
                        this.swing(InteractionHand.MAIN_HAND);
                        Vec3 start = this.position().add(0.0D, this.getBbHeight() * 0.65D, 0.0D);
                        Vec3 end = this.combatTarget.position().add(0.0D, this.combatTarget.getBbHeight() * 0.55D, 0.0D);
                        Vec3 dir = end.subtract(start);
                        double len = dir.length();
                        if (len > 0.1D) {
                            Vec3 step = dir.normalize().scale(0.6D);
                            int steps = Math.min(32, (int) (len / 0.6D));
                            for (int i = 1; i <= steps; i++) {
                                Vec3 p = start.add(step.scale(i));
                                serverLevel.sendParticles(ParticleTypes.END_ROD, p.x, p.y, p.z, 1, 0.02D, 0.02D, 0.02D, 0.01D);
                                serverLevel.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, p.x, p.y, p.z, 1, 0.02D, 0.02D, 0.02D, 0.01D);
                            }
                        }
                        if (!this.combatTarget.isBlocking()) {
                            float qiDamage = 4.0F + getRole().id();
                            this.combatTarget.hurtServer(serverLevel, serverLevel.damageSources().indirectMagic(this, this), qiDamage);
                        }
                        this.playSound(SoundEvents.AMETHYST_BLOCK_CHIME, 1.0F, 1.3F);
                        this.attackCooldown = 36;
                    }
                }
            } else if (this.aggroTicks <= 0 || (this.combatTarget != null && !this.combatTarget.isAlive())) {
                this.combatTarget = null;
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
                this.aggroTicks = 400;
                if (this.attackCooldown <= 0) {
                    this.attackCooldown = 10;
                }
                this.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 80, 0, true, false, true));
                this.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 100, 0, true, false, true));
                XuanhuanEffects.talismanActivation(level, this.position());
                this.playSound(SoundEvents.AMETHYST_BLOCK_CHIME, 0.9F, 1.15F);
                for (CultivationSettlerEntity ally : level.getEntitiesOfClass(
                        CultivationSettlerEntity.class, this.getBoundingBox().inflate(16.0D))) {
                    if (ally != this && ally.isAlive() && ally.combatTarget == null) {
                        ally.combatTarget = attacker;
                        ally.aggroTicks = 300;
                        ally.attackCooldown = 14;
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
        this.spawnAtLocation(level, new ItemStack(XuanhuanEarth.SPIRIT_GRASS.get(), 1 + this.random.nextInt(3)));
        if (this.random.nextBoolean()) {
            this.spawnAtLocation(level, TalismanComponent.drawn(new ItemStack(XuanhuanEarth.BASIC_TALISMAN.get())));
        } else {
            this.spawnAtLocation(level, new ItemStack(XuanhuanEarth.TALISMAN_PAPER.get(), 1 + this.random.nextInt(2)));
        }
        if (getRole() == Role.SECT_STEWARD) {
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
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putInt("CultivationRole", getRole().id());
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        setRole(Role.byId(input.getIntOr("CultivationRole", 0)));
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new TradeWithPlayerGoal(this));
        this.goalSelector.addGoal(1, new LookAtTradingPlayerGoal(this));
        this.goalSelector.addGoal(2, new AvoidEntityGoal<>(this, Monster.class, 9.0F, 0.7D, 0.8D));
        this.goalSelector.addGoal(7, new WaterAvoidingRandomStrollGoal(this, 0.62D));
        this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 8.0F));
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
            this.openTradingScreen(player, Component.translatable(getRole().translationKey()), 1);
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
            child.setRole(this.random.nextBoolean() ? getRole()
                    : partner instanceof CultivationSettlerEntity settler ? settler.getRole() : getRole());
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
    }

    public enum Role {
        WANDERING_MERCHANT(0, "entity.xuanhuan_earth.cultivation_settler.wandering_merchant"),
        SPIRIT_SPRING_KEEPER(1, "entity.xuanhuan_earth.cultivation_settler.spirit_spring_keeper"),
        SECT_STEWARD(2, "entity.xuanhuan_earth.cultivation_settler.sect_steward");

        private final int id;
        private final String translationKey;

        Role(int id, String translationKey) {
            this.id = id;
            this.translationKey = translationKey;
        }

        public int id() {
            return id;
        }

        public String translationKey() {
            return translationKey;
        }

        public static Role byId(int id) {
            for (Role role : values()) {
                if (role.id == id) {
                    return role;
                }
            }
            return WANDERING_MERCHANT;
        }
    }
}
