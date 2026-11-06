package com.xxsx.xuanhuanearth;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

import java.util.function.Consumer;

/**
 * X-004 Spirit Stone Core: 480 AEU high-capacity formation storage core.
 * Only accepts charge from a formed spirit array core (up to 40 AEU/tick or 40 AEU per interaction).
 */
public class SpiritStoneCoreItem extends GuidedXuanhuanItem {
    public static final double CORE_CAPACITY_AEU = 480.0D;

    public SpiritStoneCoreItem(Properties properties, String hintKey) {
        super(properties, hintKey);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        if (level.getBlockState(pos).is(XuanhuanEarth.SPIRIT_ARRAY_CORE.get())) {
            if (!level.isClientSide() && context.getPlayer() != null) {
                ItemStack stack = context.getItemInHand();
                SpiritStoneComponent state = SpiritStoneComponent.read(stack);
                if (!XuanhuanStructures.isFormalSpiritArray(level, pos)) {
                    context.getPlayer().sendSystemMessage(Component.translatable(
                            "message.xuanhuan_earth.spirit_stone_core.not_formed")
                            .withStyle(ChatFormatting.YELLOW));
                    return InteractionResult.SUCCESS_SERVER;
                }
                ArcanaChunkField.Reading reading = Spirituality.reading(level, pos);
                double available = SpiritFieldLedger.extractableAeu(reading.value(), reading.depletion());
                double needed = Math.min(40.0D, CORE_CAPACITY_AEU - state.chargeAeu());
                double draw = Math.min(available, needed);
                double after = Math.min(CORE_CAPACITY_AEU, state.chargeAeu() + draw);
                stack.set(XuanhuanEarth.SPIRIT_STONE_CHARGE.get(),
                        state.withCoreState(after, CORE_CAPACITY_AEU, pos));
                if (draw > 0.0D) {
                    Spirituality.consumeField(level, pos, draw);
                }
                if (level.getBlockEntity(pos) instanceof XuanhuanMachineBlockEntity machine) {
                    machine.addStoredAeu(draw);
                }
                context.getPlayer().sendSystemMessage(Component.translatable(
                        "message.xuanhuan_earth.spirit_stone_core.charged",
                        ArcanaPower.format(after), ArcanaPower.format(CORE_CAPACITY_AEU))
                        .withStyle(ChatFormatting.AQUA));
            }
            return level.isClientSide() ? InteractionResult.SUCCESS : InteractionResult.SUCCESS_SERVER;
        }
        return super.useOn(context);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        if (player.isShiftKeyDown()) {
            ItemStack stack = player.getItemInHand(hand);
            SpiritStoneComponent state = SpiritStoneComponent.read(stack);
            double missing = ArcanaPower.getMaxMana(player) - ArcanaPower.getCurrentMana(player);
            if (missing > 0.0001D && state.chargeAeu() > 0.0001D) {
                double spend = Math.min(state.chargeAeu(), Math.min(20.0D, missing) * SpiritStoneCharge.AEU_PER_MANA / SpiritStoneCharge.RELEASE_EFFICIENCY);
                double mana = spend * SpiritStoneCharge.RELEASE_EFFICIENCY / SpiritStoneCharge.AEU_PER_MANA;
                ArcanaPower.setCurrentMana(player, ArcanaPower.getCurrentMana(player) + mana);
                stack.set(XuanhuanEarth.SPIRIT_STONE_CHARGE.get(),
                        state.withCoreState(state.chargeAeu() - spend, CORE_CAPACITY_AEU, state.linkedArray()));
                player.sendSystemMessage(Component.translatable("message.xuanhuan_earth.spirit_stone.released",
                        ArcanaPower.format(spend), ArcanaPower.format(mana),
                        ArcanaPower.format(state.chargeAeu() - spend), ArcanaPower.format(CORE_CAPACITY_AEU))
                        .withStyle(ChatFormatting.GOLD));
            }
        }
        return InteractionResult.SUCCESS_SERVER;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display,
                                Consumer<Component> lines, TooltipFlag flag) {
        super.appendHoverText(stack, context, display, lines, flag);
        SpiritStoneComponent state = SpiritStoneComponent.read(stack);
        lines.accept(Component.translatable("tooltip.xuanhuan_earth.spirit_stone.charge",
                ArcanaPower.format(state.chargeAeu()),
                ArcanaPower.format(CORE_CAPACITY_AEU),
                state.linkedArray().equals(BlockPos.ZERO) ? "-" : state.linkedArray().toShortString())
                .withStyle(ChatFormatting.AQUA));
    }
}
