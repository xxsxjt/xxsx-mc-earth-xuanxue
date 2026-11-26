package com.xxsx.xuanhuanearth;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

/**
 * Natural variable-size Spirit Spring Stone (1..4): smaller stages are organic incomplete rocky outcrops,
 * while stage 4 is a complete 16x16x16 full block (no artificial hollow basin/groove).
 */
public final class SpiritSpringStoneBlock extends XuanhuanBlock {
    public static final IntegerProperty SIZE = IntegerProperty.create("size", 1, 4);

    private static final VoxelShape SHAPE_1 = Shapes.or(
            Block.box(3.5D, 0.0D, 3.5D, 12.5D, 6.5D, 12.5D),
            Block.box(5.0D, 6.5D, 5.0D, 11.0D, 9.0D, 11.0D));
    private static final VoxelShape SHAPE_2 = Shapes.or(
            Block.box(2.0D, 0.0D, 2.0D, 14.0D, 9.5D, 14.0D),
            Block.box(3.5D, 9.5D, 3.0D, 12.5D, 12.5D, 13.0D));
    private static final VoxelShape SHAPE_3 = Shapes.or(
            Block.box(1.0D, 0.0D, 1.0D, 15.0D, 12.5D, 15.0D),
            Block.box(2.0D, 12.5D, 2.0D, 14.0D, 15.0D, 14.0D));
    private static final VoxelShape SHAPE_4 = Shapes.block();

    public SpiritSpringStoneBlock(Properties properties, String messageKey) {
        super(properties, messageKey);
        this.registerDefaultState(this.stateDefinition.any().setValue(SIZE, 4));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(SIZE);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return switch (state.getValue(SIZE)) {
            case 1 -> SHAPE_1;
            case 2 -> SHAPE_2;
            case 3 -> SHAPE_3;
            default -> SHAPE_4;
        };
    }

    @Override
    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState existing = context.getLevel().getBlockState(context.getClickedPos());
        if (existing.is(this)) {
            int next = Math.min(4, existing.getValue(SIZE) + 1);
            return existing.setValue(SIZE, next);
        }
        // Sneaking places a guaranteed complete 16x16x16 full block; normal placement produces natural size variety (1..4)
        if (context.getPlayer() != null && context.getPlayer().isShiftKeyDown()) {
            return this.defaultBlockState().setValue(SIZE, 4);
        }
        BlockPos pos = context.getClickedPos();
        int hash = Math.abs(pos.getX() * 31 + pos.getY() * 17 + pos.getZ() * 13);
        int naturalSize = 1 + (hash % 4);
        return this.defaultBlockState().setValue(SIZE, naturalSize);
    }

    @Override
    protected boolean canBeReplaced(BlockState state, BlockPlaceContext context) {
        return !context.isSecondaryUseActive()
                && context.getItemInHand().is(this.asItem())
                && state.getValue(SIZE) < 4
                || super.canBeReplaced(state, context);
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                          Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (stack.is(this.asItem()) && state.getValue(SIZE) < 4) {
            if (!level.isClientSide()) {
                level.setBlock(pos, state.setValue(SIZE, state.getValue(SIZE) + 1), Block.UPDATE_ALL);
                if (!player.isCreative()) {
                    stack.shrink(1);
                }
                level.playSound(null, pos, SoundEvents.STONE_PLACE, SoundSource.BLOCKS, 0.9F, 1.05F);
            }
            return level.isClientSide() ? InteractionResult.SUCCESS : InteractionResult.SUCCESS_SERVER;
        }
        return super.useItemOn(stack, state, level, pos, player, hand, hitResult);
    }
}
