package com.xxsx.xuanhuanearth;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.locale.Language;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Container;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

public class XuanhuanMachineBlock extends Block implements EntityBlock {
    public static final BooleanProperty ACTIVE = BooleanProperty.create("active");
    public static final BooleanProperty FORMED = BooleanProperty.create("formed");
    public static final EnumProperty<Direction> FACING = HorizontalDirectionalBlock.FACING;
    private static final List<Recipe> RECIPES = createRecipes();

    private final Kind kind;

    public XuanhuanMachineBlock(Properties properties, Kind kind) {
        super(properties);
        this.kind = kind;
        registerDefaultState(stateDefinition.any().setValue(ACTIVE, false).setValue(FORMED, false).setValue(FACING, Direction.NORTH));
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        return openMachineAt(level, pos, player);
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player,
                                          InteractionHand hand, BlockHitResult hitResult) {
        if (kind == Kind.SPIRIT_ARRAY_CORE && (
                stack.is(XuanhuanEarth.ARRAY_CONTROL_TABLET.get())
                        || stack.is(XuanhuanEarth.JADE_ARRAY_PLATE.get())
                        || stack.is(XuanhuanEarth.SPIRIT_STONE_CORE.get()))) {
            return stack.useOn(new UseOnContext(level, player, hand, stack, hitResult));
        }
        return openMachineAt(level, pos, player);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new XuanhuanMachineBlockEntity(pos, state);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (kind != Kind.SPIRIT_ARRAY_CORE || !state.getValue(FORMED) || random.nextInt(2) != 0) {
            return;
        }
        int[][] nodes = {{0, -1}, {1, 0}, {0, 1}, {-1, 0}, {1, -1}, {1, 1}, {-1, 1}, {-1, -1}};
        int[] node = nodes[random.nextInt(nodes.length)];
        double x = pos.getX() + 0.5D + node[0] * 0.82D;
        double z = pos.getZ() + 0.5D + node[1] * 0.82D;
        level.addParticle(ParticleTypes.END_ROD, x, pos.getY() + 1.08D, z,
                -node[0] * 0.012D, 0.004D, -node[1] * 0.012D);
    }

    @Override
    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide()) {
            return null;
        }
        return type == XuanhuanEarth.XUANHUAN_MACHINE_BLOCK_ENTITY.get()
                ? (tickerLevel, pos, tickerState, blockEntity) -> XuanhuanMachineBlockEntity.serverTick(tickerLevel, pos, tickerState, (XuanhuanMachineBlockEntity) blockEntity)
                : null;
    }

    @Override
    protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel level, BlockPos pos, boolean movedByPiston) {
        if (level.getBlockEntity(pos) instanceof XuanhuanMachineBlockEntity machine) {
            Containers.dropContents(level, pos, (Container) machine);
            level.updateNeighbourForOutputSignal(pos, this);
        }
        super.affectNeighborsAfterRemoval(state, level, pos, movedByPiston);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(ACTIVE, FORMED, FACING);
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    public Kind kind() {
        return kind;
    }

    public static InteractionResult openMachineAt(Level level, BlockPos pos, Player player) {
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        if (player instanceof ServerPlayer serverPlayer && level.getBlockEntity(pos) instanceof XuanhuanMachineBlockEntity machine) {
            serverPlayer.openMenu(machine, buf -> buf.writeBlockPos(pos));
        }
        return InteractionResult.SUCCESS_SERVER;
    }

    public static List<Recipe> recipes() {
        return RECIPES;
    }

    public static List<Recipe> recipesFor(Kind kind) {
        return RECIPES.stream().filter(recipe -> recipe.kind == kind && recipe.isAvailable()).toList();
    }

    public static Optional<Recipe> findRecipe(Kind kind, ItemStack primary, ItemStack catalyst) {
        if (primary.isEmpty() || catalyst.isEmpty()) {
            return Optional.empty();
        }
        return RECIPES.stream()
                .filter(recipe -> recipe.kind == kind && recipe.matches(primary, catalyst))
                .findFirst();
    }

    public static boolean acceptsInput(Kind kind, int inputIndex, ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        return RECIPES.stream()
                .filter(recipe -> recipe.kind == kind)
                .anyMatch(recipe -> recipe.input(inputIndex).matches(stack));
    }

    public static String describeOutputs(List<Output> outputs) {
        List<String> names = new ArrayList<>();
        for (Output output : outputs) {
            ItemStack stack = new ItemStack(output.item.get().asItem(), output.count);
            names.add(output.count + "x " + stack.getItemName().getString());
        }
        return String.join(" + ", names);
    }

    private static List<Recipe> createRecipes() {
        List<Recipe> recipes = new ArrayList<>();
        recipes.add(r("herbal_qi_pill", Kind.ALCHEMY_FURNACE,
                item(XuanhuanEarth.SPIRIT_GRASS::get, 2), item(XuanhuanEarth.SPIRIT_SPRING_BOTTLE::get, 1),
                35, 12.0D, 120, 600, 0.85D, out(XuanhuanEarth.QI_RECOVERY_PILL::get, 2)));
        recipes.add(r("spirit_herb_qi_pill", Kind.ALCHEMY_FURNACE,
                item(XuanhuanEarth.SPIRIT_HERB::get, 2), item(XuanhuanEarth.SPIRIT_SPRING_BOTTLE::get, 1),
                35, 12.0D, 120, 600, 0.85D, out(XuanhuanEarth.QI_RECOVERY_PILL::get, 2)));
        recipes.add(r("crystal_qi_pill", Kind.ALCHEMY_FURNACE,
                item(XuanhuanEarth.SPIRIT_CRYSTAL_SHARD::get, 1), item(XuanhuanEarth.SPIRIT_SPRING_BOTTLE::get, 1),
                55, 18.0D, 160, 600, 0.88D, out(XuanhuanEarth.QI_RECOVERY_PILL::get, 3)));

        // 02 册 X-ALCHEMY-01 route, detox & residue recipes
        recipes.add(r("marrow_pill", Kind.ALCHEMY_FURNACE,
                item(XuanhuanEarth.SPIRIT_HERB::get, 3), item(XuanhuanEarth.SPIRIT_CRYSTAL_SHARD::get, 2),
                50, 20.0D, 200, 750, 0.70D, out(XuanhuanEarth.MARROW_PILL::get, 1)));
        recipes.add(r("breath_pill", Kind.ALCHEMY_FURNACE,
                item(XuanhuanEarth.SPIRIT_HERB::get, 2), item(XuanhuanEarth.SPIRIT_SPRING_BOTTLE::get, 2),
                42, 18.0D, 180, 650, 0.72D, out(XuanhuanEarth.BREATH_PILL::get, 1)));
        recipes.add(r("fast_pill", Kind.ALCHEMY_FURNACE,
                item(XuanhuanEarth.SPIRIT_HERB::get, 2), item(() -> Items.WHEAT, 3),
                35, 14.0D, 160, 600, 0.80D, out(XuanhuanEarth.FAST_PILL::get, 2)));
        recipes.add(r("detox_pill", Kind.ALCHEMY_FURNACE,
                item(XuanhuanEarth.SPIRIT_HERB::get, 2), item(() -> Items.MILK_BUCKET, 1),
                35, 14.0D, 140, 0, 0.88D, out(XuanhuanEarth.DETOX_PILL::get, 2)));
        recipes.add(r("pill_residue_refine", Kind.ALCHEMY_FURNACE,
                item(XuanhuanEarth.PILL_RESIDUE::get, 2), item(XuanhuanEarth.SPIRIT_SPRING_BOTTLE::get, 1),
                35, 10.0D, 140, 600, 0.35D, out(XuanhuanEarth.QI_RECOVERY_PILL::get, 1)));

        // X-008 & X-009 furnace material processing
        recipes.add(r("spirit_iron_ingot", Kind.ALCHEMY_FURNACE,
                item(XuanhuanEarth.SPIRIT_IRON_BLANK::get, 2), item(XuanhuanEarth.SPIRIT_CRYSTAL_SHARD::get, 1),
                55, 16.0D, 160, 800, 1.00D, out(XuanhuanEarth.SPIRIT_IRON_INGOT::get, 1)));
        recipes.add(r("spirit_glass", Kind.ALCHEMY_FURNACE,
                item(() -> Items.GLASS, 2), item(XuanhuanEarth.SPIRIT_DUST::get, 1),
                30, 8.0D, 120, 0, 1.00D, out(XuanhuanEarth.SPIRIT_GLASS::get, 2)));

        // X-041 Five herbal pill formulas (compatible with Earth herbs + standalone fallback)
        recipes.add(r("nourish_qi_pill", Kind.ALCHEMY_FURNACE,
                earthOrFallback("ginseng", XuanhuanEarth.SPIRIT_HERB::get, 1),
                earthOrFallback("astragalus", XuanhuanEarth.SPIRIT_SPRING_BOTTLE::get, 1),
                40, 15.0D, 160, 620, 0.82D, out(XuanhuanEarth.NOURISH_QI_PILL::get, 2)));
        recipes.add(r("calm_spirit_pill", Kind.ALCHEMY_FURNACE,
                earthOrFallback("jujube_seed", XuanhuanEarth.SPIRIT_HERB::get, 2),
                earthOrFallback("biota_seed", () -> Items.SWEET_BERRIES, 1),
                38, 15.0D, 180, 600, 0.78D, out(XuanhuanEarth.CALM_SPIRIT_PILL::get, 2)));
        recipes.add(r("clear_heat_pill", Kind.ALCHEMY_FURNACE,
                earthOrFallback("coptis", XuanhuanEarth.SPIRIT_HERB::get, 1),
                earthOrFallback("forsythia", XuanhuanEarth.SPIRIT_CRYSTAL_SHARD::get, 1),
                44, 16.0D, 200, 640, 0.76D, out(XuanhuanEarth.CLEAR_HEAT_PILL::get, 2)));
        recipes.add(r("invigorate_blood_pill", Kind.ALCHEMY_FURNACE,
                earthOrFallback("salvia_miltiorrhiza", XuanhuanEarth.SPIRIT_HERB::get, 1),
                earthOrFallback("safflower", () -> Items.RED_DYE, 1),
                46, 16.0D, 200, 660, 0.74D, out(XuanhuanEarth.INVIGORATE_BLOOD_PILL::get, 2)));
        recipes.add(r("resolve_toxin_pill", Kind.ALCHEMY_FURNACE,
                earthOrFallback("licorice", XuanhuanEarth.SPIRIT_HERB::get, 1),
                earthOrFallback("mung_bean", () -> Items.KELP, 2),
                36, 14.0D, 160, 600, 0.85D, out(XuanhuanEarth.RESOLVE_TOXIN_PILL::get, 2)));

        // The pill ladder: each tier needs the previous tier as a catalyst, so alchemy is a
        // progression rather than six unrelated recipes.
        recipes.add(r("foundation_pill", Kind.ALCHEMY_FURNACE,
                item(XuanhuanEarth.QI_RECOVERY_PILL::get, 2),
                item(XuanhuanEarth.SPIRIT_CRYSTAL_SHARD::get, 2),
                62, 22.0D, 200, 700, 0.75D, out(XuanhuanEarth.FOUNDATION_PILL::get, 1)));
        recipes.add(r("ironbody_pill", Kind.ALCHEMY_FURNACE,
                item(XuanhuanEarth.QI_RECOVERY_PILL::get, 1),
                tagged(EarthMaterialTags.ARCANA_GEOLOGY_CATALYSTS, () -> Items.IRON_INGOT, 2),
                66, 24.0D, 220, 740, 0.72D, out(XuanhuanEarth.IRONBODY_PILL::get, 1)));
        recipes.add(r("cloudbreath_pill", Kind.ALCHEMY_FURNACE,
                item(XuanhuanEarth.SPIRIT_SPRING_BOTTLE::get, 2),
                item(XuanhuanEarth.SPIRIT_GRASS::get, 3),
                58, 20.0D, 200, 650, 0.75D, out(XuanhuanEarth.CLOUDBREATH_PILL::get, 1)));
        recipes.add(r("grainfast_pill", Kind.ALCHEMY_FURNACE,
                item(XuanhuanEarth.SPIRIT_GRASS::get, 4),
                item(() -> Items.WHEAT, 2),
                52, 16.0D, 180, 600, 0.80D, out(XuanhuanEarth.GRAINFAST_PILL::get, 1)));
        recipes.add(r("nightsight_pill", Kind.ALCHEMY_FURNACE,
                item(XuanhuanEarth.SPIRIT_GRASS::get, 2),
                item(() -> Items.GOLDEN_CARROT, 1),
                56, 18.0D, 180, 620, 0.80D, out(XuanhuanEarth.NIGHTSIGHT_PILL::get, 1)));
        recipes.add(r("breakthrough_pill", Kind.ALCHEMY_FURNACE,
                item(XuanhuanEarth.FOUNDATION_PILL::get, 2),
                item(XuanhuanEarth.SPIRIT_CRYSTAL_SHARD::get, 4),
                80, 34.0D, 320, 900, 0.45D, out(XuanhuanEarth.BREAKTHROUGH_PILL::get, 1)));
        recipes.add(r("breakthrough_pill_herb", Kind.ALCHEMY_FURNACE,
                item(XuanhuanEarth.SPIRIT_HERB::get, 4),
                item(XuanhuanEarth.SPIRIT_CRYSTAL_SHARD::get, 4),
                75, 34.0D, 400, 900, 0.45D, out(XuanhuanEarth.BREAKTHROUGH_PILL::get, 1)));

        recipes.add(r("charged_basic_talisman", Kind.TALISMAN_TABLE,
                item(XuanhuanEarth.TALISMAN_PAPER::get, 1), item(XuanhuanEarth.CINNABAR_SPIRIT_INK::get, 1),
                60, 24.0D, 100, out(XuanhuanEarth.BASIC_TALISMAN::get, 1)));
        recipes.add(r("fire_talisman", Kind.TALISMAN_TABLE,
                item(XuanhuanEarth.EMPTY_TALISMAN::get, 1), item(() -> Items.BLAZE_POWDER, 1),
                50, 24.0D, 120, out(XuanhuanEarth.FIRE_TALISMAN::get, 1)));
        recipes.add(r("wind_talisman", Kind.TALISMAN_TABLE,
                item(XuanhuanEarth.EMPTY_TALISMAN::get, 1), item(() -> Items.FEATHER, 2),
                45, 24.0D, 100, out(XuanhuanEarth.WIND_TALISMAN::get, 1)));
        recipes.add(r("seal_talisman", Kind.TALISMAN_TABLE,
                item(XuanhuanEarth.EMPTY_TALISMAN::get, 1), item(() -> Items.STRING, 2),
                55, 24.0D, 140, out(XuanhuanEarth.SEAL_TALISMAN::get, 1)));

        recipes.add(r("spirit_stone_from_raw", Kind.SPIRIT_ARRAY_CORE,
                item(XuanhuanEarth.SPIRIT_STONE_RAW::get, 1), item(XuanhuanEarth.SPIRIT_CRYSTAL_SHARD::get, 2),
                50, 0.0D, 200, out(XuanhuanEarth.SPIRIT_STONE::get, 1)));
        recipes.add(r("jade_slip", Kind.SPIRIT_ARRAY_CORE,
                tagged(EarthMaterialTags.GEMS_NEPHRITE, () -> Items.EMERALD, 1),
                item(XuanhuanEarth.SPIRIT_CRYSTAL_SHARD::get, 2),
                30, 8.0D, 200, out(XuanhuanEarth.JADE_SLIP::get, 1)));
        recipes.add(r("spirit_iron_blank", Kind.SPIRIT_ARRAY_CORE,
                item(() -> Items.IRON_INGOT, 1), item(XuanhuanEarth.SPIRIT_CRYSTAL_SHARD::get, 1),
                45, 8.0D, 140, out(XuanhuanEarth.SPIRIT_IRON_BLANK::get, 1)));
        recipes.add(r("spirit_grass_growth", Kind.SPIRIT_ARRAY_CORE,
                item(XuanhuanEarth.SPIRIT_SOIL::get, 1), item(XuanhuanEarth.SPIRIT_SPRING_BOTTLE::get, 1),
                38, 7.0D, 110, out(XuanhuanEarth.SPIRIT_GRASS::get, 2)));
        recipes.add(r("geological_crystallization", Kind.SPIRIT_ARRAY_CORE,
                item(XuanhuanEarth.SPIRIT_SPRING_BOTTLE::get, 1),
                tagged(EarthMaterialTags.ARCANA_GEOLOGY_CATALYSTS, () -> Items.AMETHYST_SHARD, 1),
                48, 10.0D, 150, out(XuanhuanEarth.SPIRIT_CRYSTAL_SHARD::get, 2)));
        recipes.add(r("conductive_spirit_iron", Kind.SPIRIT_ARRAY_CORE,
                item(() -> Items.IRON_INGOT, 1),
                tagged(EarthMaterialTags.MANA_CONDUCTORS, () -> Items.COPPER_INGOT, 1),
                52, 11.0D, 170, out(XuanhuanEarth.SPIRIT_IRON_BLANK::get, 2)));
        return List.copyOf(recipes);
    }

    private static Recipe r(String id, Kind kind, Input primary, Input catalyst, int minField,
                            double fieldDraw, int processTicks, Output... outputs) {
        return r(id, kind, primary, catalyst, minField, fieldDraw, processTicks, 0, 1.0D, outputs);
    }

    private static Recipe r(String id, Kind kind, Input primary, Input catalyst, int minField,
                            double fieldDraw, int processTicks, int targetTempK, double successRate, Output... outputs) {
        return new Recipe(id, kind, primary, catalyst,
                "recipe.xuanhuan_earth.machine." + id, minField, fieldDraw, processTicks,
                targetTempK, successRate, List.of(outputs));
    }

    private static Input item(Supplier<? extends ItemLike> item, int count) {
        return new Input(null, null, item, count);
    }

    private static Input tagged(TagKey<Item> tag, Supplier<? extends ItemLike> fallback, int count) {
        return new Input(tag, null, fallback, count);
    }

    private static Input earthOrFallback(String earthSlug, Supplier<? extends ItemLike> fallback, int count) {
        return new Input(null, Identifier.fromNamespaceAndPath("earth_on_minecraft", earthSlug), fallback, count);
    }

    private static Output out(Supplier<? extends ItemLike> item, int count) {
        return new Output(item, count);
    }

    public record Recipe(String id, Kind kind, Input primary, Input catalyst, String noteKey,
                         int minField, double fieldDraw, int processTicks,
                         int targetTempK, double baseSuccessRate, List<Output> outputs) {
        public boolean matches(ItemStack primaryStack, ItemStack catalystStack) {
            return primary.matchesWithCount(primaryStack) && catalyst.matchesWithCount(catalystStack);
        }

        public boolean isAvailable() {
            return primary.isAvailable() && catalyst.isAvailable();
        }

        public Input input(int index) {
            return index == 0 ? primary : catalyst;
        }

        public List<ItemStack> primaryStacks() {
            return primary.displayStacks();
        }

        public List<ItemStack> catalystStacks() {
            return catalyst.displayStacks();
        }

        public List<ItemStack> outputStacks() {
            return outputs.stream().map(Output::stack).toList();
        }

        public double displayedFieldDraw() {
            return fieldDraw > 0.0D ? fieldDraw : Math.max(1.0D, minField / 8.0D);
        }
    }

    public record Input(@Nullable TagKey<Item> tag, @Nullable Identifier optionalItemId,
                        Supplier<? extends ItemLike> fallback, int count) {
        public boolean matches(ItemStack stack) {
            Item fallbackItem = fallback.get().asItem();
            if (stack.getItem() == fallbackItem || (tag != null && stack.is(tag))) {
                return true;
            }
            if (optionalItemId != null) {
                Optional<Item> opt = BuiltInRegistries.ITEM.getOptional(optionalItemId);
                if (opt.isPresent() && stack.is(opt.get())) {
                    return true;
                }
            }
            return false;
        }

        public boolean matchesWithCount(ItemStack stack) {
            return matches(stack) && stack.getCount() >= count;
        }

        public boolean isAvailable() {
            return true;
        }

        public List<ItemStack> displayStacks() {
            Item fallbackItem = fallback.get().asItem();
            List<ItemStack> stacks = new ArrayList<>();
            if (optionalItemId != null) {
                BuiltInRegistries.ITEM.getOptional(optionalItemId)
                        .ifPresent(it -> stacks.add(new ItemStack(it, count)));
            }
            if (tag != null) {
                for (var holder : BuiltInRegistries.ITEM.getTagOrEmpty(tag)) {
                    stacks.add(new ItemStack(holder.value(), count));
                }
            }
            if (stacks.stream().noneMatch(stack -> stack.getItem() == fallbackItem)) {
                stacks.add(new ItemStack(fallbackItem, count));
            }
            return List.copyOf(stacks);
        }
    }

    public record Output(Supplier<? extends ItemLike> item, int count) {
        public ItemStack stack() {
            return new ItemStack(item.get().asItem(), count);
        }
    }

    public enum Kind {
        ALCHEMY_FURNACE("alchemy_furnace", "丹炉", "把灵草、灵泉和灵晶炼成可直接使用的丹药。"),
        TALISMAN_TABLE("talisman_table", "符案", "在绘制时把大量环境灵气封入符纸，制成使用时只需微量法力激发的一次性符箓。"),
        SPIRIT_ARRAY_CORE("spirit_array_core", "便携聚灵阵", "单格时是便携聚灵阵；按 3x3 布阵后成为正式聚灵阵。");

        private final String blockId;
        private final String fallbackName;
        private final String fallbackDescription;

        Kind(String blockId, String fallbackName, String fallbackDescription) {
            this.blockId = blockId;
            this.fallbackName = fallbackName;
            this.fallbackDescription = fallbackDescription;
        }

        public String blockId() {
            return blockId;
        }

        public String displayNameKey() {
            return "block.xuanhuan_earth." + blockId;
        }

        public String descriptionKey() {
            return "tooltip.xuanhuan_earth.machine." + blockId + ".description";
        }

        public String localizedDisplayName() {
            return Language.getInstance().getOrDefault(displayNameKey(), fallbackName);
        }

        public String fallbackDescription() {
            return fallbackDescription;
        }
    }
}
