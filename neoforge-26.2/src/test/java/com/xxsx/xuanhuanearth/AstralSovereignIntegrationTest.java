package com.xxsx.xuanhuanearth;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.xxsx.xuanhuanearth.entity.AstralFlyingSwordEntity;
import com.xxsx.xuanhuanearth.entity.AstralSovereignEntity;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AstralSovereignIntegrationTest {

    @Test
    void astralSovereignResourcesAndTranslationsExist() throws Exception {
        Path resources = Path.of("src/main/resources");
        assertTrue(Files.exists(resources.resolve("assets/xuanhuan_earth/textures/entity/astral_sovereign_p1.png")));
        assertTrue(Files.exists(resources.resolve("assets/xuanhuan_earth/textures/entity/astral_sovereign_p2.png")));
        assertTrue(Files.exists(resources.resolve("assets/xuanhuan_earth/textures/item/astral_fangtian_halberd.png")));
        assertTrue(Files.exists(resources.resolve("assets/xuanhuan_earth/textures/item/astral_sovereign_spawn_egg.png")));
        assertTrue(Files.exists(resources.resolve("assets/xuanhuan_earth/items/astral_fangtian_halberd.json")));
        assertTrue(Files.exists(resources.resolve("assets/xuanhuan_earth/items/astral_sovereign_spawn_egg.json")));

        JsonObject zh = JsonParser.parseString(
                Files.readString(resources.resolve("assets/xuanhuan_earth/lang/zh_cn.json"))).getAsJsonObject();
        assertEquals("镇岳星君", zh.get("entity.xuanhuan_earth.astral_sovereign").getAsString());
        assertEquals("星辰飞剑", zh.get("entity.xuanhuan_earth.astral_flying_sword").getAsString());
        assertEquals("方天星辰戟", zh.get("item.xuanhuan_earth.astral_fangtian_halberd").getAsString());

        JsonObject en = JsonParser.parseString(
                Files.readString(resources.resolve("assets/xuanhuan_earth/lang/en_us.json"))).getAsJsonObject();
        assertEquals("Astral Sovereign", en.get("entity.xuanhuan_earth.astral_sovereign").getAsString());
        assertEquals("Astral Flying Sword", en.get("entity.xuanhuan_earth.astral_flying_sword").getAsString());
    }

    @Test
    void astralSovereignLootTableMatchesXuanhuanProgression() throws Exception {
        Path lootPath = Path.of("src/main/resources/data/xuanhuan_earth/loot_table/entities/astral_sovereign.json");
        assertTrue(Files.exists(lootPath));
        String content = Files.readString(lootPath);
        assertTrue(content.contains("xuanhuan_earth:astral_fangtian_halberd"));
        assertTrue(content.contains("xuanhuan_earth:spirit_stone_core"));
        assertTrue(content.contains("xuanhuan_earth:jade_array_plate"));
        assertTrue(content.contains("xuanhuan_earth:breakthrough_pill"));
        assertFalse(content.contains("astral_sovereign:"), "No standalone prototype namespace leaks");
        assertFalse(content.contains("minecraft:nether_star"), "Vanilla nether star replaced by xuanhuan progression drops");
    }

    @Test
    void combatAndFlyingSwordConstantsStayWithinDesignBounds() {
        assertTrue(AstralSovereignEntity.SWORD_SUMMON_MAX_RANGE >= 16.0D);
        assertTrue(AstralSovereignEntity.ULTIMATE_TELEGRAPH_DURATION >= 15);
        assertTrue(AstralSovereignEntity.ULTIMATE_SHOCKWAVE_DURATION >= 8);
        assertTrue(AstralSovereignEntity.ULTIMATE_MAX_RADIUS >= 10.0D);
        assertTrue(AstralSovereignEntity.SWORD_BUFF_DURATION >= 100);

        // Short-lived homing flying swords & stronger empowered damage
        assertTrue(AstralFlyingSwordEntity.BASE_LIFETIME_TICKS > 40 && AstralFlyingSwordEntity.BASE_LIFETIME_TICKS <= 100);
        assertTrue(AstralFlyingSwordEntity.EMPOWERED_DAMAGE_P1 > AstralFlyingSwordEntity.BASE_DAMAGE);
        assertTrue(AstralFlyingSwordEntity.EMPOWERED_DAMAGE_P2 > AstralFlyingSwordEntity.EMPOWERED_DAMAGE_P1);
        assertTrue(AstralFlyingSwordEntity.DEFLECT_RECOIL_TICKS >= 10);

        // Halberd 3-combo styles
        assertTrue(AstralHalberdItem.OVERHEAD_DAMAGE > AstralHalberdItem.SWEEP_DAMAGE);
        assertTrue(AstralHalberdItem.COOLDOWN_TICKS >= 20);
    }
}
