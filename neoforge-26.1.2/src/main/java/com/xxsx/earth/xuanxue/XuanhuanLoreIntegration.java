package com.xxsx.earth.xuanxue;

import com.xxsx.earth.lore.api.GateResult;
import com.xxsx.earth.lore.api.Grade;
import com.xxsx.earth.lore.api.GradeAccess;
import com.xxsx.earth.lore.api.LoreApi;
import com.xxsx.earth.lore.api.LoreLearnedEvent;
import com.xxsx.earth.lore.api.LoreMachines;
import com.xxsx.earth.lore.api.LoreMigrator;
import com.xxsx.earth.lore.data.LoreText;
import com.xxsx.earth.lore.registry.LoreContent;
import com.xxsx.earth.lore.study.LiteratureItem;
import com.xxsx.earth.lore.study.StudyService;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.common.NeoForge;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Optional Earth: Lore (earth_lore) difficulty-gating bridge for Earth: Xuanxue.
 * When earth_lore is installed, knowledge nodes, manual study, machine caps, and readout gates are active.
 * When earth_lore is absent, Earth: Xuanxue runs standalone without knowledge locks and manuals unlock routes directly.
 */
public final class XuanhuanLoreIntegration {
    public static final String LORE_MOD_ID = "earth_lore";
    public static final Identifier DISCIPLINE_XUA = Identifier.fromNamespaceAndPath(LORE_MOD_ID, "xua");

    public static final Identifier K_XUA_01_QI_GUIDING = node("qi_guiding");
    public static final Identifier K_XUA_02_CIRCULATION = node("circulation");
    public static final Identifier K_XUA_03_BODY_TEMPERING = node("body_tempering");
    public static final Identifier K_XUA_04_FETAL_BREATH = node("fetal_breath");
    public static final Identifier K_XUA_05_BIGU = node("bigu");
    public static final Identifier K_XUA_07_QI_REFINING = node("qi_refining");
    public static final Identifier K_XUA_08_FOUNDATION_BUILDING = node("foundation_building");
    public static final Identifier K_XUA_09_GOLDEN_CORE = node("golden_core");
    public static final Identifier K_XUA_10_NASCENT_SOUL = node("nascent_soul");
    public static final Identifier K_XUA_11_SPIRIT_TRANSFORMATION = node("spirit_transformation");
    public static final Identifier K_XUA_13_PILL_REFINING = node("pill_refining");
    public static final Identifier K_XUA_19_FIRE_TALISMAN = node("fire_talisman");
    public static final Identifier K_XUA_21_SEAL_TALISMAN = node("seal_talisman");
    public static final Identifier K_XUA_24_ARRAY_FUNCTIONS = node("array_functions");
    public static final Identifier K_XUA_28_DRAGON_VEIN_SEEKING = node("dragon_vein_seeking");
    public static final Identifier K_XUA_34_SPELL_GRAPHS = node("spell_graphs");

    private static final Identifier[] REALM_NODES = {
            K_XUA_07_QI_REFINING,
            K_XUA_08_FOUNDATION_BUILDING,
            K_XUA_09_GOLDEN_CORE,
            K_XUA_10_NASCENT_SOUL,
            K_XUA_11_SPIRIT_TRANSFORMATION
    };

    private XuanhuanLoreIntegration() {
    }

    public static boolean isLoreLoaded() {
        ModList list = ModList.get();
        return list != null && list.isLoaded(LORE_MOD_ID);
    }

    public static Identifier node(String slug) {
        return Identifier.fromNamespaceAndPath(XuanhuanEarth.MODID, "xua/" + slug);
    }

    public static Identifier realmNode(int nextRealm) {
        if (nextRealm < 1 || nextRealm > REALM_NODES.length) {
            return K_XUA_07_QI_REFINING;
        }
        return REALM_NODES[nextRealm - 1];
    }

    public static Identifier focusNode(CultivationFocus focus) {
        return switch (focus) {
            case CIRCULATION -> K_XUA_02_CIRCULATION;
            case BODY_TEMPERING -> K_XUA_03_BODY_TEMPERING;
            case FETAL_BREATH -> K_XUA_04_FETAL_BREATH;
            case BIGU -> K_XUA_05_BIGU;
        };
    }

    public static Item.Properties attachManualLoreIfPresent(Item.Properties props, List<Identifier> nodes) {
        if (!isLoreLoaded()) {
            return props;
        }
        return LoreBridge.attachManualLore(props, nodes);
    }

    public static void init() {
        if (!isLoreLoaded()) {
            return;
        }
        LoreBridge.init();
    }

    public static Set<Identifier> computeMigratedNodes(int cultivationLevel, int bodyTemperingLevel,
                                                       int fetalBreathLevel, int biguLevel, int realm) {
        Set<Identifier> granted = new LinkedHashSet<>();
        if (cultivationLevel >= 1) {
            granted.add(K_XUA_01_QI_GUIDING);
            granted.add(K_XUA_02_CIRCULATION);
        }
        if (bodyTemperingLevel >= 1) {
            granted.add(K_XUA_03_BODY_TEMPERING);
        }
        if (fetalBreathLevel >= 1) {
            granted.add(K_XUA_04_FETAL_BREATH);
        }
        if (biguLevel >= 1) {
            granted.add(K_XUA_05_BIGU);
        }
        int cappedRealm = Math.max(0, Math.min(REALM_NODES.length, realm));
        for (int r = 1; r <= cappedRealm; r++) {
            granted.add(REALM_NODES[r - 1]);
        }
        return granted;
    }

    static boolean applyDirectNodeUnlock(ServerPlayer player, Identifier nodeId) {
        boolean updated = false;
        if (K_XUA_01_QI_GUIDING.equals(nodeId)) {
            if (ArcanaPower.learnQiGuiding(player)) {
                XuanhuanJourney.complete(player, XuanhuanJourney.Milestone.INITIATION);
                updated = true;
            }
        } else if (K_XUA_02_CIRCULATION.equals(nodeId)) {
            if (ArcanaPower.learnQiGuiding(player)) {
                XuanhuanJourney.complete(player, XuanhuanJourney.Milestone.INITIATION);
                updated = true;
            }
        } else if (K_XUA_03_BODY_TEMPERING.equals(nodeId)) {
            updated = ArcanaPower.learnBodyTemperingFromLore(player);
        } else if (K_XUA_04_FETAL_BREATH.equals(nodeId)) {
            updated = ArcanaPower.learnFetalBreathFromLore(player);
        } else if (K_XUA_05_BIGU.equals(nodeId)) {
            updated = ArcanaPower.learnBiguFromLore(player);
        }
        if (updated) {
            CultivationNetwork.sync(player, player.blockPosition(), false);
        }
        return updated;
    }

    public static void grantNode(ServerPlayer player, Identifier nodeId) {
        if (isLoreLoaded()) {
            LoreBridge.grant(player, nodeId);
        } else {
            applyDirectNodeUnlock(player, nodeId);
        }
    }

    public static InteractionResult useManual(Level level, Player player, InteractionHand hand, ItemStack stack,
                                              List<Identifier> nodes) {
        if (isLoreLoaded()) {
            return LoreBridge.useManual(level, player, hand, stack, nodes);
        }
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        if (player instanceof ServerPlayer serverPlayer) {
            boolean unlockedAny = false;
            for (Identifier nodeId : nodes) {
                unlockedAny |= applyDirectNodeUnlock(serverPlayer, nodeId);
            }
            if (unlockedAny) {
                serverPlayer.sendSystemMessage(Component.translatable("message.earth_xuanxue.book.mastered",
                        stack.getHoverName()).withStyle(ChatFormatting.GOLD));
            }
        }
        return InteractionResult.SUCCESS_SERVER;
    }

    public static InteractionResult tryUseJadeSlipAsLiterature(Level level, Player player, InteractionHand hand,
                                                               ItemStack stack) {
        if (!isLoreLoaded()) {
            return null;
        }
        return LoreBridge.tryUseJadeSlipAsLiterature(level, player, hand, stack);
    }

    public static int manualStudyUseDuration() {
        return isLoreLoaded() ? LoreBridge.useDuration() : 0;
    }

    public static int jadeSlipStudyUseDuration(ItemStack stack) {
        return isLoreLoaded() ? LoreBridge.jadeSlipUseDuration(stack) : 0;
    }

    public static void cancelStudy(ServerPlayer player) {
        if (isLoreLoaded()) {
            LoreBridge.cancelStudy(player);
        }
    }

    public static boolean knowsCirculation(ServerPlayer player) {
        if (!isLoreLoaded()) {
            return ArcanaPower.getCultivationLevel(player) >= 1;
        }
        return LoreBridge.knows(player, K_XUA_02_CIRCULATION);
    }

    public static boolean knowsFocus(ServerPlayer player, CultivationFocus focus) {
        if (!isLoreLoaded()) {
            return focus == CultivationFocus.CIRCULATION
                    ? ArcanaPower.getCultivationLevel(player) >= 1
                    : ArcanaPower.getFocusLevel(player, focus) >= 1;
        }
        return LoreBridge.knows(player, focusNode(focus));
    }

    public static boolean knowsRealm(ServerPlayer player, int nextRealm) {
        if (!isLoreLoaded()) {
            return true;
        }
        return LoreBridge.knows(player, realmNode(nextRealm));
    }

    public static boolean knowsArrayFunctions(ServerPlayer player) {
        if (!isLoreLoaded()) {
            return true;
        }
        return LoreBridge.knows(player, K_XUA_24_ARRAY_FUNCTIONS);
    }

    public static boolean knowsSpellGraphs(ServerPlayer player) {
        if (!isLoreLoaded()) {
            return true;
        }
        return LoreBridge.knows(player, K_XUA_34_SPELL_GRAPHS);
    }

    public static boolean isHardLocked(ServerPlayer player, Object content) {
        if (!isLoreLoaded()) {
            return false;
        }
        return LoreBridge.isHardLocked(player, content);
    }

    public static boolean knowsAll(ServerPlayer player, List<Identifier> nodes) {
        if (!isLoreLoaded()) {
            for (Identifier id : nodes) {
                if (K_XUA_01_QI_GUIDING.equals(id) || K_XUA_02_CIRCULATION.equals(id)) {
                    if (ArcanaPower.getCultivationLevel(player) < 1) {
                        return false;
                    }
                } else if (K_XUA_03_BODY_TEMPERING.equals(id)) {
                    if (ArcanaPower.getBodyTemperingLevel(player) < 1) {
                        return false;
                    }
                } else if (K_XUA_04_FETAL_BREATH.equals(id)) {
                    if (ArcanaPower.getFetalBreathLevel(player) < 1) {
                        return false;
                    }
                } else if (K_XUA_05_BIGU.equals(id)) {
                    if (ArcanaPower.getBiguLevel(player) < 1) {
                        return false;
                    }
                }
            }
            return true;
        }
        return LoreBridge.knowsAll(player, nodes);
    }

    public static int readoutLevel(ServerPlayer player, Item content) {
        if (!isLoreLoaded()) {
            return 1;
        }
        return LoreBridge.readoutLevel(player, content);
    }

    public static boolean isMachineProductLockedAndMarkFault(BlockEntity be, Object product) {
        if (!isLoreLoaded()) {
            return false;
        }
        return LoreBridge.isMachineProductLockedAndMarkFault(be, product);
    }

    public static void clearMachineFault(BlockEntity be) {
        if (isLoreLoaded()) {
            LoreBridge.clearMachineFault(be);
        }
    }

    public static void setMachineFault(BlockEntity be, String code) {
        if (isLoreLoaded()) {
            LoreBridge.setMachineFault(be, code);
        }
    }

    public static Optional<UUID> machineOwner(BlockEntity be) {
        if (!isLoreLoaded()) {
            return Optional.empty();
        }
        return LoreBridge.machineOwner(be);
    }

    public static void recordMade(ServerPlayer owner, Item item, int count) {
        if (isLoreLoaded() && owner != null) {
            LoreBridge.recordMade(owner, item, count);
        }
    }

    public static PillComponent.PillGrade capMachinePillGrade(BlockEntity be, Object product, PillComponent.PillGrade wanted) {
        if (!isLoreLoaded()) {
            return wanted;
        }
        return LoreBridge.capMachinePillGrade(be, product, wanted);
    }

    public static List<MerchantOffer> teachingOffers(ServerLevel level, EntityType<?> entityType) {
        if (!isLoreLoaded()) {
            return List.of();
        }
        return LoreBridge.teachingOffers(level, entityType);
    }

    /**
     * Inner class isolated so JVM never loads com.xxsx.earth.lore.* unless earth_lore is present.
     */
    private static final class LoreBridge {
        private static LoreText manualLoreText(List<Identifier> nodes) {
            return new LoreText(nodes, DISCIPLINE_XUA, "manual", Optional.empty(), "", 0);
        }

        static Item.Properties attachManualLore(Item.Properties props, List<Identifier> nodes) {
            return props.component(LoreContent.LORE_TEXT.get(), manualLoreText(nodes));
        }

        static void init() {
            LoreApi.registerCondition("field", (level, pos) -> Spirituality.measure(level, pos));
            LoreApi.registerPlayerCondition("realm", ArcanaPower::getRealm);

            LoreApi.registerMigrator(new LoreMigrator() {
                @Override
                public String id() {
                    return "earth_xuanxue:arcana_v1";
                }

                @Override
                public Set<Identifier> nodesFor(ServerPlayer player) {
                    return computeMigratedNodes(
                            ArcanaPower.getCultivationLevel(player),
                            ArcanaPower.getBodyTemperingLevel(player),
                            ArcanaPower.getFetalBreathLevel(player),
                            ArcanaPower.getBiguLevel(player),
                            ArcanaPower.getRealm(player));
                }
            });

            LoreApi.registerGradeAccess(new GradeAccess() {
                @Override
                public Optional<Grade> read(ItemStack stack) {
                    PillComponent pill = stack.get(XuanhuanEarth.PILL_COMPONENT.get());
                    if (pill == null) {
                        return Optional.empty();
                    }
                    return Grade.byId(pill.grade().serialized());
                }

                @Override
                public ItemStack write(ItemStack stack, Grade grade) {
                    PillComponent pill = stack.get(XuanhuanEarth.PILL_COMPONENT.get());
                    if (pill == null) {
                        return stack;
                    }
                    PillComponent.PillGrade mapped = PillComponent.PillGrade.parse(grade.id())
                            .result().orElse(PillComponent.PillGrade.COMMON);
                    stack.set(XuanhuanEarth.PILL_COMPONENT.get(), pill.withGrade(mapped));
                    return stack;
                }
            });

            NeoForge.EVENT_BUS.addListener(LoreBridge::onLoreLearned);
        }

        static void onLoreLearned(LoreLearnedEvent event) {
            if (event.source() == LoreLearnedEvent.Source.MIGRATE) {
                return;
            }
            ServerPlayer player = event.player();
            Identifier learned = event.node();
            boolean updated = false;
            if (K_XUA_01_QI_GUIDING.equals(learned)) {
                if (ArcanaPower.learnQiGuiding(player)) {
                    XuanhuanJourney.complete(player, XuanhuanJourney.Milestone.INITIATION);
                    updated = true;
                }
            } else if (K_XUA_02_CIRCULATION.equals(learned)) {
                updated = true;
            } else if (K_XUA_03_BODY_TEMPERING.equals(learned)) {
                updated = ArcanaPower.learnBodyTemperingFromLore(player);
            } else if (K_XUA_04_FETAL_BREATH.equals(learned)) {
                updated = ArcanaPower.learnFetalBreathFromLore(player);
            } else if (K_XUA_05_BIGU.equals(learned)) {
                updated = ArcanaPower.learnBiguFromLore(player);
            }
            if (updated) {
                CultivationNetwork.sync(player, player.blockPosition(), false);
            }
        }

        static void grant(ServerPlayer player, Identifier nodeId) {
            LoreApi.grant(player, nodeId);
        }

        static InteractionResult useManual(Level level, Player player, InteractionHand hand, ItemStack stack,
                                           List<Identifier> nodes) {
            if (!stack.has(LoreContent.LORE_TEXT.get())) {
                stack.set(LoreContent.LORE_TEXT.get(), manualLoreText(nodes));
            }
            if (level.isClientSide()) {
                if (!LiteratureItem.clientCanStudy.test(player, stack)) {
                    return InteractionResult.FAIL;
                }
                player.startUsingItem(hand);
                return InteractionResult.CONSUME;
            }
            if (player instanceof ServerPlayer serverPlayer) {
                var result = LoreApi.beginStudy(serverPlayer, stack);
                if (!result.started()) {
                    serverPlayer.sendOverlayMessage(StudyService.failMessage(result));
                    return InteractionResult.FAIL;
                }
                player.startUsingItem(hand);
                return InteractionResult.CONSUME;
            }
            return InteractionResult.SUCCESS_SERVER;
        }

        static InteractionResult tryUseJadeSlipAsLiterature(Level level, Player player, InteractionHand hand,
                                                            ItemStack stack) {
            LoreText loreText = stack.get(LoreContent.LORE_TEXT.get());
            if (loreText == null || player.isShiftKeyDown()) {
                return null;
            }
            JadeComponent jade = JadeComponent.read(stack);
            String firstNode = loreText.nodes().isEmpty() ? "" : loreText.nodes().getFirst().toString();
            if (!level.isClientSide() && !firstNode.equals(jade.manualId())) {
                stack.set(XuanhuanEarth.JADE_COMPONENT.get(), jade.withManual(firstNode));
            }
            if (player instanceof ServerPlayer serverPlayer) {
                var result = LoreApi.beginStudy(serverPlayer, stack);
                if (!result.started()) {
                    serverPlayer.sendOverlayMessage(StudyService.failMessage(result));
                    return InteractionResult.FAIL;
                }
            } else if (!LiteratureItem.clientCanStudy.test(player, stack)) {
                return InteractionResult.FAIL;
            }
            player.startUsingItem(hand);
            return InteractionResult.CONSUME;
        }

        static int useDuration() {
            return LiteratureItem.USE_DURATION;
        }

        static int jadeSlipUseDuration(ItemStack stack) {
            return stack.has(LoreContent.LORE_TEXT.get()) ? LiteratureItem.USE_DURATION : 0;
        }

        static void cancelStudy(ServerPlayer player) {
            StudyService.cancel(player);
        }

        static boolean knows(ServerPlayer player, Identifier node) {
            return LoreApi.knows(player, node);
        }

        static boolean isHardLocked(ServerPlayer player, Object content) {
            return LoreApi.check(player, content).locked();
        }

        static boolean knowsAll(ServerPlayer player, List<Identifier> nodes) {
            return LoreApi.knowsAll(player, nodes);
        }

        static int readoutLevel(ServerPlayer player, Item content) {
            return LoreApi.readoutLevel(player, content);
        }

        static boolean isMachineProductLockedAndMarkFault(BlockEntity be, Object product) {
            GateResult gate = LoreMachines.check(be, product);
            if (gate.locked()) {
                LoreMachines.setFault(be, "lore_locked", gate.missing());
                return true;
            }
            return false;
        }

        static void clearMachineFault(BlockEntity be) {
            LoreMachines.clearFault(be);
        }

        static void setMachineFault(BlockEntity be, String code) {
            LoreMachines.setFault(be, code, List.of());
        }

        static Optional<UUID> machineOwner(BlockEntity be) {
            return LoreMachines.owner(be);
        }

        static void recordMade(ServerPlayer owner, Item item, int count) {
            LoreApi.recordMade(owner, item, count);
        }

        static PillComponent.PillGrade capMachinePillGrade(BlockEntity be, Object product, PillComponent.PillGrade wanted) {
            Grade wantedGrade = Grade.byId(wanted.serialized()).orElse(Grade.COMMON);
            Grade capped = LoreMachines.capGrade(be, product, wantedGrade);
            Grade furnaceCap = LoreMachines.capGrade(be, XuanhuanEarth.ALCHEMY_FURNACE.get(), capped);
            return PillComponent.PillGrade.parse(furnaceCap.id()).result().orElse(PillComponent.PillGrade.COMMON);
        }

        static List<MerchantOffer> teachingOffers(ServerLevel level, EntityType<?> entityType) {
            return LoreApi.teachingOffers(level, entityType);
        }
    }
}
