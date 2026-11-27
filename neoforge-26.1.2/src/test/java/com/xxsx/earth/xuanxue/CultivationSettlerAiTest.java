package com.xxsx.earth.xuanxue;

import com.xxsx.earth.xuanxue.entity.CultivationSettlerEntity;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CultivationSettlerAiTest {

    @Test
    void settlerPosesAndRecoveryConstantsAreDefined() {
        assertEquals(4, CultivationSettlerEntity.POSE_MEDITATE);
        assertEquals(5, CultivationSettlerEntity.POSE_DRINK_PILL);
        assertTrue(CultivationSettlerEntity.PILL_RECOVERY_COOLDOWN >= 120);
        assertTrue(CultivationSettlerEntity.SPELL_RECOVERY_COOLDOWN >= 100);
    }

    @Test
    void settlerOutOfCombatRecoveryAndCreativeImmunityAreImplemented() throws Exception {
        String settlerSource = Files.readString(Path.of(
                "src/main/java/com/xxsx/earth/xuanxue/entity/CultivationSettlerEntity.java"));
        assertTrue(settlerSource.contains("player.isCreative() || player.isSpectator()"));
        assertTrue(settlerSource.contains("tickOutOfCombatRecoveryAndLife"));
        assertTrue(settlerSource.contains("performPillRecovery"));
        assertTrue(settlerSource.contains("performRecoverySpell"));
        assertTrue(settlerSource.contains("startMeditation"));
        assertTrue(settlerSource.contains("performIdleCultivationDrill"));
        assertTrue(settlerSource.contains("isBusyCultivating"));

        String modelSource = Files.readString(Path.of(
                "src/main/java/com/xxsx/earth/xuanxue/client/model/CultivationSettlerModel.java"));
        assertTrue(modelSource.contains("POSE_MEDITATE"));
        assertTrue(modelSource.contains("POSE_DRINK_PILL"));

        String rendererSource = Files.readString(Path.of(
                "src/main/java/com/xxsx/earth/xuanxue/client/renderer/CultivationSettlerRenderer.java"));
        assertTrue(rendererSource.contains("POSE_MEDITATE"));

        String beastSource = Files.readString(Path.of(
                "src/main/java/com/xxsx/earth/xuanxue/entity/ContractableSpiritBeastEntity.java"));
        assertTrue(beastSource.contains("player.isCreative() || player.isSpectator()"));
    }
}
