package com.xxsx.xuanhuanearth;

import com.mojang.serialization.Codec;
import com.mojang.logging.LogUtils;
import com.xxsx.earthlore.registry.LoreContent;
import com.xxsx.xuanhuanearth.client.XuanhuanEarthClient;
import com.xxsx.xuanhuanearth.entity.ContractableSpiritBeastEntity;
import com.xxsx.xuanhuanearth.entity.CrystalTurtleEntity;
import com.xxsx.xuanhuanearth.entity.CultivationSettlerEntity;
import com.xxsx.xuanhuanearth.entity.EmberCraneEntity;
import com.xxsx.xuanhuanearth.entity.SpiritFoxEntity;
import com.xxsx.xuanhuanearth.entity.StonehornGoatEntity;
import com.xxsx.xuanhuanearth.entity.WindWolfEntity;
import com.xxsx.xuanhuanearth.particle.XuanhuanParticles;
import com.xxsx.xuanhuanearth.settlement.XuanhuanSettlementAnchorBlock;
import com.xxsx.xuanhuanearth.settlement.XuanhuanSettlementAnchorBlockEntity;
import com.xxsx.xuanhuanearth.settlement.XuanhuanSettlementCatalog;
import com.xxsx.xuanhuanearth.settlement.XuanhuanSettlementFeature;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.RegisterEvent;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

@Mod(XuanhuanEarth.MODID)
public class XuanhuanEarth {
    public static final String MODID = "xuanhuan_earth";
    private static final String LEGACY_MODID = "earth_online_xuanhuan";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MODID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(Registries.ENTITY_TYPE, MODID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, MODID);
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, MODID);
    public static final DeferredRegister<Feature<?>> FEATURES = DeferredRegister.create(Registries.FEATURE, MODID);
    public static final DeferredRegister<MobEffect> MOB_EFFECTS = DeferredRegister.create(Registries.MOB_EFFECT, MODID);
    public static final DeferredRegister.DataComponents DATA_COMPONENTS =
            DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, MODID);

    public static final DeferredHolder<MobEffect, AuspiciousMobEffect> AUSPICIOUS =
            MOB_EFFECTS.register("auspicious", AuspiciousMobEffect::new);

    public static final DeferredHolder<net.minecraft.core.component.DataComponentType<?>,
            net.minecraft.core.component.DataComponentType<PillComponent>> PILL_COMPONENT =
            DATA_COMPONENTS.registerComponentType("pill", builder -> builder
                    .persistent(PillComponent.CODEC)
                    .networkSynchronized(ByteBufCodecs.fromCodec(PillComponent.CODEC)));
    public static final DeferredHolder<net.minecraft.core.component.DataComponentType<?>,
            net.minecraft.core.component.DataComponentType<JadeComponent>> JADE_COMPONENT =
            DATA_COMPONENTS.registerComponentType("jade", builder -> builder
                    .persistent(JadeComponent.CODEC)
                    .networkSynchronized(ByteBufCodecs.fromCodec(JadeComponent.CODEC)));
    public static final DeferredHolder<net.minecraft.core.component.DataComponentType<?>,
            net.minecraft.core.component.DataComponentType<RuneStoneComponent>> RUNE_STONE_COMPONENT =
            DATA_COMPONENTS.registerComponentType("rune_stone", builder -> builder
                    .persistent(RuneStoneComponent.CODEC)
                    .networkSynchronized(ByteBufCodecs.fromCodec(RuneStoneComponent.CODEC)));
    public static final DeferredHolder<net.minecraft.core.component.DataComponentType<?>,
            net.minecraft.core.component.DataComponentType<ArrayControlTabletItem.TabletState>> ARRAY_TABLET_COMPONENT =
            DATA_COMPONENTS.registerComponentType("array_tablet", builder -> builder
                    .persistent(ArrayControlTabletItem.TabletState.CODEC)
                    .networkSynchronized(ByteBufCodecs.fromCodec(ArrayControlTabletItem.TabletState.CODEC)));
    public static final DeferredHolder<net.minecraft.core.component.DataComponentType<?>,
            net.minecraft.core.component.DataComponentType<Integer>> LUOPAN_RATING =
            DATA_COMPONENTS.registerComponentType("luopan_rating", builder -> builder
                    .persistent(Codec.intRange(0, 9))
                    .networkSynchronized(ByteBufCodecs.VAR_INT));

    private static final List<ItemLike> TAB_ITEMS = new ArrayList<>();
    private static final List<DeferredBlock<XuanhuanMachineBlock>> MACHINE_BLOCKS = new ArrayList<>();

    public static final DeferredBlock<XuanhuanBlock> SPIRIT_VEIN_NODE = block("spirit_vein_node",
            () -> stone(MapColor.COLOR_PURPLE, 4.0F), "message.xuanhuan_earth.block.spirit_vein_node");
    public static final DeferredBlock<XuanhuanBlock> SPIRIT_SPRING_STONE = block("spirit_spring_stone",
            () -> stone(MapColor.WATER, 2.5F), "message.xuanhuan_earth.block.spirit_spring_stone");
    public static final DeferredBlock<XuanhuanBlock> SPIRIT_SOIL = block("spirit_soil",
            () -> BlockBehaviour.Properties.of().mapColor(MapColor.DIRT).strength(0.6F).sound(SoundType.GRAVEL),
            "message.xuanhuan_earth.block.spirit_soil");
    public static final DeferredBlock<XuanhuanBlock> ARRAY_PILLAR = block("array_pillar",
            () -> stone(MapColor.COLOR_CYAN, 3.0F), "message.xuanhuan_earth.block.array_pillar");
    public static final DeferredBlock<MeditationCushionBlock> MEDITATION_CUSHION = meditationBlock("meditation_cushion",
            () -> BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_RED).strength(0.25F).sound(SoundType.WOOL).noOcclusion());
    public static final DeferredHolder<EntityType<?>, EntityType<MeditationSeatEntity>> MEDITATION_SEAT =
            ENTITY_TYPES.register("meditation_seat", () -> EntityType.Builder
                    .of(MeditationSeatEntity::new, MobCategory.MISC)
                    .sized(0.01F, 0.01F)
                    .passengerAttachments(new Vec3(0.0D, 0.18D, 0.0D))
                    .clientTrackingRange(4)
                    .updateInterval(20)
                    .noSummon()
                    .build(ResourceKey.create(Registries.ENTITY_TYPE, id("meditation_seat"))));
    public static final DeferredHolder<EntityType<?>, EntityType<SpiritFoxEntity>> SPIRIT_FOX =
            ENTITY_TYPES.register("spirit_fox", () -> EntityType.Builder
                    .of(SpiritFoxEntity::new, MobCategory.CREATURE)
                    .sized(0.75F, 0.8F)
                    .eyeHeight(0.58F)
                    .clientTrackingRange(10)
                    .build(ResourceKey.create(Registries.ENTITY_TYPE, id("spirit_fox"))));
    public static final DeferredHolder<EntityType<?>, EntityType<WindWolfEntity>> WIND_WOLF =
            ENTITY_TYPES.register("wind_wolf", () -> EntityType.Builder
                    .of(WindWolfEntity::new, MobCategory.CREATURE)
                    .sized(0.9F, 1.15F)
                    .eyeHeight(0.86F)
                    .clientTrackingRange(10)
                    .build(ResourceKey.create(Registries.ENTITY_TYPE, id("wind_wolf"))));
    public static final DeferredHolder<EntityType<?>, EntityType<StonehornGoatEntity>> STONEHORN_GOAT =
            ENTITY_TYPES.register("stonehorn_goat", () -> EntityType.Builder
                    .of(StonehornGoatEntity::new, MobCategory.CREATURE)
                    .sized(1.25F, 1.5F)
                    .eyeHeight(1.22F)
                    .clientTrackingRange(10)
                    .build(ResourceKey.create(Registries.ENTITY_TYPE, id("stonehorn_goat"))));
    public static final DeferredHolder<EntityType<?>, EntityType<CrystalTurtleEntity>> CRYSTAL_TURTLE =
            ENTITY_TYPES.register("crystal_turtle", () -> EntityType.Builder
                    .of(CrystalTurtleEntity::new, MobCategory.CREATURE)
                    .sized(1.25F, 0.72F)
                    .eyeHeight(0.42F)
                    .clientTrackingRange(10)
                    .build(ResourceKey.create(Registries.ENTITY_TYPE, id("crystal_turtle"))));
    public static final DeferredHolder<EntityType<?>, EntityType<EmberCraneEntity>> EMBER_CRANE =
            ENTITY_TYPES.register("ember_crane", () -> EntityType.Builder
                    .of(EmberCraneEntity::new, MobCategory.CREATURE)
                    .sized(0.78F, 1.82F)
                    .eyeHeight(1.58F)
                    .clientTrackingRange(10)
                    .build(ResourceKey.create(Registries.ENTITY_TYPE, id("ember_crane"))));
    public static final DeferredHolder<EntityType<?>, EntityType<CultivationSettlerEntity>> CULTIVATION_SETTLER =
            ENTITY_TYPES.register("cultivation_settler", () -> EntityType.Builder
                    .of(CultivationSettlerEntity::new, MobCategory.CREATURE)
                    .sized(0.6F, 1.95F)
                    .eyeHeight(1.62F)
                    .clientTrackingRange(10)
                    .build(ResourceKey.create(Registries.ENTITY_TYPE, id("cultivation_settler"))));
    public static final DeferredBlock<XuanhuanSettlementAnchorBlock> SETTLEMENT_ANCHOR =
            BLOCKS.registerBlock("settlement_anchor", XuanhuanSettlementAnchorBlock::new,
                    () -> stone(MapColor.COLOR_CYAN, 5.0F).noOcclusion());
    public static final DeferredBlock<XuanhuanMachineBlock> ALCHEMY_FURNACE = machineBlock("alchemy_furnace",
            XuanhuanMachineBlock.Kind.ALCHEMY_FURNACE, () -> stone(MapColor.TERRACOTTA_BLACK, 3.5F));
    public static final DeferredBlock<XuanhuanMachineBlock> TALISMAN_TABLE = machineBlock("talisman_table",
            XuanhuanMachineBlock.Kind.TALISMAN_TABLE,
            () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(2.5F, 3.0F).sound(SoundType.WOOD));
    public static final DeferredBlock<XuanhuanMachineBlock> SPIRIT_ARRAY_CORE = machineBlock("spirit_array_core",
            XuanhuanMachineBlock.Kind.SPIRIT_ARRAY_CORE, () -> stone(MapColor.COLOR_CYAN, 4.0F));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<XuanhuanMachineBlockEntity>> XUANHUAN_MACHINE_BLOCK_ENTITY =
            BLOCK_ENTITY_TYPES.register("xuanhuan_machine", () -> new BlockEntityType<>(
                    XuanhuanMachineBlockEntity::new,
                    MACHINE_BLOCKS.stream().map(DeferredBlock::get).toArray(Block[]::new)));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<XuanhuanSettlementAnchorBlockEntity>> SETTLEMENT_ANCHOR_BLOCK_ENTITY =
            BLOCK_ENTITY_TYPES.register("settlement_anchor", () -> new BlockEntityType<>(
                    XuanhuanSettlementAnchorBlockEntity::new, SETTLEMENT_ANCHOR.get()));
    public static final DeferredHolder<MenuType<?>, MenuType<XuanhuanMachineMenu>> XUANHUAN_MACHINE_MENU =
            MENUS.register("xuanhuan_machine", () -> IMenuTypeExtension.create(XuanhuanMachineMenu::new));

    public static final DeferredItem<XuanhuanHandbookItem> XUANHUAN_HANDBOOK = item("xuanhuan_handbook",
            props -> new XuanhuanHandbookItem(props.stacksTo(1)));
    public static final DeferredItem<QiGuidingManualItem> QI_GUIDING_MANUAL = item("qi_guiding_manual",
            props -> new QiGuidingManualItem(props.stacksTo(1)
                    .component(LoreContent.LORE_TEXT.get(), QiGuidingManualItem.defaultLoreText())));
    public static final DeferredItem<CultivationManualItem> BIGU_MANUAL = item("bigu_manual",
            props -> new CultivationManualItem(props.stacksTo(1)
                    .component(LoreContent.LORE_TEXT.get(), CultivationManualItem.Type.BIGU.defaultLoreText()),
                    CultivationManualItem.Type.BIGU));
    public static final DeferredItem<CultivationManualItem> BODY_TEMPERING_MANUAL = item("body_tempering_manual",
            props -> new CultivationManualItem(props.stacksTo(1)
                    .component(LoreContent.LORE_TEXT.get(), CultivationManualItem.Type.BODY_TEMPERING.defaultLoreText()),
                    CultivationManualItem.Type.BODY_TEMPERING));
    public static final DeferredItem<CultivationManualItem> FETAL_BREATH_MANUAL = item("fetal_breath_manual",
            props -> new CultivationManualItem(props.stacksTo(1)
                    .component(LoreContent.LORE_TEXT.get(), CultivationManualItem.Type.FETAL_BREATH.defaultLoreText()),
                    CultivationManualItem.Type.FETAL_BREATH));
    public static final DeferredItem<CultivationJournalItem> CULTIVATION_JOURNAL = item("cultivation_journal",
            props -> new CultivationJournalItem(props.stacksTo(1), "tooltip.xuanhuan_earth.cultivation_journal"));
    public static final DeferredItem<SpiritCompassItem> SPIRIT_COMPASS = item("spirit_compass",
            props -> new SpiritCompassItem(props.stacksTo(1)));
    public static final DeferredItem<GuidedXuanhuanItem> SPIRIT_SPRING_BOTTLE = item("spirit_spring_bottle",
            props -> new GuidedXuanhuanItem(props.stacksTo(16), "tooltip.xuanhuan_earth.spirit_spring_bottle"));
    public static final DeferredItem<GuidedXuanhuanItem> SPIRIT_GRASS = material("spirit_grass", "tooltip.xuanhuan_earth.spirit_grass");
    public static final DeferredItem<GuidedXuanhuanItem> SPIRIT_HERB = material("spirit_herb", "tooltip.xuanhuan_earth.spirit_herb");
    public static final DeferredItem<GuidedXuanhuanItem> CINNABAR_SPIRIT_INK = material("cinnabar_spirit_ink", "tooltip.xuanhuan_earth.cinnabar_spirit_ink");
    public static final DeferredItem<GuidedXuanhuanItem> TALISMAN_PAPER = material("talisman_paper", "tooltip.xuanhuan_earth.talisman_paper");
    public static final DeferredHolder<net.minecraft.core.component.DataComponentType<?>,
            net.minecraft.core.component.DataComponentType<SpiritStoneComponent>> SPIRIT_STONE_CHARGE =
            DATA_COMPONENTS.registerComponentType("spirit_stone_charge", builder -> builder
                    .persistent(SpiritStoneComponent.CODEC)
                    .networkSynchronized(net.minecraft.network.codec.ByteBufCodecs.fromCodec(SpiritStoneComponent.CODEC)));
    public static final DeferredHolder<net.minecraft.core.component.DataComponentType<?>,
            net.minecraft.core.component.DataComponentType<TalismanComponent>> TALISMAN_CHARGE =
            DATA_COMPONENTS.registerComponentType("talisman_charge", builder -> builder
                    .persistent(TalismanComponent.CODEC)
                    .networkSynchronized(net.minecraft.network.codec.ByteBufCodecs.fromCodec(TalismanComponent.CODEC)));
    public static final DeferredItem<GuidedXuanhuanItem> EMPTY_TALISMAN = item("empty_talisman",
            props -> new GuidedXuanhuanItem(props.stacksTo(16), "tooltip.xuanhuan_earth.empty_talisman"));
    public static final DeferredItem<BasicTalismanItem> BASIC_TALISMAN = item("basic_talisman",
            props -> new BasicTalismanItem(props.stacksTo(16)
                    .component(TALISMAN_CHARGE.get(), new TalismanComponent(TalismanCharge.CRAFTED_CHARGE_AEU, TalismanComponent.CURRENT_SCHEMA)),
                    "tooltip.xuanhuan_earth.basic_talisman"));
    public static final DeferredItem<ElementalTalismanItem> FIRE_TALISMAN = item("fire_talisman",
            props -> new ElementalTalismanItem(props.stacksTo(16)
                    .component(TALISMAN_CHARGE.get(), new TalismanComponent(TalismanCharge.CRAFTED_CHARGE_AEU, TalismanComponent.CURRENT_SCHEMA)),
                    "tooltip.xuanhuan_earth.fire_talisman", ElementalTalismanItem.Kind.FIRE));
    public static final DeferredItem<ElementalTalismanItem> WIND_TALISMAN = item("wind_talisman",
            props -> new ElementalTalismanItem(props.stacksTo(16)
                    .component(TALISMAN_CHARGE.get(), new TalismanComponent(TalismanCharge.CRAFTED_CHARGE_AEU, TalismanComponent.CURRENT_SCHEMA)),
                    "tooltip.xuanhuan_earth.wind_talisman", ElementalTalismanItem.Kind.WIND));
    public static final DeferredItem<ElementalTalismanItem> SEAL_TALISMAN = item("seal_talisman",
            props -> new ElementalTalismanItem(props.stacksTo(16)
                    .component(TALISMAN_CHARGE.get(), new TalismanComponent(TalismanCharge.CRAFTED_CHARGE_AEU, TalismanComponent.CURRENT_SCHEMA)),
                    "tooltip.xuanhuan_earth.seal_talisman", ElementalTalismanItem.Kind.SEAL));
    public static final DeferredItem<TalismanBrushItem> TALISMAN_BRUSH = item("talisman_brush",
            props -> new TalismanBrushItem(props.durability(TalismanBrushItem.MAX_DURABILITY), "tooltip.xuanhuan_earth.talisman_brush"));
    public static final DeferredItem<GuidedXuanhuanItem> SPIRIT_CRYSTAL_SHARD = material("spirit_crystal_shard", "tooltip.xuanhuan_earth.spirit_crystal_shard");
    public static final DeferredItem<GuidedXuanhuanItem> SPIRIT_DUST = material("spirit_dust", "tooltip.xuanhuan_earth.spirit_dust");
    public static final DeferredItem<SpiritStoneRawItem> SPIRIT_STONE_RAW = item("spirit_stone_raw",
            props -> new SpiritStoneRawItem(props.stacksTo(16), "tooltip.xuanhuan_earth.spirit_stone_raw"));
    public static final DeferredItem<SpiritStoneItem> SPIRIT_STONE = item("spirit_stone",
            props -> new SpiritStoneItem(props.stacksTo(1), "tooltip.xuanhuan_earth.spirit_stone"));
    public static final DeferredItem<SpiritStoneItem> SPIRIT_STONE_CRACKED = item("spirit_stone_cracked",
            props -> new SpiritStoneItem(props.stacksTo(1), "tooltip.xuanhuan_earth.spirit_stone_cracked", true));
    public static final DeferredItem<SpiritStoneCoreItem> SPIRIT_STONE_CORE = item("spirit_stone_core",
            props -> new SpiritStoneCoreItem(props.stacksTo(1), "tooltip.xuanhuan_earth.spirit_stone_core"));
    public static final DeferredItem<GuidedXuanhuanItem> SPIRIT_IRON_BLANK = material("spirit_iron_blank", "tooltip.xuanhuan_earth.spirit_iron_blank");
    public static final DeferredItem<GuidedXuanhuanItem> SPIRIT_IRON_INGOT = material("spirit_iron_ingot", "tooltip.xuanhuan_earth.spirit_iron_ingot");
    public static final DeferredItem<GuidedXuanhuanItem> SPIRIT_GLASS = material("spirit_glass", "tooltip.xuanhuan_earth.spirit_glass");
    public static final DeferredItem<GuidedXuanhuanItem> SPIRIT_THREAD = material("spirit_thread", "tooltip.xuanhuan_earth.spirit_thread");
    public static final DeferredItem<QiRecoveryPillItem> QI_RECOVERY_PILL = item("qi_recovery_pill",
            props -> new QiRecoveryPillItem(props.stacksTo(1)
                    .component(PILL_COMPONENT.get(), PillComponent.ofDefault("qi_recovery_pill")),
                    "tooltip.xuanhuan_earth.qi_recovery_pill"));
    public static final DeferredItem<XuanhuanPillItem> FOUNDATION_PILL = pill(XuanhuanPills.FOUNDATION);
    public static final DeferredItem<XuanhuanPillItem> IRONBODY_PILL = pill(XuanhuanPills.IRONBODY);
    public static final DeferredItem<XuanhuanPillItem> CLOUDBREATH_PILL = pill(XuanhuanPills.CLOUDBREATH);
    public static final DeferredItem<XuanhuanPillItem> GRAINFAST_PILL = pill(XuanhuanPills.GRAINFAST);
    public static final DeferredItem<XuanhuanPillItem> BREAKTHROUGH_PILL = pill(XuanhuanPills.BREAKTHROUGH);
    public static final DeferredItem<XuanhuanPillItem> NIGHTSIGHT_PILL = pill(XuanhuanPills.NIGHTSIGHT);
    public static final DeferredItem<XuanhuanPillItem> MARROW_PILL = pill(XuanhuanPills.MARROW);
    public static final DeferredItem<XuanhuanPillItem> BREATH_PILL = pill(XuanhuanPills.BREATH);
    public static final DeferredItem<XuanhuanPillItem> FAST_PILL = pill(XuanhuanPills.FAST);
    public static final DeferredItem<XuanhuanPillItem> DETOX_PILL = pill(XuanhuanPills.DETOX);
    public static final DeferredItem<GuidedXuanhuanItem> PILL_RESIDUE = material("pill_residue", "tooltip.xuanhuan_earth.pill_residue");
    public static final DeferredItem<XuanhuanPillItem> FAILED_PILL = item("failed_pill",
            props -> new XuanhuanPillItem(props.stacksTo(64), XuanhuanPills.FAILED.hintKey(), XuanhuanPills.FAILED));
    public static final DeferredItem<XuanhuanPillItem> NOURISH_QI_PILL = pill(XuanhuanPills.NOURISH_QI);
    public static final DeferredItem<XuanhuanPillItem> CALM_SPIRIT_PILL = pill(XuanhuanPills.CALM_SPIRIT);
    public static final DeferredItem<XuanhuanPillItem> CLEAR_HEAT_PILL = pill(XuanhuanPills.CLEAR_HEAT);
    public static final DeferredItem<XuanhuanPillItem> INVIGORATE_BLOOD_PILL = pill(XuanhuanPills.INVIGORATE_BLOOD);
    public static final DeferredItem<XuanhuanPillItem> RESOLVE_TOXIN_PILL = pill(XuanhuanPills.RESOLVE_TOXIN);

    public static final DeferredItem<JadeSlipItem> JADE_SLIP = item("jade_slip",
            props -> new JadeSlipItem(props.stacksTo(1).component(JADE_COMPONENT.get(), JadeComponent.EMPTY),
                    "tooltip.xuanhuan_earth.jade_slip"));
    public static final DeferredItem<JadeWardAmuletItem> JADE_WARD_AMULET = item("jade_ward_amulet",
            props -> new JadeWardAmuletItem(props.stacksTo(1).component(JADE_COMPONENT.get(), JadeComponent.EMPTY),
                    "tooltip.xuanhuan_earth.jade_ward_amulet"));
    public static final DeferredItem<JadeArrayPlateItem> JADE_ARRAY_PLATE = item("jade_array_plate",
            props -> new JadeArrayPlateItem(props.stacksTo(1).component(JADE_COMPONENT.get(), JadeComponent.EMPTY),
                    "tooltip.xuanhuan_earth.jade_array_plate"));
    public static final DeferredItem<ArrayRuneStoneItem> ARRAY_RUNE_STONE = item("array_rune_stone",
            props -> new ArrayRuneStoneItem(props.component(RUNE_STONE_COMPONENT.get(), RuneStoneComponent.DEFAULT),
                    "tooltip.xuanhuan_earth.array_rune_stone"));
    public static final DeferredItem<ArrayControlTabletItem> ARRAY_CONTROL_TABLET = item("array_control_tablet",
            props -> new ArrayControlTabletItem(props.stacksTo(1)
                    .component(ARRAY_TABLET_COMPONENT.get(), ArrayControlTabletItem.TabletState.DEFAULT),
                    "tooltip.xuanhuan_earth.array_control_tablet"));
    public static final DeferredItem<LuopanItem> LUOPAN = item("luopan",
            props -> new LuopanItem(props.stacksTo(1), "tooltip.xuanhuan_earth.luopan"));
    public static final DeferredItem<DragonSeekingRodItem> DRAGON_SEEKING_ROD = item("dragon_seeking_rod",
            props -> new DragonSeekingRodItem(props.durability(DragonSeekingRodItem.MAX_DURABILITY),
                    "tooltip.xuanhuan_earth.dragon_seeking_rod"));
    public static final DeferredItem<DivinationCoinsItem> DIVINATION_COINS = item("divination_coins",
            props -> new DivinationCoinsItem(props.stacksTo(1), "tooltip.xuanhuan_earth.divination_coins"));
    public static final DeferredItem<OracleBoneItem> ORACLE_BONE = item("oracle_bone",
            props -> new OracleBoneItem(props.stacksTo(16), "tooltip.xuanhuan_earth.oracle_bone"));

    public static final DeferredHolder<net.minecraft.core.component.DataComponentType<?>,
            net.minecraft.core.component.DataComponentType<ImplementComponent>> IMPLEMENT_CHARGE =
            DATA_COMPONENTS.registerComponentType("implement_charge", builder -> builder
                    .persistent(ImplementComponent.CODEC)
                    .networkSynchronized(net.minecraft.network.codec.ByteBufCodecs.fromCodec(
                            ImplementComponent.CODEC)));
    public static final DeferredItem<SpiritImplementItem> WIND_FAN =
            implement(SpiritImplementItem.Implement.WIND_FAN);
    public static final DeferredItem<SpiritImplementItem> STONE_SEAL =
            implement(SpiritImplementItem.Implement.STONE_SEAL);
    public static final DeferredItem<SpiritImplementItem> GATHER_ORB =
            implement(SpiritImplementItem.Implement.GATHER_ORB);
    public static final DeferredItem<SpiritImplementItem> LANTERN_ROD =
            implement(SpiritImplementItem.Implement.LANTERN_ROD);
    public static final DeferredItem<GuidedXuanhuanItem> SPIRIT_BEAST_SEAL = material(
            "spirit_beast_seal", "tooltip.xuanhuan_earth.spirit_beast_seal");
    public static final DeferredItem<SpawnEggItem> SPIRIT_FOX_SPAWN_EGG = item("spirit_fox_spawn_egg",
            props -> new SpawnEggItem(props.spawnEgg(SPIRIT_FOX.get())));
    public static final DeferredItem<SpawnEggItem> WIND_WOLF_SPAWN_EGG = item("wind_wolf_spawn_egg",
            props -> new SpawnEggItem(props.spawnEgg(WIND_WOLF.get())));
    public static final DeferredItem<SpawnEggItem> STONEHORN_GOAT_SPAWN_EGG = item("stonehorn_goat_spawn_egg",
            props -> new SpawnEggItem(props.spawnEgg(STONEHORN_GOAT.get())));
    public static final DeferredItem<SpawnEggItem> CRYSTAL_TURTLE_SPAWN_EGG = item("crystal_turtle_spawn_egg",
            props -> new SpawnEggItem(props.spawnEgg(CRYSTAL_TURTLE.get())));
    public static final DeferredItem<SpawnEggItem> EMBER_CRANE_SPAWN_EGG = item("ember_crane_spawn_egg",
            props -> new SpawnEggItem(props.spawnEgg(EMBER_CRANE.get())));
    public static final DeferredItem<SpawnEggItem> CULTIVATION_SETTLER_SPAWN_EGG = item("cultivation_settler_spawn_egg",
            props -> new SpawnEggItem(props.spawnEgg(CULTIVATION_SETTLER.get())));

    public static final DeferredHolder<Feature<?>, XuanhuanSettlementFeature> WANDERING_MARKET_FEATURE =
            FEATURES.register("wandering_cultivator_market", () -> new XuanhuanSettlementFeature(
                    NoneFeatureConfiguration.CODEC, XuanhuanSettlementFeature.Type.WANDERING_MARKET));
    public static final DeferredHolder<Feature<?>, XuanhuanSettlementFeature> SPIRIT_SPRING_VILLAGE_FEATURE =
            FEATURES.register("spirit_spring_village", () -> new XuanhuanSettlementFeature(
                    NoneFeatureConfiguration.CODEC, XuanhuanSettlementFeature.Type.SPIRIT_SPRING_VILLAGE));
    public static final DeferredHolder<Feature<?>, XuanhuanSettlementFeature> SECT_OUTPOST_FEATURE =
            FEATURES.register("sect_frontier_outpost", () -> new XuanhuanSettlementFeature(
                    NoneFeatureConfiguration.CODEC, XuanhuanSettlementFeature.Type.SECT_OUTPOST));
    public static final DeferredHolder<Feature<?>, XuanhuanSettlementFeature> MOUNTAIN_SHRINE_FEATURE =
            FEATURES.register("mountain_spirit_shrine", () -> new XuanhuanSettlementFeature(
                    NoneFeatureConfiguration.CODEC, XuanhuanSettlementFeature.Type.MOUNTAIN_SPIRIT_SHRINE));
    public static final DeferredHolder<Feature<?>, XuanhuanSettlementFeature> KEEPER_CAMP_FEATURE =
            FEATURES.register("spirit_beast_keeper_camp", () -> new XuanhuanSettlementFeature(
                    NoneFeatureConfiguration.CODEC, XuanhuanSettlementFeature.Type.SPIRIT_BEAST_KEEPER_CAMP));

    public XuanhuanEarth(IEventBus modBus) {
        registerLegacyAliases();
        DATA_COMPONENTS.register(modBus);
        MOB_EFFECTS.register(modBus);
        BLOCKS.register(modBus);
        ITEMS.register(modBus);
        ENTITY_TYPES.register(modBus);
        BLOCK_ENTITY_TYPES.register(modBus);
        MENUS.register(modBus);
        FEATURES.register(modBus);
        XuanhuanParticles.PARTICLE_TYPES.register(modBus);
        XuanhuanLoreIntegration.init();
        modBus.addListener(CultivationNetwork::registerPayloads);
        modBus.addListener(this::registerEntityAttributes);
        modBus.addListener(this::registerSpawnPlacements);
        NeoForge.EVENT_BUS.addListener(ArcanaEvents::onPlayerClone);
        NeoForge.EVENT_BUS.addListener(ArcanaPassiveEffects::onPlayerTick);
        NeoForge.EVENT_BUS.addListener(ArcanaPassiveEffects::onIncomingDamage);
        NeoForge.EVENT_BUS.addListener(ArcanaPassiveEffects::onFall);
        NeoForge.EVENT_BUS.addListener(ArcanaPassiveEffects::onBreathe);
        NeoForge.EVENT_BUS.addListener(XuanhuanSettlementCatalog::register);
        NeoForge.EVENT_BUS.addListener(SpellGraphLoader::register);
        NeoForge.EVENT_BUS.addListener(XuanhuanLoreIntegration::onLoreLearned);
        modBus.addListener(this::registerCreativeTab);
        if (FMLEnvironment.getDist() == Dist.CLIENT) {
            XuanhuanEarthClient.register(modBus);
        }
        LOGGER.info("[Xuanhuan Earth] NeoForge 26.2 module loaded");
    }

    private static DeferredBlock<XuanhuanBlock> block(String id, Supplier<BlockBehaviour.Properties> props, String hintKey) {
        DeferredBlock<XuanhuanBlock> block = BLOCKS.registerBlock(id, p -> new XuanhuanBlock(p, hintKey), props);
        DeferredItem<?> item = ITEMS.registerItem(id,
                itemProps -> new GuidedXuanhuanBlockItem(block.get(), itemProps, "tooltip.xuanhuan_earth.block"),
                itemProps -> itemProps);
        TAB_ITEMS.add(item);
        return block;
    }

    private static DeferredBlock<XuanhuanMachineBlock> machineBlock(String id, XuanhuanMachineBlock.Kind kind,
                                                                    Supplier<BlockBehaviour.Properties> props) {
        DeferredBlock<XuanhuanMachineBlock> block = BLOCKS.registerBlock(id, p -> new XuanhuanMachineBlock(p, kind), props);
        DeferredItem<?> item = ITEMS.registerItem(id,
                itemProps -> new GuidedXuanhuanBlockItem(block.get(), itemProps, "tooltip.xuanhuan_earth.machine.block"),
                itemProps -> itemProps);
        MACHINE_BLOCKS.add(block);
        TAB_ITEMS.add(item);
        return block;
    }

    private static DeferredBlock<MeditationCushionBlock> meditationBlock(String id, Supplier<BlockBehaviour.Properties> props) {
        DeferredBlock<MeditationCushionBlock> block = BLOCKS.registerBlock(id, MeditationCushionBlock::new, props);
        DeferredItem<?> item = ITEMS.registerItem(id,
                itemProps -> new GuidedXuanhuanBlockItem(block.get(), itemProps, "tooltip.xuanhuan_earth.meditation_cushion"),
                itemProps -> itemProps);
        TAB_ITEMS.add(item);
        return block;
    }

    private static BlockBehaviour.Properties stone(MapColor color, float strength) {
        return BlockBehaviour.Properties.of()
                .mapColor(color)
                .strength(strength, strength * 2.0F)
                .requiresCorrectToolForDrops()
                .sound(SoundType.STONE);
    }

    private static DeferredItem<GuidedXuanhuanItem> material(String id, String hintKey) {
        return item(id, props -> new GuidedXuanhuanItem(props, hintKey));
    }

    private static DeferredItem<SpiritImplementItem> implement(SpiritImplementItem.Implement kind) {
        return item(kind.id(), props -> new SpiritImplementItem(props.stacksTo(1), kind.hintKey(), kind));
    }

    private static DeferredItem<XuanhuanPillItem> pill(XuanhuanPillItem.Pill pill) {
        return item(pill.id(), props -> new XuanhuanPillItem(
                props.stacksTo(1).component(PILL_COMPONENT.get(), PillComponent.ofDefault(pill.id())),
                pill.hintKey(), pill));
    }

    private static <T extends Item> DeferredItem<T> item(String id, Function<Item.Properties, T> factory) {
        DeferredItem<T> item = ITEMS.registerItem(id, factory, props -> props);
        TAB_ITEMS.add(item);
        return item;
    }

    private void registerCreativeTab(RegisterEvent event) {
        var registry = event.getRegistry(Registries.CREATIVE_MODE_TAB);
        if (registry != null) {
            registry.addAlias(
                    Identifier.fromNamespaceAndPath(LEGACY_MODID, LEGACY_MODID),
                    id(MODID));
        }
        event.register(Registries.CREATIVE_MODE_TAB, helper -> helper.register(id("xuanhuan_earth"),
                CreativeModeTab.builder()
                        .title(Component.translatable("itemGroup.xuanhuan_earth"))
                        .icon(() -> new ItemStack(SPIRIT_COMPASS.get()))
                        .displayItems((params, output) -> TAB_ITEMS.forEach(output::accept))
                        .build()));
    }

    private static void registerLegacyAliases() {
        addLegacyAliases(BLOCKS);
        addLegacyAliases(ITEMS);
        addLegacyAliases(ENTITY_TYPES);
        addLegacyAliases(BLOCK_ENTITY_TYPES);
        addLegacyAliases(MENUS);
        addLegacyAliases(FEATURES);
    }

    private static void addLegacyAliases(DeferredRegister<?> register) {
        for (var entry : register.getEntries()) {
            Identifier current = entry.getId();
            register.addAlias(
                    Identifier.fromNamespaceAndPath(LEGACY_MODID, current.getPath()),
                    current);
        }
    }

    private void registerEntityAttributes(EntityAttributeCreationEvent event) {
        event.put(SPIRIT_FOX.get(), SpiritFoxEntity.createAttributes().build());
        event.put(WIND_WOLF.get(), WindWolfEntity.createAttributes().build());
        event.put(STONEHORN_GOAT.get(), StonehornGoatEntity.createAttributes().build());
        event.put(CRYSTAL_TURTLE.get(), CrystalTurtleEntity.createAttributes().build());
        event.put(EMBER_CRANE.get(), EmberCraneEntity.createAttributes().build());
        event.put(CULTIVATION_SETTLER.get(), CultivationSettlerEntity.createAttributes().build());
    }

    private void registerSpawnPlacements(RegisterSpawnPlacementsEvent event) {
        event.register(SPIRIT_FOX.get(), SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                SpiritFoxEntity::checkSpawnRules,
                RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(WIND_WOLF.get(), SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                ContractableSpiritBeastEntity::checkSpawnRules,
                RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(STONEHORN_GOAT.get(), SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                ContractableSpiritBeastEntity::checkSpawnRules,
                RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(CRYSTAL_TURTLE.get(), SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                ContractableSpiritBeastEntity::checkSpawnRules,
                RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(EMBER_CRANE.get(), SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                ContractableSpiritBeastEntity::checkSpawnRules,
                RegisterSpawnPlacementsEvent.Operation.REPLACE);
    }

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MODID, path);
    }
}
