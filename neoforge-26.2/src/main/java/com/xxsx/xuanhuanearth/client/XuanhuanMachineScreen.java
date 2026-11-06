package com.xxsx.xuanhuanearth.client;

import com.xxsx.xuanhuanearth.XuanhuanMachineBlock;
import com.xxsx.xuanhuanearth.XuanhuanMachineBlockEntity;
import com.xxsx.xuanhuanearth.XuanhuanMachineMenu;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.List;
import java.util.Optional;

public class XuanhuanMachineScreen extends AbstractContainerScreen<XuanhuanMachineMenu> {
    private static final int INVENTORY_TOP = 84;
    private static final int INVENTORY_TEXT_Y = 74;
    private static final int WARNING = 0xFFD36A45;
    private static final int OK = 0xFF68B87A;
    private static final int MUTED = 0xFF8D8779;

    public XuanhuanMachineScreen(XuanhuanMachineMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.titleLabelY = 6;
        this.inventoryLabelX = 8;
        this.inventoryLabelY = INVENTORY_TEXT_Y;
    }

    @Override
    protected void init() {
        super.init();
        this.titleLabelX = 8;
        addRenderableWidget(Button.builder(Component.empty(), b -> cycleRedstoneMode())
                .bounds(this.leftPos + 151, this.topPos + 59, 18, 18)
                .build());
        if (this.menu.kind() == XuanhuanMachineBlock.Kind.ALCHEMY_FURNACE) {
            addRenderableWidget(Button.builder(Component.empty(), b -> cycleFireMode())
                    .bounds(this.leftPos + 151, this.topPos + 39, 18, 18)
                    .build());
        } else if (this.menu.kind() == XuanhuanMachineBlock.Kind.SPIRIT_ARRAY_CORE) {
            addRenderableWidget(Button.builder(Component.empty(), b -> cycleArrayMode())
                    .bounds(this.leftPos + 151, this.topPos + 39, 18, 18)
                    .build());
        }
        addRenderableWidget(Button.builder(Component.translatable("screen.xuanhuan_earth.button.handbook"), button -> {
                    this.onClose();
                    XuanhuanEarthClient.openHandbook();
                })
                .bounds(this.leftPos + this.imageWidth - 42, this.topPos + 4, 34, 14)
                .build());
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor g, int mouseX, int mouseY, float delta) {
        super.extractBackground(g, mouseX, mouseY, delta);
        Palette palette = palette();
        int x = this.leftPos;
        int y = this.topPos;
        g.fill(x, y, x + this.imageWidth, y + this.imageHeight, palette.edge);
        g.fill(x + 2, y + 2, x + this.imageWidth - 2, y + this.imageHeight - 2, palette.base);
        g.fill(x + 4, y + 19, x + this.imageWidth - 4, y + 72, palette.workArea);
        g.outline(x, y, this.imageWidth, this.imageHeight, palette.outline);
        g.fill(x + 4, y + 80, x + this.imageWidth - 4, y + 81, palette.divider);
        drawMachineMotif(g, palette);
        drawMachineSlots(g, palette);
        drawInventorySlots(g, palette);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float delta) {
        super.extractRenderState(g, mouseX, mouseY, delta);
        drawRedstoneIcon(g);
        drawExtraControlIcon(g);
        drawStatus(g);
        drawTooltips(g, mouseX, mouseY);
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor g, int mouseX, int mouseY) {
        Palette palette = palette();
        g.text(this.font, trimmedTitle(), this.titleLabelX, this.titleLabelY, palette.title, false);
        g.text(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, palette.inventoryText, false);
    }

    private void drawMachineMotif(GuiGraphicsExtractor g, Palette palette) {
        int progress = Math.min(100, 100 * this.menu.progress() / this.menu.maxProgress());
        switch (this.menu.kind()) {
            case ALCHEMY_FURNACE -> drawAlchemyFurnace(g, palette, progress);
            case TALISMAN_TABLE -> drawTalismanDesk(g, palette, progress);
            case SPIRIT_ARRAY_CORE -> drawSpiritArray(g, palette, progress);
        }
    }

    private void drawAlchemyFurnace(GuiGraphicsExtractor g, Palette palette, int progress) {
        int x = this.leftPos + 79;
        int y = this.topPos + 25;
        g.fill(x, y + 5, x + 25, y + 37, palette.motifDark);
        g.outline(x, y + 5, 25, 32, palette.motifEdge);
        g.fill(x + 4, y + 1, x + 21, y + 7, palette.motifEdge);
        g.fill(x + 7, y + 11, x + 18, y + 30, 0xFF2B1A14);
        int heat = Math.max(1, 17 * progress / 100);
        g.fill(x + 8, y + 29 - heat, x + 17, y + 29, progress > 66 ? 0xFFFFB33D : 0xFFE76B2E);
        g.fill(x + 6, y + 33, x + 19, y + 36, palette.motifEdge);
    }

    private void drawTalismanDesk(GuiGraphicsExtractor g, Palette palette, int progress) {
        int x = this.leftPos + 79;
        int y = this.topPos + 27;
        g.fill(x, y, x + 28, y + 34, 0xFFF0DDAE);
        g.outline(x, y, 28, 34, palette.motifEdge);
        g.fill(x + 4, y + 5, x + 24, y + 6, 0xFFB99A62);
        int strokes = Math.max(0, Math.min(5, progress / 20));
        for (int i = 0; i < strokes; i++) {
            int sy = y + 10 + i * 4;
            g.fill(x + 6 + (i % 2), sy, x + 21 - (i % 3), sy + 1, 0xFF9B2F28);
        }
        if (progress >= 80) {
            g.fill(x + 10, y + 25, x + 18, y + 31, 0xFFB43A30);
            g.outline(x + 10, y + 25, 8, 6, 0xFF7B211E);
        }
    }

    private void drawSpiritArray(GuiGraphicsExtractor g, Palette palette, int progress) {
        int cx = this.leftPos + 91;
        int cy = this.topPos + 43;
        g.fill(cx - 18, cy - 18, cx + 19, cy + 19, palette.motifDark);
        g.outline(cx - 18, cy - 18, 37, 37, palette.motifEdge);
        g.fill(cx - 12, cy, cx + 13, cy + 1, palette.divider);
        g.fill(cx, cy - 12, cx + 1, cy + 13, palette.divider);
        int lit = Math.max(0, Math.min(5, (progress + 19) / 20));
        int[][] nodes = {{0, 0}, {-11, -11}, {11, -11}, {-11, 11}, {11, 11}};
        for (int i = 0; i < nodes.length; i++) {
            int color = i < lit ? 0xFFB99BFF : 0xFF5D536B;
            int size = i == 0 ? 3 : 2;
            g.fill(cx + nodes[i][0] - size, cy + nodes[i][1] - size,
                    cx + nodes[i][0] + size + 1, cy + nodes[i][1] + size + 1, color);
        }
    }

    private void drawMachineSlots(GuiGraphicsExtractor g, Palette palette) {
        XuanhuanMachineMenu.MachineLayout layout = XuanhuanMachineMenu.layoutFor(this.menu.kind());
        drawSlotFrame(g, layout.primaryX(), layout.primaryY(), palette.inputSlot, palette.slotEdge);
        drawSlotFrame(g, layout.catalystX(), layout.catalystY(), palette.catalystSlot, palette.slotEdge);
        drawSlotFrame(g, layout.output0X(), layout.output0Y(), palette.outputSlot, palette.slotEdge);
        drawSlotFrame(g, layout.output1X(), layout.output1Y(), palette.outputSlot, palette.slotEdge);
        drawSlotFrame(g, layout.output2X(), layout.output2Y(), palette.outputSlot, palette.slotEdge);
    }

    private void drawInventorySlots(GuiGraphicsExtractor g, Palette palette) {
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                drawSlotFrame(g, 8 + col * 18, INVENTORY_TOP + row * 18, palette.inventorySlot(), palette.slotEdge);
            }
        }
        for (int col = 0; col < 9; col++) {
            drawSlotFrame(g, 8 + col * 18, INVENTORY_TOP + 58, palette.inventorySlot(), palette.slotEdge);
        }
    }

    private void drawSlotFrame(GuiGraphicsExtractor g, int slotX, int slotY, int fill, int edge) {
        int x = this.leftPos + slotX - 1;
        int y = this.topPos + slotY - 1;
        g.fill(x, y, x + 18, y + 18, edge);
        g.fill(x + 1, y + 1, x + 17, y + 17, fill);
        g.outline(x, y, 18, 18, 0xFF2B2823);
    }

    private void drawStatus(GuiGraphicsExtractor g) {
        Palette palette = palette();
        int color = statusColor();
        Component status = fitted(statusComponent(), 132);
        int x = this.leftPos + 8;
        int y = this.topPos + 63;
        g.fill(x, y + 2, x + 5, y + 7, color);
        g.text(this.font, status, x + 8, y, color, false);
        g.text(this.font, Component.translatable("screen.xuanhuan_earth.machine.field",
                this.menu.fieldValue(), this.menu.fieldDepletion()), this.leftPos + 7, this.topPos + 18, palette.secondaryText, false);
        drawStructureIndicator(g);
    }

    private Component statusComponent() {
        Optional<XuanhuanMachineBlock.Recipe> recipe = currentRecipe();
        return switch (this.menu.processState()) {
            case MISSING_INPUT -> Component.translatable("screen.xuanhuan_earth.machine.missing_inputs");
            case INVALID_COMBINATION -> Component.translatable("screen.xuanhuan_earth.machine.invalid_combination");
            case FIELD_LOW -> Component.translatable("screen.xuanhuan_earth.machine.field_low",
                    recipe.map(this::effectiveMinField).orElse(0));
            case OUTPUT_FULL -> Component.translatable("screen.xuanhuan_earth.machine.output_full");
            case REDSTONE_PAUSED -> Component.translatable("screen.xuanhuan_earth.machine.redstone_paused");
            case LORE_LOCKED -> Component.translatable("lore.earth_lore.fail.lore_locked");
            case RUNNING -> recipe.map(value -> Component.translatable(value.noteKey()))
                    .orElseGet(() -> Component.translatable("screen.xuanhuan_earth.machine.running"));
        };
    }

    private int statusColor() {
        return switch (this.menu.processState()) {
            case INVALID_COMBINATION, FIELD_LOW, OUTPUT_FULL, LORE_LOCKED -> WARNING;
            case RUNNING -> OK;
            case MISSING_INPUT, REDSTONE_PAUSED -> MUTED;
        };
    }

    private Optional<XuanhuanMachineBlock.Recipe> currentRecipe() {
        return XuanhuanMachineBlock.findRecipe(this.menu.kind(),
                this.menu.getSlot(XuanhuanMachineBlockEntity.SLOT_PRIMARY).getItem(),
                this.menu.getSlot(XuanhuanMachineBlockEntity.SLOT_CATALYST).getItem());
    }

    private void drawRedstoneIcon(GuiGraphicsExtractor g) {
        int x = this.leftPos + 152;
        int y = this.topPos + 60;
        XuanhuanMachineBlockEntity.RedstoneMode redstone = this.menu.redstoneMode();
        g.item(new ItemStack(redstoneIcon(redstone)), x, y);
        if (redstone == XuanhuanMachineBlockEntity.RedstoneMode.REQUIRE_NO_SIGNAL) {
            g.fill(x, y, x + 16, y + 16, 0x99000000);
        }
    }

    private Item redstoneIcon(XuanhuanMachineBlockEntity.RedstoneMode redstone) {
        return switch (redstone) {
            case ALWAYS -> Items.BARRIER;
            case REQUIRE_SIGNAL, REQUIRE_NO_SIGNAL -> Items.REDSTONE_TORCH;
        };
    }

    private void drawTooltips(GuiGraphicsExtractor g, int mouseX, int mouseY) {
        if (isHovering(7, 4, 120, 14, mouseX, mouseY)) {
            g.setComponentTooltipForNextFrame(this.font, List.of(
                    Component.translatable(this.menu.kind().displayNameKey()),
                    Component.translatable("screen.xuanhuan_earth.machine.role." + this.menu.kind().blockId())
            ), mouseX, mouseY);
        }
        if (isHovering(151, 59, 18, 18, mouseX, mouseY)) {
            XuanhuanMachineBlockEntity.RedstoneMode redstone = this.menu.redstoneMode();
            g.setComponentTooltipForNextFrame(this.font, List.of(
                    Component.translatable("screen.xuanhuan_earth.redstone.current", Component.translatable(redstone.labelKey())),
                    Component.translatable(redstone.descriptionKey()),
                    Component.translatable("screen.xuanhuan_earth.redstone.tooltip")
            ), mouseX, mouseY);
        }
        if (this.menu.kind() == XuanhuanMachineBlock.Kind.SPIRIT_ARRAY_CORE && isHovering(157, 18, 11, 10, mouseX, mouseY)) {
            g.setComponentTooltipForNextFrame(this.font, List.of(
                    Component.translatable(structureStatusKey()),
                    Component.translatable("screen.xuanhuan_earth.structure.array.hint")
            ), mouseX, mouseY);
        }
        if (this.menu.kind() == XuanhuanMachineBlock.Kind.ALCHEMY_FURNACE && isHovering(151, 39, 18, 18, mouseX, mouseY)) {
            g.setComponentTooltipForNextFrame(this.font, List.of(
                    Component.translatable("screen.xuanhuan_earth.furnace.fire",
                            Component.translatable(this.menu.fireMode().labelKey()),
                            this.menu.temperature())
            ), mouseX, mouseY);
        } else if (this.menu.kind() == XuanhuanMachineBlock.Kind.SPIRIT_ARRAY_CORE && isHovering(151, 39, 18, 18, mouseX, mouseY)) {
            g.setComponentTooltipForNextFrame(this.font, List.of(
                    Component.translatable("screen.xuanhuan_earth.array.mode",
                            Component.translatable(this.menu.arrayMode().labelKey()))
            ), mouseX, mouseY);
        }
    }

    private void drawExtraControlIcon(GuiGraphicsExtractor g) {
        int x = this.leftPos + 152;
        int y = this.topPos + 40;
        if (this.menu.kind() == XuanhuanMachineBlock.Kind.ALCHEMY_FURNACE) {
            Item icon = switch (this.menu.fireMode()) {
                case GENTLE -> Items.CAMPFIRE;
                case MEDIUM -> Items.BLAZE_POWDER;
                case FIERCE -> Items.FIRE_CHARGE;
            };
            g.item(new ItemStack(icon), x, y);
        } else if (this.menu.kind() == XuanhuanMachineBlock.Kind.SPIRIT_ARRAY_CORE) {
            g.item(new ItemStack(Items.AMETHYST_SHARD), x, y);
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
        if (this.font.width(text) > 116) {
            return Component.literal(this.font.plainSubstrByWidth(text, 113) + "...");
        }
        return this.title;
    }

    private void drawStructureIndicator(GuiGraphicsExtractor g) {
        if (this.menu.kind() != XuanhuanMachineBlock.Kind.SPIRIT_ARRAY_CORE) {
            return;
        }
        int x = this.leftPos + 159;
        int y = this.topPos + 20;
        int color = this.menu.structureTier() > 0 ? OK : 0xFF68737A;
        g.fill(x - 2, y - 2, x + 7, y + 7, 0xFF1A2025);
        g.fill(x, y, x + 5, y + 5, color);
        g.fill(x + 2, y - 3, x + 3, y + 8, color);
        g.fill(x - 3, y + 2, x + 8, y + 3, color);
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
                ? "screen.xuanhuan_earth.structure.array.formed"
                : "screen.xuanhuan_earth.structure.array.portable";
    }

    private Palette palette() {
        return switch (this.menu.kind()) {
            case ALCHEMY_FURNACE -> new Palette(0xFF4E3A2C, 0xFF2A211C, 0xFF382820, 0xFF1C1714,
                    0xFF9B7046, 0xFF6F4C32, 0xFF3B3028, 0xFF514238, 0xFF42362E, 0xFF5E4937,
                    0xFFB98A52, 0xFFFFD9A0, 0xFFC8B69A, 0xFFD5C4AA);
            case TALISMAN_TABLE -> new Palette(0xFF8A6A40, 0xFFCFB57B, 0xFFE4D09A, 0xFFB28C54,
                    0xFF74452F, 0xFF9A6A3E, 0xFFD7C08A, 0xFFE7D7AE, 0xFFDCC89A, 0xFFCCB57C,
                    0xFF7E3A2D, 0xFF3A241A, 0xFF6B5338, 0xFF4A3524);
            case SPIRIT_ARRAY_CORE -> new Palette(0xFF4E5966, 0xFF222A32, 0xFF303A45, 0xFF171C22,
                    0xFF6F8195, 0xFF55677A, 0xFF323C47, 0xFF405061, 0xFF384754, 0xFF455666,
                    0xFF8E7AB9, 0xFFDCD5F5, 0xFF9DA8B5, 0xFFC3CBD4);
        };
    }

    private record Palette(int edge, int base, int workArea, int outline, int divider, int motifEdge,
                           int motifDark, int inputSlot, int catalystSlot, int outputSlot, int slotEdge,
                           int title, int secondaryText, int inventoryText) {
        private int inventorySlot() {
            return base;
        }
    }
}
