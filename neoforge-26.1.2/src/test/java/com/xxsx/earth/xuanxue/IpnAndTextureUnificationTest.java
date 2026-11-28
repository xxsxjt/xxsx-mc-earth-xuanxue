package com.xxsx.earth.xuanxue;

import java.io.DataInputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IpnAndTextureUnificationTest {

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
    void allItemAndBlockTexturesAreUnified16x16() throws Exception {
        Path texRoot = Path.of("src/main/resources/assets/earth_xuanxue/textures");
        int checked = 0;
        for (String sub : new String[] { "item", "block" }) {
            try (Stream<Path> files = Files.list(texRoot.resolve(sub))) {
                for (Path p : files.filter(f -> f.toString().endsWith(".png")).toList()) {
                    try (DataInputStream in = new DataInputStream(Files.newInputStream(p))) {
                        in.skipBytes(16); // PNG sig (8) + IHDR len (4) + "IHDR" (4)
                        int w = in.readInt();
                        int h = in.readInt();
                        assertEquals(16, w, "Expected 16px width for " + sub + "/" + p.getFileName());
                        assertEquals(16, h, "Expected 16px height for " + sub + "/" + p.getFileName());
                        checked++;
                    }
                }
            }
        }
        assertTrue(checked >= 90, "Expected at least 90 item+block textures verified, got " + checked);
    }
}
