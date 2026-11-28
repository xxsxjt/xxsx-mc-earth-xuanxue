package com.xxsx.earth.xuanxue;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.xxsx.earth.xuanxue.entity.AstralFlyingSwordEntity;
import com.xxsx.earth.xuanxue.entity.AstralSovereignEntity;
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
        assertTrue(Files.exists(resources.resolve("assets/earth_xuanxue/textures/entity/astral_sovereign_p1.png")));
        assertTrue(Files.exists(resources.resolve("assets/earth_xuanxue/textures/entity/astral_sovereign_p2.png")));
        assertTrue(Files.exists(resources.resolve("assets/earth_xuanxue/textures/entity/astral_vfx_beam.png")));
        assertTrue(Files.exists(resources.resolve("assets/earth_xuanxue/textures/entity/astral_vfx_ribbon.png")));
        assertTrue(Files.exists(resources.resolve("assets/earth_xuanxue/textures/entity/astral_vfx_talisman.png")));
        assertTrue(Files.exists(resources.resolve("assets/earth_xuanxue/textures/entity/astral_vfx_sword_gate.png")));
        assertTrue(Files.exists(resources.resolve("assets/earth_xuanxue/textures/entity/astral_vfx_halberd_trail.png")));
        assertTrue(Files.exists(resources.resolve("assets/earth_xuanxue/textures/entity/astral_vfx_sweep.png")));
        assertTrue(Files.exists(resources.resolve("assets/earth_xuanxue/textures/item/astral_fangtian_halberd.png")));
        assertTrue(Files.exists(resources.resolve("assets/earth_xuanxue/textures/item/astral_sovereign_spawn_egg.png")));
        assertTrue(Files.exists(resources.resolve("assets/earth_xuanxue/items/astral_fangtian_halberd.json")));
        assertTrue(Files.exists(resources.resolve("assets/earth_xuanxue/items/astral_sovereign_spawn_egg.json")));

        for (String model : new String[] {
                "alchemy_furnace.json", "alchemy_furnace_active.json",
                "talisman_table.json", "talisman_table_active.json",
                "spirit_array_core.json", "spirit_array_core_active.json", "spirit_array_core_formed.json",
                "array_pillar.json", "meditation_cushion.json", "spirit_vein_node.json",
                "spirit_spring_stone.json", "spirit_spring_stone_size1.json", "spirit_spring_stone_size2.json", "spirit_spring_stone_size3.json", "spirit_soil.json"
        }) {
            Path p = resources.resolve("assets/earth_xuanxue/models/block/" + model);
            assertTrue(Files.exists(p), "Missing block model: " + model);
            String json = Files.readString(p);
            if (!model.equals("spirit_soil.json") && !model.equals("spirit_spring_stone.json")) {
                assertTrue(json.contains("\"elements\""), "Block model must use sculpted 3D elements: " + model);
            }
        }
        for (String tex : new String[] {
                "jade_crystal.png", "bronze_cauldron.png", "spirit_water_pool.png",
                "spirit_soil_top.png", "lantern_jade_core.png"
        }) {
            assertTrue(Files.exists(resources.resolve("assets/earth_xuanxue/textures/block/" + tex)), "Missing block texture: " + tex);
        }

        JsonObject zh = JsonParser.parseString(
                Files.readString(resources.resolve("assets/earth_xuanxue/lang/zh_cn.json"))).getAsJsonObject();
        assertEquals("镇岳星君", zh.get("entity.earth_xuanxue.astral_sovereign").getAsString());
        assertEquals("星辰飞剑", zh.get("entity.earth_xuanxue.astral_flying_sword").getAsString());
        assertEquals("方天星辰戟", zh.get("item.earth_xuanxue.astral_fangtian_halberd").getAsString());

        JsonObject en = JsonParser.parseString(
                Files.readString(resources.resolve("assets/earth_xuanxue/lang/en_us.json"))).getAsJsonObject();
        assertEquals("Astral Sovereign", en.get("entity.earth_xuanxue.astral_sovereign").getAsString());
        assertEquals("Astral Flying Sword", en.get("entity.earth_xuanxue.astral_flying_sword").getAsString());
    }

    @Test
    void astralSovereignLootTableMatchesXuanhuanProgression() throws Exception {
        Path lootPath = Path.of("src/main/resources/data/earth_xuanxue/loot_table/entities/astral_sovereign.json");
        assertTrue(Files.exists(lootPath));
        String content = Files.readString(lootPath);
        assertTrue(content.contains("earth_xuanxue:astral_fangtian_halberd"));
        assertTrue(content.contains("earth_xuanxue:spirit_stone_core"));
        assertTrue(content.contains("earth_xuanxue:jade_array_plate"));
        assertTrue(content.contains("earth_xuanxue:breakthrough_pill"));
        assertFalse(content.contains("astral_sovereign:"));
        assertFalse(content.contains("minecraft:nether_star"));
    }

    @Test
    void combatAndFlyingSwordConstantsStayWithinDesignBounds() throws Exception {
        assertTrue(AstralSovereignEntity.SWORD_SUMMON_MAX_RANGE >= 16.0D);
        assertTrue(AstralSovereignEntity.ULTIMATE_TELEGRAPH_DURATION >= 15);
        assertTrue(AstralSovereignEntity.ULTIMATE_SHOCKWAVE_DURATION >= 8);
        assertTrue(AstralSovereignEntity.ULTIMATE_MAX_RADIUS >= 10.0D);
        assertTrue(AstralSovereignEntity.SWORD_BUFF_DURATION >= 100);

        assertTrue(AstralFlyingSwordEntity.BASE_LIFETIME_TICKS >= 100 && AstralFlyingSwordEntity.BASE_LIFETIME_TICKS <= 180);
        assertTrue(AstralFlyingSwordEntity.EMPOWERED_LIFETIME_TICKS > AstralFlyingSwordEntity.BASE_LIFETIME_TICKS);
        assertTrue(AstralFlyingSwordEntity.PIERCE_CURVE_TICKS >= 8);
        assertTrue(AstralFlyingSwordEntity.PER_TARGET_COOLDOWN_TICKS >= 10);
        assertTrue(AstralFlyingSwordEntity.EMPOWERED_DAMAGE_P1 > AstralFlyingSwordEntity.BASE_DAMAGE);
        assertTrue(AstralFlyingSwordEntity.EMPOWERED_DAMAGE_P2 > AstralFlyingSwordEntity.EMPOWERED_DAMAGE_P1);
        assertTrue(AstralFlyingSwordEntity.DEFLECT_RECOIL_TICKS >= 10);

        String entitySource = Files.readString(Path.of("src/main/java/com/xxsx/earth/xuanxue/entity/AstralSovereignEntity.java"));
        assertTrue(entitySource.contains("isPhase2()"));
        assertTrue(entitySource.contains("ARMOR_TOUGHNESS"));

        String modelSource = Files.readString(Path.of("src/main/java/com/xxsx/earth/xuanxue/client/model/AstralSovereignModel.java"));
        assertTrue(modelSource.contains("this.astralWings.visible = false;"));
        assertTrue(AstralSovereignEntity.PHASE_TRANSITION_DURATION >= 40);
        assertTrue(modelSource.contains("cape_mid") && modelSource.contains("cape_lower"));
        assertTrue(modelSource.contains("tabard_front") && modelSource.contains("p2_pauldron_wing_l"));

        assertTrue(AstralHalberdItem.CLEAVE_DAMAGE > AstralHalberdItem.SWEEP_DAMAGE);
        assertTrue(AstralHalberdItem.COOLDOWN_TICKS >= 20);
        String halberdSource = Files.readString(Path.of("src/main/java/com/xxsx/earth/xuanxue/AstralHalberdItem.java"));
        assertFalse(halberdSource.contains("SEAL_RING"));

        // v0.9.1 playtest AI & combat bounds: agile P1 chase, low knockback, reliable P2 aerial strike, Creative immunity
        assertTrue(AstralSovereignEntity.BASE_MOVE_SPEED >= 0.34D);
        assertTrue(AstralSovereignEntity.MELEE_CHASE_SPEED >= 1.20D);
        assertTrue(AstralSovereignEntity.PHASE2_AIR_SPEED >= 0.40D);
        assertTrue(AstralSovereignEntity.PHASE2_AERIAL_STRIKE_RANGE >= 4.2D);
        assertTrue(AstralSovereignEntity.MELEE_CLEAVE_KNOCKBACK_H <= 0.30D);
        assertTrue(AstralSovereignEntity.MELEE_CLEAVE_KNOCKBACK_V <= 0.16D);
        assertTrue(AstralSovereignEntity.SHOCKWAVE_KNOCKBACK_H <= 0.55D);
        assertTrue(entitySource.contains("performStarStepDash"));
        assertTrue(entitySource.contains("performPhase1QiWave"));
        assertTrue(entitySource.contains("aerialMeleeCooldown"));
        assertTrue(entitySource.contains("isValidHostileTarget"));
        assertTrue(entitySource.contains("player.isCreative()"));
    }
}
