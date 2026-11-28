package com.xxsx.earth.xuanxue.settlement;

import java.util.Optional;
import net.minecraft.ChatFormatting;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.entity.SignText;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.ValueInput;

/**
 * Worldgen-safe helper for populating {@link SignBlockEntity} front text.
 *
 * <p>During world generation ({@code WorldGenRegion} / {@code ProtoChunk}), newly created
 * {@link SignBlockEntity} instances have {@code level == null}. Calling
 * {@link SignBlockEntity#setText(SignText, boolean)} directly triggers
 * {@code SignBlockEntity.markUpdated()} -> {@code this.level.sendBlockUpdated(...)} and throws
 * a {@link NullPointerException}. When {@code !sign.hasLevel()}, we encode {@link SignText}
 * via {@link SignText#DIRECT_CODEC} and load it with {@link SignBlockEntity#loadCustomOnly(ValueInput)},
 * which assigns {@code frontText} cleanly without dereferencing {@code this.level}.</p>
 */
public final class SettlementSignHelper {
    private SettlementSignHelper() {
    }

    public static SignText buildSettlementSignText(SignText base, String settlementTitle) {
        return base
                .setHasGlowingText(true)
                .setMessage(0, Component.literal("—— 修行聚落 ——").withStyle(ChatFormatting.GOLD))
                .setMessage(1, Component.literal(settlementTitle).withStyle(ChatFormatting.YELLOW))
                .setMessage(2, Component.literal("灵气聚汇之地").withStyle(ChatFormatting.AQUA))
                .setMessage(3, Component.literal("Earth: Xuanxue").withStyle(ChatFormatting.GRAY));
    }

    public static Optional<CompoundTag> encodeFrontTextTag(HolderLookup.Provider registries, SignText frontText) {
        var ops = registries.createSerializationContext(NbtOps.INSTANCE);
        return SignText.DIRECT_CODEC.encodeStart(ops, frontText).result().map(encoded -> {
            CompoundTag tag = new CompoundTag();
            tag.put("front_text", encoded);
            return tag;
        });
    }

    public static void applyFrontTextSafely(WorldGenLevel level, SignBlockEntity sign, SignText frontText) {
        if (sign.hasLevel()) {
            sign.setText(frontText, true);
            return;
        }
        encodeFrontTextTag(level.registryAccess(), frontText).ifPresent(tag -> {
            ValueInput input = TagValueInput.create(ProblemReporter.DISCARDING, level.registryAccess(), tag);
            sign.loadCustomOnly(input);
        });
    }
}
