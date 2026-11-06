package com.xxsx.xuanhuanearth;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.structure.Structure;

/**
 * X-073 Oracle Bone: used at night with flint_and_steel in the offhand to locate the nearest
 * #xuanhuan_earth:oracle_targets structure within 100 chunks (distance rounded to 64 blocks).
 */
public class OracleBoneItem extends GuidedXuanhuanItem {
    public static final int COOLDOWN_TICKS = 600;
    public static final int SEARCH_CHUNK_RADIUS = 100;
    public static final TagKey<Structure> ORACLE_TARGETS = TagKey.create(
            Registries.STRUCTURE, XuanhuanEarth.id("oracle_targets"));

    public OracleBoneItem(Properties properties, String hintKey) {
        super(properties, hintKey);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.getCooldowns().isOnCooldown(stack)) {
            if (!level.isClientSide()) {
                player.sendSystemMessage(Component.translatable("message.xuanhuan_earth.oracle_bone.cooldown")
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
        if (!serverLevel.isDarkOutside()) {
            player.sendSystemMessage(Component.translatable("lore.earth_lore.fail.needs_night")
                    .withStyle(ChatFormatting.YELLOW));
            return InteractionResult.FAIL;
        }
        ItemStack offhand = player.getOffhandItem();
        if (!offhand.is(Items.FLINT_AND_STEEL)) {
            player.sendSystemMessage(Component.translatable("message.xuanhuan_earth.oracle_bone.missing_flint_and_steel")
                    .withStyle(ChatFormatting.YELLOW));
            return InteractionResult.FAIL;
        }

        BlockPos origin = player.blockPosition();
        BlockPos found = serverLevel.findNearestMapStructure(ORACLE_TARGETS, origin, SEARCH_CHUNK_RADIUS, false);
        if (found == null) {
            player.sendSystemMessage(Component.translatable("message.xuanhuan_earth.oracle_bone.nothing_found")
                    .withStyle(ChatFormatting.GRAY));
            return InteractionResult.FAIL;
        }

        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }
        offhand.hurtAndBreak(1, serverPlayer, EquipmentSlot.OFFHAND);
        player.getCooldowns().addCooldown(this.getDefaultInstance(), COOLDOWN_TICKS);

        int dx = found.getX() - origin.getX();
        int dz = found.getZ() - origin.getZ();
        double rawDist = Math.hypot(dx, dz);
        int roundedDist = Math.max(64, (int) (Math.round(rawDist / 64.0D) * 64L));

        serverLevel.playSound(null, origin, SoundEvents.FLINTANDSTEEL_USE, SoundSource.PLAYERS, 0.8F, 0.9F);
        player.sendSystemMessage(Component.translatable(
                "message.xuanhuan_earth.oracle_bone.found",
                Component.translatable(LuopanItem.octantKey(dx, dz)),
                roundedDist).withStyle(ChatFormatting.GOLD));
        return InteractionResult.SUCCESS_SERVER;
    }
}
