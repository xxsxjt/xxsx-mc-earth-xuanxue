package com.xxsx.xuanhuanearth;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class XuanhuanMachineMenu extends AbstractContainerMenu {
    public static final int BUTTON_REDSTONE_ALWAYS = 0;
    public static final int BUTTON_REDSTONE_REQUIRE_SIGNAL = 1;
    public static final int BUTTON_REDSTONE_REQUIRE_NO_SIGNAL = 2;
    public static final int BUTTON_CYCLE_FIRE = 3;
    public static final int BUTTON_CYCLE_ARRAY_MODE = 4;
    private static final int PLAYER_INV_START = XuanhuanMachineBlockEntity.SLOT_COUNT;
    private static final int PLAYER_INV_END = PLAYER_INV_START + 27;
    private static final int HOTBAR_END = PLAYER_INV_END + 9;

    private final Container container;
    private final ContainerData data;
    private final BlockPos pos;
    private final XuanhuanMachineBlock.Kind kind;

    public XuanhuanMachineMenu(int containerId, Inventory inventory, RegistryFriendlyByteBuf buf) {
        this(containerId, inventory, buf.readBlockPos());
    }

    private XuanhuanMachineMenu(int containerId, Inventory inventory, BlockPos pos) {
        this(containerId, inventory, new SimpleContainer(XuanhuanMachineBlockEntity.SLOT_COUNT),
                new SimpleContainerData(XuanhuanMachineBlockEntity.DATA_COUNT), pos, kindFromClientLevel(inventory, pos));
    }

    public XuanhuanMachineMenu(int containerId, Inventory inventory, XuanhuanMachineBlockEntity machine, ContainerData data) {
        this(containerId, inventory, machine, data, machine.getBlockPos(), machine.kind());
    }

    private XuanhuanMachineMenu(int containerId, Inventory inventory, Container container, ContainerData data,
                                BlockPos pos, XuanhuanMachineBlock.Kind kind) {
        super(XuanhuanEarth.XUANHUAN_MACHINE_MENU.get(), containerId);
        checkContainerSize(container, XuanhuanMachineBlockEntity.SLOT_COUNT);
        checkContainerDataCount(data, XuanhuanMachineBlockEntity.DATA_COUNT);
        this.container = container;
        this.data = data;
        this.pos = pos;
        this.kind = kind;
        this.container.startOpen(inventory.player);

        MachineLayout layout = layoutFor(kind);
        addSlot(new Slot(container, XuanhuanMachineBlockEntity.SLOT_PRIMARY, layout.primaryX, layout.primaryY) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return XuanhuanMachineBlock.acceptsInput(XuanhuanMachineMenu.this.kind, 0, stack);
            }
        });
        addSlot(new Slot(container, XuanhuanMachineBlockEntity.SLOT_CATALYST, layout.catalystX, layout.catalystY) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return XuanhuanMachineBlock.acceptsInput(XuanhuanMachineMenu.this.kind, 1, stack);
            }
        });
        addSlot(new OutputSlot(container, XuanhuanMachineBlockEntity.SLOT_OUTPUT_START, layout.output0X, layout.output0Y));
        addSlot(new OutputSlot(container, XuanhuanMachineBlockEntity.SLOT_OUTPUT_START + 1, layout.output1X, layout.output1Y));
        addSlot(new OutputSlot(container, XuanhuanMachineBlockEntity.SLOT_OUTPUT_START + 2, layout.output2X, layout.output2Y));
        addStandardInventorySlots(inventory, 8, 84);
        addDataSlots(data);
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (container instanceof XuanhuanMachineBlockEntity machine) {
            if (id >= BUTTON_REDSTONE_ALWAYS && id <= BUTTON_REDSTONE_REQUIRE_NO_SIGNAL) {
                machine.setRedstoneMode(XuanhuanMachineBlockEntity.RedstoneMode.byId(id));
                return true;
            }
            if (id == BUTTON_CYCLE_FIRE && machine.kind() == XuanhuanMachineBlock.Kind.ALCHEMY_FURNACE) {
                machine.cycleFireMode();
                return true;
            }
            if (id == BUTTON_CYCLE_ARRAY_MODE && machine.kind() == XuanhuanMachineBlock.Kind.SPIRIT_ARRAY_CORE) {
                if (!(player instanceof ServerPlayer serverPlayer) || XuanhuanLoreIntegration.knowsArrayFunctions(serverPlayer)) {
                    machine.cycleArrayMode();
                    return true;
                }
            }
        }
        return super.clickMenuButton(player, id);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = this.slots.get(index);
        if (slot == null || !slot.hasItem()) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = slot.getItem();
        ItemStack moved = stack.copy();
        boolean outputSlot = index >= XuanhuanMachineBlockEntity.SLOT_OUTPUT_START
                && index < XuanhuanMachineBlockEntity.SLOT_COUNT;
        if (index < XuanhuanMachineBlockEntity.SLOT_COUNT) {
            if (!moveItemStackTo(stack, PLAYER_INV_START, HOTBAR_END, true)) {
                return ItemStack.EMPTY;
            }
        } else if (XuanhuanMachineBlock.acceptsInput(this.kind, 0, stack)) {
            boolean movedInput = moveItemStackTo(stack, XuanhuanMachineBlockEntity.SLOT_PRIMARY,
                    XuanhuanMachineBlockEntity.SLOT_PRIMARY + 1, false);
            if (!movedInput && XuanhuanMachineBlock.acceptsInput(this.kind, 1, stack)) {
                movedInput = moveItemStackTo(stack, XuanhuanMachineBlockEntity.SLOT_CATALYST,
                        XuanhuanMachineBlockEntity.SLOT_CATALYST + 1, false);
            }
            if (!movedInput) {
                return ItemStack.EMPTY;
            }
        } else if (XuanhuanMachineBlock.acceptsInput(this.kind, 1, stack)) {
            if (!moveItemStackTo(stack, XuanhuanMachineBlockEntity.SLOT_CATALYST,
                    XuanhuanMachineBlockEntity.SLOT_CATALYST + 1, false)) {
                return ItemStack.EMPTY;
            }
        } else if (index < PLAYER_INV_END) {
            if (!moveItemStackTo(stack, PLAYER_INV_END, HOTBAR_END, false)) {
                return ItemStack.EMPTY;
            }
        } else if (!moveItemStackTo(stack, PLAYER_INV_START, PLAYER_INV_END, false)) {
            return ItemStack.EMPTY;
        }
        if (stack.isEmpty()) {
            slot.setByPlayer(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        if (outputSlot && player instanceof ServerPlayer serverPlayer) {
            XuanhuanJourney.complete(serverPlayer, XuanhuanJourney.Milestone.FACILITY_OUTPUT);
        }
        return moved;
    }

    @Override
    public boolean stillValid(Player player) {
        return container.stillValid(player);
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        this.container.stopOpen(player);
    }

    public XuanhuanMachineBlock.Kind kind() {
        return kind;
    }

    public BlockPos pos() {
        return pos;
    }

    public int progress() {
        return data.get(0);
    }

    public int maxProgress() {
        return Math.max(1, data.get(1));
    }

    public XuanhuanMachineBlockEntity.RedstoneMode redstoneMode() {
        return XuanhuanMachineBlockEntity.RedstoneMode.byId(data.get(2));
    }

    public boolean active() {
        return data.get(3) != 0;
    }

    public int fieldValue() {
        return data.get(4);
    }

    public int fieldDepletion() {
        return data.get(5);
    }

    public int structureTier() {
        return data.get(6);
    }

    public XuanhuanMachineBlockEntity.ProcessState processState() {
        return XuanhuanMachineBlockEntity.ProcessState.byId(data.get(7));
    }

    public XuanhuanMachineBlockEntity.FireMode fireMode() {
        return XuanhuanMachineBlockEntity.FireMode.byId(data.get(8));
    }

    public int temperature() {
        return Math.max(300, data.get(9));
    }

    public XuanhuanMachineBlockEntity.ArrayMode arrayMode() {
        return XuanhuanMachineBlockEntity.ArrayMode.byId(data.get(10));
    }

    public static MachineLayout layoutFor(XuanhuanMachineBlock.Kind kind) {
        return switch (kind) {
            case ALCHEMY_FURNACE -> new MachineLayout(42, 25, 42, 47, 116, 25, 138, 25, 127, 47);
            case TALISMAN_TABLE -> new MachineLayout(35, 36, 59, 36, 120, 36, 142, 36, 131, 57);
            case SPIRIT_ARRAY_CORE -> new MachineLayout(29, 36, 53, 36, 114, 25, 136, 25, 125, 47);
        };
    }

    private static XuanhuanMachineBlock.Kind kindFromClientLevel(Inventory inventory, BlockPos pos) {
        if (inventory.player.level().getBlockState(pos).getBlock() instanceof XuanhuanMachineBlock block) {
            return block.kind();
        }
        return XuanhuanMachineBlock.Kind.ALCHEMY_FURNACE;
    }

    private static class OutputSlot extends Slot {
        OutputSlot(Container container, int slot, int x, int y) {
            super(container, slot, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return false;
        }

        @Override
        public void onTake(Player player, ItemStack carried) {
            super.onTake(player, carried);
            if (!carried.isEmpty() && player instanceof ServerPlayer serverPlayer) {
                XuanhuanJourney.complete(serverPlayer, XuanhuanJourney.Milestone.FACILITY_OUTPUT);
            }
        }
    }

    public record MachineLayout(int primaryX, int primaryY, int catalystX, int catalystY,
                                int output0X, int output0Y, int output1X, int output1Y,
                                int output2X, int output2Y) {
    }
}
