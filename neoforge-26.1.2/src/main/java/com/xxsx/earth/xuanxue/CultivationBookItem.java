package com.xxsx.earth.xuanxue;

import java.util.List;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * Paper-and-ink cultivation tome: deliberately harder and more ceremonial to master than a jade slip.
 * Requires 4 chapters of focused, motionless reading in adequate light (>= 7), consuming mana and stamina
 * per chapter with a contemplation cooldown between chapters. Sitting on a meditation cushion or in a
 * high spiritual-field zone shortens each chapter's study duration.
 */
public class CultivationBookItem extends Item {
    public static final int MIN_LIGHT = 7;
    public static final int CHAPTER_TICKS_NORMAL = 80;
    public static final int CHAPTER_TICKS_CUSHION = 45;
    public static final double MANA_PER_CHAPTER = 8.0D;
    public static final float EXHAUSTION_PER_CHAPTER = 2.0F;
    public static final long CONTEMPLATION_COOLDOWN_TICKS = 120L;

    public enum Tome {
        QI_GUIDING("qi_guiding_book", List.of(
                XuanhuanLoreIntegration.K_XUA_01_QI_GUIDING,
                XuanhuanLoreIntegration.K_XUA_02_CIRCULATION)),
        BIGU("bigu_book", List.of(XuanhuanLoreIntegration.K_XUA_05_BIGU)),
        BODY_TEMPERING("body_tempering_book", List.of(XuanhuanLoreIntegration.K_XUA_03_BODY_TEMPERING)),
        FETAL_BREATH("fetal_breath_book", List.of(XuanhuanLoreIntegration.K_XUA_04_FETAL_BREATH));

        private final String id;
        private final List<Identifier> nodes;

        Tome(String id, List<Identifier> nodes) {
            this.id = id;
            this.nodes = nodes;
        }

        public String id() {
            return id;
        }

        public List<Identifier> nodes() {
            return nodes;
        }
    }

    private final Tome tome;

    public CultivationBookItem(Properties properties, Tome tome) {
        super(properties);
        this.tome = tome;
    }

    public Tome tome() {
        return tome;
    }

    private static CultivationBookState stateOf(ItemStack stack) {
        CultivationBookState state = stack.get(XuanhuanEarth.CULTIVATION_BOOK_STATE.get());
        return state != null ? state : CultivationBookState.EMPTY;
    }

    private static boolean hasFocusBonus(LivingEntity entity) {
        if (entity.getVehicle() instanceof MeditationSeatEntity) {
            return true;
        }
        if (entity.level() instanceof ServerLevel serverLevel) {
            return Spirituality.reading(serverLevel, entity.blockPosition()).value() >= 60.0D;
        }
        return false;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return hasFocusBonus(entity) ? CHAPTER_TICKS_CUSHION : CHAPTER_TICKS_NORMAL;
    }

    @Override
    public ItemUseAnimation getUseAnimation(ItemStack stack) {
        return ItemUseAnimation.BOW;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        CultivationBookState state = stateOf(stack);
        if (level.isClientSide()) {
            player.startUsingItem(hand);
            return InteractionResult.CONSUME;
        }
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResult.PASS;
        }
        if (XuanhuanLoreIntegration.knowsAll(serverPlayer, tome.nodes())) {
            serverPlayer.sendOverlayMessage(Component.translatable("message.earth_xuanxue.book.already_mastered")
                    .withStyle(ChatFormatting.GREEN));
            return InteractionResult.FAIL;
        }
        long now = level.getGameTime();
        if (state.nextStudyTick() > now) {
            long sec = Math.max(1L, (state.nextStudyTick() - now + 19L) / 20L);
            serverPlayer.sendOverlayMessage(Component.translatable("message.earth_xuanxue.book.contemplating", sec)
                    .withStyle(ChatFormatting.YELLOW));
            return InteractionResult.FAIL;
        }
        BlockPos pos = player.blockPosition();
        if (level.getMaxLocalRawBrightness(pos) < MIN_LIGHT) {
            serverPlayer.sendOverlayMessage(Component.translatable("message.earth_xuanxue.book.too_dark", MIN_LIGHT)
                    .withStyle(ChatFormatting.RED));
            return InteractionResult.FAIL;
        }
        if (!ArcanaPower.canSpendMana(player, MANA_PER_CHAPTER)) {
            serverPlayer.sendOverlayMessage(Component.translatable("message.earth_xuanxue.book.low_mana",
                    ArcanaPower.format(MANA_PER_CHAPTER)).withStyle(ChatFormatting.RED));
            return InteractionResult.FAIL;
        }
        if (player.getFoodData().getFoodLevel() <= 4) {
            serverPlayer.sendOverlayMessage(Component.translatable("message.earth_xuanxue.book.too_hungry")
                    .withStyle(ChatFormatting.RED));
            return InteractionResult.FAIL;
        }
        player.startUsingItem(hand);
        int chapter = Math.min(CultivationBookState.TOTAL_CHAPTERS, state.chaptersRead() + 1);
        serverPlayer.sendOverlayMessage(Component.translatable("message.earth_xuanxue.book.reading_chapter",
                chapter, CultivationBookState.TOTAL_CHAPTERS).withStyle(ChatFormatting.AQUA));
        return InteractionResult.CONSUME;
    }

    @Override
    public void onUseTick(Level level, LivingEntity entity, ItemStack stack, int remainingUseDuration) {
        if (level.isClientSide() || !(entity instanceof ServerPlayer player)) {
            return;
        }
        Vec3 delta = player.getDeltaMovement();
        boolean moved = (delta.x * delta.x + delta.z * delta.z) > 0.0025D || player.hurtTime > 0;
        if (moved && !(player.getVehicle() instanceof MeditationSeatEntity)) {
            player.stopUsingItem();
            player.sendOverlayMessage(Component.translatable("message.earth_xuanxue.book.interrupted")
                    .withStyle(ChatFormatting.RED));
            return;
        }
        if (remainingUseDuration % 20 == 0) {
            level.playSound(null, player.blockPosition(), SoundEvents.BOOK_PAGE_TURN, SoundSource.PLAYERS, 0.55F, 0.95F);
        }
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (level.isClientSide() || !(entity instanceof ServerPlayer player)) {
            return stack;
        }
        if (level.getMaxLocalRawBrightness(player.blockPosition()) < MIN_LIGHT) {
            player.sendOverlayMessage(Component.translatable("message.earth_xuanxue.book.too_dark", MIN_LIGHT)
                    .withStyle(ChatFormatting.RED));
            return stack;
        }
        if (!ArcanaPower.trySpendMana(player, MANA_PER_CHAPTER)) {
            player.sendOverlayMessage(Component.translatable("message.earth_xuanxue.book.low_mana",
                    ArcanaPower.format(MANA_PER_CHAPTER)).withStyle(ChatFormatting.RED));
            return stack;
        }
        player.causeFoodExhaustion(EXHAUSTION_PER_CHAPTER);
        CultivationBookState next = stateOf(stack).advance(level.getGameTime() + CONTEMPLATION_COOLDOWN_TICKS);
        stack.set(XuanhuanEarth.CULTIVATION_BOOK_STATE.get(), next);
        level.playSound(null, player.blockPosition(), SoundEvents.BOOK_PAGE_TURN, SoundSource.PLAYERS, 0.9F, 1.1F);

        if (next.completed()) {
            for (Identifier nodeId : tome.nodes()) {
                XuanhuanLoreIntegration.grantNode(player, nodeId);
            }
            if (level instanceof ServerLevel serverLevel) {
                XuanhuanEffects.stoneCharge(serverLevel, player.position());
            }
            player.sendSystemMessage(Component.translatable("message.earth_xuanxue.book.mastered",
                    stack.getHoverName()).withStyle(ChatFormatting.GOLD));
        } else {
            player.sendSystemMessage(Component.translatable("message.earth_xuanxue.book.chapter_done",
                    next.chaptersRead(), CultivationBookState.TOTAL_CHAPTERS).withStyle(ChatFormatting.GREEN));
        }
        return stack;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display,
                                Consumer<Component> lines, TooltipFlag flag) {
        CultivationBookState state = stateOf(stack);
        lines.accept(Component.translatable("tooltip.earth_xuanxue.book.progress",
                state.chaptersRead(), CultivationBookState.TOTAL_CHAPTERS)
                .withStyle(state.completed() ? ChatFormatting.GREEN : ChatFormatting.AQUA));
        lines.accept(Component.translatable("tooltip.earth_xuanxue." + tome.id).withStyle(ChatFormatting.GOLD));
        lines.accept(Component.translatable("tooltip.earth_xuanxue.book.requirements").withStyle(ChatFormatting.GRAY));
    }
}
