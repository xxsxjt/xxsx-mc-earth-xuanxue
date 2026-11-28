package com.xxsx.earth.xuanxue;

import com.xxsx.earth.core.api.v1.EarthCoreApi;
import com.xxsx.earth.core.api.v1.FluidMixtureApi;
import com.xxsx.earth.core.component.SolutionComposition;
import com.xxsx.earth.xuanxue.particle.XuanhuanParticles;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
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
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import org.jetbrains.annotations.Nullable;

/**
 * LiquidBlock for Spirit Spring fluid:
 * - Finite bottle charges (3 glass bottles = 1 full source = 1 bucket; never infinite water)
 * - Discrete Qi concentration levels (1=25%, 2=50%, 3=75%, 4=100%)
 * - Miscible diffusion with adjacent minecraft:water via EarthCoreApi.fluids().diffuseStep(...)
 * - Immersion status effects: SPIRIT_SPRING_NOURISH for matching cultivators, QI_OVERLOAD for
 *   non-cultivators or cultivators soaking in over-concentrated spring water.
 */
public class SpiritSpringFluidBlock extends LiquidBlock {
    public static final String SOLUTE_SPIRIT_QI = "earth_xuanxue:spirit_qi";
    public static final int MAX_BOTTLE_CHARGES = 3;
    public static final int MAX_CONCENTRATION = 4;
    public static final IntegerProperty CHARGES = IntegerProperty.create("charges", 1, MAX_BOTTLE_CHARGES);
    public static final IntegerProperty CONCENTRATION = IntegerProperty.create("concentration", 1, MAX_CONCENTRATION);

    public SpiritSpringFluidBlock(FlowingFluid fluid, Properties properties) {
        super(fluid, properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(LEVEL, 0)
                .setValue(CHARGES, MAX_BOTTLE_CHARGES)
                .setValue(CONCENTRATION, MAX_CONCENTRATION));
    }

    public static int concentrationFromPermille(int permille) {
        return Math.max(1, Math.min(MAX_CONCENTRATION, Math.round(Math.max(250, permille) / 250.0F)));
    }

    public static int requiredRealmForConcentration(int concentration) {
        return switch (Math.max(1, Math.min(MAX_CONCENTRATION, concentration))) {
            case 1, 2 -> 1;
            case 3 -> 2;
            default -> 3;
        };
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(CHARGES, CONCENTRATION);
    }

    @Override
    protected boolean isRandomlyTicking(BlockState state) {
        return state.getValue(LEVEL) == 0;
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        if (!level.isClientSide() && state.getValue(LEVEL) == 0) {
            level.scheduleTick(pos, this, 12);
        }
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock,
                                   @Nullable Orientation orientation, boolean movedByPiston) {
        super.neighborChanged(state, level, pos, neighborBlock, orientation, movedByPiston);
        if (!level.isClientSide() && state.getValue(LEVEL) == 0) {
            level.scheduleTick(pos, this, 12);
        }
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        super.tick(state, level, pos, random);
        tryDiffuseWithNeighbors(state, level, pos);
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        super.randomTick(state, level, pos, random);
        tryDiffuseWithNeighbors(state, level, pos);
    }

    private void tryDiffuseWithNeighbors(BlockState state, ServerLevel level, BlockPos pos) {
        if (state.getValue(LEVEL) != 0) {
            return;
        }
        int myConc = state.getValue(CONCENTRATION);
        if (myConc <= 1) {
            return;
        }
        for (Direction dir : Direction.Plane.HORIZONTAL) {
            BlockPos nPos = pos.relative(dir);
            BlockState nState = level.getBlockState(nPos);
            if (nState.is(Blocks.WATER) && nState.getFluidState().isSource()) {
                FluidMixtureApi.DiffusionStep step = EarthCoreApi.fluids().diffuseStep(myConc, 0, MAX_CONCENTRATION);
                if (step.changed() && step.nextA() >= 1 && step.nextB() >= 1) {
                    level.setBlock(pos, state.setValue(CONCENTRATION, step.nextA()), Block.UPDATE_ALL);
                    level.setBlock(nPos, this.defaultBlockState()
                            .setValue(LEVEL, 0)
                            .setValue(CHARGES, MAX_BOTTLE_CHARGES)
                            .setValue(CONCENTRATION, step.nextB()), Block.UPDATE_ALL);
                    level.scheduleTick(pos, this, 18);
                    level.scheduleTick(nPos, this, 18);
                    level.sendParticles(XuanhuanParticles.QI_MOTE.get(),
                            (pos.getX() + nPos.getX()) * 0.5D + 0.5D, pos.getY() + 0.85D,
                            (pos.getZ() + nPos.getZ()) * 0.5D + 0.5D,
                            4, 0.2D, 0.1D, 0.2D, 0.015D);
                    return;
                }
            } else if (nState.getBlock() instanceof SpiritSpringFluidBlock && nState.getValue(LEVEL) == 0) {
                int nConc = nState.getValue(CONCENTRATION);
                FluidMixtureApi.DiffusionStep step = EarthCoreApi.fluids().diffuseStep(myConc, nConc, MAX_CONCENTRATION);
                if (step.changed() && step.nextA() >= 1 && step.nextB() >= 1) {
                    level.setBlock(pos, state.setValue(CONCENTRATION, step.nextA()), Block.UPDATE_ALL);
                    level.setBlock(nPos, nState.setValue(CONCENTRATION, step.nextB()), Block.UPDATE_ALL);
                    level.scheduleTick(pos, this, 18);
                    level.scheduleTick(nPos, this, 18);
                    return;
                }
            }
        }
    }

    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity,
                                InsideBlockEffectApplier effectApplier, boolean intersects) {
        super.entityInside(state, level, pos, entity, effectApplier, intersects);
        if (level.isClientSide() || !(entity instanceof LivingEntity living)) {
            return;
        }
        if (living.tickCount % 15 != 0) {
            return;
        }
        int conc = state.getValue(CONCENTRATION);
        int concPct = conc * 25;
        if (living instanceof Player player) {
            if (player.isCreative() || player.isSpectator()) {
                player.addEffect(new MobEffectInstance(
                        XuanhuanEarth.SPIRIT_SPRING_NOURISH, 80, Math.max(0, conc - 1), true, false, true));
                return;
            }
            int realm = ArcanaPower.getRealm(player);
            int cultLevel = ArcanaPower.getCultivationLevel(player);
            int effectiveStage = Math.max(realm, cultLevel >= 1 ? 1 : 0);
            int required = requiredRealmForConcentration(conc);

            if (effectiveStage <= 0) {
                int amp = Math.max(0, conc - 1);
                player.addEffect(new MobEffectInstance(XuanhuanEarth.QI_OVERLOAD, 100, amp, false, true, true));
                if (player.tickCount % 60 == 0) {
                    if (player instanceof ServerPlayer sp) sp.sendSystemMessage(Component.translatable(
                            "msg.earth_xuanxue.spirit_spring_mortal_overload", concPct
                    ).withStyle(ChatFormatting.RED), true);
                }
            } else if (effectiveStage < required) {
                int amp = Math.max(0, required - effectiveStage);
                player.addEffect(new MobEffectInstance(XuanhuanEarth.QI_OVERLOAD, 100, amp, false, true, true));
                if (player.tickCount % 60 == 0) {
                    if (player instanceof ServerPlayer sp) sp.sendSystemMessage(Component.translatable(
                            "msg.earth_xuanxue.spirit_spring_too_concentrated", concPct, required
                    ).withStyle(ChatFormatting.LIGHT_PURPLE), true);
                }
            } else {
                player.removeEffect(XuanhuanEarth.QI_OVERLOAD);
                player.addEffect(new MobEffectInstance(
                        XuanhuanEarth.SPIRIT_SPRING_NOURISH, 90, Math.max(0, conc - 1), true, false, true));
                if (player.tickCount % 75 == 0) {
                    if (player instanceof ServerPlayer sp) sp.sendSystemMessage(Component.translatable(
                            "msg.earth_xuanxue.spirit_spring_nourish", concPct
                    ).withStyle(ChatFormatting.AQUA), true);
                }
            }
        }
    }

    @Override
    public ItemStack pickupBlock(@Nullable LivingEntity entity, LevelAccessor level, BlockPos pos, BlockState state) {
        if (state.getValue(LEVEL) == 0 && state.getValue(CHARGES) == MAX_BOTTLE_CHARGES) {
            int conc = state.getValue(CONCENTRATION);
            ItemStack picked = super.pickupBlock(entity, level, pos, state);
            if (!picked.isEmpty()) {
                EarthCoreApi.fluids().writeComposition(picked, SolutionComposition.singleSolute(
                        "minecraft:water", SOLUTE_SPIRIT_QI, conc * 250, 1000, 295));
            }
            return picked;
        }
        return ItemStack.EMPTY;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        int conc = state.getValue(CONCENTRATION);
        int chance = Math.max(2, 7 - conc);
        if (state.getValue(LEVEL) == 0 && random.nextInt(chance) == 0) {
            double x = pos.getX() + 0.15D + random.nextDouble() * 0.7D;
            double y = pos.getY() + 0.9D;
            double z = pos.getZ() + 0.15D + random.nextDouble() * 0.7D;
            level.addParticle(XuanhuanParticles.QI_MOTE.get(), x, y, z, 0.0D, 0.025D, 0.0D);
        }
    }

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
            int conc = state.getValue(CONCENTRATION);
            if (charges <= 1) {
                level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
            } else {
                level.setBlock(pos, state.setValue(CHARGES, charges - 1), Block.UPDATE_ALL);
            }
            level.playSound(null, pos, SoundEvents.BOTTLE_FILL, SoundSource.BLOCKS, 1.0F, 1.12F);
            ItemStack filled = new ItemStack(XuanhuanEarth.SPIRIT_SPRING_BOTTLE.get());
            EarthCoreApi.fluids().writeComposition(filled, SolutionComposition.singleSolute(
                    "minecraft:water", SOLUTE_SPIRIT_QI, conc * 250, 333, 295));
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
