package com.xxsx.xuanhuanearth;

import com.xxsx.earthlore.api.LoreApi;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.material.FluidState;

import java.util.Optional;
import java.util.function.Consumer;

/**
 * X-070 Geomantic Luopan: evaluates local site qi (1..9 rating), gated by readout node K-XUA-28
 * for detailed sub-scores and nearest spirit_vein_node within 48 blocks.
 */
public class LuopanItem extends GuidedXuanhuanItem {
    public static final int COOLDOWN_TICKS = 100;
    public static final int VEIN_SEARCH_RADIUS = 48;

    public record Evaluation(double fieldPart, int waterPart, int mountainPart, int openPart, double score, int rating) {
    }

    public LuopanItem(Properties properties, String hintKey) {
        super(properties, hintKey);
    }

    public static Evaluation computeEvaluation(int fieldValue, int nearestWaterDist, int northRise, boolean openSky) {
        int clampedField = Math.max(0, Math.min(100, fieldValue));
        double fieldPart = 0.55D * clampedField;
        int waterPart = nearestWaterDist <= 16 ? 15 : (nearestWaterDist <= 32 ? 8 : 0);
        int mountainPart = northRise >= 8 ? 15 : (northRise >= 4 ? 8 : 0);
        int openPart = openSky ? 15 : 0;
        double score = Math.max(0.0D, Math.min(100.0D, fieldPart + waterPart + mountainPart + openPart));
        int rating = Math.max(1, Math.min(9, 1 + (int) Math.floor(score / 12.5D)));
        return new Evaluation(fieldPart, waterPart, mountainPart, openPart, score, rating);
    }

    public static Evaluation evaluateAt(Level level, BlockPos pos) {
        int fieldVal = Spirituality.measure(level, pos);
        int waterDist = findWaterSourceDistance(level, pos);
        int northRise = measureNorthMountainRise(level, pos);
        boolean openSky = level.canSeeSky(pos.above());
        return computeEvaluation(fieldVal, waterDist, northRise, openSky);
    }

    private static int findWaterSourceDistance(Level level, BlockPos origin) {
        for (int dx = -16; dx <= 16; dx++) {
            for (int dz = -16; dz <= 16; dz++) {
                if (!level.isLoaded(origin.offset(dx, 0, dz))) {
                    continue;
                }
                for (int dy = -4; dy <= 4; dy++) {
                    FluidState fluid = level.getFluidState(origin.offset(dx, dy, dz));
                    if (fluid.is(FluidTags.WATER) && fluid.isSource()) {
                        return Math.max(Math.abs(dx), Math.abs(dz));
                    }
                }
            }
        }
        for (int dx = -32; dx <= 32; dx++) {
            for (int dz = -32; dz <= 32; dz++) {
                if (Math.abs(dx) <= 16 && Math.abs(dz) <= 16) {
                    continue;
                }
                if (!level.isLoaded(origin.offset(dx, 0, dz))) {
                    continue;
                }
                for (int dy = -4; dy <= 4; dy++) {
                    FluidState fluid = level.getFluidState(origin.offset(dx, dy, dz));
                    if (fluid.is(FluidTags.WATER) && fluid.isSource()) {
                        return Math.max(Math.abs(dx), Math.abs(dz));
                    }
                }
            }
        }
        return Integer.MAX_VALUE;
    }

    private static int measureNorthMountainRise(Level level, BlockPos origin) {
        int maxRise = 0;
        for (int dz = -16; dz <= -1; dz++) {
            for (int dx = -4; dx <= 4; dx++) {
                BlockPos col = origin.offset(dx, 0, dz);
                if (!level.isLoaded(col)) {
                    continue;
                }
                int surfaceY = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, col.getX(), col.getZ()) - 1;
                maxRise = Math.max(maxRise, surfaceY - origin.getY());
            }
        }
        return maxRise;
    }

    public static String octantKey(int dx, int dz) {
        if (dx == 0 && dz == 0) {
            return "direction.xuanhuan_earth.here";
        }
        double angle = Math.toDegrees(Math.atan2(dx, -dz));
        if (angle < 0.0D) {
            angle += 360.0D;
        }
        int index = (int) Math.floor((angle + 22.5D) / 45.0D) % 8;
        return switch (index) {
            case 0 -> "direction.xuanhuan_earth.n";
            case 1 -> "direction.xuanhuan_earth.ne";
            case 2 -> "direction.xuanhuan_earth.e";
            case 3 -> "direction.xuanhuan_earth.se";
            case 4 -> "direction.xuanhuan_earth.s";
            case 5 -> "direction.xuanhuan_earth.sw";
            case 6 -> "direction.xuanhuan_earth.w";
            default -> "direction.xuanhuan_earth.nw";
        };
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.getCooldowns().isOnCooldown(stack)) {
            if (!level.isClientSide()) {
                player.sendSystemMessage(Component.translatable("message.xuanhuan_earth.luopan.cooldown")
                        .withStyle(ChatFormatting.YELLOW));
            }
            return InteractionResult.FAIL;
        }
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        player.getCooldowns().addCooldown(stack, COOLDOWN_TICKS);
        BlockPos pos = player.blockPosition();
        Evaluation eval = evaluateAt(level, pos);
        stack.set(XuanhuanEarth.LUOPAN_RATING.get(), eval.rating());

        player.sendSystemMessage(Component.translatable("message.xuanhuan_earth.luopan.rating", eval.rating())
                .withStyle(ChatFormatting.GOLD));

        if (player instanceof ServerPlayer serverPlayer && LoreApi.readoutLevel(serverPlayer, this) >= 1) {
            player.sendSystemMessage(Component.translatable(
                    "message.xuanhuan_earth.luopan.detail",
                    ArcanaPower.format(eval.fieldPart()),
                    eval.waterPart(),
                    eval.mountainPart(),
                    eval.openPart()).withStyle(ChatFormatting.AQUA));
            Optional<BlockPos> nearestVein = ArcanaChunkField.findNearestSpiritVein(level, pos, VEIN_SEARCH_RADIUS);
            if (nearestVein.isPresent()) {
                BlockPos vein = nearestVein.get();
                int dx = vein.getX() - pos.getX();
                int dz = vein.getZ() - pos.getZ();
                int dy = vein.getY() - pos.getY();
                int horiz = (int) Math.round(Math.hypot(dx, dz));
                player.sendSystemMessage(Component.translatable(
                        "message.xuanhuan_earth.luopan.vein",
                        Component.translatable(octantKey(dx, dz)),
                        horiz,
                        dy).withStyle(ChatFormatting.LIGHT_PURPLE));
            } else {
                player.sendSystemMessage(Component.translatable("message.xuanhuan_earth.luopan.no_vein")
                        .withStyle(ChatFormatting.GRAY));
            }
        }
        return InteractionResult.SUCCESS_SERVER;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display,
                                Consumer<Component> lines, TooltipFlag flag) {
        super.appendHoverText(stack, context, display, lines, flag);
        int last = stack.getOrDefault(XuanhuanEarth.LUOPAN_RATING.get(), 0);
        if (last > 0) {
            lines.accept(Component.translatable("tooltip.xuanhuan_earth.luopan.last_rating", last)
                    .withStyle(ChatFormatting.GOLD));
        }
    }
}
