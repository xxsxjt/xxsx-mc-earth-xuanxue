package com.xxsx.xuanhuanearth;

import com.xxsx.xuanhuanearth.entity.ContractableSpiritBeastEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
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
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.function.Consumer;

public class BasicTalismanItem extends GuidedXuanhuanItem {
    private static final double TRIGGER_MANA_COST = SpellCatalog.WARD_TALISMAN.manaCost();

    public BasicTalismanItem(Properties properties, String hintKey) {
        super(properties, hintKey);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display,
                                Consumer<Component> lines, TooltipFlag flag) {
        super.appendHoverText(stack, context, display, lines, flag);
        TalismanComponent charge = TalismanComponent.read(stack);
        lines.accept(Component.translatable("tooltip.xuanhuan_earth.basic_talisman.charge",
                ArcanaPower.format(charge.chargeAeu()),
                ArcanaPower.format(TalismanCharge.CAPACITY_AEU))
                .withStyle(ChatFormatting.AQUA));
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        Player player = context.getPlayer();
        if (player == null || !player.isShiftKeyDown()) {
            return super.useOn(context);
        }
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        if (!(level instanceof ServerLevel serverLevel) || !(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResult.SUCCESS_SERVER;
        }
        ItemStack stack = context.getItemInHand();
        if (!TalismanComponent.isSealed(stack)) {
            TalismanComponent.drawn(stack);
        }
        if (!ArcanaPower.trySpendMana(player, TRIGGER_MANA_COST)) {
            player.sendSystemMessage(Component.translatable(
                    "message.xuanhuan_earth.talisman.mana_low",
                    ArcanaPower.format(TRIGGER_MANA_COST),
                    ArcanaPower.format(ArcanaPower.getCurrentMana(player)),
                    ArcanaPower.format(ArcanaPower.getMaxMana(player))).withStyle(ChatFormatting.YELLOW));
            return InteractionResult.SUCCESS_SERVER;
        }
        BlockPos center = context.getClickedPos();
        int mined = 0;
        for (BlockPos pos : BlockPos.betweenClosed(center.offset(-1, -1, -1), center.offset(1, 1, 1))) {
            if (CultivationSkill.tryHarvestRockOrOre(serverLevel, serverPlayer, pos.immutable())) {
                mined++;
            }
        }
        if (mined > 0) {
            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }
            player.swing(context.getHand(), true);
            CultivationNetwork.broadcastVisual(serverPlayer, CultivationVisualAction.TALISMAN);
            serverLevel.sendParticles(ParticleTypes.EXPLOSION,
                    center.getX() + 0.5D, center.getY() + 0.5D, center.getZ() + 0.5D,
                    2, 0.3D, 0.3D, 0.3D, 0.02D);
            serverLevel.playSound(null, center, SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 0.7F, 1.25F);
            XuanhuanJourney.complete(serverPlayer, XuanhuanJourney.Milestone.ARTIFACT_USE);
            XuanhuanEffects.talismanActivation(serverLevel, Vec3.atCenterOf(center));
            return InteractionResult.SUCCESS_SERVER;
        }
        return super.useOn(context);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (!level.isClientSide()) {
            ItemStack stack = player.getItemInHand(hand);
            if (!(player instanceof ServerPlayer serverPlayer)) {
                return InteractionResult.SUCCESS_SERVER;
            }
            if (!(level instanceof net.minecraft.server.level.ServerLevel serverLevel)) {
                return InteractionResult.SUCCESS_SERVER;
            }
            if (!TalismanComponent.isSealed(stack)) {
                TalismanComponent.drawn(stack);
            }
            SpellExecutor.Plan plan = SpellExecutor.plan(serverPlayer, SpellCatalog.wardTalisman(), hand);
            if (!plan.accepted()) {
                player.sendSystemMessage(Component.translatable(
                        "message.xuanhuan_earth.talisman.plan_rejected",
                        plan.errorCode(),
                        plan.message(),
                        ArcanaPower.format(TRIGGER_MANA_COST),
                        ArcanaPower.format(ArcanaPower.getCurrentMana(player)),
                        ArcanaPower.format(ArcanaPower.getMaxMana(player))).withStyle(ChatFormatting.YELLOW));
                return InteractionResult.SUCCESS_SERVER;
            }
            if (!SpellExecutor.commitTalisman(serverLevel, serverPlayer, hand)) {
                player.sendSystemMessage(Component.translatable(
                        "message.xuanhuan_earth.talisman.mana_low",
                        ArcanaPower.format(TRIGGER_MANA_COST),
                        ArcanaPower.format(ArcanaPower.getCurrentMana(player)),
                        ArcanaPower.format(ArcanaPower.getMaxMana(player))).withStyle(ChatFormatting.YELLOW));
                return InteractionResult.SUCCESS_SERVER;
            }
            player.swing(hand, true);
            player.addEffect(new MobEffectInstance(MobEffects.STRENGTH, 160, 0, true, false, true));
            player.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 160, 0, true, false, true));
            player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 240, 1, true, false, true));
            EarthHumanCompat.treatFractureAndCleanseAir(serverPlayer, 0.65D, 3.5D, 25.0D, 0.0D, 10.0D);
            AABB box = player.getBoundingBox().inflate(4.5D);
            for (LivingEntity target : serverLevel.getEntitiesOfClass(LivingEntity.class, box,
                    e -> e != player && e instanceof Enemy && !(e instanceof ContractableSpiritBeastEntity))) {
                target.hurtServer(serverLevel, serverLevel.damageSources().playerAttack(player), 5.0F);
                Vec3 push = target.position().subtract(player.position());
                if (push.lengthSqr() > 0.001D) {
                    Vec3 dir = push.normalize().scale(0.75D);
                    target.push(dir.x, 0.3D, dir.z);
                }
            }
            EarthHumanCompat.RecoveryReport report = EarthHumanCompat.recoverCore(serverPlayer, 0.8D, 0.10D);
            player.sendSystemMessage(Component.translatable("message.xuanhuan_earth.talisman.used",
                    ArcanaPower.format(TRIGGER_MANA_COST),
                    ArcanaPower.format(ArcanaPower.getCurrentMana(player)),
                    ArcanaPower.format(ArcanaPower.getMaxMana(player))).withStyle(ChatFormatting.GOLD));
            if (report.changed()) {
                player.sendSystemMessage(Component.translatable("message.xuanhuan_earth.human_recovery",
                        ArcanaPower.format(report.fatigueReduced()),
                        ArcanaPower.format(report.bodyHealed())).withStyle(ChatFormatting.GREEN));
            }
        }
        return level.isClientSide() ? InteractionResult.SUCCESS : InteractionResult.SUCCESS_SERVER;
    }
}
