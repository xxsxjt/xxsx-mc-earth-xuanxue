package com.xxsx.earth.xuanxue;

import com.xxsx.earth.xuanxue.particle.XuanhuanParticles;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

/**
 * LiquidBlock for Spirit Spring fluid with finite bottle charges (3 glass bottles = 1 full source = 1 bucket).
 * Prevents any infinite-water or bucket-bottle duplication loops.
 */
public class SpiritSpringFluidBlock extends LiquidBlock {
    public static final int MAX_BOTTLE_CHARGES = 3;
    public static final IntegerProperty CHARGES = IntegerProperty.create("charges", 1, MAX_BOTTLE_CHARGES);

    public SpiritSpringFluidBlock(FlowingFluid fluid, Properties properties) {
        super(fluid, properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(LEVEL, 0)
                .setValue(CHARGES, MAX_BOTTLE_CHARGES));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(CHARGES);
    }

    @Override
    public ItemStack pickupBlock(LivingEntity entity, LevelAccessor level, BlockPos pos, BlockState state) {
        // Only a full (3/3) source block can be scooped into a full Spirit Spring Bucket.
        if (state.getValue(LEVEL) == 0 && state.getValue(CHARGES) == MAX_BOTTLE_CHARGES) {
            return super.pickupBlock(entity, level, pos, state);
        }
        return ItemStack.EMPTY;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (state.getValue(LEVEL) == 0 && random.nextInt(5) == 0) {
            double x = pos.getX() + 0.15D + random.nextDouble() * 0.7D;
            double y = pos.getY() + 0.9D;
            double z = pos.getZ() + 0.15D + random.nextDouble() * 0.7D;
            level.addParticle(XuanhuanParticles.QI_MOTE.get(), x, y, z, 0.0D, 0.025D, 0.0D);
        }
    }

    /**
     * Intercepts vanilla Glass Bottle right-click against a Spirit Spring fluid source block,
     * filling one {@code earth_xuanxue:spirit_spring_bottle} and decrementing source charges (3 -> 2 -> 1 -> air).
     */
    public static void onGlassBottleUse(PlayerInteractEvent.RightClickItem event) {
        ItemStack stack = event.getItemStack();
        if (!stack.is(Items.GLASS_BOTTLE)) {
            return;
        }
        Player player = event.getEntity();
        Level level = event.getLevel();
        BlockHitResult hit = Item.getPlayerPOVHitResult(level, player, ClipContext.Fluid.SOURCE_ONLY);
        if (hit.getType() != HitResult.Type.BLOCK) {
            return;
        }
        BlockPos pos = hit.getBlockPos();
        BlockState state = level.getBlockState(pos);
        if (!(state.getBlock() instanceof SpiritSpringFluidBlock) || state.getValue(LEVEL) != 0) {
            return;
        }
        if (!level.mayInteract(player, pos)) {
            return;
        }
        InteractionHand hand = event.getHand();
        if (!level.isClientSide()) {
            int charges = state.getValue(CHARGES);
            if (charges <= 1) {
                level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
            } else {
                level.setBlock(pos, state.setValue(CHARGES, charges - 1), Block.UPDATE_ALL);
            }
            level.playSound(null, pos, SoundEvents.BOTTLE_FILL, SoundSource.BLOCKS, 1.0F, 1.12F);
            ItemStack filled = new ItemStack(XuanhuanEarth.SPIRIT_SPRING_BOTTLE.get());
            ItemStack result = ItemUtils.createFilledResult(stack, player, filled);
            player.setItemInHand(hand, result);
            if (level instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(XuanhuanParticles.QI_MOTE.get(),
                        pos.getX() + 0.5D, pos.getY() + 0.85D, pos.getZ() + 0.5D, 5, 0.2D, 0.15D, 0.2D, 0.02D);
            }
        }
        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.SUCCESS);
    }
}
