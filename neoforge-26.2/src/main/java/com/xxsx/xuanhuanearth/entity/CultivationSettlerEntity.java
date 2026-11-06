package com.xxsx.xuanhuanearth.entity;

import com.xxsx.earthlore.api.LoreApi;
import com.xxsx.xuanhuanearth.XuanhuanEarth;
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
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.LookAtTradingPlayerGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
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
import org.jspecify.annotations.Nullable;

public final class CultivationSettlerEntity extends AbstractVillager {
    private static final EntityDataAccessor<Integer> DATA_ROLE = SynchedEntityData.defineId(
            CultivationSettlerEntity.class, EntityDataSerializers.INT);

    public CultivationSettlerEntity(EntityType<? extends CultivationSettlerEntity> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 24.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.42D)
                .add(Attributes.FOLLOW_RANGE, 24.0D);
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
        this.goalSelector.addGoal(3, new PanicGoal(this, 0.8D));
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
