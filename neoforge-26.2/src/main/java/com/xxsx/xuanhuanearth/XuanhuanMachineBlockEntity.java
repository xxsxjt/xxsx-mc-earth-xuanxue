package com.xxsx.xuanhuanearth;

import com.xxsx.earthlore.api.GateResult;
import com.xxsx.earthlore.api.LoreApi;
import com.xxsx.earthlore.api.LoreMachines;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import com.xxsx.xuanhuanearth.particle.XuanhuanParticles;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

public class XuanhuanMachineBlockEntity extends BaseContainerBlockEntity {
    public static final int SLOT_PRIMARY = 0;
    public static final int SLOT_CATALYST = 1;
    public static final int INPUT_SLOT_COUNT = 2;
    public static final int SLOT_OUTPUT_START = 2;
    public static final int OUTPUT_SLOT_COUNT = 3;
    public static final int SLOT_COUNT = SLOT_OUTPUT_START + OUTPUT_SLOT_COUNT;
    public static final int DATA_COUNT = 11;
    private static final int INVENTORY_VERSION = 2;

    private final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> progress;
                case 1 -> machineProcessTime();
                case 2 -> redstoneMode.id;
                case 3 -> active ? 1 : 0;
                case 4 -> fieldValue;
                case 5 -> (int) Math.round(fieldDepletion);
                case 6 -> structureTier;
                case 7 -> processState.id;
                case 8 -> fireMode.id;
                case 9 -> temperature;
                case 10 -> arrayMode.id;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case 0 -> progress = Math.max(0, value);
                case 2 -> redstoneMode = RedstoneMode.byId(value);
                case 3 -> active = value != 0;
                case 4 -> fieldValue = Math.max(0, value);
                case 5 -> fieldDepletion = Math.max(0, value);
                case 6 -> structureTier = Math.max(0, value);
                case 7 -> processState = ProcessState.byId(value);
                case 8 -> fireMode = FireMode.byId(value);
                case 9 -> temperature = Math.max(300, Math.min(1200, value));
                case 10 -> arrayMode = ArrayMode.byId(value);
                default -> {
                }
            }
        }

        @Override
        public int getCount() {
            return DATA_COUNT;
        }
    };

    private NonNullList<ItemStack> items = NonNullList.withSize(SLOT_COUNT, ItemStack.EMPTY);
    private int progress;
    private RedstoneMode redstoneMode = RedstoneMode.ALWAYS;
    private boolean active;
    private int fieldValue;
    private double fieldDepletion;
    private int structureTier;
    private ProcessState processState = ProcessState.MISSING_INPUT;
    private String currentRecipeId = "";
    private FireMode fireMode = FireMode.MEDIUM;
    private int temperature = 300;
    private ArrayMode arrayMode = ArrayMode.GATHER;
    private double storedAeu = 0.0D;
    private int arrayRadius = 8;
    private int radiusBonus = 0;

    public XuanhuanMachineBlockEntity(BlockPos pos, BlockState state) {
        super(XuanhuanEarth.XUANHUAN_MACHINE_BLOCK_ENTITY.get(), pos, state);
    }

    public static int computeTemperature(int fieldVal, FireMode mode) {
        int clamped = Math.max(0, Math.min(100, fieldVal));
        int raw = (int) Math.floor(300.0D + 9.0D * clamped * mode.factor());
        return Math.max(300, Math.min(1200, raw));
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, XuanhuanMachineBlockEntity machine) {
        boolean changed = machine.active;
        machine.active = false;

        ArcanaChunkField.Reading reading = Spirituality.reading(level, pos);
        machine.fieldValue = reading.value();
        machine.fieldDepletion = reading.depletion();
        machine.structureTier = XuanhuanStructures.tierFor(machine.kind(), level, pos);
        if (machine.kind() == XuanhuanMachineBlock.Kind.ALCHEMY_FURNACE
                && (machine.temperature == 300 || level.getGameTime() % 20L == 0L)) {
            machine.temperature = computeTemperature(machine.fieldValue, machine.fireMode);
        }
        if (machine.kind() == XuanhuanMachineBlock.Kind.SPIRIT_ARRAY_CORE && machine.structureTier > 0) {
            machine.tickArrayModeEffects(level, pos);
        }

        if (!machine.redstoneMode.allows(level.hasNeighborSignal(pos))) {
            machine.processState = ProcessState.REDSTONE_PAUSED;
            machine.finishTick(level, pos, state, changed);
            return;
        }

        ItemStack primary = machine.items.get(SLOT_PRIMARY);
        ItemStack catalyst = machine.items.get(SLOT_CATALYST);
        if (primary.isEmpty() || catalyst.isEmpty()) {
            machine.processState = ProcessState.MISSING_INPUT;
        }
        Optional<XuanhuanMachineBlock.Recipe> match = XuanhuanMachineBlock.findRecipe(machine.kind(), primary, catalyst);
        if (match.isEmpty()) {
            if (!primary.isEmpty() && !catalyst.isEmpty()) {
                machine.processState = ProcessState.INVALID_COMBINATION;
            }
            if (machine.progress != 0) {
                machine.progress = 0;
                changed = true;
            }
            machine.currentRecipeId = "";
            machine.finishTick(level, pos, state, changed);
            return;
        }

        XuanhuanMachineBlock.Recipe recipe = match.get();
        if (!recipe.id().equals(machine.currentRecipeId)) {
            machine.progress = 0;
            machine.currentRecipeId = recipe.id();
            changed = true;
        }

        // A-013 Machine knowledge gate check on product
        if (!recipe.outputs().isEmpty()) {
            GateResult gate = XuanhuanLoreIntegration.checkMachineProduct(
                    machine, recipe.outputs().getFirst().item().get().asItem());
            if (gate.locked()) {
                machine.processState = ProcessState.LORE_LOCKED;
                LoreMachines.setFault(machine, "lore_locked", gate.missing());
                machine.finishTick(level, pos, state, changed);
                return;
            }
        }
        LoreMachines.clearFault(machine);

        int requiredField = machine.effectiveMinField(recipe);
        if (reading.value() < requiredField) {
            machine.processState = ProcessState.FIELD_LOW;
            machine.finishTick(level, pos, state, changed);
            return;
        }
        if (!machine.canFitOutputs(recipe.outputStacks())) {
            machine.processState = ProcessState.OUTPUT_FULL;
            machine.finishTick(level, pos, state, changed);
            return;
        }

        machine.active = true;
        machine.processState = ProcessState.RUNNING;
        if (machine.kind() == XuanhuanMachineBlock.Kind.TALISMAN_TABLE
                && machine.progress + 1 >= machine.machineProcessTime()
                && SpiritFieldLedger.availableField(reading.value(), reading.depletion())
                < SpiritFieldLedger.fieldCostOfAeu(TalismanCharge.CRAFTED_CHARGE_AEU)) {
            machine.active = false;
            machine.processState = ProcessState.FIELD_LOW;
            LoreMachines.setFault(machine, "no_field", List.of());
            machine.finishTick(level, pos, state, changed);
            return;
        }
        machine.progress++;
        changed = true;
        machine.emitWorkingEffects(level, pos);
        if (machine.progress >= machine.machineProcessTime()) {
            machine.completeCraft(level, pos, reading, recipe, requiredField);
            machine.progress = 0;
        }
        machine.finishTick(level, pos, state, changed);
    }

    private void completeCraft(Level level, BlockPos pos, ArcanaChunkField.Reading reading,
                               XuanhuanMachineBlock.Recipe recipe, int requiredField) {
        consumeInput(SLOT_PRIMARY, recipe.primary().count());
        consumeInput(SLOT_CATALYST, recipe.catalyst().count());
        ServerPlayer onlineOwner = findOnlineOwner(level);

        if (kind() == XuanhuanMachineBlock.Kind.ALCHEMY_FURNACE && recipe.targetTempK() > 0) {
            this.temperature = computeTemperature(this.fieldValue, this.fireMode);
            if (this.temperature < recipe.targetTempK() - 100) {
                insertOutputs(List.of(new ItemStack(XuanhuanEarth.FAILED_PILL.get(), 1)));
            } else if (this.temperature > recipe.targetTempK() + 150) {
                if (level instanceof ServerLevel serverLevel) {
                    AABB blast = new AABB(pos).inflate(3.5D);
                    for (LivingEntity entity : serverLevel.getEntitiesOfClass(LivingEntity.class, blast)) {
                        entity.hurtServer(serverLevel, serverLevel.damageSources().magic(), 4.0F);
                    }
                    serverLevel.playSound(null, pos, SoundEvents.GENERIC_EXPLODE.value(), SoundSource.BLOCKS, 0.7F, 1.2F);
                    XuanhuanEffects.sparkBurst(serverLevel, Vec3.atCenterOf(pos).add(0.0D, 0.8D, 0.0D), 16, 0.14D);
                }
                insertOutputs(List.of(new ItemStack(XuanhuanEarth.PILL_RESIDUE.get(), 1)));
            } else {
                double rate = recipe.baseSuccessRate();
                if (onlineOwner != null && onlineOwner.hasEffect(XuanhuanEarth.AUSPICIOUS)) {
                    rate += 0.05D;
                }
                if (hasNearbyAlchemyArray(level, pos)) {
                    rate += 0.10D;
                }
                if (level.getRandom().nextDouble() < Math.min(1.0D, rate)) {
                    List<ItemStack> produced = stampOutputs(level, pos, reading, recipe.outputStacks());
                    insertOutputs(produced);
                    recordOutputs(onlineOwner, produced);
                    if (level.getRandom().nextDouble() < 0.25D) {
                        ItemStack residue = new ItemStack(XuanhuanEarth.PILL_RESIDUE.get(), 1);
                        if (canFitOutputs(List.of(residue))) {
                            insertOutputs(List.of(residue));
                        }
                    }
                } else {
                    insertOutputs(List.of(new ItemStack(XuanhuanEarth.FAILED_PILL.get(), 1)));
                }
            }
        } else {
            List<ItemStack> produced = stampOutputs(level, pos, reading, recipe.outputStacks());
            insertOutputs(produced);
            recordOutputs(onlineOwner, produced);
            if (kind() == XuanhuanMachineBlock.Kind.TALISMAN_TABLE && level instanceof ServerLevel serverLevel) {
                consumeBrushDurabilityIfApplicable(serverLevel, pos, onlineOwner);
            }
        }

        double fieldDraw = recipe.fieldDraw() > 0.0D
                ? recipe.fieldDraw()
                : (recipe.id().equals("spirit_stone_from_raw") ? 4.0D
                : requiredField / (this.structureTier > 0 ? 10.0D : 8.0D));
        if (kind() == XuanhuanMachineBlock.Kind.TALISMAN_TABLE) {
            Spirituality.consumeField(level, pos, TalismanCharge.CRAFTED_CHARGE_AEU);
        } else {
            Spirituality.consumeFieldLevels(level, pos, Math.max(1.0D, fieldDraw));
        }
        emitCompletionEffects(level, pos);
    }

    private ServerPlayer findOnlineOwner(Level level) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return null;
        }
        Optional<UUID> ownerId = LoreMachines.owner(this);
        return ownerId.map(uuid -> serverLevel.getServer().getPlayerList().getPlayer(uuid)).orElse(null);
    }

    private static void recordOutputs(ServerPlayer owner, List<ItemStack> outputs) {
        if (owner == null) {
            return;
        }
        for (ItemStack out : outputs) {
            LoreApi.recordMade(owner, out.getItem(), out.getCount());
        }
    }

    private static void consumeBrushDurabilityIfApplicable(ServerLevel level, BlockPos pos, ServerPlayer owner) {
        if (owner != null && owner.hasEffect(XuanhuanEarth.AUSPICIOUS)) {
            return;
        }
        if (owner != null) {
            for (int i = 0; i < owner.getInventory().getContainerSize(); i++) {
                ItemStack s = owner.getInventory().getItem(i);
                if (s.is(XuanhuanEarth.TALISMAN_BRUSH.get())) {
                    s.hurtAndBreak(1, owner, EquipmentSlot.MAINHAND);
                    return;
                }
            }
        }
        for (Direction dir : Direction.values()) {
            if (level.getBlockEntity(pos.relative(dir)) instanceof Container adj) {
                for (int i = 0; i < adj.getContainerSize(); i++) {
                    ItemStack s = adj.getItem(i);
                    if (s.is(XuanhuanEarth.TALISMAN_BRUSH.get())) {
                        int nextDmg = s.getDamageValue() + 1;
                        if (nextDmg >= s.getMaxDamage()) {
                            adj.setItem(i, ItemStack.EMPTY);
                        } else {
                            s.setDamageValue(nextDmg);
                        }
                        adj.setChanged();
                        return;
                    }
                }
            }
        }
    }

    private static boolean hasNearbyAlchemyArray(Level level, BlockPos pos) {
        for (int dx = -8; dx <= 8; dx++) {
            for (int dz = -8; dz <= 8; dz++) {
                for (int dy = -3; dy <= 3; dy++) {
                    BlockPos check = pos.offset(dx, dy, dz);
                    if (!level.isLoaded(check)) {
                        continue;
                    }
                    if (level.getBlockEntity(check) instanceof XuanhuanMachineBlockEntity array
                            && array.kind() == XuanhuanMachineBlock.Kind.SPIRIT_ARRAY_CORE
                            && array.structureTier > 0
                            && array.arrayMode == ArrayMode.ALCHEMY
                            && array.storedAeu >= ArrayMode.ALCHEMY.aeuPerTick()) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private void tickArrayModeEffects(Level level, BlockPos pos) {
        if (!(level instanceof ServerLevel serverLevel) || arrayMode == ArrayMode.GATHER) {
            return;
        }
        if (storedAeu < arrayMode.aeuPerTick()) {
            // Attempt to draw from a SpiritStoneCore in primary slot
            ItemStack primary = items.get(SLOT_PRIMARY);
            if (primary.is(XuanhuanEarth.SPIRIT_STONE_CORE.get())) {
                SpiritStoneComponent comp = SpiritStoneComponent.read(primary);
                double pull = Math.min(comp.chargeAeu(), 10.0D);
                if (pull > 0.0D) {
                    primary.set(XuanhuanEarth.SPIRIT_STONE_CHARGE.get(),
                            comp.withCoreState(comp.chargeAeu() - pull, SpiritStoneCoreItem.CORE_CAPACITY_AEU, pos));
                    storedAeu = Math.min(SpiritStoneCoreItem.CORE_CAPACITY_AEU, storedAeu + pull);
                }
            }
        }
        if (storedAeu < arrayMode.aeuPerTick()) {
            return;
        }
        storedAeu = Math.max(0.0D, storedAeu - arrayMode.aeuPerTick());
        int effectiveRadius = Math.max(4, Math.min(18, arrayRadius + radiusBonus));
        if (level.getGameTime() % 20L != 0L) {
            return;
        }
        switch (arrayMode) {
            case GUARD -> {
                AABB box = new AABB(pos).inflate(effectiveRadius);
                for (LivingEntity entity : serverLevel.getEntitiesOfClass(LivingEntity.class, box, e -> e instanceof Enemy)) {
                    entity.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 40, 1, true, false, true));
                }
            }
            case SEAL -> {
                AABB box = new AABB(pos).inflate(Math.min(6, effectiveRadius));
                for (LivingEntity entity : serverLevel.getEntitiesOfClass(LivingEntity.class, box, e -> e instanceof Enemy)) {
                    entity.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 40, 4, true, false, true));
                }
            }
            default -> {
            }
        }
    }

    public XuanhuanMachineBlock.Kind kind() {
        if (getBlockState().getBlock() instanceof XuanhuanMachineBlock block) {
            return block.kind();
        }
        return XuanhuanMachineBlock.Kind.ALCHEMY_FURNACE;
    }

    public ContainerData data() {
        return data;
    }

    public void setRedstoneMode(RedstoneMode mode) {
        if (redstoneMode != mode) {
            redstoneMode = mode;
            setChanged();
        }
    }

    public FireMode fireMode() {
        return fireMode;
    }

    public void setFireMode(FireMode next) {
        if (fireMode != next) {
            fireMode = next;
            temperature = computeTemperature(fieldValue, fireMode);
            setChanged();
        }
    }

    public FireMode cycleFireMode() {
        FireMode next = fireMode.next();
        setFireMode(next);
        return next;
    }

    public int temperature() {
        return temperature;
    }

    public ArrayMode arrayMode() {
        return arrayMode;
    }

    public ArrayMode cycleArrayMode() {
        arrayMode = arrayMode.next();
        setChanged();
        return arrayMode;
    }

    public void setArrayRadius(int nextRadius) {
        this.arrayRadius = Math.max(4, Math.min(16, nextRadius));
        setChanged();
    }

    public void setRadiusBonus(int bonus) {
        this.radiusBonus = Math.max(0, Math.min(2, bonus));
        setChanged();
    }

    public void addStoredAeu(double delta) {
        if (Double.isFinite(delta) && delta > 0.0D) {
            this.storedAeu = Math.max(0.0D, Math.min(SpiritStoneCoreItem.CORE_CAPACITY_AEU, this.storedAeu + delta));
            setChanged();
        }
    }

    public double storedAeu() {
        return storedAeu;
    }

    public int arrayFieldBonus(boolean formal, boolean onAcupoint) {
        if (!formal) {
            return 12;
        }
        int base = arrayMode.fieldBonus();
        if (arrayMode == ArrayMode.GATHER && onAcupoint) {
            base += 4;
        }
        return Math.min(36, base);
    }

    @Override
    public int getContainerSize() {
        return SLOT_COUNT;
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return slot >= SLOT_PRIMARY && slot <= SLOT_CATALYST
                && XuanhuanMachineBlock.acceptsInput(kind(), slot, stack);
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable(kind().displayNameKey());
    }

    @Override
    protected NonNullList<ItemStack> getItems() {
        return items;
    }

    @Override
    protected void setItems(NonNullList<ItemStack> items) {
        this.items = items;
    }

    @Override
    protected AbstractContainerMenu createMenu(int containerId, Inventory inventory) {
        return new XuanhuanMachineMenu(containerId, inventory, this, data);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.items = NonNullList.withSize(SLOT_COUNT, ItemStack.EMPTY);
        ContainerHelper.loadAllItems(input, this.items);
        if (input.getIntOr("InventoryVersion", 0) < INVENTORY_VERSION) {
            ItemStack oldOutput0 = this.items.get(1);
            ItemStack oldOutput1 = this.items.get(2);
            ItemStack oldOutput2 = this.items.get(3);
            this.items.set(SLOT_CATALYST, ItemStack.EMPTY);
            this.items.set(SLOT_OUTPUT_START, oldOutput0);
            this.items.set(SLOT_OUTPUT_START + 1, oldOutput1);
            this.items.set(SLOT_OUTPUT_START + 2, oldOutput2);
        }
        this.progress = input.getIntOr("Progress", 0);
        this.redstoneMode = RedstoneMode.byId(input.getIntOr("RedstoneMode", 0));
        this.fireMode = FireMode.byId(input.getIntOr("FireMode", FireMode.MEDIUM.id));
        this.arrayMode = ArrayMode.byId(input.getIntOr("ArrayMode", ArrayMode.GATHER.id));
        this.storedAeu = Math.max(0.0D, Math.min(480.0D, input.getDoubleOr("StoredAeu", 0.0D)));
        this.arrayRadius = Math.max(4, Math.min(16, input.getIntOr("ArrayRadius", 8)));
        this.radiusBonus = Math.max(0, Math.min(2, input.getIntOr("RadiusBonus", 0)));
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        ContainerHelper.saveAllItems(output, this.items);
        output.putInt("Progress", progress);
        output.putInt("RedstoneMode", redstoneMode.id);
        output.putInt("FireMode", fireMode.id);
        output.putInt("ArrayMode", arrayMode.id);
        output.putDouble("StoredAeu", storedAeu);
        output.putInt("ArrayRadius", arrayRadius);
        output.putInt("RadiusBonus", radiusBonus);
        output.putInt("InventoryVersion", INVENTORY_VERSION);
    }

    private boolean canFitOutputs(List<ItemStack> outputs) {
        NonNullList<ItemStack> simulated = NonNullList.withSize(OUTPUT_SLOT_COUNT, ItemStack.EMPTY);
        for (int i = 0; i < OUTPUT_SLOT_COUNT; i++) {
            simulated.set(i, this.items.get(SLOT_OUTPUT_START + i).copy());
        }
        for (ItemStack output : outputs) {
            if (!fitInto(simulated, output.copy(), 0, simulated.size())) {
                return false;
            }
        }
        return true;
    }

    private void insertOutputs(List<ItemStack> outputs) {
        for (ItemStack output : outputs) {
            fitInto(this.items, output.copy(), SLOT_OUTPUT_START, SLOT_COUNT);
        }
    }

    /**
     * Talismans leave the talisman table with a sealed charge. The charge is stamped
     * here, on the server, so a crafted item can never depend on a client-side count.
     */
    private List<ItemStack> stampOutputs(Level level, BlockPos pos, ArcanaChunkField.Reading reading, List<ItemStack> outputs) {
        List<ItemStack> stamped = new java.util.ArrayList<>(outputs.size());
        for (ItemStack output : outputs) {
            ItemStack copy = output.copy();
            if (copy.is(XuanhuanEarth.BASIC_TALISMAN.get())
                    || copy.is(XuanhuanEarth.FIRE_TALISMAN.get())
                    || copy.is(XuanhuanEarth.WIND_TALISMAN.get())
                    || copy.is(XuanhuanEarth.SEAL_TALISMAN.get())) {
                TalismanComponent.drawn(copy);
            } else if (copy.is(XuanhuanEarth.SPIRIT_STONE.get())) {
                String sig = SpiritStoneComponent.computeSignature(level, pos, reading.mainSourceKey());
                copy.set(XuanhuanEarth.SPIRIT_STONE_CHARGE.get(),
                        SpiritStoneComponent.EMPTY.withStoneState(40.0D, SpiritStoneCharge.CAPACITY_AEU, 1.0D, sig));
            } else if (copy.is(XuanhuanEarth.JADE_SLIP.get())) {
                copy.set(XuanhuanEarth.JADE_COMPONENT.get(),
                        JadeComponent.EMPTY.withCharge(24.0D, 1.0D));
            } else if (copy.has(XuanhuanEarth.PILL_COMPONENT.get())
                    || copy.getItem() instanceof XuanhuanPillItem
                    || copy.getItem() instanceof QiRecoveryPillItem) {
                if (!copy.is(XuanhuanEarth.FAILED_PILL.get())) {
                    String slug = net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(copy.getItem()).getPath();
                    double quality = Math.max(0.45D, Math.min(0.96D, 0.45D + (reading.value() / 200.0D)));
                    PillComponent raw = PillComponent.fromQuality(slug, quality, Long.toHexString(level.getGameTime()));
                    PillComponent.PillGrade capped = XuanhuanLoreIntegration.capMachinePillGrade(this, copy.getItem(), raw.grade());
                    copy.set(XuanhuanEarth.PILL_COMPONENT.get(), raw.withGrade(capped));
                }
            }
            stamped.add(copy);
        }
        return stamped;
    }

    private static boolean fitInto(NonNullList<ItemStack> stacks, ItemStack output, int start, int end) {
        for (int i = start; i < end && !output.isEmpty(); i++) {
            ItemStack existing = stacks.get(i);
            if (!existing.isEmpty() && ItemStack.isSameItemSameComponents(existing, output)) {
                int move = Math.min(output.getCount(), existing.getMaxStackSize() - existing.getCount());
                if (move > 0) {
                    existing.grow(move);
                    output.shrink(move);
                }
            }
        }
        for (int i = start; i < end && !output.isEmpty(); i++) {
            if (stacks.get(i).isEmpty()) {
                int move = Math.min(output.getCount(), output.getMaxStackSize());
                stacks.set(i, output.copyWithCount(move));
                output.shrink(move);
            }
        }
        return output.isEmpty();
    }

    private void finishTick(Level level, BlockPos pos, BlockState state, boolean changed) {
        BlockState nextState = state;
        if (state.hasProperty(XuanhuanMachineBlock.ACTIVE) && state.getValue(XuanhuanMachineBlock.ACTIVE) != active) {
            nextState = nextState.setValue(XuanhuanMachineBlock.ACTIVE, active);
        }
        boolean formed = structureTier > 0;
        if (state.hasProperty(XuanhuanMachineBlock.FORMED) && state.getValue(XuanhuanMachineBlock.FORMED) != formed) {
            nextState = nextState.setValue(XuanhuanMachineBlock.FORMED, formed);
        }
        if (nextState != state) {
            level.setBlock(pos, nextState, Block.UPDATE_CLIENTS);
            changed = true;
        }
        if (changed) {
            setChanged();
        }
    }

    private int machineProcessTime() {
        int base = XuanhuanMachineBlock.findRecipe(kind(), items.get(SLOT_PRIMARY), items.get(SLOT_CATALYST))
                .map(XuanhuanMachineBlock.Recipe::processTicks)
                .orElse(100);
        return structureTier > 0 ? Math.max(40, base * 3 / 4) : base;
    }

    private int effectiveMinField(XuanhuanMachineBlock.Recipe recipe) {
        return effectiveMinField(kind(), recipe.minField(), structureTier);
    }

    public static int effectiveMinField(XuanhuanMachineBlock.Kind kind, int base, int structureTier) {
        if (kind == XuanhuanMachineBlock.Kind.SPIRIT_ARRAY_CORE && structureTier > 0) {
            return Math.max(1, base - 12);
        }
        return base;
    }

    public enum RedstoneMode {
        ALWAYS(0, "screen.xuanhuan_earth.redstone.always", "screen.xuanhuan_earth.redstone.always.desc"),
        REQUIRE_SIGNAL(1, "screen.xuanhuan_earth.redstone.require_signal", "screen.xuanhuan_earth.redstone.require_signal.desc"),
        REQUIRE_NO_SIGNAL(2, "screen.xuanhuan_earth.redstone.require_no_signal", "screen.xuanhuan_earth.redstone.require_no_signal.desc");

        final int id;
        private final String labelKey;
        private final String descriptionKey;

        RedstoneMode(int id, String labelKey, String descriptionKey) {
            this.id = id;
            this.labelKey = labelKey;
            this.descriptionKey = descriptionKey;
        }

        public static RedstoneMode byId(int id) {
            for (RedstoneMode mode : values()) {
                if (mode.id == id) {
                    return mode;
                }
            }
            return ALWAYS;
        }

        public boolean allows(boolean powered) {
            return switch (this) {
                case ALWAYS -> true;
                case REQUIRE_SIGNAL -> powered;
                case REQUIRE_NO_SIGNAL -> !powered;
            };
        }

        public String labelKey() {
            return labelKey;
        }

        public String descriptionKey() {
            return descriptionKey;
        }
    }

    public enum ProcessState {
        MISSING_INPUT(0),
        INVALID_COMBINATION(1),
        FIELD_LOW(2),
        OUTPUT_FULL(3),
        REDSTONE_PAUSED(4),
        RUNNING(5),
        LORE_LOCKED(6);

        private final int id;

        ProcessState(int id) {
            this.id = id;
        }

        public static ProcessState byId(int id) {
            for (ProcessState state : values()) {
                if (state.id == id) {
                    return state;
                }
            }
            return MISSING_INPUT;
        }
    }

    public enum FireMode {
        GENTLE(0, "gentle", 0.70D),
        MEDIUM(1, "medium", 0.85D),
        FIERCE(2, "fierce", 1.00D);

        final int id;
        private final String nameId;
        private final double factor;

        FireMode(int id, String nameId, double factor) {
            this.id = id;
            this.nameId = nameId;
            this.factor = factor;
        }

        public double factor() {
            return factor;
        }

        public String labelKey() {
            return "fire_mode.xuanhuan_earth." + nameId;
        }

        public FireMode next() {
            return values()[(ordinal() + 1) % values().length];
        }

        public static FireMode byId(int id) {
            for (FireMode m : values()) {
                if (m.id == id) {
                    return m;
                }
            }
            return MEDIUM;
        }
    }

    public enum ArrayMode {
        GATHER(0, "gather", 28, 0.05D),
        CHARGE(1, "charge", 18, 0.10D),
        GUARD(2, "guard", 12, 0.08D),
        ALCHEMY(3, "alchemy", 10, 0.06D),
        SEAL(4, "seal", 6, 0.12D);

        final int id;
        private final String modeId;
        private final int fieldBonus;
        private final double aeuPerTick;

        ArrayMode(int id, String modeId, int fieldBonus, double aeuPerTick) {
            this.id = id;
            this.modeId = modeId;
            this.fieldBonus = fieldBonus;
            this.aeuPerTick = aeuPerTick;
        }

        public int fieldBonus() {
            return fieldBonus;
        }

        public double aeuPerTick() {
            return aeuPerTick;
        }

        public String labelKey() {
            return "array_mode.xuanhuan_earth." + modeId;
        }

        public ArrayMode next() {
            return values()[(ordinal() + 1) % values().length];
        }

        public static ArrayMode byId(int id) {
            for (ArrayMode m : values()) {
                if (m.id == id) {
                    return m;
                }
            }
            return GATHER;
        }
    }

    private void consumeInput(int slot, int count) {
        ItemStack stack = items.get(slot);
        if (stack.is(Items.MILK_BUCKET) && count == 1 && stack.getCount() == 1) {
            items.set(slot, new ItemStack(Items.BUCKET));
            return;
        }
        stack.shrink(count);
        if (stack.isEmpty()) {
            items.set(slot, ItemStack.EMPTY);
        }
    }

    private void emitWorkingEffects(Level level, BlockPos pos) {
        if (!(level instanceof ServerLevel serverLevel) || level.getGameTime() % 12L != 0L) {
            return;
        }
        Vec3 center = Vec3.atCenterOf(pos);
        switch (kind()) {
            case ALCHEMY_FURNACE -> XuanhuanEffects.smokePlume(serverLevel,
                    center.add(0.0D, 1.02D, 0.0D), true, 1);
            case TALISMAN_TABLE -> {
                serverLevel.sendParticles(XuanhuanParticles.QI_MOTE.get(),
                        center.x, center.y + 1.02D, center.z, 1, 0.2D, 0.08D, 0.2D, 0.005D);
                if (level.getGameTime() % 36L == 0L) {
                    XuanhuanEffects.runeCluster(serverLevel, center, 1, 0.6D, false);
                }
            }
            case SPIRIT_ARRAY_CORE -> {
                XuanhuanEffects.qiRise(serverLevel, center, 0.55D, 2, 0.012D);
                if (level.getGameTime() % 48L == 0L) {
                    XuanhuanEffects.groundRing(serverLevel, center, 1.0F);
                }
            }
        }
    }

    private void emitCompletionEffects(Level level, BlockPos pos) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }
        switch (kind()) {
            case ALCHEMY_FURNACE -> {
                XuanhuanEffects.smokePlume(serverLevel, Vec3.atCenterOf(pos).add(0.0D, 1.05D, 0.0D), true, 8);
                serverLevel.sendParticles(XuanhuanParticles.EMBER_SPARK.get(),
                        pos.getX() + 0.5D, pos.getY() + 1.0D, pos.getZ() + 0.5D,
                        6, 0.28D, 0.18D, 0.28D, 0.02D);
                serverLevel.playSound(null, pos, SoundEvents.BREWING_STAND_BREW, SoundSource.BLOCKS, 0.7F, 0.85F);
            }
            case TALISMAN_TABLE -> {
                XuanhuanEffects.talismanActivation(serverLevel, Vec3.atCenterOf(pos));
                serverLevel.playSound(null, pos, SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.BLOCKS, 0.7F, 1.15F);
            }
            case SPIRIT_ARRAY_CORE -> {
                Vec3 center = Vec3.atCenterOf(pos);
                XuanhuanEffects.groundRing(serverLevel, center, 1.6F);
                XuanhuanEffects.qiRise(serverLevel, center, 0.85D, 12, 0.03D);
                XuanhuanEffects.sparkBurst(serverLevel, center.add(0.0D, 0.8D, 0.0D), 10, 0.08D);
                serverLevel.playSound(null, pos, SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.BLOCKS, 0.8F, 1.1F);
            }
        }
    }
}
