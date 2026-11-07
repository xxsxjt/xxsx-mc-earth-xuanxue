package com.xxsx.xuanhuanearth;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * A spirit iron implement: a held focus that converts stored charge into a world effect.
 *
 * <p>Unlike a talisman, an implement is reusable. It holds a charge pool that refills from
 * the local qi field while held, and each cast spends part of that pool. This gives the
 * mid-game a tool that rewards standing in a rich field instead of carrying consumables.</p>
 */
public class SpiritImplementItem extends GuidedXuanhuanItem {
    private final Implement kind;

    public SpiritImplementItem(Properties properties, String hintKey, Implement kind) {
        super(properties, hintKey);
        this.kind = kind;
    }

    public Implement implement() {
        return kind;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        ItemStack stack = player.getItemInHand(hand);

        // Shift: draw charge from the field. Plain use: spend it.
        if (player.isShiftKeyDown()) {
            return charge(level, player, hand, stack);
        }
        return cast(level, player, hand, stack);
    }

    private InteractionResult charge(Level level, Player player, InteractionHand hand, ItemStack stack) {
        ImplementComponent state = ImplementComponent.read(stack);
        ArcanaChunkField.Reading reading = Spirituality.reading(level, player.blockPosition());
        double usable = SpiritFieldLedger.availableField(reading.value(), reading.depletion());
        double coupling = usable / SpiritFieldLedger.FIELD_MAX;
        double missing = kind.capacity() - state.charge();
        if (missing <= 0.0001D) {
            player.sendSystemMessage(Component.translatable("message.xuanhuan_earth.implement.full")
                    .withStyle(ChatFormatting.YELLOW));
            return InteractionResult.SUCCESS_SERVER;
        }
        double drawAeu = Math.min(kind.chargeBatch(), missing / SpiritStoneCharge.CAPTURE_EFFICIENCY)
                * coupling * coupling;
        if (drawAeu <= 0.0001D) {
            player.sendSystemMessage(Component.translatable("message.xuanhuan_earth.implement.no_field")
                    .withStyle(ChatFormatting.YELLOW));
            return InteractionResult.SUCCESS_SERVER;
        }
        double gained = drawAeu * SpiritStoneCharge.CAPTURE_EFFICIENCY;
        stack.set(XuanhuanEarth.IMPLEMENT_CHARGE, state.withCharge(state.charge() + gained));
        Spirituality.consumeField(level, player.blockPosition(), drawAeu);
        player.swing(hand, true);
        if (player instanceof ServerPlayer serverPlayer) {
            CultivationNetwork.broadcastVisual(serverPlayer, CultivationVisualAction.RECOVERY);
        }
        if (level instanceof ServerLevel serverLevel) {
            XuanhuanEffects.stoneCharge(serverLevel, player.position());
        }
        player.sendSystemMessage(Component.translatable("message.xuanhuan_earth.implement.charged",
                ArcanaPower.format(gained), ArcanaPower.format(state.charge() + gained),
                ArcanaPower.format(kind.capacity())).withStyle(ChatFormatting.AQUA));
        return InteractionResult.SUCCESS_SERVER;
    }

    private InteractionResult cast(Level level, Player player, InteractionHand hand, ItemStack stack) {
        ImplementComponent state = ImplementComponent.read(stack);
        double remainingCharge = state.charge();
        if (state.charge() < kind.costPerCast() - 0.0001D) {
            double manaFallbackCost = Math.max(4.0D, kind.costPerCast() * 0.12D);
            if (!ArcanaPower.trySpendMana(player, manaFallbackCost)) {
                player.sendSystemMessage(Component.translatable("message.xuanhuan_earth.implement.empty",
                        ArcanaPower.format(state.charge()), ArcanaPower.format(kind.costPerCast()))
                        .withStyle(ChatFormatting.YELLOW));
                return InteractionResult.SUCCESS_SERVER;
            }
        } else {
            remainingCharge = Math.max(0.0D, state.charge() - kind.costPerCast());
            stack.set(XuanhuanEarth.IMPLEMENT_CHARGE, state.withCharge(remainingCharge));
        }
        if (!(level instanceof ServerLevel serverLevel)) {
            return InteractionResult.SUCCESS_SERVER;
        }
        kind.apply(serverLevel, player);
        XuanhuanEffects.implementCast(serverLevel, player.position(), kind);
        player.swing(hand, true);
        if (player instanceof ServerPlayer serverPlayer) {
            CultivationNetwork.broadcastVisual(serverPlayer, CultivationVisualAction.IMPLEMENT_CAST);
            XuanhuanJourney.complete(serverPlayer, XuanhuanJourney.Milestone.ARTIFACT_USE);
        }
        player.sendSystemMessage(Component.translatable(kind.messageKey(),
                ArcanaPower.format(remainingCharge),
                ArcanaPower.format(kind.capacity())).withStyle(ChatFormatting.GOLD));
        return InteractionResult.SUCCESS_SERVER;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context,
                                net.minecraft.world.item.component.TooltipDisplay display,
                                java.util.function.Consumer<Component> lines,
                                net.minecraft.world.item.TooltipFlag flag) {
        super.appendHoverText(stack, context, display, lines, flag);
        ImplementComponent state = ImplementComponent.read(stack);
        lines.accept(Component.translatable("tooltip.xuanhuan_earth.implement.charge",
                ArcanaPower.format(state.charge()), ArcanaPower.format(kind.capacity()),
                ArcanaPower.format(kind.costPerCast())).withStyle(ChatFormatting.AQUA));
        lines.accept(Component.translatable("tooltip.xuanhuan_earth.implement.use")
                .withStyle(ChatFormatting.DARK_GRAY));
    }

    /** The four implements and what each one does with its charge. */
    public enum Implement {
        /** Sweeps the ground: pushes and slows nearby hostile mobs. */
        WIND_FAN("wind_fan", 160.0D, 40.0D, 60.0D, 6.0D) {
            @Override
            public void apply(ServerLevel level, Player player) {
                Vec3 origin = player.position();
                Vec3 look = player.getLookAngle().normalize();
                player.setDeltaMovement(look.x * 1.15D, Math.max(0.35D, look.y * 0.7D + 0.25D), look.z * 1.15D);
                player.hurtMarked = true;
                player.resetFallDistance();
                for (var entity : level.getEntities(player, player.getBoundingBox().expandTowards(look.scale(9.0D)).inflate(3.5D))) {
                    if (entity == player || !(entity instanceof net.minecraft.world.entity.LivingEntity mob)
                            || mob instanceof com.xxsx.xuanhuanearth.entity.ContractableSpiritBeastEntity) {
                        continue;
                    }
                    Vec3 push = mob.position().subtract(origin).normalize().scale(0.95D).add(0.0D, 0.35D, 0.0D);
                    mob.push(push.x, push.y, push.z);
                    mob.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                            net.minecraft.world.effect.MobEffects.SLOWNESS, 80, 1, true, false, true));
                    mob.hurtServer(level, level.damageSources().playerAttack(player), 6.5F);
                }
            }
        },
        /** Grounds the caster: brief resistance, rock shatter, and a burst that hurts attackers. */
        STONE_SEAL("stone_seal", 200.0D, 50.0D, 80.0D, 8.0D) {
            @Override
            public void apply(ServerLevel level, Player player) {
                player.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                        net.minecraft.world.effect.MobEffects.RESISTANCE, 200, 1, true, false, true));
                player.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                        net.minecraft.world.effect.MobEffects.ABSORPTION, 200, 2, true, false, true));
                for (var entity : level.getEntities(player, player.getBoundingBox().inflate(5.0D))) {
                    if (entity instanceof net.minecraft.world.entity.LivingEntity mob
                            && !(mob instanceof com.xxsx.xuanhuanearth.entity.ContractableSpiritBeastEntity)) {
                        mob.hurtServer(level, level.damageSources().playerAttack(player), 8.0F);
                    }
                }
                if (player instanceof ServerPlayer serverPlayer) {
                    CultivationSkill.applySwordQiAndRockShatter(level, serverPlayer, 4);
                }
            }
        },
        /** Draws ambient qi into mana without a cushion. */
        GATHER_ORB("gather_orb", 240.0D, 60.0D, 100.0D, 10.0D) {
            @Override
            public void apply(ServerLevel level, Player player) {
                double before = ArcanaPower.getCurrentMana(player);
                ArcanaPower.setCurrentMana(player, before + 24.0D);
                if (player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
                    EarthHumanCompat.treatFractureAndCleanseAir(serverPlayer, 0.45D, 2.5D, 35.0D, 15.0D, 12.0D);
                }
            }
        },
        /** Lights the area and reveals nearby creatures. */
        LANTERN_ROD("lantern_rod", 120.0D, 30.0D, 50.0D, 5.0D) {
            @Override
            public void apply(ServerLevel level, Player player) {
                player.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                        net.minecraft.world.effect.MobEffects.NIGHT_VISION, 300, 0, true, false, true));
                player.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                        net.minecraft.world.effect.MobEffects.GLOWING, 100, 0, true, false, true));
                for (var entity : level.getEntities(player, player.getBoundingBox().inflate(16.0D))) {
                    if (entity instanceof net.minecraft.world.entity.Mob mob
                            && !(mob instanceof com.xxsx.xuanhuanearth.entity.ContractableSpiritBeastEntity)) {
                        mob.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                                net.minecraft.world.effect.MobEffects.GLOWING, 120, 0, true, false, true));
                    }
                }
            }
        };

        private final String id;
        private final double capacity;
        private final double chargeBatch;
        private final double costPerCast;
        private final double reach;

        Implement(String id, double capacity, double chargeBatch, double costPerCast, double reach) {
            this.id = id;
            this.capacity = capacity;
            this.chargeBatch = chargeBatch;
            this.costPerCast = costPerCast;
            this.reach = reach;
        }

        public abstract void apply(ServerLevel level, Player player);

        public String id() {
            return id;
        }

        public double capacity() {
            return capacity;
        }

        public double chargeBatch() {
            return chargeBatch;
        }

        public double costPerCast() {
            return costPerCast;
        }

        public double reach() {
            return reach;
        }

        public String translationKey() {
            return "item.xuanhuan_earth." + id;
        }

        public String hintKey() {
            return "tooltip.xuanhuan_earth." + id;
        }

        public String messageKey() {
            return "message.xuanhuan_earth.implement.cast." + id;
        }
    }
}
