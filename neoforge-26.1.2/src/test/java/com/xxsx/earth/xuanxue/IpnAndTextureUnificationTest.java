package com.xxsx.earth.xuanxue;

import java.io.DataInputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IpnAndTextureUnificationTest {

    private static int[] readPngSize(Path p) throws Exception {
        try (DataInputStream in = new DataInputStream(Files.newInputStream(p))) {
            in.skipBytes(16); // PNG sig (8) + IHDR len (4) + "IHDR" (4)
            return new int[] { in.readInt(), in.readInt() };
        }
    }

    @Test
    void machineMenuAndScreenBytecodeCarryRuntimeIpnPlayerSideOnlyAndNoBundledApi() throws Exception {
        Path classesRoot = Path.of("build/classes/java/main");
        for (String rel : new String[] {
                "com/xxsx/earth/xuanxue/XuanhuanMachineMenu.class",
                "com/xxsx/earth/xuanxue/client/XuanhuanMachineScreen.class"
        }) {
            String raw = new String(Files.readAllBytes(classesRoot.resolve(rel)), StandardCharsets.ISO_8859_1);
            assertTrue(raw.contains("RuntimeVisibleAnnotations"), "Missing RuntimeVisibleAnnotations in " + rel);
            assertTrue(raw.contains("Lorg/anti_ad/mc/ipn/api/IPNPlayerSideOnly;"),
                    "Missing @IPNPlayerSideOnly bytecode annotation in " + rel);
        }
        assertFalse(Files.exists(classesRoot.resolve("org/anti_ad/mc/ipn/api/IPNPlayerSideOnly.class")),
                "IPN API stubs must remain compileOnly and never be packaged into mod main classes");
    }

    @Test
    void blockTexturesAreVanilla16x16AndItemTexturesPreserveCrispGoldenMasterArt() throws Exception {
        Path texRoot = Path.of("src/main/resources/assets/earth_xuanxue/textures");
        int blockCount = 0;
        try (Stream<Path> files = Files.list(texRoot.resolve("block"))) {
            for (Path p : files.filter(f -> f.toString().endsWith(".png")).toList()) {
                int[] dim = readPngSize(p);
                assertEquals(16, dim[0], "Block texture width must be 16px: " + p.getFileName());
                assertEquals(16, dim[1], "Block texture height must be 16px: " + p.getFileName());
                blockCount++;
            }
        }
        assertTrue(blockCount >= 22, "Expected at least 22 block textures, got " + blockCount);

        Set<Integer> allowedItemSizes = Set.of(16, 32, 128);
        int itemCount = 0;
        int crisp32Count = 0;
        try (Stream<Path> files = Files.list(texRoot.resolve("item"))) {
            for (Path p : files.filter(f -> f.toString().endsWith(".png")).toList()) {
                int[] dim = readPngSize(p);
                assertEquals(dim[0], dim[1], "Item texture must be square: " + p.getFileName());
                assertTrue(allowedItemSizes.contains(dim[0]),
                        "Unexpected item texture size " + dim[0] + "px for " + p.getFileName());
                if (dim[0] == 32) crisp32Count++;
                itemCount++;
            }
        }
        assertTrue(itemCount >= 72, "Expected at least 72 item textures, got " + itemCount);
        assertTrue(crisp32Count >= 55, "Crisp 32x32 weapons/equipment/pills/talismans must not be downgraded to Q-version 16x16 (found " + crisp32Count + ")");

        int[] halberd = readPngSize(texRoot.resolve("item/astral_fangtian_halberd.png"));
        assertEquals(128, halberd[0], "Hero Boss weapon astral_fangtian_halberd.png must stay crisp 128x128");
        int[] spiritSword = readPngSize(texRoot.resolve("item/dragon_seeking_rod.png"));
        assertEquals(32, spiritSword[0], "Spirit weapon must stay crisp 32x32");
    }

    @Test
    void astralSovereignFaceMatchesWeb3dViewerPaintedVisorNotLegacyBoxes() throws Exception {
        String modelSource = Files.readString(
                Path.of("src/main/java/com/xxsx/earth/xuanxue/client/model/AstralSovereignModel.java"));
        assertTrue(modelSource.contains("wg_face_plate"), "Boss model must render painted visor wg_face_plate");
        assertTrue(modelSource.contains("texOffs(448, 64)"), "Boss visor must map bbmodel UV (448,64)->(480,96)");
        assertTrue(modelSource.contains("cuirass_front_plate") && modelSource.contains("texOffs(128, 192)"),
                "Boss cuirass must map bbmodel painted filigree UV (128,192)->(160,224)");
        assertFalse(modelSource.contains("face_nose_bridge"), "Legacy protruding face_nose_bridge must be removed");
        assertFalse(modelSource.contains("face_eye_l"), "Legacy protruding face_eye_l must be removed");
        assertFalse(modelSource.contains("face_third_eye"), "Legacy protruding face_third_eye must be removed");
    }

    @Test
    void spiritSpringPhysicsSettlementWorldgenAndCoreFallbackContractsHold() throws Exception {
        String fluidSrc = Files.readString(Path.of("src/main/java/com/xxsx/earth/xuanxue/SpiritSpringFluid.java"));
        assertTrue(fluidSrc.contains(".isWaterLike(true)"),
                "SpiritSpringFluid MUST set isWaterLike(true) to avoid NeoForge 26.1.2 movement freeze");

        String blockSrc = Files.readString(Path.of("src/main/java/com/xxsx/earth/xuanxue/SpiritSpringFluidBlock.java"));
        assertTrue(blockSrc.contains("CONCENTRATION"), "SpiritSpringFluidBlock must track concentration 1..4");
        assertTrue(blockSrc.contains("EarthCoreApi.fluids().diffuseStep"), "SpiritSpring must use EarthCore miscible diffusion");
        assertTrue(blockSrc.contains("SPIRIT_SPRING_NOURISH") && blockSrc.contains("QI_OVERLOAD"),
                "SpiritSpring must apply nourish buff & overload debuff");

        String settlementSrc = Files.readString(Path.of("src/main/java/com/xxsx/earth/xuanxue/settlement/XuanhuanSettlementFeature.java"));
        assertFalse(settlementSrc.contains("SETTLEMENT_ANCHOR"), "Artificial settlement_anchor block must be removed");
        assertFalse(settlementSrc.contains("Blocks.FERN"), "Settlement spirit soil must grow SpiritHerbBlock, never vanilla fern");
        assertTrue(settlementSrc.contains("SPIRIT_HERB_PLANT"), "Settlement terrace must plant SPIRIT_HERB_PLANT");
        assertTrue(settlementSrc.contains("EarthCoreApi.compat()"), "Settlement must use EarthCore signpost/Waystones compat");
        assertFalse(Files.exists(Path.of("src/main/java/com/xxsx/earth/xuanxue/settlement/XuanhuanSettlementAnchorBlock.java")),
                "XuanhuanSettlementAnchorBlock.java must be deleted");
        try (Stream<Path> walk = Files.walk(Path.of("src/main/resources"))) {
            long leftover = walk.filter(f -> f.getFileName().toString().contains("settlement_anchor")).count();
            assertEquals(0L, leftover, "Zero leftover settlement_anchor resource files may remain");
        }
        assertTrue(settlementSrc.contains("findOpenSignpostSpot"), "Signpost must use findOpenSignpostSpot to avoid colliding with center spring/altar/campfire");
        assertTrue(settlementSrc.contains("BlockState topState = edge ? palette.wall() : palette.path();"),
                "Courtyard pave() must use natural palette.path(), reserving wooden planks strictly for interior huts");
        assertTrue(Files.exists(Path.of("src/main/java/com/xxsx/earth/xuanxue/SpiritSpringBottleItem.java")),
                "SpiritSpringBottleItem must exist for drinkable/dilutable spirit spring bottles");

        String tooltipSrc = Files.readString(Path.of("src/main/java/com/xxsx/earth/xuanxue/client/XuanhuanTooltipHandler.java"));
        assertTrue(tooltipSrc.contains("EarthCoreApi.forging().resolveProfile"),
                "Weapons/implements must integrate EarthCore ForgingPresetApi fallback");
    }
}
