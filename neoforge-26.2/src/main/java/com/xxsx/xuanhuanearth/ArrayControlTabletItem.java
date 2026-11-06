package com.xxsx.xuanhuanearth;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
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
 * X-066 Array Control Tablet & X-ARRAY-03: binds to a spirit_array_core, cycles its mode
 * (gather, charge, guard, alchemy, seal), and adjusts radius 4..16 on sneak+use.
 */
public class ArrayControlTabletItem extends GuidedXuanhuanItem {
    public record TabletState(BlockPos linked, int radius) {
        public static final TabletState DEFAULT = new TabletState(BlockPos.ZERO, 8);
        public static final Codec<TabletState> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                BlockPos.CODEC.optionalFieldOf("linked", BlockPos.ZERO).forGetter(TabletState::linked),
                Codec.intRange(4, 16).optionalFieldOf("radius", 8).forGetter(TabletState::radius)
        ).apply(instance, TabletState::new));

        public TabletState {
            linked = linked == null ? BlockPos.ZERO : linked;
            radius = Math.max(4, Math.min(16, radius));
        }
    }

    public ArrayControlTabletItem(Properties properties, String hintKey) {
        super(properties, hintKey);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        if (level.getBlockState(pos).is(XuanhuanEarth.SPIRIT_ARRAY_CORE.get())) {
            if (!level.isClientSide() && context.getPlayer() != null) {
                ItemStack stack = context.getItemInHand();
                TabletState prev = read(stack);
                stack.set(XuanhuanEarth.ARRAY_TABLET_COMPONENT.get(), new TabletState(pos, prev.radius()));
                context.getPlayer().sendSystemMessage(Component.translatable(
                        "message.xuanhuan_earth.array_tablet.linked", pos.toShortString())
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
        ItemStack stack = player.getItemInHand(hand);
        TabletState state = read(stack);
        if (state.linked().equals(BlockPos.ZERO)
                || !level.isLoaded(state.linked())
                || !(level.getBlockEntity(state.linked()) instanceof XuanhuanMachineBlockEntity machine)
                || machine.kind() != XuanhuanMachineBlock.Kind.SPIRIT_ARRAY_CORE) {
            player.sendSystemMessage(Component.translatable("message.xuanhuan_earth.array_tablet.no_linked_array")
                    .withStyle(ChatFormatting.YELLOW));
            return InteractionResult.SUCCESS_SERVER;
        }
        if (player.isShiftKeyDown()) {
            int nextRadius = state.radius() >= 16 ? 4 : state.radius() + 2;
            stack.set(XuanhuanEarth.ARRAY_TABLET_COMPONENT.get(), new TabletState(state.linked(), nextRadius));
            machine.setArrayRadius(nextRadius);
            player.sendSystemMessage(Component.translatable("message.xuanhuan_earth.array_tablet.radius", nextRadius)
                    .withStyle(ChatFormatting.GOLD));
            return InteractionResult.SUCCESS_SERVER;
        }
        if (player instanceof ServerPlayer serverPlayer && !XuanhuanLoreIntegration.knowsArrayFunctions(serverPlayer)) {
            player.sendSystemMessage(Component.translatable("lore.earth_lore.fail.lore_locked")
                    .withStyle(ChatFormatting.RED));
            return InteractionResult.SUCCESS_SERVER;
        }
        XuanhuanMachineBlockEntity.ArrayMode nextMode = machine.cycleArrayMode();
        int fieldVal = Spirituality.measure(level, state.linked());
        player.sendSystemMessage(Component.translatable("message.xuanhuan_earth.array_tablet.mode",
                Component.translatable(nextMode.labelKey()), fieldVal, ArcanaPower.format(nextMode.aeuPerTick()))
                .withStyle(ChatFormatting.AQUA));
        return InteractionResult.SUCCESS_SERVER;
    }

    public static TabletState read(ItemStack stack) {
        TabletState val = stack.get(XuanhuanEarth.ARRAY_TABLET_COMPONENT.get());
        return val == null ? TabletState.DEFAULT : val;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display,
                                Consumer<Component> lines, TooltipFlag flag) {
        super.appendHoverText(stack, context, display, lines, flag);
        TabletState state = read(stack);
        lines.accept(Component.translatable("tooltip.xuanhuan_earth.array_tablet.status",
                state.linked().equals(BlockPos.ZERO) ? "-" : state.linked().toShortString(),
                state.radius()).withStyle(ChatFormatting.GRAY));
    }
}
