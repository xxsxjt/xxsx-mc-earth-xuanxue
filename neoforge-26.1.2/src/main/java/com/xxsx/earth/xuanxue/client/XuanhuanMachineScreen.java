package com.xxsx.earth.xuanxue.client;

import org.anti_ad.mc.ipn.api.IPNPlayerSideOnly;

import com.xxsx.earth.xuanxue.XuanhuanEarth;
import com.xxsx.earth.xuanxue.XuanhuanMachineBlock;
import com.xxsx.earth.xuanxue.XuanhuanMachineBlockEntity;
import com.xxsx.earth.xuanxue.XuanhuanMachineMenu;
import java.util.List;
import java.util.Optional;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

/**
 * Vanilla+ 256x256 Sprite-Atlas Machine Screen with seamless beveled right-side control tab (x=176..200).
 * Keeps the 176x166 container grid identical to vanilla Minecraft while neatly docking fire/array/redstone
 * controls on the right ear tab with zero slot/border overlap.
 */
@IPNPlayerSideOnly
public class XuanhuanMachineScreen extends AbstractContainerScreen<XuanhuanMachineMenu> {
    private static final Identifier ALCHEMY_TEX = XuanhuanEarth.id("textures/gui/container/alchemy_furnace_gui.png");
    private static final Identifier TALISMAN_TEX = XuanhuanEarth.id("textures/gui/container/talisman_table_gui.png");
    private static final Identifier ARRAY_TEX = XuanhuanEarth.id("textures/gui/container/spirit_array_gui.png");

    private static final int ATLAS_SIZE = 256;
    private static final int VPLUS_WIDTH = 200;
    private static final int WARNING = 0xFF9E2C1C;
    private static final int OK = 0xFF1A6634;
    private static final int MUTED = 0xFF5A5248;

    public XuanhuanMachineScreen(XuanhuanMachineMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, 176, 166);
        this.titleLabelX = 8;
        this.titleLabelY = 6;
        this.inventoryLabelX = 8;
        this.inventoryLabelY = menu.kind() == XuanhuanMachineBlock.Kind.TALISMAN_TABLE ? 75 : 73;
    }

    @Override
    protected void init() {
        super.init();
        // Compact top-bar Handbook button cleanly aligned with title & aura gauge
        addRenderableWidget(Button.builder(Component.translatable("screen.earth_xuanxue.button.handbook"), button -> {
                    this.onClose();
                    XuanhuanEarthClient.openHandbook();
                })
                .bounds(this.leftPos + 136, this.topPos + 4, 34, 12)
                .build());

        // Right-side beveled ear-tab controls (16x16 evenly stacked, zero overlap with container slots)
        switch (this.menu.kind()) {
            case ALCHEMY_FURNACE -> {
                addRenderableWidget(Button.builder(Component.empty(), b -> cycleFireMode())
                        .bounds(this.leftPos + 179, this.topPos + 25, 16, 16)
                        .build());
                addRenderableWidget(Button.builder(Component.empty(), b -> cycleRedstoneMode())
                        .bounds(this.leftPos + 179, this.topPos + 45, 16, 16)
                        .build());
            }
            case TALISMAN_TABLE -> {
                addRenderableWidget(Button.builder(Component.empty(), b -> cycleRedstoneMode())
                        .bounds(this.leftPos + 179, this.topPos + 25, 16, 16)
                        .build());
            }
            case SPIRIT_ARRAY_CORE -> {
                addRenderableWidget(Button.builder(Component.empty(), b -> cycleArrayMode())
                        .bounds(this.leftPos + 179, this.topPos + 45, 16, 16)
                        .build());
                addRenderableWidget(Button.builder(Component.empty(), b -> cycleRedstoneMode())
                        .bounds(this.leftPos + 179, this.topPos + 65, 16, 16)
                        .build());
            }
        }
    }

    private Identifier atlas() {
        return switch (this.menu.kind()) {
            case ALCHEMY_FURNACE -> ALCHEMY_TEX;
            case TALISMAN_TABLE -> TALISMAN_TEX;
            case SPIRIT_ARRAY_CORE -> ARRAY_TEX;
        };
    }

    private int titleInk() {
        return switch (this.menu.kind()) {
            case ALCHEMY_FURNACE -> 0xFF3A322A;
            case TALISMAN_TABLE -> 0xFF3B281C;
            case SPIRIT_ARRAY_CORE -> 0xFF252E3A;
        };
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor g, int mouseX, int mouseY, float delta) {
        super.extractBackground(g, mouseX, mouseY, delta);
        int x = this.leftPos;
        int y = this.topPos;
        Identifier tex = atlas();

        // Draw 200x166 Vanilla+ base panel (176px main container + 24px right side-tab)
        g.blit(RenderPipelines.GUI_TEXTURED, tex, x, y, 0, 0, VPLUS_WIDTH, this.imageHeight, ATLAS_SIZE, ATLAS_SIZE);

        // Draw top-bar Qi aura meter
        drawAuraTrack(g);

        // Draw animated machine motif from right-side sprite strip (x >= 204)
        int progress = Math.min(100, 100 * this.menu.progress() / Math.max(1, this.menu.maxProgress()));
        switch (this.menu.kind()) {
            case ALCHEMY_FURNACE -> drawAlchemyMotif(g, tex, progress);
            case TALISMAN_TABLE -> drawTalismanMotif(g, tex, progress);
            case SPIRIT_ARRAY_CORE -> drawArrayMotif(g, tex, progress);
        }

        // Draw status lamp (ok green / paused red)
        boolean ok = this.menu.processState() == XuanhuanMachineBlockEntity.ProcessState.RUNNING;
        int lampSrcX = ok ? 204 : 214;
        int lampSrcY = this.menu.kind() == XuanhuanMachineBlock.Kind.ALCHEMY_FURNACE ? 32 : 42;
        int lampDstY = this.menu.kind() == XuanhuanMachineBlock.Kind.TALISMAN_TABLE ? 63 : 62;
        g.blit(RenderPipelines.GUI_TEXTURED, tex, x + 9, y + lampDstY, lampSrcX, lampSrcY, 7, 7, ATLAS_SIZE, ATLAS_SIZE);
    }

    private void drawAuraTrack(GuiGraphicsExtractor g) {
        int tx = this.leftPos + (this.menu.kind() == XuanhuanMachineBlock.Kind.SPIRIT_ARRAY_CORE ? 66 : 58);
        int ty = this.topPos + 8;
        int tw = this.menu.kind() == XuanhuanMachineBlock.Kind.SPIRIT_ARRAY_CORE ? 62 : 70;
        int field = Math.max(0, Math.min(100, this.menu.fieldValue()));
        int fillW = Math.round((tw - 2) * (field / 100.0F));
        if (fillW > 0) {
            int color = field < 25 ? 0xFFD06040 : (field < 55 ? 0xFFD9A84C : 0xFF47B87B);
            g.fill(tx + 1, ty + 1, tx + 1 + fillW, ty + 5, color);
        }
    }

    private void drawAlchemyMotif(GuiGraphicsExtractor g, Identifier tex, int progress) {
        int x = this.leftPos;
        int y = this.topPos;
        if (progress > 0) {
            int h = Math.max(2, (14 * progress) / 100);
            g.blit(RenderPipelines.GUI_TEXTURED, tex, x + 83, y + 36 + (14 - h), 204, 14 - h, 12, h, ATLAS_SIZE, ATLAS_SIZE);
            int aw = Math.max(1, (7 * progress) / 100);
            g.blit(RenderPipelines.GUI_TEXTURED, tex, x + 107, y + 30, 204, 20, aw, 7, ATLAS_SIZE, ATLAS_SIZE);
        }
    }

    private void drawTalismanMotif(GuiGraphicsExtractor g, Identifier tex, int progress) {
        int x = this.leftPos;
        int y = this.topPos;
        if (progress > 0) {
            int h = Math.max(2, (26 * progress) / 100);
            g.blit(RenderPipelines.GUI_TEXTURED, tex, x + 84, y + 31, 204, 0, 20, h, ATLAS_SIZE, ATLAS_SIZE);
            int aw = Math.max(1, (7 * progress) / 100);
            g.blit(RenderPipelines.GUI_TEXTURED, tex, x + 110, y + 41, 204, 30, aw, 7, ATLAS_SIZE, ATLAS_SIZE);
        }
    }

    private void drawArrayMotif(GuiGraphicsExtractor g, Identifier tex, int progress) {
        int x = this.leftPos;
        int y = this.topPos;
        boolean jade = this.menu.structureTier() > 0;
        int lit = progress <= 0 ? 0 : Math.min(5, (progress + 19) / 20);
        if (lit >= 1) {
            g.blit(RenderPipelines.GUI_TEXTURED, tex, x + 87, y + 38, jade ? 218 : 204, 0, 9, 9, ATLAS_SIZE, ATLAS_SIZE);
        }
        int[][] corners = {{76, 27}, {100, 27}, {76, 51}, {100, 51}};
        for (int i = 0; i < 4; i++) {
            if (lit >= i + 2) {
                g.blit(RenderPipelines.GUI_TEXTURED, tex, x + corners[i][0], y + corners[i][1],
                        jade ? 218 : 204, 14, 7, 7, ATLAS_SIZE, ATLAS_SIZE);
            }
        }
        // Structure status badge docked cleanly at top of side-tab (179, 25)
        g.blit(RenderPipelines.GUI_TEXTURED, tex, x + 181, y + 27, jade ? 204 : 218, 26, 11, 11, ATLAS_SIZE, ATLAS_SIZE);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float delta) {
        super.extractRenderState(g, mouseX, mouseY, delta);
        drawSideTabIcons(g);
        drawStatusLine(g);
        drawTooltips(g, mouseX, mouseY);
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor g, int mouseX, int mouseY) {
        int ink = titleInk();
        g.text(this.font, trimmedTitle(), this.titleLabelX, this.titleLabelY, ink, false);
        g.text(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, ink, false);
    }

    private void drawSideTabIcons(GuiGraphicsExtractor g) {
        Identifier tex = atlas();
        int x = this.leftPos;
        int y = this.topPos;

        int rsIdx = switch (this.menu.redstoneMode()) {
            case ALWAYS -> 0;
            case REQUIRE_SIGNAL -> 1;
            case REQUIRE_NO_SIGNAL -> 2;
        };
        int rsSrcX = 204 + rsIdx * 14;

        switch (this.menu.kind()) {
            case ALCHEMY_FURNACE -> {
                int fireIdx = switch (this.menu.fireMode()) {
                    case GENTLE -> 0;
                    case MEDIUM -> 1;
                    case FIERCE -> 2;
                };
                g.blit(RenderPipelines.GUI_TEXTURED, tex, x + 181, y + 27, 204 + fireIdx * 14, 62, 12, 12, ATLAS_SIZE, ATLAS_SIZE);
                g.blit(RenderPipelines.GUI_TEXTURED, tex, x + 181, y + 47, rsSrcX, 46, 12, 12, ATLAS_SIZE, ATLAS_SIZE);
            }
            case TALISMAN_TABLE -> {
                g.blit(RenderPipelines.GUI_TEXTURED, tex, x + 181, y + 27, rsSrcX, 54, 12, 12, ATLAS_SIZE, ATLAS_SIZE);
            }
            case SPIRIT_ARRAY_CORE -> {
                boolean jade = this.menu.arrayMode() == XuanhuanMachineBlockEntity.ArrayMode.GATHER;
                g.blit(RenderPipelines.GUI_TEXTURED, tex, x + 183, y + 48, jade ? 218 : 204, 0, 9, 9, ATLAS_SIZE, ATLAS_SIZE);
                g.blit(RenderPipelines.GUI_TEXTURED, tex, x + 181, y + 67, rsSrcX, 54, 12, 12, ATLAS_SIZE, ATLAS_SIZE);
            }
        }
    }

    private void drawStatusLine(GuiGraphicsExtractor g) {
        Component status = statusComponent();
        int color = switch (this.menu.processState()) {
            case INVALID_COMBINATION, FIELD_LOW, OUTPUT_FULL, LORE_LOCKED -> WARNING;
            case RUNNING -> OK;
            case MISSING_INPUT, REDSTONE_PAUSED -> MUTED;
        };
        int sy = this.topPos + (this.menu.kind() == XuanhuanMachineBlock.Kind.TALISMAN_TABLE ? 63 : 62);
        g.text(this.font, fitted(status, 150), this.leftPos + 19, sy, color, false);
    }

    private Component statusComponent() {
        Optional<XuanhuanMachineBlock.Recipe> recipe = currentRecipe();
        return switch (this.menu.processState()) {
            case MISSING_INPUT -> Component.translatable("screen.earth_xuanxue.machine.idle");
            case INVALID_COMBINATION -> Component.translatable("screen.earth_xuanxue.machine.invalid");
            case FIELD_LOW -> recipe.map(value -> Component.translatable("screen.earth_xuanxue.machine.field_low",
                            effectiveMinField(value)))
                    .orElseGet(() -> Component.translatable("screen.earth_xuanxue.machine.field_low", 20));
            case OUTPUT_FULL -> Component.translatable("screen.earth_xuanxue.machine.output_full");
            case REDSTONE_PAUSED -> Component.translatable("screen.earth_xuanxue.machine.redstone_paused");
            case LORE_LOCKED -> Component.translatable("lore.earth_lore.fail.lore_locked");
            case RUNNING -> recipe.map(value -> Component.translatable(value.noteKey()))
                    .orElseGet(() -> Component.translatable("screen.earth_xuanxue.machine.running"));
        };
    }

    private Optional<XuanhuanMachineBlock.Recipe> currentRecipe() {
        return XuanhuanMachineBlock.findRecipe(this.menu.kind(),
                this.menu.getSlot(XuanhuanMachineBlockEntity.SLOT_PRIMARY).getItem(),
                this.menu.getSlot(XuanhuanMachineBlockEntity.SLOT_CATALYST).getItem());
    }

    private void drawTooltips(GuiGraphicsExtractor g, int mouseX, int mouseY) {
        if (isHovering(7, 4, 48, 12, mouseX, mouseY)) {
            g.setComponentTooltipForNextFrame(this.font, List.of(
                    Component.translatable(this.menu.kind().displayNameKey()),
                    Component.translatable("screen.earth_xuanxue.machine.role." + this.menu.kind().blockId())
            ), mouseX, mouseY);
        }
        int auraX = this.menu.kind() == XuanhuanMachineBlock.Kind.SPIRIT_ARRAY_CORE ? 66 : 58;
        int auraW = this.menu.kind() == XuanhuanMachineBlock.Kind.SPIRIT_ARRAY_CORE ? 62 : 70;
        if (isHovering(auraX, 7, auraW, 8, mouseX, mouseY)) {
            g.setComponentTooltipForNextFrame(this.font, List.of(
                    Component.translatable("screen.earth_xuanxue.machine.field", this.menu.fieldValue())
            ), mouseX, mouseY);
        }

        int rsBtnY = switch (this.menu.kind()) {
            case ALCHEMY_FURNACE -> 45;
            case TALISMAN_TABLE -> 25;
            case SPIRIT_ARRAY_CORE -> 65;
        };
        if (isHovering(179, rsBtnY, 16, 16, mouseX, mouseY)) {
            XuanhuanMachineBlockEntity.RedstoneMode redstone = this.menu.redstoneMode();
            g.setComponentTooltipForNextFrame(this.font, List.of(
                    Component.translatable("screen.earth_xuanxue.redstone.current", Component.translatable(redstone.labelKey())),
                    Component.translatable(redstone.descriptionKey()),
                    Component.translatable("screen.earth_xuanxue.redstone.tooltip")
            ), mouseX, mouseY);
        }
        if (this.menu.kind() == XuanhuanMachineBlock.Kind.ALCHEMY_FURNACE && isHovering(179, 25, 16, 16, mouseX, mouseY)) {
            g.setComponentTooltipForNextFrame(this.font, List.of(
                    Component.translatable("screen.earth_xuanxue.furnace.fire",
                            Component.translatable(this.menu.fireMode().labelKey()),
                            this.menu.temperature())
            ), mouseX, mouseY);
        } else if (this.menu.kind() == XuanhuanMachineBlock.Kind.SPIRIT_ARRAY_CORE) {
            if (isHovering(179, 25, 16, 16, mouseX, mouseY)) {
                g.setComponentTooltipForNextFrame(this.font, List.of(
                        Component.translatable(structureStatusKey()),
                        Component.translatable("screen.earth_xuanxue.structure.array.hint")
                ), mouseX, mouseY);
            } else if (isHovering(179, 45, 16, 16, mouseX, mouseY)) {
                g.setComponentTooltipForNextFrame(this.font, List.of(
                        Component.translatable("screen.earth_xuanxue.array.mode",
                                Component.translatable(this.menu.arrayMode().labelKey()))
                ), mouseX, mouseY);
            }
        }
    }

    private void cycleFireMode() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.gameMode != null) {
            mc.gameMode.handleInventoryButtonClick(this.menu.containerId, XuanhuanMachineMenu.BUTTON_CYCLE_FIRE);
        }
    }

    private void cycleArrayMode() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.gameMode != null) {
            mc.gameMode.handleInventoryButtonClick(this.menu.containerId, XuanhuanMachineMenu.BUTTON_CYCLE_ARRAY_MODE);
        }
    }

    private void cycleRedstoneMode() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.gameMode == null) {
            return;
        }
        int id = switch (this.menu.redstoneMode()) {
            case ALWAYS -> XuanhuanMachineMenu.BUTTON_REDSTONE_REQUIRE_SIGNAL;
            case REQUIRE_SIGNAL -> XuanhuanMachineMenu.BUTTON_REDSTONE_REQUIRE_NO_SIGNAL;
            case REQUIRE_NO_SIGNAL -> XuanhuanMachineMenu.BUTTON_REDSTONE_ALWAYS;
        };
        mc.gameMode.handleInventoryButtonClick(this.menu.containerId, id);
    }

    private Component trimmedTitle() {
        String text = this.title.getString();
        int maxW = this.menu.kind() == XuanhuanMachineBlock.Kind.SPIRIT_ARRAY_CORE ? 56 : 48;
        if (this.font.width(text) > maxW) {
            return Component.literal(this.font.plainSubstrByWidth(text, Math.max(12, maxW - 8)) + "...");
        }
        return this.title;
    }

    private Component fitted(Component component, int width) {
        String text = component.getString();
        if (this.font.width(text) <= width) {
            return component;
        }
        return Component.literal(this.font.plainSubstrByWidth(text, Math.max(0, width - this.font.width("..."))) + "...");
    }

    private int effectiveMinField(XuanhuanMachineBlock.Recipe recipe) {
        return XuanhuanMachineBlockEntity.effectiveMinField(this.menu.kind(), recipe.minField(), this.menu.structureTier());
    }

    private String structureStatusKey() {
        return this.menu.structureTier() > 0
                ? "screen.earth_xuanxue.structure.array.formed"
                : "screen.earth_xuanxue.structure.array.portable";
    }
}
