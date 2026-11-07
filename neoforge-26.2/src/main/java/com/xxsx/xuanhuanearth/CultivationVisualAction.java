package com.xxsx.xuanhuanearth;

public enum CultivationVisualAction {
    MEDITATION(0, 30),
    RECOVERY(1, 24),
    CIRCULATION(2, 20),
    BODY_TEMPERING(3, 24),
    FETAL_BREATH(4, 24),
    BIGU(5, 20),
    TALISMAN(6, 16),
    IMPLEMENT_CAST(7, 20);

    private final int id;
    private final int durationTicks;

    CultivationVisualAction(int id, int durationTicks) {
        this.id = id;
        this.durationTicks = durationTicks;
    }

    public int id() {
        return id;
    }

    public int durationTicks() {
        return durationTicks;
    }

    public static CultivationVisualAction forFocus(CultivationFocus focus) {
        return switch (focus) {
            case CIRCULATION -> CIRCULATION;
            case BODY_TEMPERING -> BODY_TEMPERING;
            case FETAL_BREATH -> FETAL_BREATH;
            case BIGU -> BIGU;
        };
    }

    public static CultivationVisualAction byId(int id) {
        for (CultivationVisualAction action : values()) {
            if (action.id == id) {
                return action;
            }
        }
        return MEDITATION;
    }
}
