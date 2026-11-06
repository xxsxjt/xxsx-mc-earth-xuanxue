package com.xxsx.xuanhuanearth;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;

import java.util.function.Consumer;

/**
 * A pill with a declared effect, used for every pill beyond the basic qi recovery one.
 *
 * <p>Each pill is a small data object rather than its own class: kind, mana restored, route
 * experience granted, effects, and how much of the local field it disturbs. That keeps the
 * alchemy recipe table and the item behaviour in one place.</p>
 */
public class XuanhuanPillItem extends GuidedXuanhuanItem {
    private final Pill pill;

    public XuanhuanPillItem(Properties properties, String hintKey, Pill pill) {
        super(properties, hintKey);
        this.pill = pill;
    }

    public Pill pill() {
        return pill;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        ItemStack stack = player.getItemInHand(hand);
        String id = pill.id();

        // X-034: Breakthrough pill is only consumed during realm breakthrough
        if ("breakthrough_pill".equals(id)) {
            player.sendSystemMessage(Component.translatable("message.xuanhuan_earth.pill.breakthrough_only")
                    .withStyle(ChatFormatting.YELLOW));
            return InteractionResult.FAIL;
        }

        // X-031..033 route level checks
        if ("marrow_pill".equals(id) && ArcanaPower.getBodyTemperingLevel(player) < 1) {
            player.sendSystemMessage(Component.translatable("message.xuanhuan_earth.pill.route_locked")
                    .withStyle(ChatFormatting.RED));
            return InteractionResult.FAIL;
        }
        if ("breath_pill".equals(id) && ArcanaPower.getFetalBreathLevel(player) < 1) {
            player.sendSystemMessage(Component.translatable("message.xuanhuan_earth.pill.route_locked")
                    .withStyle(ChatFormatting.RED));
            return InteractionResult.FAIL;
        }
        if ("fast_pill".equals(id) && ArcanaPower.getBiguLevel(player) < 1) {
            player.sendSystemMessage(Component.translatable("message.xuanhuan_earth.pill.route_locked")
                    .withStyle(ChatFormatting.RED));
            return InteractionResult.FAIL;
        }

        // X-035 detox pill requires at least one matching debuff
        if ("detox_pill".equals(id)) {
            boolean hasDebuff = player.hasEffect(MobEffects.POISON)
                    || player.hasEffect(MobEffects.WEAKNESS)
                    || player.hasEffect(MobEffects.NAUSEA);
            if (!hasDebuff) {
                player.sendSystemMessage(Component.translatable("message.xuanhuan_earth.pill.no_effect")
                        .withStyle(ChatFormatting.YELLOW));
                return InteractionResult.FAIL;
            }
            player.removeEffect(MobEffects.POISON);
            player.removeEffect(MobEffects.WEAKNESS);
            player.removeEffect(MobEffects.NAUSEA);
        }

        PillComponent pillComp = PillComponent.read(stack, id);
        double durationMult = "failed_pill".equals(id) ? 1.0D : pillComp.grade().durationMultiplier();

        if ("calm_spirit_pill".equals(id)) {
            player.removeEffect(MobEffects.NAUSEA);
            player.removeEffect(MobEffects.WEAKNESS);
            long immuneTicks = Math.max(20L, Math.round(600.0D * durationMult));
            ArcanaPower.data(player).putLong(ArcanaPassiveEffects.CALM_SPIRIT_UNTIL,
                    level.getGameTime() + immuneTicks);
        } else if ("clear_heat_pill".equals(id)) {
            stepDownDebuff(player, MobEffects.POISON);
            stepDownDebuff(player, MobEffects.WITHER);
            stepDownDebuff(player, MobEffects.HUNGER);
        } else if ("resolve_toxin_pill".equals(id)) {
            player.removeEffect(MobEffects.POISON);
            long immuneTicks = Math.max(20L, Math.round(300.0D * durationMult));
            ArcanaPower.data(player).putLong(ArcanaPassiveEffects.RESOLVE_TOXIN_UNTIL,
                    level.getGameTime() + immuneTicks);
        }

        boolean manaFull = ArcanaPower.getCurrentMana(player) >= ArcanaPower.getMaxMana(player) - 0.0001D;
        boolean humanFull = !EarthHumanCompat.canRecover(player);
        if (pill.mana() > 0.0D && manaFull && humanFull && pill.effects().length == 0
                && pill.experience() <= 0 && !"calm_spirit_pill".equals(id) && !"resolve_toxin_pill".equals(id)) {
            player.sendSystemMessage(Component.translatable("message.xuanhuan_earth.pill.no_need",
                    ArcanaPower.format(ArcanaPower.getCurrentMana(player)),
                    ArcanaPower.format(ArcanaPower.getMaxMana(player)))
                    .withStyle(ChatFormatting.YELLOW));
            return InteractionResult.SUCCESS_SERVER;
        }

        double before = ArcanaPower.getCurrentMana(player);
        if (pill.mana() > 0.0D) {
            ArcanaPower.setCurrentMana(player, before + pill.mana() * durationMult);
        }
        double restored = ArcanaPower.getCurrentMana(player) - before;

        if (pill.focus() != null && pill.experience() > 0) {
            ArcanaPower.addCultivationExperience(player, pill.focus(), pill.experience());
        }

        for (MobEffectInstance effect : pill.effects()) {
            int scaledDuration = Math.max(20, (int) Math.round(effect.getDuration() * durationMult));
            player.addEffect(new MobEffectInstance(effect.getEffect(), scaledDuration,
                    effect.getAmplifier(), effect.isAmbient(), effect.isVisible(), effect.showIcon()));
        }

        if (!"failed_pill".equals(id) && !"detox_pill".equals(id) && !"calm_spirit_pill".equals(id)) {
            double nauseaChance = pillComp.grade().nauseaChance();
            if (nauseaChance > 0.0D && level.getRandom().nextDouble() < nauseaChance) {
                player.addEffect(new MobEffectInstance(MobEffects.NAUSEA, 120, 0, false, true, true));
            }
        }

        if (pill.fatigue() > 0.0D || pill.bodyHeal() > 0.0D) {
            EarthHumanCompat.recoverCore((ServerPlayer) player, pill.fatigue(), pill.bodyHeal());
        }

        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }

        if (player instanceof ServerPlayer serverPlayer) {
            CultivationNetwork.broadcastVisual(serverPlayer, CultivationVisualAction.RECOVERY);
            XuanhuanJourney.complete(serverPlayer, XuanhuanJourney.Milestone.ARTIFACT_USE);
            if (level instanceof ServerLevel serverLevel) {
                XuanhuanEffects.pillBurst(serverLevel, player.position());
            }
        }

        player.sendSystemMessage(Component.translatable(pill.messageKey(),
                ArcanaPower.format(restored),
                ArcanaPower.format(ArcanaPower.getCurrentMana(player)),
                ArcanaPower.format(ArcanaPower.getMaxMana(player)))
                .withStyle(ChatFormatting.AQUA));
        return InteractionResult.SUCCESS_SERVER;
    }

    private static void stepDownDebuff(Player player,
                                       net.minecraft.core.Holder<net.minecraft.world.effect.MobEffect> effect) {
        MobEffectInstance active = player.getEffect(effect);
        if (active == null) {
            return;
        }
        player.removeEffect(effect);
        if (active.getAmplifier() > 0) {
            player.addEffect(new MobEffectInstance(effect, active.getDuration(),
                    active.getAmplifier() - 1, active.isAmbient(), active.isVisible(), active.showIcon()));
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display,
                                Consumer<Component> lines, TooltipFlag flag) {
        super.appendHoverText(stack, context, display, lines, flag);
        if (!"failed_pill".equals(pill.id())) {
            PillComponent comp = PillComponent.read(stack, pill.id());
            lines.accept(Component.translatable("tooltip.xuanhuan_earth.pill.grade",
                    Component.translatable("grade.earth_lore." + comp.grade().serialized()),
                    Math.round(comp.purity() * 100.0D)).withStyle(ChatFormatting.GOLD));
        }
    }

    /**
     * Static definition of one pill.
     *
     * @param id        recipe and translation id
     * @param mana      mana restored immediately
     * @param focus     route that gains experience, or null
     * @param experience route experience granted
     * @param fatigue   Earth Human fatigue relieved
     * @param bodyHeal  Earth Human body healing per part
     * @param effects   potion effects applied on use
     */
    public record Pill(String id, double mana, CultivationFocus focus, int experience,
                       double fatigue, double bodyHeal, MobEffectInstance[] effects) {
        public String messageKey() {
            return "message.xuanhuan_earth.pill." + id;
        }

        public String translationKey() {
            return "item.xuanhuan_earth." + id;
        }

        public String hintKey() {
            return "tooltip.xuanhuan_earth." + id;
        }

        public static MobEffectInstance effect(net.minecraft.core.Holder<net.minecraft.world.effect.MobEffect> effect,
                                               int seconds, int amplifier) {
            return new MobEffectInstance(effect, seconds * 20, amplifier, true, false, true);
        }

        public static MobEffectInstance regeneration(int seconds, int amplifier) {
            return new MobEffectInstance(MobEffects.REGENERATION, seconds * 20, amplifier, true, false, true);
        }
    }
}
