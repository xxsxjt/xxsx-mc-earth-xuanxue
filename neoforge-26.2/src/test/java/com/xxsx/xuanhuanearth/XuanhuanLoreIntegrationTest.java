package com.xxsx.xuanhuanearth;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.xxsx.earthlore.core.Gate;
import com.xxsx.earthlore.core.GraphValidator;
import com.xxsx.earthlore.core.NodeDef;
import com.xxsx.earthlore.core.Prereq;
import com.xxsx.earthlore.core.Reachability;
import net.minecraft.resources.Identifier;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class XuanhuanLoreIntegrationTest {
    private static final Set<String> DISCIPLINES = Set.of("earth_lore:xua", "earth_lore:med", "earth_lore:geo");
    private static final GraphValidator.DependencyRule STANDALONE_DEPS =
            (owner, other) -> owner.equals(other) || "earth_lore".equals(other);

    @Test
    void allThirtyFiveXuaNodesValidateAndAreReachableStandalone() throws IOException {
        List<NodeDef> nodes = loadXuaNodes();
        assertEquals(35, nodes.size(), "Expected 35 K-XUA nodes");

        GraphValidator.Result validated = GraphValidator.validate(nodes, DISCIPLINES, STANDALONE_DEPS);
        assertTrue(validated.rejected().isEmpty(), () -> "Rejected nodes: " + validated.rejected());
        assertEquals(35, validated.accepted().size());

        Reachability.Report reach = Reachability.compute(validated.accepted());
        assertTrue(reach.unreachable().isEmpty(), () -> "Unreachable nodes: " + reach.unreachable());
        assertEquals(35, reach.learnable().size());
        assertEquals(4, reach.altUsed(), "All 4 cross-mod any-groups should use their self-taught xua fallback standalone");
    }

    @Test
    void legacySaveMigrationMatchesSpec() {
        Set<Identifier> migrated = XuanhuanLoreIntegration.computeMigratedNodes(1, 0, 0, 0, 3);
        assertEquals(Set.of(
                XuanhuanLoreIntegration.K_XUA_01_QI_GUIDING,
                XuanhuanLoreIntegration.K_XUA_02_CIRCULATION,
                XuanhuanLoreIntegration.K_XUA_07_QI_REFINING,
                XuanhuanLoreIntegration.K_XUA_08_FOUNDATION_BUILDING,
                XuanhuanLoreIntegration.K_XUA_09_GOLDEN_CORE
        ), migrated);

        Set<Identifier> withRoutes = XuanhuanLoreIntegration.computeMigratedNodes(2, 1, 3, 1, 5);
        assertTrue(withRoutes.contains(XuanhuanLoreIntegration.K_XUA_03_BODY_TEMPERING));
        assertTrue(withRoutes.contains(XuanhuanLoreIntegration.K_XUA_04_FETAL_BREATH));
        assertTrue(withRoutes.contains(XuanhuanLoreIntegration.K_XUA_05_BIGU));
        assertTrue(withRoutes.contains(XuanhuanLoreIntegration.K_XUA_10_NASCENT_SOUL));
        assertTrue(withRoutes.contains(XuanhuanLoreIntegration.K_XUA_11_SPIRIT_TRANSFORMATION));
    }

    @Test
    void furnaceTemperatureAndPillGradesMatchSpec() {
        assertEquals(646, ArcanaBalance.furnaceTemperature(55, 0.70D));
        assertEquals(300, ArcanaBalance.furnaceTemperature(0, 0.85D));
        assertEquals(1200, ArcanaBalance.furnaceTemperature(100, 1.00D));

        assertEquals(PillComponent.PillGrade.CRUDE, PillComponent.PillGrade.fromQuality(0.39D));
        assertEquals(PillComponent.PillGrade.COMMON, PillComponent.PillGrade.fromQuality(0.40D));
        assertEquals(PillComponent.PillGrade.FINE, PillComponent.PillGrade.fromQuality(0.60D));
        assertEquals(PillComponent.PillGrade.MASTERWORK, PillComponent.PillGrade.fromQuality(0.80D));
        assertEquals(PillComponent.PillGrade.HEIRLOOM, PillComponent.PillGrade.fromQuality(0.95D));
        assertTrue(PillComponent.PillGrade.parse("invalid").isError());
    }

    @Test
    void luopanAndDivinationCalculationsAreDeterministic() {
        assertEquals(1, ArcanaBalance.luopanRating(0, 100, 0, false));
        assertEquals(9, ArcanaBalance.luopanRating(100, 8, 12, true));

        UUID playerUuid = UUID.fromString("12345678-1234-5678-1234-567812345678");
        int h1 = ArcanaBalance.divinationHexagram(42L, playerUuid, 10L);
        int h2 = ArcanaBalance.divinationHexagram(42L, playerUuid, 10L);
        assertEquals(h1, h2);
        assertTrue(h1 >= 0 && h1 < 64);
    }

    @Test
    void visualActionsAndResourceJsonAreValid() throws IOException {
        assertEquals(CultivationVisualAction.IMPLEMENT_CAST, CultivationVisualAction.byId(7));
        assertTrue(CultivationVisualAction.IMPLEMENT_CAST.durationTicks() > 0);

        JsonObject veinPlacement = JsonParser.parseString(Files.readString(
                Path.of("src/main/resources/data/xuanhuan_earth/worldgen/placed_feature/spirit_vein_node.json"),
                StandardCharsets.UTF_8)).getAsJsonObject();
        JsonObject firstModifier = veinPlacement.getAsJsonArray("placement").get(0).getAsJsonObject();
        assertEquals("minecraft:count", firstModifier.get("type").getAsString());
        assertTrue(firstModifier.get("count").getAsInt() >= 2);

        JsonObject settlerLoot = JsonParser.parseString(Files.readString(
                Path.of("src/main/resources/data/xuanhuan_earth/loot_table/entities/cultivation_settler.json"),
                StandardCharsets.UTF_8)).getAsJsonObject();
        assertTrue(settlerLoot.getAsJsonArray("pools").size() >= 2);

        JsonObject zhCn = JsonParser.parseString(Files.readString(
                Path.of("src/main/resources/assets/xuanhuan_earth/lang/zh_cn.json"),
                StandardCharsets.UTF_8)).getAsJsonObject();
        JsonObject enUs = JsonParser.parseString(Files.readString(
                Path.of("src/main/resources/assets/xuanhuan_earth/lang/en_us.json"),
                StandardCharsets.UTF_8)).getAsJsonObject();
        assertTrue(zhCn.has("screen.xuanhuan_earth.cultivation.hub"));
        assertTrue(zhCn.has("screen.xuanhuan_earth.cultivation.lore_tree"));
        assertTrue(enUs.has("screen.xuanhuan_earth.cultivation.hub"));
        assertTrue(enUs.has("screen.xuanhuan_earth.cultivation.lore_tree"));
    }

    private static List<NodeDef> loadXuaNodes() throws IOException {
        Path dir = Path.of("src/main/resources/data/xuanhuan_earth/earth_lore/knowledge_node/xua");
        List<NodeDef> list = new ArrayList<>();
        try (Stream<Path> paths = Files.list(dir)) {
            for (Path file : paths.filter(p -> p.toString().endsWith(".json")).sorted().toList()) {
                String slug = file.getFileName().toString().replace(".json", "");
                String id = "xuanhuan_earth:xua/" + slug;
                JsonObject obj = JsonParser.parseString(Files.readString(file, StandardCharsets.UTF_8)).getAsJsonObject();
                String discipline = obj.get("discipline").getAsString();
                int tier = obj.get("tier").getAsInt();
                Gate gate = Gate.parse(obj.get("gate").getAsString()).orElseThrow();
                List<Prereq> requires = new ArrayList<>();
                for (JsonElement reqEl : obj.getAsJsonArray("requires")) {
                    JsonObject reqObj = reqEl.getAsJsonObject();
                    if (reqObj.has("all")) {
                        requires.add(new Prereq.All(reqObj.get("all").getAsString()));
                    } else if (reqObj.has("any")) {
                        List<String> members = new ArrayList<>();
                        JsonArray arr = reqObj.getAsJsonArray("any");
                        arr.forEach(m -> members.add(m.getAsString()));
                        requires.add(new Prereq.Any(members));
                    }
                }
                List<String> unlockKeys = new ArrayList<>();
                for (JsonElement uEl : obj.getAsJsonArray("unlocks")) {
                    JsonObject uObj = uEl.getAsJsonObject();
                    Map.Entry<String, JsonElement> entry = uObj.entrySet().iterator().next();
                    String kind = entry.getKey();
                    String val = entry.getValue().getAsString();
                    unlockKeys.add("capstone".equals(kind) ? ("capstone:" + val + "@xuanhuan_earth") : (kind + ":" + val));
                }
                list.add(new NodeDef(id, discipline, tier, gate, requires, unlockKeys, false));
            }
        }
        return list;
    }
}
