package com.xxsx.xuanhuanearth.client;

import com.xxsx.xuanhuanearth.XuanhuanEarth;
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
 * (category badge, acquisition path, core mechanics/numbers, and progression links).
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
        String slug = id.getPath();
        String base = "tooltip.xuanhuan_earth.detail." + slug;
        if (!I18n.exists(base + ".category")) {
            return;
        }

        List<Component> lines = event.getToolTip();
        // Category tag right beneath item display name
        int insertAt = Math.min(1, lines.size());
        lines.add(insertAt, Component.literal("[" + I18n.get(base + ".category") + "]")
                .withStyle(ChatFormatting.GOLD));

        if (Minecraft.getInstance().hasShiftDown()) {
            if (I18n.exists(base + ".acquire")) {
                lines.add(Component.translatable("tooltip.xuanhuan_earth.ui.acquire",
                        Component.translatable(base + ".acquire").withStyle(ChatFormatting.GRAY))
                        .withStyle(ChatFormatting.YELLOW));
            }
            if (I18n.exists(base + ".mechanics")) {
                lines.add(Component.translatable("tooltip.xuanhuan_earth.ui.mechanics",
                        Component.translatable(base + ".mechanics").withStyle(ChatFormatting.WHITE))
                        .withStyle(ChatFormatting.AQUA));
            }
            if (I18n.exists(base + ".progression")) {
                lines.add(Component.translatable("tooltip.xuanhuan_earth.ui.progression",
                        Component.translatable(base + ".progression").withStyle(ChatFormatting.GRAY))
                        .withStyle(ChatFormatting.GREEN));
            }
        } else {
            lines.add(Component.translatable("tooltip.xuanhuan_earth.ui.hold_shift")
                    .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
        }
    }
}
