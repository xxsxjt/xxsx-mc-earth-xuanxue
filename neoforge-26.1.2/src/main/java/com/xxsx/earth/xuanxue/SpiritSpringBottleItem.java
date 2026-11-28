package com.xxsx.earth.xuanxue;

import com.xxsx.earth.core.api.v1.EarthCoreApi;
import com.xxsx.earth.core.component.SolutionComposition;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Bottled Spirit Spring water carrying EarthCore SolutionComposition.
 * - Right-click a water cauldron to dilute concentration by 25% (100% -> 75% -> 50% -> 25%).
 * - Right-click in air to drink: cultivators gain Qi & 「灵泉浸润」, while mortals or
 *   under-realm cultivators drinking overly concentrated water suffer 「灵气逆冲」.
 */
public final class SpiritSpringBottleItem extends GuidedXuanhuanItem {

    public SpiritSpringBottleItem(Properties properties, String hintKey) {
        super(properties, hintKey);
    }

    public static SolutionComposition defaultComposition() {
        return SolutionComposition.singleSolute(
                "minecraft:water",
                SpiritSpringFluidBlock.SOLUTE_SPIRIT_QI,
                SpiritSpringFluidBlock.MAX_CONCENTRATION * 250,
                333,
                295
        );
    }

    public static int readConcentration(ItemStack stack) {
        var opt = EarthCoreApi.fluids().readComposition(stack);
        if (opt.isEmpty()) {
            return SpiritSpringFluidBlock.MAX_CONCENTRATION;
        }
        int permille = opt.get().concentrationOf(SpiritSpringFluidBlock.SOLUTE_SPIRIT_QI);
        return SpiritSpringFluidBlock.concentrationFromPermille(permille);
    }

    @Override
    public InteractionResult useOn(UseOnContext ctx) {
        Level level = ctx.getLevel();
        BlockPos pos = ctx.getClickedPos();
        BlockState state = level.getBlockState(pos);
        Player player = ctx.getPlayer();
        if (player != null && state.is(Blocks.WATER_CAULDRON)) {
            ItemStack held = ctx.getItemInHand();
            int conc = readConcentration(held);
            if (conc > 1) {
                if (!level.isClientSide()) {
                    int nextConc = conc - 1;
                    ItemStack diluted = new ItemStack(XuanhuanEarth.SPIRIT_SPRING_BOTTLE.get());
                    EarthCoreApi.fluids().writeComposition(diluted, SolutionComposition.singleSolute(
                            "minecraft:water",
                            SpiritSpringFluidBlock.SOLUTE_SPIRIT_QI,
                            nextConc * 250,
                            333,
                            295
                    ));
                    if (!player.getAbilities().instabuild) {
                        held.shrink(1);
                    }
                    if (!player.getInventory().add(diluted)) {
                        player.drop(diluted, false);
                    }
                    LayeredCauldronBlock.lowerFillLevel(state, level, pos);
                    level.playSound(null, pos, SoundEvents.BOTTLE_FILL, SoundSource.BLOCKS, 0.9F, 1.2F);
                    if (player instanceof ServerPlayer sp) {
                        sp.sendSystemMessage(Component.translatable(
                                "msg.earth_xuanxue.spirit_spring_bottle_diluted",
                                conc * 25, nextConc * 25
                        ).withStyle(ChatFormatting.AQUA), true);
                    }
                }
                return InteractionResult.SUCCESS;
            }
        }
        return super.useOn(ctx);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide()) {
            int conc = readConcentration(stack);
            int concPct = conc * 25;
            int realm = ArcanaPower.getRealm(player);
            int cultLevel = ArcanaPower.getCultivationLevel(player);
            int effectiveStage = Math.max(realm, cultLevel >= 1 ? 1 : 0);
            int required = SpiritSpringFluidBlock.requiredRealmForConcentration(conc);

            if (!player.isCreative() && !player.isSpectator() && (effectiveStage <= 0 || effectiveStage < required)) {
                int amp = effectiveStage <= 0 ? Math.max(0, conc - 1) : Math.max(0, required - effectiveStage);
                player.addEffect(new MobEffectInstance(XuanhuanEarth.QI_OVERLOAD, 140, amp, false, true, true));
                if (player instanceof ServerPlayer sp) {
                    sp.sendSystemMessage(Component.translatable(
                            "msg.earth_xuanxue.spirit_spring_bottle_overload", concPct
                    ).withStyle(ChatFormatting.RED), true);
                }
            } else {
                double gain = 18.0D * conc;
                ArcanaPower.setCurrentMana(player, ArcanaPower.getCurrentMana(player) + gain);
                player.addEffect(new MobEffectInstance(
                        XuanhuanEarth.SPIRIT_SPRING_NOURISH, 180, Math.max(0, conc - 1), true, false, true));
                if (player instanceof ServerPlayer sp) {
                    sp.sendSystemMessage(Component.translatable(
                            "msg.earth_xuanxue.spirit_spring_bottle_drink", concPct, (int) gain
                    ).withStyle(ChatFormatting.AQUA), true);
                }
            }

            level.playSound(null, player.blockPosition(), SoundEvents.GENERIC_DRINK.value(),
                    SoundSource.PLAYERS, 0.85F, 1.08F);
            player.getCooldowns().addCooldown(stack, 20);
            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
                ItemStack emptyBottle = new ItemStack(Items.GLASS_BOTTLE);
                if (!player.getInventory().add(emptyBottle)) {
                    player.drop(emptyBottle, false);
                }
            }
        }
        return InteractionResult.SUCCESS;
    }
}
