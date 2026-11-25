package com.xxsx.earth.xuanxue.settlement;

import com.xxsx.earth.xuanxue.XuanhuanEarth;
import net.minecraft.core.BlockPos;
import com.xxsx.earth.xuanxue.particle.XuanhuanParticles;
import net.minecraft.network.chat.Component;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jspecify.annotations.Nullable;

public final class XuanhuanSettlementAnchorBlock extends Block implements EntityBlock {
    public XuanhuanSettlementAnchorBlock(Properties properties) {
        super(properties);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new XuanhuanSettlementAnchorBlockEntity(pos, state);
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
                                                                            BlockEntityType<T> type) {
        if (level.isClientSide() || type != XuanhuanEarth.SETTLEMENT_ANCHOR_BLOCK_ENTITY.get()) {
            return null;
        }
        return (tickerLevel, pos, tickerState, blockEntity) ->
                XuanhuanSettlementAnchorBlockEntity.serverTick(tickerLevel, pos, tickerState,
                        (XuanhuanSettlementAnchorBlockEntity) blockEntity);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
                                               Player player, BlockHitResult hitResult) {
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof XuanhuanSettlementAnchorBlockEntity anchor) {
            player.sendSystemMessage(Component.translatable(
                    "message.earth_xuanxue.settlement.anchor",
                    Component.translatable("settlement.earth_xuanxue." + anchor.settlementType())));
        }
        return level.isClientSide() ? InteractionResult.SUCCESS : InteractionResult.SUCCESS_SERVER;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (random.nextInt(4) == 0) {
            level.addParticle(XuanhuanParticles.QI_MOTE.get(), pos.getX() + 0.5D, pos.getY() + 1.05D,
                    pos.getZ() + 0.5D, 0.0D, 0.025D, 0.0D);
        }
        if (random.nextInt(90) == 0) {
            level.addParticle(XuanhuanParticles.RUNE_GLYPH_A.get(), pos.getX() + 0.5D,
                    pos.getY() + 1.2D, pos.getZ() + 0.5D, 0.0D, 0.0D, 0.0D);
        }
    }
}
