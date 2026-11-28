package com.xxsx.earth.xuanxue.settlement;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import net.minecraft.world.level.block.entity.SignText;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SettlementWorldgenSignSafetyTest {

    @Test
    void settlementSignBuilderProducesGlowingFourLineTextForAllFiveTypes() {
        List<String> titles = List.of("「散修坊市」", "「灵泉村」", "「宗门前哨」", "「山灵古祠」", "「御兽营地」");
        for (String title : titles) {
            SignText built = SettlementSignHelper.buildSettlementSignText(new SignText(), title);
            assertTrue(built.hasGlowingText(), "Signpost must glow for " + title);
            assertEquals("—— 修行聚落 ——", built.getMessage(0, false).getString());
            assertEquals(title, built.getMessage(1, false).getString());
            assertEquals("灵气聚汇之地", built.getMessage(2, false).getString());
            assertEquals("Earth: Xuanxue", built.getMessage(3, false).getString());
        }
    }

    @Test
    void compiledBytecodeAndSourceGuardWorldgenSignNullLevelAndWaystonesHalf() throws Exception {
        Path classesRoot = Path.of("build/classes/java/main");
        String featBytes = new String(
                Files.readAllBytes(classesRoot.resolve("com/xxsx/earth/xuanxue/settlement/XuanhuanSettlementFeature.class")),
                StandardCharsets.ISO_8859_1);
        String helperBytes = new String(
                Files.readAllBytes(classesRoot.resolve("com/xxsx/earth/xuanxue/settlement/SettlementSignHelper.class")),
                StandardCharsets.ISO_8859_1);

        assertFalse(featBytes.contains("setText"),
                "XuanhuanSettlementFeature bytecode must never call SignBlockEntity.setText directly");
        assertTrue(featBytes.contains("applyFrontTextSafely"),
                "XuanhuanSettlementFeature bytecode must delegate sign updates to SettlementSignHelper.applyFrontTextSafely");
        assertTrue(helperBytes.contains("hasLevel") && helperBytes.contains("loadCustomOnly")
                        && helperBytes.contains("DIRECT_CODEC") && helperBytes.contains("TagValueInput"),
                "SettlementSignHelper bytecode must guard hasLevel() and fall back to SignText.DIRECT_CODEC + loadCustomOnly()");

        String featSrc = Files.readString(Path.of(
                "src/main/java/com/xxsx/earth/xuanxue/settlement/XuanhuanSettlementFeature.java"));
        assertTrue(featSrc.contains("DoubleBlockHalf.LOWER") && featSrc.contains("DoubleBlockHalf.UPPER"),
                "Waystones soft-compat must place both LOWER and UPPER halves");
        assertTrue(featSrc.contains("ensureCanWrite"),
                "Settlement signpost and waystone placement must guard WorldGenRegion write bounds");

        String buildGradle = Files.readString(Path.of("build.gradle"));
        String modsToml = Files.readString(Path.of("src/main/resources/META-INF/neoforge.mods.toml"));
        assertTrue(buildGradle.contains("version = '0.9.5'"), "build.gradle must be 0.9.5");
        assertTrue(modsToml.contains("version=\"0.9.5\""), "neoforge.mods.toml must match 0.9.5");
    }
}
