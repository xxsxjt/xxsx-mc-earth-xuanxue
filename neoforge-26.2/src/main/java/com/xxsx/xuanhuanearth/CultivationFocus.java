package com.xxsx.xuanhuanearth;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public enum CultivationFocus {
    CIRCULATION(0, "circulation"),
    BODY_TEMPERING(1, "body_tempering"),
    FETAL_BREATH(2, "fetal_breath"),
    BIGU(3, "bigu");

    private final int id;
    private final String path;

    CultivationFocus(int id, String path) {
        this.id = id;
        this.path = path;
    }

    public int id() {
        return id;
    }

    public String path() {
        return path;
    }

    public String titleKey() {
        return "screen.xuanhuan_earth.cultivation.focus." + path;
    }

    public String descriptionKey() {
        return titleKey() + ".desc";
    }

    public boolean isUnlocked(Player player) {
        boolean hasLevel = switch (this) {
            case CIRCULATION -> ArcanaPower.getCultivationLevel(player) > 0;
            case BODY_TEMPERING -> ArcanaPower.getBodyTemperingLevel(player) > 0;
            case FETAL_BREATH -> ArcanaPower.getFetalBreathLevel(player) > 0;
            case BIGU -> ArcanaPower.getBiguLevel(player) > 0;
        };
        if (!hasLevel) {
            return false;
        }
        if (player instanceof ServerPlayer serverPlayer) {
            return XuanhuanLoreIntegration.knowsFocus(serverPlayer, this);
        }
        return true;
    }

    public static CultivationFocus byId(int id) {
        for (CultivationFocus focus : values()) {
            if (focus.id == id) {
                return focus;
            }
        }
        return CIRCULATION;
    }
}
