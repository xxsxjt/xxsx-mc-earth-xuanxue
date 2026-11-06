package com.xxsx.xuanhuanearth;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * X-071 Dragon-Seeking Rod: locates the weighted source centroid in the current chunk and marks
 * its surface block as the chunk's acupoint in ArcanaChunkField.
 */
public class DragonSeekingRodItem extends GuidedXuanhuanItem {
    public static final int MAX_DURABILITY = 128;
    public static final int COOLDOWN_TICKS = 200;

    public DragonSeekingRodItem(Properties properties, String hintKey) {
        super(properties, hintKey);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (stack.getDamageValue() >= stack.getMaxDamage()) {
            if (!level.isClientSide()) {
                player.sendSystemMessage(Component.translatable("message.xuanhuan_earth.dragon_rod.broken")
                        .withStyle(ChatFormatting.RED));
            }
            return InteractionResult.FAIL;
        }
        if (player.getCooldowns().isOnCooldown(stack)) {
            if (!level.isClientSide()) {
                player.sendSystemMessage(Component.translatable("message.xuanhuan_earth.dragon_rod.cooldown")
                        .withStyle(ChatFormatting.YELLOW));
            }
            return InteractionResult.FAIL;
        }
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        if (!(level instanceof ServerLevel serverLevel) || !(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResult.SUCCESS_SERVER;
        }
        player.getCooldowns().addCooldown(stack, COOLDOWN_TICKS);
        BlockPos acupoint = ArcanaChunkField.locateAndMarkAcupoint(serverLevel, player.blockPosition());
        Vec3 marker = Vec3.atCenterOf(acupoint);
        XuanhuanEffects.groundRing(serverLevel, marker, 1.35F);
        XuanhuanEffects.qiRise(serverLevel, marker, 0.85D, 18, 0.025D);
        XuanhuanEffects.runeCluster(serverLevel, marker, 4, 0.95D, true);

        EquipmentSlot slot = hand == InteractionHand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND;
        stack.hurtAndBreak(1, serverPlayer, slot);
        player.sendSystemMessage(Component.translatable(
                "message.xuanhuan_earth.dragon_rod.marked",
                acupoint.getX(), acupoint.getY(), acupoint.getZ())
                .withStyle(ChatFormatting.GOLD));
        return InteractionResult.SUCCESS_SERVER;
    }
}
