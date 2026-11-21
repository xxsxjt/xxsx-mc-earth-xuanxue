package com.xxsx.xuanhuanearth;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.xxsx.xuanhuanearth.entity.AstralSovereignEntity;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AstralSovereignIntegrationTest {

    @Test
    void astralSovereignResourcesAndTranslationsExist() throws IOException {
        JsonObject zhCn = JsonParser.parseString(Files.readString(
                Path.of("src/main/resources/assets/xuanhuan_earth/lang/zh_cn.json"),
                StandardCharsets.UTF_8)).getAsJsonObject();
        JsonObject enUs = JsonParser.parseString(Files.readString(
                Path.of("src/main/resources/assets/xuanhuan_earth/lang/en_us.json"),
                StandardCharsets.UTF_8)).getAsJsonObject();

        for (String key : new String[]{
                "entity.xuanhuan_earth.astral_sovereign",
                "entity.xuanhuan_earth.astral_sovereign.phase2",
                "item.xuanhuan_earth.astral_sovereign_spawn_egg",
                "item.xuanhuan_earth.astral_fangtian_halberd"
        }) {
            assertTrue(zhCn.has(key), "Missing zh_cn key: " + key);
            assertTrue(enUs.has(key), "Missing en_us key: " + key);
        }

        for (String asset : new String[]{
                "src/main/resources/assets/xuanhuan_earth/items/astral_fangtian_halberd.json",
                "src/main/resources/assets/xuanhuan_earth/items/astral_sovereign_spawn_egg.json",
                "src/main/resources/assets/xuanhuan_earth/models/item/astral_fangtian_halberd.json",
                "src/main/resources/assets/xuanhuan_earth/models/item/astral_sovereign_spawn_egg.json",
                "src/main/resources/assets/xuanhuan_earth/textures/entity/astral_sovereign_p1.png",
                "src/main/resources/assets/xuanhuan_earth/textures/entity/astral_sovereign_p2.png",
                "src/main/resources/assets/xuanhuan_earth/textures/item/astral_fangtian_halberd.png",
                "src/main/resources/assets/xuanhuan_earth/textures/item/astral_sovereign_spawn_egg.png"
        }) {
            assertTrue(Files.exists(Path.of(asset)), "Missing asset: " + asset);
        }
    }

    @Test
    void astralSovereignLootTableMatchesXuanhuanProgression() throws IOException {
        String raw = Files.readString(
                Path.of("src/main/resources/data/xuanhuan_earth/loot_table/entities/astral_sovereign.json"),
                StandardCharsets.UTF_8);
        assertFalse(raw.contains("astral_sovereign:"), "Legacy prototype namespace leaked into loot table");
        assertFalse(raw.contains("minecraft:nether_star"), "Vanilla nether_star should be replaced by xuanhuan progression drops");

        JsonObject loot = JsonParser.parseString(raw).getAsJsonObject();
        JsonArray pools = loot.getAsJsonArray("pools");
        assertEquals(4, pools.size());
        assertEquals("xuanhuan_earth:astral_fangtian_halberd",
                pools.get(0).getAsJsonObject().getAsJsonArray("entries").get(0).getAsJsonObject().get("name").getAsString());
    }

    @Test
    void combatConstantsStayWithinDesignBounds() {
        assertEquals(450.0D, AstralSovereignEntity.MAX_HEALTH);
        assertEquals(18.0D, AstralSovereignEntity.SWORD_VOLLEY_MAX_RANGE);
        assertTrue(AstralSovereignEntity.JUDGMENT_TELEGRAPH_TICKS >= 20);
        assertEquals(10.0D, AstralHalberdItem.SWEEP_MAX_RANGE);
        assertEquals(60, AstralHalberdItem.SWEEP_COOLDOWN_TICKS);
    }
}
