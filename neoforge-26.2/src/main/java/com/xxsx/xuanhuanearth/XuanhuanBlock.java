package com.xxsx.xuanhuanearth;

import com.xxsx.xuanhuanearth.particle.XuanhuanParticles;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public class XuanhuanBlock extends Block {
    private final String messageKey;

    public XuanhuanBlock(Properties properties, String messageKey) {
        super(properties);
        this.messageKey = messageKey;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (state.is(XuanhuanEarth.SPIRIT_SPRING_STONE.get())) {
            for (int i = 0; i < 2; i++) {
                double x = pos.getX() + 0.2D + random.nextDouble() * 0.6D;
                double y = pos.getY() + 0.85D + random.nextDouble() * 0.3D;
                double z = pos.getZ() + 0.2D + random.nextDouble() * 0.6D;
                level.addParticle(ParticleTypes.BUBBLE_COLUMN_UP, x, y, z, 0.0D, 0.06D, 0.0D);
                level.addParticle(XuanhuanParticles.QI_MOTE.get(), x, y + 0.1D, z,
                        (random.nextDouble() - 0.5D) * 0.01D, 0.025D, (random.nextDouble() - 0.5D) * 0.01D);
            }
            if (random.nextInt(3) == 0) {
                level.addParticle(ParticleTypes.SPLASH,
                        pos.getX() + 0.25D + random.nextDouble() * 0.5D,
                        pos.getY() + 1.02D,
                        pos.getZ() + 0.25D + random.nextDouble() * 0.5D,
                        0.0D, 0.04D, 0.0D);
            }
            if (random.nextInt(14) == 0) {
                level.playLocalSound(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D,
                        SoundEvents.WATER_AMBIENT, SoundSource.BLOCKS,
                        0.32F + random.nextFloat() * 0.15F, 0.95F + random.nextFloat() * 0.2F, false);
            }
            if (random.nextInt(28) == 0) {
                level.playLocalSound(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D,
                        SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.BLOCKS,
                        0.28F, 1.15F + random.nextFloat() * 0.2F, false);
            }
        } else if (state.is(XuanhuanEarth.SPIRIT_VEIN_NODE.get()) && random.nextInt(3) == 0) {
            double x = pos.getX() + 0.15D + random.nextDouble() * 0.7D;
            double y = pos.getY() + 0.2D + random.nextDouble() * 0.8D;
            double z = pos.getZ() + 0.15D + random.nextDouble() * 0.7D;
            level.addParticle(XuanhuanParticles.QI_SPARK.get(), x, y, z, 0.0D, 0.015D, 0.0D);
        }
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        return describe(level, pos, player);
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (state.is(XuanhuanEarth.SPIRIT_SPRING_STONE.get()) && stack.is(Items.GLASS_BOTTLE)) {
            if (!level.isClientSide()) {
                ItemStack filled = new ItemStack(XuanhuanEarth.SPIRIT_SPRING_BOTTLE.get());
                player.setItemInHand(hand, ItemUtils.createFilledResult(stack, player, filled));
                level.playSound(null, pos, SoundEvents.BOTTLE_FILL, SoundSource.BLOCKS, 0.8F, 1.0F);
                if (level instanceof ServerLevel serverLevel) {
                    XuanhuanEffects.qiRise(serverLevel, Vec3.atCenterOf(pos), 0.45D, 6, 0.025D);
                }
            }
            return level.isClientSide() ? InteractionResult.SUCCESS : InteractionResult.SUCCESS_SERVER;
        }
        return describe(level, pos, player);
    }

    private InteractionResult describe(Level level, BlockPos pos, Player player) {
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        var arrayCenter = XuanhuanStructures.findFormalSpiritArrayCenter(level, pos);
        if (arrayCenter.isPresent()) {
            return XuanhuanMachineBlock.openMachineAt(level, arrayCenter.get(), player);
        }
        ArcanaChunkField.Reading reading = Spirituality.reading(level, pos);
        player.sendSystemMessage(Component.translatable(messageKey, Spirituality.gradeName(reading.value()), reading.value())
                .withStyle(ChatFormatting.LIGHT_PURPLE));
        player.sendSystemMessage(Component.translatable("message.xuanhuan_earth.block.field",
                reading.mainSource(),
                ArcanaPower.format(reading.depletion())).withStyle(ChatFormatting.DARK_AQUA));
        return InteractionResult.SUCCESS_SERVER;
    }
}
