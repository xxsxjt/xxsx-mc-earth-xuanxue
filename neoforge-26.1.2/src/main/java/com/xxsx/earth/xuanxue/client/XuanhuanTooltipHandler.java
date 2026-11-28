package com.xxsx.earth.xuanxue.client;

import com.xxsx.earth.core.api.v1.EarthCoreApi;
import com.xxsx.earth.core.api.v1.ForgingPresetApi;
import com.xxsx.earth.xuanxue.AstralHalberdItem;
import com.xxsx.earth.xuanxue.SpiritImplementItem;
import com.xxsx.earth.xuanxue.XuanhuanEarth;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

/**
 * Rich, structured Shift-expandable tooltips for every Earth: Xuanxue item & block
 * plus live EarthCore SolutionComposition & ForgingPresetApi material readouts.
 */
final class XuanhuanTooltipHandler {
    private XuanhuanTooltipHandler() {
    }

    static void onItemTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        if (stack.isEmpty()) {
            return;
        }
        Identifier id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        if (id == null || !XuanhuanEarth.MODID.equals(id.getNamespace())) {
            return;
        }
        List<Component> lines = event.getToolTip();
        EarthCoreApi.fluids().appendCompositionTooltip(stack, lines::add);

        if (stack.getItem() instanceof AstralHalberdItem || stack.getItem() instanceof SpiritImplementItem) {
            ForgingPresetApi.MaterialProfile profile = EarthCoreApi.forging().resolveProfile(stack);
            lines.add(Component.translatable(
                    "tooltip.earth_xuanxue.forging_profile",
                    profile.displayNameZh(),
                    String.format("%.2f", profile.qiAffinity()),
                    String.format("+%.1f", profile.attackBonus())
            ).withStyle(profile.fromFallbackPreset() ? ChatFormatting.DARK_AQUA : ChatFormatting.GOLD));
        }

        String slug = id.getPath();
        String base = "tooltip.earth_xuanxue.detail." + slug;
        if (!I18n.exists(base + ".category")) {
            return;
        }
        int insertAt = Math.min(1, lines.size());
        lines.add(insertAt, Component.literal("[" + I18n.get(base + ".category") + "]")
                .withStyle(ChatFormatting.GOLD));

        if (Minecraft.getInstance().hasShiftDown()) {
            if (I18n.exists(base + ".acquire")) {
                lines.add(Component.translatable("tooltip.earth_xuanxue.ui.acquire",
                        Component.translatable(base + ".acquire").withStyle(ChatFormatting.GRAY))
                        .withStyle(ChatFormatting.YELLOW));
            }
            if (I18n.exists(base + ".mechanics")) {
                lines.add(Component.translatable("tooltip.earth_xuanxue.ui.mechanics",
                        Component.translatable(base + ".mechanics").withStyle(ChatFormatting.WHITE))
                        .withStyle(ChatFormatting.AQUA));
            }
            if (I18n.exists(base + ".progression")) {
                lines.add(Component.translatable("tooltip.earth_xuanxue.ui.progression",
                        Component.translatable(base + ".progression").withStyle(ChatFormatting.GRAY))
                        .withStyle(ChatFormatting.GREEN));
            }
        } else {
            lines.add(Component.translatable("tooltip.earth_xuanxue.ui.hold_shift")
                    .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
        }
    }
}
