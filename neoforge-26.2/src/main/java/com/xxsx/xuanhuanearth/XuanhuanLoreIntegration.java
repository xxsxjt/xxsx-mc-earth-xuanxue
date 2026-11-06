package com.xxsx.xuanhuanearth;

import com.xxsx.earthlore.api.GateResult;
import com.xxsx.earthlore.api.Grade;
import com.xxsx.earthlore.api.GradeAccess;
import com.xxsx.earthlore.api.LoreApi;
import com.xxsx.earthlore.api.LoreLearnedEvent;
import com.xxsx.earthlore.api.LoreMachines;
import com.xxsx.earthlore.api.LoreMigrator;
import com.xxsx.earthlore.data.LoreText;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Bridges xuanhuan_earth with earth_lore (02 册 & 15 册 §1):
 * - Registers research condition provider 'field'
 * - Registers save migrator 'xuanhuan_earth:arcana_v1'
 * - Registers PillComponent GradeAccess
 * - Listens to LoreLearnedEvent to grant legacy route levels and bonuses on study/grant
 */
public final class XuanhuanLoreIntegration {
    public static final Identifier DISCIPLINE_XUA = Identifier.fromNamespaceAndPath("earth_lore", "xua");

    public static final Identifier K_XUA_01_QI_GUIDING = node("qi_guiding");
    public static final Identifier K_XUA_02_CIRCULATION = node("circulation");
    public static final Identifier K_XUA_03_BODY_TEMPERING = node("body_tempering");
    public static final Identifier K_XUA_04_FETAL_BREATH = node("fetal_breath");
    public static final Identifier K_XUA_05_BIGU = node("bigu");
    public static final Identifier K_XUA_06_SPIRIT_STONES = node("spirit_stones");
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

    public static LoreText manualLoreText(List<Identifier> nodes) {
        return new LoreText(nodes, DISCIPLINE_XUA, "manual", Optional.empty(), "", 0);
    }

    public static void init() {
        LoreApi.registerCondition("field", (level, pos) -> Spirituality.measure(level, pos));

        LoreApi.registerMigrator(new LoreMigrator() {
            @Override
            public String id() {
                return "xuanhuan_earth:arcana_v1";
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

    public static void onLoreLearned(LoreLearnedEvent event) {
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

    public static boolean knowsCirculation(ServerPlayer player) {
        return LoreApi.knows(player, K_XUA_02_CIRCULATION);
    }

    public static boolean knowsFocus(ServerPlayer player, CultivationFocus focus) {
        return LoreApi.knows(player, focusNode(focus));
    }

    public static boolean knowsRealm(ServerPlayer player, int nextRealm) {
        return LoreApi.knows(player, realmNode(nextRealm));
    }

    public static boolean knowsArrayFunctions(ServerPlayer player) {
        return LoreApi.knows(player, K_XUA_24_ARRAY_FUNCTIONS);
    }

    public static boolean knowsSpellGraphs(ServerPlayer player) {
        return LoreApi.knows(player, K_XUA_34_SPELL_GRAPHS);
    }

    public static boolean isHardLocked(ServerPlayer player, Object content) {
        return LoreApi.check(player, content).locked();
    }

    public static GateResult checkMachineProduct(BlockEntity be, Object product) {
        return LoreMachines.check(be, product);
    }

    public static PillComponent.PillGrade capMachinePillGrade(BlockEntity be, Object product, PillComponent.PillGrade wanted) {
        Grade wantedGrade = Grade.byId(wanted.serialized()).orElse(Grade.COMMON);
        Grade capped = LoreMachines.capGrade(be, product, wantedGrade);
        Grade furnaceCap = LoreMachines.capGrade(be, XuanhuanEarth.ALCHEMY_FURNACE.get(), capped);
        return PillComponent.PillGrade.parse(furnaceCap.id()).result().orElse(PillComponent.PillGrade.COMMON);
    }
}
