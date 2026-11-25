package com.xxsx.earth.xuanxue;

final class JourneyProgress {
    private JourneyProgress() {
    }

    static int sanitize(int mask, int total) {
        return mask & allMask(total);
    }

    static int apply(int mask, int milestoneId, int total) {
        if (milestoneId < 0 || milestoneId >= total) {
            throw new IllegalArgumentException("milestone id out of range: " + milestoneId);
        }
        return sanitize(mask | (1 << milestoneId), total);
    }

    static int count(int mask, int total) {
        return Integer.bitCount(sanitize(mask, total));
    }

    static boolean isComplete(int mask, int total) {
        return sanitize(mask, total) == allMask(total);
    }

    static int nextId(int mask, int total) {
        int sanitized = sanitize(mask, total);
        for (int id = 0; id < total; id++) {
            if ((sanitized & (1 << id)) == 0) {
                return id;
            }
        }
        return -1;
    }

    private static int allMask(int total) {
        if (total <= 0 || total > 30) {
            throw new IllegalArgumentException("journey milestone count out of range: " + total);
        }
        return (1 << total) - 1;
    }
}
