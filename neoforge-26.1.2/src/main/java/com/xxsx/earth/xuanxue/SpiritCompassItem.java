package com.xxsx.earth.xuanxue;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;

import java.util.function.Consumer;

public class SpiritCompassItem extends Item {
    public SpiritCompassItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (!level.isClientSide()) {
            ArcanaChunkField.Reading reading = Spirituality.reading(level, player.blockPosition());
            player.sendSystemMessage(Component.translatable(
                    "message.earth_xuanxue.compass.reading",
                    Spirituality.gradeName(reading.value()),
                    reading.value()).withStyle(ChatFormatting.AQUA));
            player.sendSystemMessage(Component.translatable("message.earth_xuanxue.compass.field",
                    reading.mainSource(),
                    ArcanaPower.format(reading.depletion())).withStyle(ChatFormatting.DARK_AQUA));
            player.sendSystemMessage(Component.translatable("message.earth_xuanxue.arcana.status",
                    ArcanaPower.format(ArcanaPower.getCurrentMana(player)),
                    ArcanaPower.format(ArcanaPower.getMaxMana(player)),
                    ArcanaPower.format(ArcanaPower.getXuanhuanBonus(player)),
                    ArcanaPower.format(ArcanaPower.getMagicBonus(player))).withStyle(ChatFormatting.LIGHT_PURPLE));
            player.sendSystemMessage(Component.translatable("message.earth_xuanxue.compass.hint")
                    .withStyle(ChatFormatting.GRAY));
        }
        return level.isClientSide() ? InteractionResult.SUCCESS : InteractionResult.SUCCESS_SERVER;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> lines, TooltipFlag flag) {
        lines.accept(Component.translatable("tooltip.earth_xuanxue.spirit_compass").withStyle(ChatFormatting.GOLD));
        lines.accept(Component.translatable("tooltip.earth_xuanxue.spirit_compass.use").withStyle(ChatFormatting.GRAY));
    }
}
