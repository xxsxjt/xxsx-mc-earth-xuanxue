package com.xxsx.earth.xuanxue.client;

import com.xxsx.earth.lore.client.LoreTreeScreen;
import com.xxsx.earth.xuanxue.XuanhuanLoreIntegration;
import net.minecraft.client.Minecraft;

/**
 * Isolated client bridge so LoreTreeScreen is never class-loaded unless earth_lore is installed.
 */
final class XuanhuanLoreClientBridge {
    private XuanhuanLoreClientBridge() {
    }

    static void openXuanxueTree(Minecraft minecraft) {
        minecraft.setScreen(new LoreTreeScreen(XuanhuanLoreIntegration.DISCIPLINE_XUA));
    }
}
