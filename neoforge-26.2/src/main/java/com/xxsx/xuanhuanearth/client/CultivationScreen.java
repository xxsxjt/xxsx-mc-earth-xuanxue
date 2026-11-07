package com.xxsx.xuanhuanearth.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.xxsx.earthlore.client.LoreTreeScreen;
import com.xxsx.xuanhuanearth.ArcanaPower;
import com.xxsx.xuanhuanearth.CultivationFocus;
import com.xxsx.xuanhuanearth.CultivationStatusPayload;
import com.xxsx.xuanhuanearth.RealmBreakthrough;
import com.xxsx.xuanhuanearth.Spirituality;
import com.xxsx.xuanhuanearth.XuanhuanLoreIntegration;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.neoforged.fml.ModList;

import java.lang.reflect.Constructor;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class CultivationScreen extends Screen {
    private static final int BACKDROP = 0xA80A0F0B;
    private static final int PANEL = 0xF0141D17;
    private static final int PANEL_ALT = 0xFF1E2B22;
    private static final int EDGE = 0xFF6CCB8A;
    private static final int ACCENT = 0xFF79E09A;
    private static final int INK = 0xFFF1FFF4;
    private static final int MUTED = 0xFFB7C9BC;
    private static final int LOCKED = 0xFF8B7777;

    private final Map<CultivationFocus, Button> focusButtons = new EnumMap<>(CultivationFocus.class);
    private Button primaryActionButton;
    private Button breakthroughButton;
    private Button techniqueButton;
    private Button hubButton;
    private Button loreTreeButton;
    private int refreshTicks;

    public CultivationScreen() {
        super(Component.translatable("screen.xuanhuan_earth.cultivation.title"));
    }

    @Override
    protected void init() {
        focusButtons.clear();
        int left = panelLeft();
        int top = panelTop();
        int w = panelWidth();
        int h = panelHeight();
        int buttonWidth = navigationWidth() - 20;
        int y = top + 37;
        for (CultivationFocus focus : CultivationFocus.values()) {
            Button button = addRenderableWidget(Button.builder(Component.translatable(focus.titleKey()),
                            ignored -> XuanhuanEarthClient.requestCultivationFocus(focus))
                    .bounds(left + 10, y, buttonWidth, 18)
                    .build());
            button.setTooltip(Tooltip.create(Component.translatable(focus.descriptionKey())));
            focusButtons.put(focus, button);
            y += 19;
        }
        breakthroughButton = addRenderableWidget(Button.builder(
                        Component.translatable("screen.xuanhuan_earth.cultivation.breakthrough"),
                        ignored -> XuanhuanEarthClient.requestBreakthrough())
                .bounds(left + 10, top + h - 68, buttonWidth, 18)
                .build());
        techniqueButton = addRenderableWidget(Button.builder(
                        Component.translatable("screen.xuanhuan_earth.cultivation.skill"),
                        ignored -> XuanhuanEarthClient.requestActivateTechnique())
                .bounds(left + 10, top + h - 48, buttonWidth, 18)
                .build());
        primaryActionButton = addRenderableWidget(Button.builder(
                        Component.translatable("screen.xuanhuan_earth.cultivation.practice"),
                        ignored -> performPrimaryAction())
                .bounds(left + 10, top + h - 28, buttonWidth, 20)
                .build());

        int closeX = left + w - 26;
        int loreW = 54;
        int hubW = 54;
        int loreX = closeX - loreW - 4;
        int hubX = loreX - hubW - 4;
        hubButton = addRenderableWidget(Button.builder(
                        Component.translatable("screen.xuanhuan_earth.cultivation.hub"),
                        ignored -> openCultivationHubOrHandbook())
                .bounds(hubX, top + 8, hubW, 18)
                .build());
        hubButton.setTooltip(Tooltip.create(Component.translatable(
                "screen.xuanhuan_earth.cultivation.hub.tooltip")));

        loreTreeButton = addRenderableWidget(Button.builder(
                        Component.translatable("screen.xuanhuan_earth.cultivation.lore_tree"),
                        ignored -> openXuanxueLoreTree())
                .bounds(loreX, top + 8, loreW, 18)
                .build());
        loreTreeButton.setTooltip(Tooltip.create(Component.translatable(
                "screen.xuanhuan_earth.cultivation.lore_tree.tooltip")));

        addRenderableWidget(Button.builder(Component.literal("X"), ignored -> onClose())
                .bounds(closeX, top + 8, 18, 18)
                .build());
        updateButtons();
    }

    @Override
    public void tick() {
        updateButtons();
        if (minecraft == null || minecraft.player == null || minecraft.getConnection() == null) {
            onClose();
            return;
        }
        if (++refreshTicks >= 20) {
            refreshTicks = 0;
            XuanhuanEarthClient.requestCultivationRefresh();
        }
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float delta) {
        CultivationStatusPayload status = XuanhuanEarthClient.cultivationStatus();
        CultivationFocus focus = CultivationFocus.byId(status.focusId());
        int left = panelLeft();
        int top = panelTop();
        int w = panelWidth();
        int h = panelHeight();
        int navW = navigationWidth();
        int contentX = left + navW + 8;
        int contentW = left + w - 10 - contentX;

        g.fill(0, 0, width, height, BACKDROP);
        g.fill(left, top, left + w, top + h, PANEL);
        g.outline(left, top, w, h, EDGE);
        g.fill(left + 1, top + 1, left + w - 1, top + 34, PANEL_ALT);
        g.fill(left + navW, top + 35, left + navW + 1, top + h - 8, 0xFF3B5543);
        drawAmbientMotif(g, left, top, w);

        g.text(font, title, left + 12, top + 10, INK, false);
        Component support = Component.translatable(status.seated()
                ? "screen.xuanhuan_earth.cultivation.support.cushion"
                : "screen.xuanhuan_earth.cultivation.support.free");
        int maxSupportWidth = Math.max(48, w - navW - 156);
        g.text(font, fitted(support, maxSupportWidth), left + navW + 8, top + 11,
                status.seated() ? ACCENT : MUTED, false);

        List<FormattedCharSequence> description = font.split(Component.translatable(focus.descriptionKey()),
                Math.max(80, contentW));
        if (!description.isEmpty()) {
            g.text(font, description.getFirst(), contentX, top + 39, MUTED, false);
        }

        int meridianWidth = Math.min(54, Math.max(42, contentW / 4));
        int meterX = contentX + meridianWidth + 4;
        int meterWidth = Math.max(82, contentW - meridianWidth - 4);
        int y = top + 52;
        int manaY = y;
        drawCompactBar(g, meterX, y, meterWidth,
                Component.translatable("screen.xuanhuan_earth.cultivation.mana"),
                status.maxMana() <= 0.0D ? 0.0D : status.currentMana() / status.maxMana(),
                Math.round(status.currentMana()) + " / " + Math.round(status.maxMana()), ACCENT);
        y += 17;
        int fieldY = y;
        drawCompactBar(g, meterX, y, meterWidth,
                Component.translatable("screen.xuanhuan_earth.cultivation.field"),
                status.fieldValue() / 100.0D, status.fieldValue() + " / 100", fieldColor(status.fieldValue()));
        y += 17;
        int depletionY = y;
        drawCompactBar(g, meterX, y, meterWidth,
                Component.translatable("screen.xuanhuan_earth.cultivation.depletion"),
                status.depletion() / 45.0D, Math.round(status.depletion()) + " / 45", 0xFFD6A55B);
        y += 17;
        int growthY = y;
        boolean focusUnlocked = status.isUnlocked(focus);
        drawCompactBar(g, meterX, y, meterWidth,
                Component.translatable("screen.xuanhuan_earth.cultivation.growth"),
                !focusUnlocked ? 0.0D : status.focusXpNeeded() <= 0
                        ? 1.0D : status.focusXp() / (double) status.focusXpNeeded(),
                !focusUnlocked
                        ? Component.translatable("screen.xuanhuan_earth.cultivation.growth.locked").getString()
                        : status.focusXpNeeded() <= 0
                        ? "Lv." + status.focusLevel() + " · MAX"
                        : "Lv." + status.focusLevel() + " · " + status.focusXp() + "/" + status.focusXpNeeded(),
                0xFF75C7E8);
        drawMeridian(g, contentX, top + 53, meridianWidth - 2, 72, status);

        int stageY = top + h - 39;
        drawStageTrack(g, contentX, stageY, contentW, status);

        if (h >= 184 && contentW >= 160) {
            int infoY = stageY - 12;
            Component source = Component.translatable("screen.xuanhuan_earth.cultivation.source",
                    Component.translatable(status.sourceKey()));
            g.text(font, fitted(source, contentW), contentX, infoY, MUTED, false);
        }
        super.extractRenderState(g, mouseX, mouseY, delta);
        renderHoverTooltips(g, mouseX, mouseY, contentX, top + 53, meridianWidth - 2, 72,
                meterX, meterWidth, manaY, fieldY, depletionY, growthY, status, focus);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if ((event.key() == InputConstants.KEY_LSHIFT || event.key() == InputConstants.KEY_RSHIFT)
                && XuanhuanEarthClient.cultivationStatus().seated()) {
            leaveCushion();
            return true;
        }
        return super.keyPressed(event);
    }

    @Override
    public void onClose() {
        Minecraft.getInstance().setScreen(null);
    }

    private void performPrimaryAction() {
        if (XuanhuanEarthClient.cultivationStatus().seated()) {
            leaveCushion();
        } else {
            XuanhuanEarthClient.requestPractice();
        }
    }

    private void openXuanxueLoreTree() {
        if (minecraft == null) {
            return;
        }
        minecraft.setScreen(new LoreTreeScreen(XuanhuanLoreIntegration.DISCIPLINE_XUA));
    }

    private void openCultivationHubOrHandbook() {
        if (minecraft == null) {
            return;
        }
        if (ModList.get().isLoaded("earth_human")) {
            try {
                Class<?> cls = Class.forName("com.xxsx.earthhuman.client.CultivationHubScreen");
                Constructor<?> ctor = cls.getConstructor(Screen.class);
                Screen hub = (Screen) ctor.newInstance(this);
                minecraft.setScreen(hub);
                return;
            } catch (Throwable ignored) {
            }
        }
        XuanhuanEarthClient.openHandbook();
    }

    private void leaveCushion() {
        XuanhuanEarthClient.requestStopCultivating();
        onClose();
    }

    private void updateButtons() {
        CultivationStatusPayload status = XuanhuanEarthClient.cultivationStatus();
        CultivationFocus selected = CultivationFocus.byId(status.focusId());
        for (Map.Entry<CultivationFocus, Button> entry : focusButtons.entrySet()) {
            boolean unlocked = status.isUnlocked(entry.getKey());
            entry.getValue().active = unlocked && entry.getKey() != selected;
            entry.getValue().setTooltip(Tooltip.create(Component.translatable(unlocked
                    ? entry.getKey().descriptionKey()
                    : "screen.xuanhuan_earth.cultivation.focus.locked")));
        }
        if (breakthroughButton != null) {
            boolean maxed = status.realm() >= RealmBreakthrough.MAX_REALM;
            breakthroughButton.active = !maxed;
            Component label = Component.translatable(
                    "screen.xuanhuan_earth.cultivation.breakthrough")
                    .copy()
                    .append(Component.literal("·"))
                    .append(Component.translatable(RealmBreakthrough.realmNameKey(status.realm())));
            breakthroughButton.setMessage(fitted(label, Math.max(48, navigationWidth() - 28)));
            int nextRealm = Math.min(RealmBreakthrough.MAX_REALM, status.realm() + 1);
            Component tooltip = maxed
                    ? Component.translatable("message.xuanhuan_earth.realm.max")
                    : Component.translatable("screen.xuanhuan_earth.cultivation.breakthrough.tooltip",
                            Component.translatable(RealmBreakthrough.realmNameKey(status.realm())),
                            Component.translatable(RealmBreakthrough.realmNameKey(nextRealm)),
                            RealmBreakthrough.requiredLevel(nextRealm),
                            Component.translatable("item.xuanhuan_earth.breakthrough_pill"));
            breakthroughButton.setTooltip(Tooltip.create(tooltip));
        }
        if (primaryActionButton != null) {
            boolean seated = status.seated();
            primaryActionButton.setMessage(Component.translatable(seated
                    ? "screen.xuanhuan_earth.cultivation.leave_cushion"
                    : "screen.xuanhuan_earth.cultivation.practice"));
            primaryActionButton.active = seated
                    || status.isUnlocked(CultivationFocus.CIRCULATION) && status.remainingTicks() <= 0;
            primaryActionButton.setTooltip(Tooltip.create(Component.translatable(seated
                    ? "screen.xuanhuan_earth.cultivation.leave_cushion.tooltip"
                    : "screen.xuanhuan_earth.cultivation.practice.tooltip")));
        }
        if (techniqueButton != null) {
            int seconds = Math.max(0, (status.skillRemainingTicks() + 19) / 20);
            techniqueButton.setMessage(seconds > 0
                    ? Component.translatable("screen.xuanhuan_earth.cultivation.skill.cooldown", seconds)
                    : Component.translatable("screen.xuanhuan_earth.cultivation.skill"));
            techniqueButton.active = status.isUnlocked(selected) && status.skillRemainingTicks() <= 0;
            techniqueButton.setTooltip(Tooltip.create(Component.translatable(
                    "screen.xuanhuan_earth.cultivation.skill." + selected.path() + ".tooltip")));
        }
    }

    private void drawCompactBar(GuiGraphicsExtractor g, int x, int y, int width, Component label,
                                double ratio, String value, int color) {
        double clamped = Math.max(0.0D, Math.min(1.0D, ratio));
        int valueW = font.width(value);
        int maxLabelW = Math.max(24, width - valueW - 6);
        g.text(font, fitted(label, maxLabelW), x, y, MUTED, false);
        g.text(font, value, x + width - valueW, y, INK, false);
        g.fill(x, y + 11, x + width, y + 16, 0xFF314037);
        g.fill(x + 1, y + 12, x + 1 + (int) Math.round((width - 2) * clamped), y + 15, color);
    }

    private void drawMeridian(GuiGraphicsExtractor g, int x, int y, int width, int height,
                              CultivationStatusPayload status) {
        int cx = x + width / 2;
        float progress = 1.0F - Math.min(1.0F,
                status.remainingTicks() / (float) ArcanaPower.QI_MEDITATION_COOLDOWN_TICKS);
        g.fill(cx - 1, y + 3, cx + 1, y + height - 3, 0xFF3A5944);
        g.fill(cx - 10, y + 11, cx - 1, y + 12, 0xFF31483A);
        g.fill(cx + 1, y + height - 12, cx + 11, y + height - 11, 0xFF31483A);
        for (int i = 0; i < 4; i++) {
            int ny = y + 4 + (height - 8) * i / 3;
            int nodeColor = progress * 3.0F >= i ? ACCENT : 0xFF53685A;
            g.fill(cx - 4, ny - 3, cx + 5, ny + 4, 0xFF233229);
            g.fill(cx - 2, ny - 1, cx + 3, ny + 2, nodeColor);
        }
        int pulseY = y + 4 + Math.round((height - 8) * progress);
        g.fill(cx - 5, pulseY - 1, cx + 6, pulseY + 2, 0xFFCCEED6);
    }

    private void drawStageTrack(GuiGraphicsExtractor g, int x, int y, int width, CultivationStatusPayload status) {
        float progress = 1.0F - Math.min(1.0F, status.remainingTicks() / (float) ArcanaPower.QI_MEDITATION_COOLDOWN_TICKS);
        int current = Math.min(3, Math.max(0, (int) (progress * 4.0F)));
        int lineY = y + 5;
        g.fill(x + 8, lineY, x + width - 8, lineY + 2, 0xFF3A4C3F);
        g.fill(x + 8, lineY, x + 8 + (int) ((width - 16) * progress), lineY + 2, ACCENT);
        for (int i = 0; i < 4; i++) {
            int cx = x + 8 + (width - 16) * i / 3;
            int color = i <= current ? ACCENT : 0xFF526458;
            g.fill(cx - 3, lineY - 3, cx + 4, lineY + 5, color);
            g.fill(cx - 1, lineY - 1, cx + 2, lineY + 3, PANEL);
        }
        Component stage = Component.translatable(MeditationHud.stageKey(current));
        Component remaining = Component.translatable("screen.xuanhuan_earth.cultivation.remaining",
                Math.max(0, (status.remainingTicks() + 19) / 20));
        int remW = font.width(remaining);
        int maxStageW = Math.max(40, width - remW - 8);
        g.text(font, fitted(stage, maxStageW), x, y + 11, ACCENT, false);
        g.text(font, remaining, x + width - remW, y + 11, MUTED, false);
    }

    private void drawAmbientMotif(GuiGraphicsExtractor g, int left, int top, int width) {
        long time = Minecraft.getInstance().level == null ? 0L : Minecraft.getInstance().level.getGameTime();
        for (int i = 0; i < 5; i++) {
            int px = left + width - 196 + i * 9;
            int py = top + 7 + (int) Math.round(Math.sin((time + i * 7) * 0.12D) * 3.0D);
            g.fill(px, py, px + 2, py + 2, i % 2 == 0 ? ACCENT : 0xFFCCEED6);
        }
    }

    private void renderHoverTooltips(GuiGraphicsExtractor g, int mouseX, int mouseY,
                                     int meridianX, int meridianY, int meridianW, int meridianH,
                                     int meterX, int meterW, int manaY, int fieldY, int depletionY, int growthY,
                                     CultivationStatusPayload status, CultivationFocus focus) {
        if (inside(mouseX, mouseY, meridianX, meridianY, meridianW, meridianH)) {
            float progress = 1.0F - Math.min(1.0F,
                    status.remainingTicks() / (float) ArcanaPower.QI_MEDITATION_COOLDOWN_TICKS);
            int current = Math.min(3, Math.max(0, (int) (progress * 4.0F)));
            g.setTooltipForNextFrame(font, List.of(
                    Component.translatable(MeditationHud.stageKey(current)).getVisualOrderText(),
                    Component.translatable("screen.xuanhuan_earth.cultivation.meridian.tooltip",
                            Component.translatable(RealmBreakthrough.realmNameKey(status.realm())),
                            Math.max(0, (status.remainingTicks() + 19) / 20)).getVisualOrderText()
            ), mouseX, mouseY);
        } else if (inside(mouseX, mouseY, meterX, manaY, meterW, 16)) {
            g.setTooltipForNextFrame(font, Component.translatable(
                    "screen.xuanhuan_earth.cultivation.mana.tooltip",
                    ArcanaPower.format(status.currentMana()),
                    ArcanaPower.format(status.maxMana())), mouseX, mouseY);
        } else if (inside(mouseX, mouseY, meterX, fieldY, meterW, 16)) {
            g.setTooltipForNextFrame(font, Component.translatable(
                    "screen.xuanhuan_earth.cultivation.field.tooltip",
                    Spirituality.gradeName(status.fieldValue()),
                    status.fieldValue(),
                    Component.translatable(status.sourceKey())), mouseX, mouseY);
        } else if (inside(mouseX, mouseY, meterX, depletionY, meterW, 16)) {
            g.setTooltipForNextFrame(font, Component.translatable(
                    "screen.xuanhuan_earth.cultivation.depletion.tooltip",
                    ArcanaPower.format(status.depletion())), mouseX, mouseY);
        } else if (inside(mouseX, mouseY, meterX, growthY, meterW, 16)) {
            g.setTooltipForNextFrame(font, Component.translatable(
                    "screen.xuanhuan_earth.cultivation.growth.tooltip",
                    Component.translatable(focus.titleKey()),
                    status.focusLevel()), mouseX, mouseY);
        }
    }

    private static boolean inside(int mx, int my, int x, int y, int w, int h) {
        return mx >= x && mx < x + w && my >= y && my < y + h;
    }

    private static int fieldColor(int value) {
        if (value < 25) return LOCKED;
        if (value < 50) return 0xFFD6A55B;
        return ACCENT;
    }

    private Component fitted(Component component, int width) {
        String text = component.getString();
        if (font.width(text) <= width) {
            return component;
        }
        return Component.literal(font.plainSubstrByWidth(text, Math.max(0, width - font.width("..."))) + "...");
    }

    private int panelWidth() {
        return Math.min(430, Math.max(304, width - 16));
    }

    private int panelHeight() {
        return Math.min(238, Math.max(186, height - 12));
    }

    private int navigationWidth() {
        return Math.min(118, Math.max(104, panelWidth() / 4 + 12));
    }

    private int panelLeft() {
        return (width - panelWidth()) / 2;
    }

    private int panelTop() {
        return (height - panelHeight()) / 2;
    }
}
