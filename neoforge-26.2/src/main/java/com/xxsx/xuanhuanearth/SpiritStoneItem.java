package com.xxsx.xuanhuanearth;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;

import java.util.function.Consumer;

public class SpiritStoneItem extends GuidedXuanhuanItem {
    private static final double CHARGE_BATCH_AEU = 24.0D;
    private static final double RELEASE_BATCH_MANA = 10.0D;
    private final boolean cracked;

    public SpiritStoneItem(Properties properties, String hintKey) {
        this(properties, hintKey, false);
    }

    public SpiritStoneItem(Properties properties, String hintKey, boolean cracked) {
        super(properties, hintKey);
        this.cracked = cracked;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        ItemStack stack = player.getItemInHand(hand);
        if (player.isShiftKeyDown()) {
            releaseIntoMana(level, player, stack);
        } else {
            chargeFromField(level, player, hand, stack);
        }
        return InteractionResult.SUCCESS_SERVER;
    }

    private void chargeFromField(Level level, Player player, InteractionHand hand, ItemStack stack) {
        SpiritStoneComponent state = SpiritStoneComponent.read(stack);
        double cap = cracked ? SpiritStoneComponent.CRACKED_CAPACITY_AEU : SpiritStoneCharge.CAPACITY_AEU;
        ArcanaChunkField.Reading reading = Spirituality.reading(level, player.blockPosition());
        double usableField = SpiritFieldLedger.availableField(reading.value(), reading.depletion());
        double coupling = Math.min(1.0D, Math.max(0.0D, usableField / SpiritFieldLedger.FIELD_MAX));
        if (coupling <= 0.0D) {
            player.sendSystemMessage(Component.translatable("message.xuanhuan_earth.spirit_stone.no_field")
                    .withStyle(ChatFormatting.YELLOW));
            return;
        }
        double requested = CHARGE_BATCH_AEU * coupling * coupling;
        double remaining = Math.max(0.0D, cap - state.chargeAeu());
        double capturable = remaining / SpiritStoneCharge.CAPTURE_EFFICIENCY;
        double fieldSpent = Math.min(requested, capturable);
        double rejected = Math.max(0.0D, requested - fieldSpent);
        double nextStability = Math.max(0.0D, state.stability() - 0.02D * rejected);
        double gained = fieldSpent * SpiritStoneCharge.CAPTURE_EFFICIENCY;
        double nextCharge = Math.min(cap, state.chargeAeu() + gained);
        String sig = SpiritStoneComponent.computeSignature(level, player.blockPosition(), reading.mainSourceKey());

        if (!cracked && nextStability < 0.2D) {
            ItemStack crackedStack = new ItemStack(XuanhuanEarth.SPIRIT_STONE_CRACKED.get());
            double clamped = Math.min(SpiritStoneComponent.CRACKED_CAPACITY_AEU, nextCharge);
            crackedStack.set(XuanhuanEarth.SPIRIT_STONE_CHARGE.get(),
                    state.withStoneState(clamped, SpiritStoneComponent.CRACKED_CAPACITY_AEU, nextStability, sig));
            player.setItemInHand(hand, crackedStack);
            if (fieldSpent > 0.0D) {
                Spirituality.consumeField(level, player.blockPosition(), fieldSpent);
            }
            player.sendSystemMessage(Component.translatable("message.xuanhuan_earth.spirit_stone.cracked")
                    .withStyle(ChatFormatting.RED));
            return;
        }

        if (fieldSpent <= 0.0001D) {
            stack.set(XuanhuanEarth.SPIRIT_STONE_CHARGE.get(), state.withStoneState(nextCharge, cap, nextStability, sig));
            player.sendSystemMessage(Component.translatable("message.xuanhuan_earth.spirit_stone.full")
                    .withStyle(ChatFormatting.YELLOW));
            return;
        }
        stack.set(XuanhuanEarth.SPIRIT_STONE_CHARGE.get(), state.withStoneState(nextCharge, cap, nextStability, sig));
        Spirituality.consumeField(level, player.blockPosition(), fieldSpent);
        if (level instanceof net.minecraft.server.level.ServerLevel serverLevel) {
            XuanhuanEffects.stoneCharge(serverLevel, player.position());
        }
        player.sendSystemMessage(Component.translatable("message.xuanhuan_earth.spirit_stone.charged",
                ArcanaPower.format(fieldSpent), ArcanaPower.format(gained),
                ArcanaPower.format(nextCharge), ArcanaPower.format(cap),
                ArcanaPower.format(Math.max(0.0D, fieldSpent - gained)), Spirituality.gradeName(reading.value()))
                .withStyle(ChatFormatting.AQUA));
    }

    private void releaseIntoMana(Level level, Player player, ItemStack stack) {
        SpiritStoneComponent state = SpiritStoneComponent.read(stack);
        double cap = cracked ? SpiritStoneComponent.CRACKED_CAPACITY_AEU : SpiritStoneCharge.CAPACITY_AEU;
        double missing = ArcanaPower.getMaxMana(player) - ArcanaPower.getCurrentMana(player);
        if (missing <= 0.0001D) {
            player.sendSystemMessage(Component.translatable("message.xuanhuan_earth.spirit_stone.mana_full")
                    .withStyle(ChatFormatting.YELLOW));
            return;
        }
        SpiritStoneCharge.ReleaseResult result = SpiritStoneCharge.release(
                state.chargeAeu(), Math.min(RELEASE_BATCH_MANA, missing));
        if (result.aeuSpent() <= 0.0D || result.manaRestored() <= 0.0D) {
            player.sendSystemMessage(Component.translatable("message.xuanhuan_earth.spirit_stone.empty")
                    .withStyle(ChatFormatting.YELLOW));
            return;
        }
        double before = ArcanaPower.getCurrentMana(player);
        ArcanaPower.setCurrentMana(player, before + result.manaRestored());
        double restored = ArcanaPower.getCurrentMana(player) - before;
        stack.set(XuanhuanEarth.SPIRIT_STONE_CHARGE.get(),
                state.withStoneState(Math.min(cap, result.charge()), cap, state.stability(), state.fieldSignature()));
        if (player instanceof ServerPlayer serverPlayer) {
            CultivationNetwork.broadcastVisual(serverPlayer, CultivationVisualAction.RECOVERY);
            XuanhuanJourney.complete(serverPlayer, XuanhuanJourney.Milestone.ARTIFACT_USE);
            if (level instanceof net.minecraft.server.level.ServerLevel serverLevel) {
                XuanhuanEffects.stoneRelease(serverLevel, player.position());
            }
        }
        player.sendSystemMessage(Component.translatable("message.xuanhuan_earth.spirit_stone.released",
                ArcanaPower.format(result.aeuSpent()), ArcanaPower.format(restored),
                ArcanaPower.format(Math.min(cap, result.charge())), ArcanaPower.format(cap))
                .withStyle(ChatFormatting.GOLD));
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display,
                                Consumer<Component> lines, TooltipFlag flag) {
        super.appendHoverText(stack, context, display, lines, flag);
        SpiritStoneComponent state = SpiritStoneComponent.read(stack);
        double cap = cracked ? SpiritStoneComponent.CRACKED_CAPACITY_AEU : SpiritStoneCharge.CAPACITY_AEU;
        lines.accept(Component.translatable("tooltip.xuanhuan_earth.spirit_stone.matrix")
                .withStyle(ChatFormatting.GRAY));
        lines.accept(Component.translatable("tooltip.xuanhuan_earth.spirit_stone.charge",
                ArcanaPower.format(state.chargeAeu()),
                ArcanaPower.format(cap),
                Component.translatable("spirit_stone.xuanhuan_earth.grade." + SpiritStoneCharge.grade(state.chargeAeu())))
                .withStyle(ChatFormatting.AQUA));
        lines.accept(Component.translatable("tooltip.xuanhuan_earth.spirit_stone.use")
                .withStyle(ChatFormatting.DARK_GRAY));
    }
}
