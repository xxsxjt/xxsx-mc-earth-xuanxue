package com.xxsx.xuanhuanearth;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.hurtingprojectile.SmallFireball;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;

/**
 * X-053 fire_talisman, X-054 wind_talisman, X-055 seal_talisman.
 */
public class ElementalTalismanItem extends GuidedXuanhuanItem {
    public enum Kind {
        FIRE("fire_talisman", 2.0D),
        WIND("wind_talisman", 1.0D),
        SEAL("seal_talisman", 3.0D);

        private final String id;
        private final double manaCost;

        Kind(String id, double manaCost) {
            this.id = id;
            this.manaCost = manaCost;
        }

        public String id() {
            return id;
        }

        public double manaCost() {
            return manaCost;
        }
    }

    private final Kind kind;

    public ElementalTalismanItem(Properties properties, String hintKey, Kind kind) {
        super(properties, hintKey);
        this.kind = kind;
    }

    public Kind kind() {
        return kind;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display,
                                Consumer<Component> lines, TooltipFlag flag) {
        super.appendHoverText(stack, context, display, lines, flag);
        TalismanComponent charge = TalismanComponent.read(stack);
        lines.accept(Component.translatable("tooltip.xuanhuan_earth.basic_talisman.charge",
                ArcanaPower.format(charge.chargeAeu()),
                ArcanaPower.format(TalismanCharge.CAPACITY_AEU))
                .withStyle(ChatFormatting.AQUA));
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        if (!(level instanceof ServerLevel serverLevel) || !(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResult.SUCCESS_SERVER;
        }
        ItemStack stack = player.getItemInHand(hand);
        if ((kind == Kind.FIRE || kind == Kind.SEAL) && XuanhuanLoreIntegration.isHardLocked(serverPlayer, this)) {
            player.sendSystemMessage(Component.translatable("lore.earth_lore.fail.lore_locked")
                    .withStyle(ChatFormatting.RED));
            return InteractionResult.SUCCESS_SERVER;
        }
        TalismanComponent charge = TalismanComponent.read(stack);
        if (!TalismanCharge.isCharged(charge.chargeAeu())) {
            player.sendSystemMessage(Component.translatable(
                    "message.xuanhuan_earth.talisman.plan_rejected",
                    "talisman_empty",
                    "talisman carries no sealed field charge",
                    ArcanaPower.format(kind.manaCost()),
                    ArcanaPower.format(ArcanaPower.getCurrentMana(player)),
                    ArcanaPower.format(ArcanaPower.getMaxMana(player))).withStyle(ChatFormatting.YELLOW));
            return InteractionResult.SUCCESS_SERVER;
        }
        if (!ArcanaPower.canSpendMana(player, kind.manaCost())) {
            player.sendSystemMessage(Component.translatable(
                    "message.xuanhuan_earth.talisman.mana_low",
                    ArcanaPower.format(kind.manaCost()),
                    ArcanaPower.format(ArcanaPower.getCurrentMana(player)),
                    ArcanaPower.format(ArcanaPower.getMaxMana(player))).withStyle(ChatFormatting.YELLOW));
            return InteractionResult.SUCCESS_SERVER;
        }

        boolean applied = switch (kind) {
            case FIRE -> triggerFire(serverLevel, serverPlayer);
            case WIND -> triggerWind(serverLevel, serverPlayer);
            case SEAL -> triggerSeal(serverLevel, serverPlayer);
        };
        if (!applied) {
            return InteractionResult.SUCCESS_SERVER;
        }

        ArcanaPower.trySpendMana(player, kind.manaCost());
        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }
        XuanhuanJourney.complete(serverPlayer, XuanhuanJourney.Milestone.ARTIFACT_USE);
        CultivationNetwork.broadcastVisual(serverPlayer, CultivationVisualAction.TALISMAN);
        XuanhuanEffects.talismanActivation(serverLevel, player.position());
        player.sendSystemMessage(Component.translatable("message.xuanhuan_earth.talisman.used",
                ArcanaPower.format(kind.manaCost()),
                ArcanaPower.format(ArcanaPower.getCurrentMana(player)),
                ArcanaPower.format(ArcanaPower.getMaxMana(player))).withStyle(ChatFormatting.GOLD));
        return InteractionResult.SUCCESS_SERVER;
    }

    private static boolean triggerFire(ServerLevel level, ServerPlayer player) {
        Vec3 look = player.getLookAngle().normalize();
        SmallFireball fireball = new SmallFireball(level, player, look);
        fireball.setPos(player.getX() + look.x * 0.8D, player.getEyeY() - 0.1D + look.y * 0.8D, player.getZ() + look.z * 0.8D);
        level.addFreshEntity(fireball);
        level.playSound(null, player.blockPosition(), SoundEvents.BLAZE_SHOOT, SoundSource.PLAYERS, 0.8F, 1.1F);
        return true;
    }

    private static boolean triggerWind(ServerLevel level, ServerPlayer player) {
        Vec3 look = player.getLookAngle();
        Vec3 horizontal = new Vec3(look.x, 0.0D, look.z);
        if (horizontal.lengthSqr() < 1.0E-4D) {
            horizontal = Vec3.directionFromRotation(0.0F, player.getYRot());
        }
        Vec3 dir = horizontal.normalize();
        Vec3 best = null;
        for (int step = 6; step >= 1; step--) {
            Vec3 candidate = player.position().add(dir.scale(step));
            BlockPos feet = BlockPos.containing(candidate);
            if (level.noCollision(player, player.getBoundingBox().move(dir.scale(step)))
                    && level.getBlockState(feet).getCollisionShape(level, feet).isEmpty()) {
                best = candidate;
                break;
            }
        }
        if (best == null) {
            player.sendSystemMessage(Component.translatable("message.xuanhuan_earth.wind_talisman.blocked")
                    .withStyle(ChatFormatting.YELLOW));
            return false;
        }
        player.teleportTo(best.x, best.y, best.z);
        player.fallDistance = 0.0F;
        level.playSound(null, BlockPos.containing(best), SoundEvents.BREEZE_WIND_CHARGE_BURST.value(),
                SoundSource.PLAYERS, 0.8F, 1.2F);
        return true;
    }

    private static boolean triggerSeal(ServerLevel level, ServerPlayer player) {
        Vec3 eye = player.getEyePosition();
        Vec3 look = player.getLookAngle().normalize();
        AABB searchBox = player.getBoundingBox().expandTowards(look.scale(12.0D)).inflate(2.0D);
        List<LivingEntity> candidates = level.getEntitiesOfClass(LivingEntity.class, searchBox,
                e -> e != player && e.isAlive() && !e.isSpectator());
        LivingEntity target = candidates.stream()
                .filter(e -> {
                    Vec3 toEntity = e.getBoundingBox().getCenter().subtract(eye);
                    double dist = toEntity.length();
                    return dist <= 12.5D && dist > 0.001D && toEntity.normalize().dot(look) >= 0.75D;
                })
                .min(Comparator.comparingDouble(e -> e.distanceToSqr(player)))
                .orElse(null);
        if (target == null) {
            player.sendSystemMessage(Component.translatable("message.xuanhuan_earth.seal_talisman.no_target")
                    .withStyle(ChatFormatting.YELLOW));
            return false;
        }
        target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 200, 3, false, true, true));
        level.playSound(null, target.blockPosition(), SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.PLAYERS, 0.8F, 0.8F);
        return true;
    }
}
