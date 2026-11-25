package com.xxsx.earth.xuanxue.client;

import com.xxsx.earth.xuanxue.ArcanaPower;
import com.xxsx.earth.xuanxue.CultivationStatusPayload;
import com.xxsx.earth.xuanxue.MeditationSeatEntity;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;

public final class MeditationHud {
    private static final String[] STAGES = {
            "hud.earth_xuanxue.meditation.stage.breath",
            "hud.earth_xuanxue.meditation.stage.gather",
            "hud.earth_xuanxue.meditation.stage.circulate",
            "hud.earth_xuanxue.meditation.stage.settle"
    };

    private MeditationHud() {
    }

    public static String stageKey(int stage) {
        return STAGES[Math.max(0, Math.min(STAGES.length - 1, stage))];
    }

    public static void render(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.screen != null
                || !(minecraft.player.getVehicle() instanceof MeditationSeatEntity seat)) {
            return;
        }

        Font font = minecraft.font;
        int panelWidth = Math.min(218, graphics.guiWidth() - 16);
        int panelHeight = 56;
        int x = (graphics.guiWidth() - panelWidth) / 2;
        int y = Math.max(8, graphics.guiHeight() - 82);

        CultivationStatusPayload status = XuanhuanEarthClient.cultivationStatus();
        float cycleTick = Math.max(0.0F,
                ArcanaPower.QI_MEDITATION_COOLDOWN_TICKS - status.remainingTicks());
        float progress = Math.min(1.0F, cycleTick / ArcanaPower.QI_MEDITATION_COOLDOWN_TICKS);
        int stage = Math.min(STAGES.length - 1, (int) (progress * STAGES.length));
        int remainingSeconds = Math.max(1, (status.remainingTicks() + 19) / 20);

        int border = 0xD86CCB8A;
        int panel = 0xD0161D18;
        int accent = 0xFF79E09A;
        int muted = 0xFFD1DDCF;

        graphics.fill(x, y, x + panelWidth, y + panelHeight, border);
        graphics.fill(x + 1, y + 1, x + panelWidth - 1, y + panelHeight - 1, panel);
        graphics.fill(x + 5, y + 17, x + panelWidth - 5, y + 18, 0xFF3A5944);
        graphics.fill(x + 5, y + 42, x + panelWidth - 5, y + 43, 0xFF3A5944);
        graphics.fill(x + 4, y + 5, x + 6, y + panelHeight - 5, 0xFF956215);
        graphics.fill(x + panelWidth - 6, y + 5, x + panelWidth - 4, y + panelHeight - 5, 0xFF956215);
        Component focus = Component.translatable(
                com.xxsx.earth.xuanxue.CultivationFocus.byId(status.focusId()).titleKey());
        Component title = fit(font, Component.translatable(
                "hud.earth_xuanxue.meditation.title", focus, status.focusLevel()), panelWidth - 18);
        graphics.centeredText(font, title, x + panelWidth / 2, y + 5, 0xFFF1FFF4);

        Component stageText = Component.translatable(stageKey(stage));
        Component remainingText = Component.translatable("hud.earth_xuanxue.meditation.remaining", remainingSeconds);
        graphics.text(font, stageText, x + 10, y + 21, accent, false);
        graphics.text(font, remainingText, x + panelWidth - 10 - font.width(remainingText), y + 21, muted, false);

        int lineX = x + 12;
        int lineY = y + 34;
        int lineWidth = panelWidth - 24;
        graphics.fill(lineX, lineY, lineX + lineWidth, lineY + 2, 0xFF344139);
        graphics.fill(lineX, lineY, lineX + Math.max(1, (int) (lineWidth * progress)), lineY + 2, accent);
        for (int i = 0; i < STAGES.length; i++) {
            int markerX = lineX + lineWidth * i / (STAGES.length - 1);
            int markerColor = i <= stage ? accent : 0xFF617065;
            graphics.fill(markerX - 3, lineY - 3, markerX + 4, lineY + 5, 0xFF203027);
            graphics.fill(markerX - 1, lineY - 1, markerX + 2, lineY + 3, markerColor);
        }
        graphics.centeredText(font, fit(font, Component.translatable("hud.earth_xuanxue.meditation.hint"), panelWidth - 18),
                x + panelWidth / 2, y + 45, muted);
    }

    private static Component fit(Font font, Component value, int width) {
        String text = value.getString();
        if (font.width(text) <= width) {
            return value;
        }
        return Component.literal(font.plainSubstrByWidth(text, Math.max(0, width - font.width("..."))) + "...");
    }
}
