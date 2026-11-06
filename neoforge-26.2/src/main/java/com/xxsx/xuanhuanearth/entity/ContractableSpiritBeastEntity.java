package com.xxsx.xuanhuanearth.entity;

import com.xxsx.xuanhuanearth.XuanhuanEarth;
import com.xxsx.xuanhuanearth.XuanhuanJourney;
import net.minecraft.core.BlockPos;
import com.xxsx.xuanhuanearth.XuanhuanEffects;
import com.xxsx.xuanhuanearth.particle.XuanhuanParticles;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.BreedGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.FollowOwnerGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.SitWhenOrderedToGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtTargetGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

public abstract class ContractableSpiritBeastEntity extends TamableAnimal {
    public static final int MAX_AFFINITY = 100;
    public static final int CONTRACT_AFFINITY = 30;
    public static final int FEED_AFFINITY_STEP = 10;
    private static final EntityDataAccessor<Integer> DATA_AFFINITY = SynchedEntityData.defineId(
            ContractableSpiritBeastEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Byte> DATA_MODE = SynchedEntityData.defineId(
            ContractableSpiritBeastEntity.class, EntityDataSerializers.BYTE);
    /** Beast cultivation rank, 1-5. Separate from affinity: trust versus training. */
    public static final int MAX_RANK = 5;
    private static final int[] RANK_XP = {0, 20, 55, 110, 190, 300};
    private static final EntityDataAccessor<Integer> DATA_RANK = SynchedEntityData.defineId(
            ContractableSpiritBeastEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_RANK_XP = SynchedEntityData.defineId(
            ContractableSpiritBeastEntity.class, EntityDataSerializers.INT);

    private final Kind kind;

    /**
     * Unmodified attribute values, captured after construction. Rank bonuses are applied as
     * base + rank * step, so they must start from the template rather than accumulate.
     */
    private double baseHealth;
    private double baseAttack;
    private double baseArmor;

    protected ContractableSpiritBeastEntity(EntityType<? extends ContractableSpiritBeastEntity> type,
                                             Level level, Kind kind) {
        super(type, level);
        this.kind = kind;
        this.setTame(false, false);
        this.captureBaseAttributes();
    }

    private void captureBaseAttributes() {
        AttributeInstance health = getAttribute(Attributes.MAX_HEALTH);
        AttributeInstance attack = getAttribute(Attributes.ATTACK_DAMAGE);
        AttributeInstance armor = getAttribute(Attributes.ARMOR);
        this.baseHealth = health == null ? 20.0D : health.getBaseValue();
        this.baseAttack = attack == null ? 2.0D : attack.getBaseValue();
        this.baseArmor = armor == null ? 0.0D : armor.getBaseValue();
    }

    public static AttributeSupplier.Builder createAttributes(double health, double speed, double attack,
                                                              double armor, double knockbackResistance) {
        return Animal.createAnimalAttributes()
                .add(Attributes.MAX_HEALTH, health)
                .add(Attributes.MOVEMENT_SPEED, speed)
                .add(Attributes.ATTACK_DAMAGE, attack)
                .add(Attributes.ARMOR, armor)
                .add(Attributes.KNOCKBACK_RESISTANCE, knockbackResistance)
                .add(Attributes.FOLLOW_RANGE, 30.0D)
                .add(Attributes.SAFE_FALL_DISTANCE, 8.0D);
    }

    public static boolean checkSpawnRules(EntityType<? extends ContractableSpiritBeastEntity> type,
                                          LevelAccessor level, EntitySpawnReason reason,
                                          BlockPos pos, RandomSource random) {
        return Animal.checkAnimalSpawnRules(type, level, reason, pos, random);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_AFFINITY, 0);
        builder.define(DATA_MODE, (byte) CompanionMode.FOLLOW.id());
        builder.define(DATA_RANK, 1);
        builder.define(DATA_RANK_XP, 0);
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putInt("SpiritAffinity", getAffinity());
        output.putInt("CompanionMode", getCompanionMode().id());
        output.putInt("BeastRank", getRank());
        output.putInt("BeastRankXp", getRankXp());
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        setAffinity(input.getIntOr("SpiritAffinity", 0));
        setCompanionMode(CompanionMode.byId(input.getIntOr("CompanionMode", 0)));
        setRank(input.getIntOr("BeastRank", 1));
        setRankXp(input.getIntOr("BeastRankXp", 0));
        applyRankAttributes();
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new SitWhenOrderedToGoal(this));
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.08D, true));
        this.goalSelector.addGoal(3, new ModeAwareFollowOwnerGoal(this, 1.15D, 9.0F, 2.2F));
        this.goalSelector.addGoal(4, new BreedGoal(this, 1.0D));
        this.goalSelector.addGoal(5, new TemptGoal(this, 1.12D,
                this::isTameFood, false));
        this.goalSelector.addGoal(7, new WaterAvoidingRandomStrollGoal(this, 0.92D));
        this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 9.0F));
        this.goalSelector.addGoal(9, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new OwnerHurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new OwnerHurtTargetGoal(this));
        this.targetSelector.addGoal(3, new HurtByTargetGoal(this));
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!this.isTame()) {
            if (isTameFood(stack)) {
                if (!this.level().isClientSide()) {
                    stack.consume(1, player);
                    setAffinity(Math.min(CONTRACT_AFFINITY, getAffinity() + FEED_AFFINITY_STEP));
                    if (this.level() instanceof ServerLevel serverLevel) {
                        XuanhuanEffects.qiRise(serverLevel,
                                this.position().add(0.0D, this.getBbHeight() * 0.5D, 0.0D),
                                0.55D, 10, 0.025D);
                        serverLevel.sendParticles(XuanhuanParticles.QI_SPARK.get(),
                                getX(), getY() + getBbHeight() * 0.75D, getZ(),
                                5, 0.3D, 0.25D, 0.3D, 0.02D);
                    }
                    player.sendSystemMessage(Component.translatable(
                            "message.xuanhuan_earth.beast.affinity", getAffinity(), CONTRACT_AFFINITY));
                }
                return this.level().isClientSide() ? InteractionResult.SUCCESS : InteractionResult.SUCCESS_SERVER;
            }
            if (stack.is(XuanhuanEarth.SPIRIT_BEAST_SEAL.get())) {
                if (!this.level().isClientSide()) {
                    if (getAffinity() < CONTRACT_AFFINITY) {
                        player.sendSystemMessage(Component.translatable(
                                "message.xuanhuan_earth.beast.affinity_required",
                                getAffinity(), CONTRACT_AFFINITY));
                        return InteractionResult.FAIL;
                    } else if (!net.neoforged.neoforge.event.EventHooks.onAnimalTame(this, player)) {
                        stack.consume(1, player);
                        tame(player);
                        setCompanionMode(CompanionMode.FOLLOW);
                        setHealth(getMaxHealth());
                        this.navigation.stop();
                        this.setTarget(null);
                        this.level().broadcastEntityEvent(this, (byte) 7);
                        player.sendSystemMessage(Component.translatable(
                                "message.xuanhuan_earth.beast.contracted", getDisplayName()));
                        if (player instanceof ServerPlayer serverPlayer) {
                            XuanhuanJourney.complete(serverPlayer, XuanhuanJourney.Milestone.BEAST_CONTRACT);
                        }
                    }
                }
                return this.level().isClientSide() ? InteractionResult.SUCCESS : InteractionResult.SUCCESS_SERVER;
            }
        } else if (this.isOwnedBy(player)) {
            // Feeding a crystal trains the beast. Grass only heals; crystals raise its rank,
            // which is what actually makes it stronger.
            if (stack.is(XuanhuanEarth.SPIRIT_CRYSTAL_SHARD.get()) && getRank() < MAX_RANK) {
                if (!this.level().isClientSide()) {
                    stack.consume(1, player);
                    boolean promoted = addRankExperience(12);
                    if (this.level() instanceof ServerLevel serverLevel) {
                        XuanhuanEffects.qiRise(serverLevel,
                                this.position().add(0.0D, this.getBbHeight() * 0.5D, 0.0D),
                                0.7D, 12, 0.03D);
                    }
                    if (promoted) {
                        player.sendSystemMessage(Component.translatable(
                                "message.xuanhuan_earth.beast.rank_up",
                                getDisplayName(), rankName(), getRank()));
                    } else {
                        player.sendSystemMessage(Component.translatable(
                                "message.xuanhuan_earth.beast.rank_progress",
                                getDisplayName(), rankName(),
                                getRankXp(), getRankXpNeeded()));
                    }
                }
                return this.level().isClientSide() ? InteractionResult.SUCCESS : InteractionResult.SUCCESS_SERVER;
            }
            if (isTameFood(stack) && (getHealth() < getMaxHealth() || getAffinity() < MAX_AFFINITY)) {
                if (!this.level().isClientSide()) {
                    stack.consume(1, player);
                    setAffinity(Math.min(MAX_AFFINITY, getAffinity() + FEED_AFFINITY_STEP));
                    heal(5.0F);
                }
                return this.level().isClientSide() ? InteractionResult.SUCCESS : InteractionResult.SUCCESS_SERVER;
            }
            // Empty hand is checked directly; AIR is not a held item and must not be treated as one.
            if (stack.isEmpty()) {
                if (!this.level().isClientSide()) {
                    setCompanionMode(getCompanionMode().next());
                    player.sendSystemMessage(Component.translatable(
                            "message.xuanhuan_earth.beast.mode",
                            Component.translatable(getCompanionMode().translationKey())));
                }
                return this.level().isClientSide() ? InteractionResult.SUCCESS : InteractionResult.SUCCESS_SERVER;
            }
        }
        return super.mobInteract(player, hand);
    }

    @Override
    public boolean isFood(ItemStack stack) {
        if (stack.is(XuanhuanEarth.SPIRIT_GRASS.get())) {
            return true;
        }
        return switch (kind) {
            case SPIRIT_FOX -> stack.is(Items.GLOW_BERRIES);
            case WIND_WOLF -> stack.is(Items.ROTTEN_FLESH);
            case STONEHORN_GOAT -> stack.is(Items.HAY_BLOCK);
            case CRYSTAL_TURTLE -> stack.is(Items.PRISMARINE_SHARD);
            case EMBER_CRANE -> stack.is(Items.BLAZE_POWDER);
        };
    }

    public boolean isTameFood(ItemStack stack) {
        if (stack.is(XuanhuanEarth.SPIRIT_GRASS.get()) || stack.is(XuanhuanEarth.SPIRIT_HERB.get())) {
            return true;
        }
        return switch (kind) {
            case SPIRIT_FOX -> stack.is(Items.SWEET_BERRIES);
            case WIND_WOLF -> stack.is(Items.BONE);
            case STONEHORN_GOAT -> stack.is(Items.WHEAT);
            case CRYSTAL_TURTLE -> stack.is(Items.SEAGRASS);
            case EMBER_CRANE -> stack.is(Items.COOKED_CHICKEN);
        };
    }

    @Override
    public @Nullable ContractableSpiritBeastEntity getBreedOffspring(ServerLevel level, AgeableMob partner) {
        var child = this.getType().create(level, EntitySpawnReason.BREEDING);
        if (child instanceof ContractableSpiritBeastEntity beast) {
            if (this.isTame()) {
                beast.setOwnerReference(this.getOwnerReference());
                beast.setTame(true, true);
                beast.setCompanionMode(CompanionMode.FOLLOW);
            }
            return beast;
        }
        return null;
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (!this.level().isClientSide()) {
            return;
        }
        // Client-side ambient aura: cheap enough to run every few ticks, and it is what
        // makes a tamed beast read as "carrying qi" without any server packet traffic.
        if (this.tickCount % kind.particleInterval() == 0 && this.random.nextFloat() < 0.8F) {
            this.level().addParticle(kind.particle(), this.getRandomX(0.7D),
                    this.getRandomY() + 0.08D, this.getRandomZ(0.7D), 0.0D, 0.018D, 0.0D);
        }
        if (this.isTame() && this.tickCount % 26 == 0) {
            XuanhuanEffects.beastAura(this.level(), this.position().add(0.0D, this.getBbHeight() * 0.6D, 0.0D),
                    kind);
        }
        if (this.hurtTime == this.hurtDuration && this.hurtDuration > 0) {
            for (int i = 0; i < 6; i++) {
                this.level().addParticle(XuanhuanParticles.BLOOD_MOTE.get(),
                        this.getRandomX(0.5D), this.getRandomY(), this.getRandomZ(0.5D),
                        (this.random.nextDouble() - 0.5D) * 0.05D, 0.02D,
                        (this.random.nextDouble() - 0.5D) * 0.05D);
            }
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.level().isClientSide() && this.tickCount % 100 == 0 && this.isTame()
                && getCompanionMode() != CompanionMode.ROAM
                && this.getOwner() instanceof ServerPlayer owner && this.distanceToSqr(owner) <= 196.0D) {
            applyOwnerSupport(owner);
        }
    }

    @Override
    public boolean doHurtTarget(net.minecraft.server.level.ServerLevel level,
                                net.minecraft.world.entity.Entity target) {
        boolean hit = super.doHurtTarget(level, target);
        // Fighting is the other half of training, so a beast that hunts with its owner
        // progresses without needing to be hand-fed everything.
        if (hit && this.isTame() && getRank() < MAX_RANK && this.random.nextFloat() < 0.35F) {
            boolean promoted = addRankExperience(3);
            if (promoted && this.getOwner() instanceof ServerPlayer owner) {
                owner.sendSystemMessage(Component.translatable(
                        "message.xuanhuan_earth.beast.rank_up",
                        getDisplayName(), rankName(), getRank()));
            }
        }
        return hit;
    }

    private void applyOwnerSupport(ServerPlayer owner) {
        switch (kind) {
            case SPIRIT_FOX -> owner.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 140, 0, true, false, true));
            case WIND_WOLF -> owner.addEffect(new MobEffectInstance(MobEffects.SPEED, 140, 0, true, false, true));
            case STONEHORN_GOAT -> owner.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 140, 0, true, false, true));
            case CRYSTAL_TURTLE -> owner.addEffect(new MobEffectInstance(MobEffects.WATER_BREATHING, 140, 0, true, false, true));
            case EMBER_CRANE -> owner.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 140, 0, true, false, true));
        }
    }

    public Kind kind() {
        return kind;
    }

    public int getRank() {
        return this.entityData.get(DATA_RANK);
    }

    public void setRank(int rank) {
        this.entityData.set(DATA_RANK, Math.max(1, Math.min(MAX_RANK, rank)));
    }

    public int getRankXp() {
        return this.entityData.get(DATA_RANK_XP);
    }

    public void setRankXp(int xp) {
        this.entityData.set(DATA_RANK_XP, Math.max(0, xp));
    }

    public int getRankXpNeeded() {
        int rank = getRank();
        return rank >= MAX_RANK ? 0 : RANK_XP[rank];
    }

    /**
     * Feeds growth experience to a contracted beast. Rank drives health, attack and armor,
     * so a beast that is kept and used actually becomes stronger.
     */
    public boolean addRankExperience(int amount) {
        if (amount <= 0 || getRank() >= MAX_RANK) {
            return false;
        }
        int previous = getRank();
        int xp = getRankXp() + amount;
        int rank = previous;
        while (rank < MAX_RANK && xp >= RANK_XP[rank]) {
            xp -= RANK_XP[rank];
            rank++;
        }
        setRank(rank);
        setRankXp(rank >= MAX_RANK ? 0 : xp);
        applyRankAttributes();
        if (rank > previous) {
            setHealth(getMaxHealth());
        }
        return rank > previous;
    }

    /** Applies the stat bonus for the current rank. Idempotent: recomputed from rank. */
    public void applyRankAttributes() {
        AttributeInstance health = getAttribute(Attributes.MAX_HEALTH);
        AttributeInstance attack = getAttribute(Attributes.ATTACK_DAMAGE);
        AttributeInstance armor = getAttribute(Attributes.ARMOR);
        if (baseHealth <= 0.0D) {
            captureBaseAttributes();
        }
        double step = getRank() - 1;
        if (health != null) {
            health.setBaseValue(baseHealth + step * 3.0D);
        }
        if (attack != null) {
            attack.setBaseValue(baseAttack + step * 0.8D);
        }
        if (armor != null) {
            armor.setBaseValue(baseArmor + step * 1.2D);
        }
    }

    public Component rankName() {
        return Component.translatable("beast_rank.xuanhuan_earth." + getRank());
    }

    public int getAffinity() {
        return this.entityData.get(DATA_AFFINITY);
    }

    public void setAffinity(int affinity) {
        this.entityData.set(DATA_AFFINITY, Math.max(0, Math.min(MAX_AFFINITY, affinity)));
    }

    public CompanionMode getCompanionMode() {
        return CompanionMode.byId(this.entityData.get(DATA_MODE));
    }

    @Override
    public Component getName() {
        // Rank is part of the beast's identity: the name plate shows it so the owner can see
        // training progress at a glance.
        Component base = super.getName();
        if (!this.isTame()) {
            return base;
        }
        return Component.empty()
                .append(base)
                .append(Component.literal(" "))
                .append(rankName());
    }

    public void setCompanionMode(CompanionMode mode) {
        this.entityData.set(DATA_MODE, (byte) mode.id());
        this.setOrderedToSit(mode == CompanionMode.GUARD);
        this.setInSittingPose(mode == CompanionMode.GUARD);
        if (mode != CompanionMode.FOLLOW) {
            this.navigation.stop();
        }
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return switch (kind) {
            case SPIRIT_FOX -> SoundEvents.FOX_AMBIENT;
            case WIND_WOLF -> SoundEvents.BREEZE_IDLE_GROUND;
            case STONEHORN_GOAT -> SoundEvents.GOAT_AMBIENT;
            case CRYSTAL_TURTLE -> SoundEvents.TURTLE_AMBIENT_LAND;
            case EMBER_CRANE -> SoundEvents.PARROT_AMBIENT;
        };
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return switch (kind) {
            case SPIRIT_FOX -> SoundEvents.FOX_HURT;
            case WIND_WOLF -> SoundEvents.BREEZE_HURT;
            case STONEHORN_GOAT -> SoundEvents.GOAT_HURT;
            case CRYSTAL_TURTLE -> SoundEvents.TURTLE_HURT;
            case EMBER_CRANE -> SoundEvents.PARROT_HURT;
        };
    }

    @Override
    protected SoundEvent getDeathSound() {
        return switch (kind) {
            case SPIRIT_FOX -> SoundEvents.FOX_DEATH;
            case WIND_WOLF -> SoundEvents.BREEZE_DEATH;
            case STONEHORN_GOAT -> SoundEvents.GOAT_DEATH;
            case CRYSTAL_TURTLE -> SoundEvents.TURTLE_DEATH;
            case EMBER_CRANE -> SoundEvents.PARROT_DEATH;
        };
    }

    public enum Kind {
        SPIRIT_FOX(XuanhuanParticles.QI_MOTE.get(), 16),
        WIND_WOLF(XuanhuanParticles.QI_SPARK.get(), 13),
        STONEHORN_GOAT(XuanhuanParticles.EMBER_SPARK.get(), 18),
        CRYSTAL_TURTLE(XuanhuanParticles.RUNE_GLYPH_B.get(), 15),
        EMBER_CRANE(XuanhuanParticles.EMBER_LEAF.get(), 12);

        private final net.minecraft.core.particles.ParticleOptions particle;
        private final int particleInterval;

        Kind(net.minecraft.core.particles.ParticleOptions particle, int particleInterval) {
            this.particle = particle;
            this.particleInterval = particleInterval;
        }

        public net.minecraft.core.particles.ParticleOptions particle() {
            return particle;
        }

        public int particleInterval() {
            return particleInterval;
        }
    }

    public enum CompanionMode {
        FOLLOW(0, "message.xuanhuan_earth.beast.mode.follow"),
        GUARD(1, "message.xuanhuan_earth.beast.mode.guard"),
        ROAM(2, "message.xuanhuan_earth.beast.mode.roam");

        private final int id;
        private final String translationKey;

        CompanionMode(int id, String translationKey) {
            this.id = id;
            this.translationKey = translationKey;
        }

        public int id() {
            return id;
        }

        public String translationKey() {
            return translationKey;
        }

        public CompanionMode next() {
            return values()[(ordinal() + 1) % values().length];
        }

        public static CompanionMode byId(int id) {
            for (CompanionMode mode : values()) {
                if (mode.id == id) {
                    return mode;
                }
            }
            return FOLLOW;
        }
    }

    private static final class ModeAwareFollowOwnerGoal extends FollowOwnerGoal {
        private final ContractableSpiritBeastEntity beast;

        private ModeAwareFollowOwnerGoal(ContractableSpiritBeastEntity beast, double speed,
                                         float startDistance, float stopDistance) {
            super(beast, speed, startDistance, stopDistance);
            this.beast = beast;
        }

        @Override
        public boolean canUse() {
            return beast.getCompanionMode() == CompanionMode.FOLLOW && super.canUse();
        }

        @Override
        public boolean canContinueToUse() {
            return beast.getCompanionMode() == CompanionMode.FOLLOW && super.canContinueToUse();
        }
    }
}
