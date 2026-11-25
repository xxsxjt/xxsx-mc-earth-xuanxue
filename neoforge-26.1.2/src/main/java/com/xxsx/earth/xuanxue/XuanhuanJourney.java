package com.xxsx.earth.xuanxue;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public final class XuanhuanJourney {
    public static final String DATA_KEY = "earth_arcana.xuanhuan_journey_mask";

    private XuanhuanJourney() {
    }

    public static int reconcile(Player player) {
        int mask = storedMask(player);
        if (ArcanaPower.getCultivationLevel(player) > 0) {
            mask = apply(mask, Milestone.INITIATION);
        }
        boolean hasPractice = false;
        for (CultivationFocus focus : CultivationFocus.values()) {
            if (ArcanaPower.getFocusXp(player, focus) > 0 || ArcanaPower.getFocusLevel(player, focus) > 1) {
                hasPractice = true;
                break;
            }
        }
        if (hasPractice) {
            mask = apply(mask, Milestone.PRACTICE);
        }
        store(player, mask);
        return mask;
    }

    public static boolean complete(ServerPlayer player, Milestone milestone) {
        int stored = storedMask(player);
        if (apply(stored, milestone) == stored) {
            return false;
        }
        int after = apply(reconcile(player), milestone);
        store(player, after);
        player.sendSystemMessage(Component.translatable(
                "message.earth_xuanxue.journey.milestone",
                Component.translatable(milestone.translationKey()), count(after), total())
                .withStyle(ChatFormatting.AQUA));
        if (isComplete(after)) {
            player.giveExperiencePoints(30);
            player.sendSystemMessage(Component.translatable(
                    "message.earth_xuanxue.journey.complete")
                    .withStyle(ChatFormatting.GOLD));
        }
        return true;
    }

    public static int total() {
        return Milestone.values().length;
    }

    public static int count(int mask) {
        return JourneyProgress.count(mask, total());
    }

    public static boolean isComplete(int mask) {
        return JourneyProgress.isComplete(mask, total());
    }

    static int apply(int mask, Milestone milestone) {
        return JourneyProgress.apply(mask, milestone.id(), total());
    }

    public static Milestone nextMilestone(int mask) {
        int id = JourneyProgress.nextId(mask, total());
        return id < 0 ? null : Milestone.values()[id];
    }

    private static int storedMask(Player player) {
        return JourneyProgress.sanitize(ArcanaPower.data(player).getIntOr(DATA_KEY, 0), total());
    }

    private static void store(Player player, int mask) {
        ArcanaPower.data(player).putInt(DATA_KEY, JourneyProgress.sanitize(mask, total()));
    }

    public enum Milestone {
        INITIATION(0, "initiation"),
        PRACTICE(1, "practice"),
        FACILITY_OUTPUT(2, "facility_output"),
        ACTIVE_SKILL(3, "active_skill"),
        ARTIFACT_USE(4, "artifact_use"),
        BEAST_CONTRACT(5, "beast_contract");

        private final int id;
        private final String path;

        Milestone(int id, String path) {
            this.id = id;
            this.path = path;
        }

        int id() {
            return id;
        }

        public String translationKey() {
            return "journey.earth_xuanxue.milestone." + path;
        }

        public String nextKey() {
            return "journey.earth_xuanxue.next." + path;
        }
    }
}
