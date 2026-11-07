package com.xxsx.xuanhuanearth;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.fml.ModList;

import java.lang.reflect.Method;

public final class EarthHumanCompat {
    private static final String MODID = "earth_human";
    private static final String API_CLASS = "com.xxsx.earthhuman.api.EarthHumanApi";
    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(EarthHumanCompat.class);
    private static volatile ApiBinding binding;
    private static volatile boolean resolved;
    private static volatile boolean warned;

    private EarthHumanCompat() {
    }

    public static RecoveryReport recover(ServerPlayer player, double fatigueReduction,
                                         double bodyHeal, BodyTarget... targets) {
        ApiBinding api = api();
        if (api == null) {
            return RecoveryReport.EMPTY;
        }
        try {
            Object value = api.recover.invoke(null,
                    new Object[]{player, fatigueReduction, bodyHeal, bodyIds(targets)});
            return new RecoveryReport(
                    number(api.recoveryFatigue.invoke(value)),
                    number(api.recoveryBody.invoke(value)));
        } catch (ReflectiveOperationException | RuntimeException e) {
            warnOnce("Earth Human recovery API call failed; integration is disabled for this session", e);
            return RecoveryReport.EMPTY;
        }
    }

    public static RecoveryReport recoverCore(ServerPlayer player, double fatigueReduction, double bodyHeal) {
        return recover(player, fatigueReduction, bodyHeal, BodyTarget.TORSO, BodyTarget.LEFT_ARM,
                BodyTarget.RIGHT_ARM, BodyTarget.LEFT_LEG, BodyTarget.RIGHT_LEG);
    }

    public static RecoveryReport recoverBreath(ServerPlayer player, double fatigueReduction, double bodyHeal) {
        return recover(player, fatigueReduction, bodyHeal, BodyTarget.TORSO);
    }

    /**
     * Repairs fractures, restores limb/torso integrity, and optionally purifies lung smoke/air quality.
     */
    public static void treatFractureAndCleanseAir(ServerPlayer player, double fractureRelief,
                                                  double bodyHeal, double lungAirRestore,
                                                  double hydrationBonus, double fatigueReduction) {
        recoverCore(player, fatigueReduction, bodyHeal);
        boolean bridged = false;
        try {
            Class<?> bodyClass = Class.forName("com.xxsx.earthhuman.HumanBodyStatus");
            Class<?> statusClass = Class.forName("com.xxsx.earthhuman.HumanStatus");
            Class<?> netClass = Class.forName("com.xxsx.earthhuman.HumanNetwork");

            Method getBody = bodyClass.getMethod("get", Player.class);
            Object body = getBody.invoke(null, player);
            if (fractureRelief > 0.0D) {
                Method treatFracture = bodyClass.getMethod("treatFracture", ServerPlayer.class, double.class, double.class);
                treatFracture.invoke(body, player, fractureRelief, bodyHeal);
            }
            Method applyFunc = bodyClass.getMethod("applyFunctionalEffects", ServerPlayer.class);
            applyFunc.invoke(body, player);
            Method saveBody = bodyClass.getMethod("save", Player.class);
            saveBody.invoke(body, player);
            Method syncBody = netClass.getMethod("syncBody", ServerPlayer.class, bodyClass);
            syncBody.invoke(null, player, body);

            Method getStatus = statusClass.getMethod("get", Player.class);
            Object status = getStatus.invoke(null, player);
            if (lungAirRestore > 0.0D) {
                double air = statusClass.getField("airQuality").getDouble(status);
                statusClass.getField("airQuality").setDouble(status, Math.max(0.0D, Math.min(100.0D, air + lungAirRestore)));
            }
            if (hydrationBonus > 0.0D) {
                double h = statusClass.getField("hydration").getDouble(status);
                statusClass.getField("hydration").setDouble(status, Math.max(0.0D, Math.min(100.0D, h + hydrationBonus)));
            }
            Method saveStatus = statusClass.getMethod("save", Player.class);
            saveStatus.invoke(status, player);
            Method syncStatus = netClass.getMethod("sync", ServerPlayer.class, statusClass);
            syncStatus.invoke(null, player, status);
            bridged = true;
        } catch (Throwable ignored) {
        }
        if (!bridged) {
            CompoundTag data = player.getPersistentData();
            if (fractureRelief > 0.0D) {
                double leg = data.getDoubleOr("earth_human.body.injury.leg", 0.0D);
                double hand = data.getDoubleOr("earth_human.body.injury.right_hand", 0.0D);
                data.putDouble("earth_human.body.injury.leg", Math.max(0.0D, leg - fractureRelief));
                data.putDouble("earth_human.body.injury.right_hand", Math.max(0.0D, hand - fractureRelief));
            }
            if (lungAirRestore > 0.0D) {
                double air = data.getDoubleOr("earth_human.air_quality", 100.0D);
                data.putDouble("earth_human.air_quality", Math.min(100.0D, air + lungAirRestore));
            }
            if (hydrationBonus > 0.0D) {
                double h = data.getDoubleOr("earth_human.hydration", 100.0D);
                data.putDouble("earth_human.hydration", Math.min(100.0D, h + hydrationBonus));
            }
        }
    }

    public static boolean isLoaded() {
        return api() != null;
    }

    public static HumanSnapshot snapshot(Player player) {
        ApiBinding api = api();
        if (api == null || !(player instanceof ServerPlayer serverPlayer)) {
            return HumanSnapshot.EMPTY;
        }
        try {
            Object value = api.snapshot.invoke(null, serverPlayer);
            return new HumanSnapshot(true,
                    number(api.snapshotFatigue.invoke(value)),
                    number(api.snapshotBodyIntegrity.invoke(value)));
        } catch (ReflectiveOperationException | RuntimeException e) {
            warnOnce("Earth Human snapshot API call failed; integration is disabled for this session", e);
            return HumanSnapshot.EMPTY;
        }
    }

    public static boolean canRecover(Player player) {
        HumanSnapshot snapshot = snapshot(player);
        return snapshot.linked()
                && (snapshot.fatigue() > 0.01D || snapshot.bodyIntegrity() < 0.999D);
    }

    private static ApiBinding api() {
        if (!ModList.get().isLoaded(MODID)) {
            return null;
        }
        if (resolved) {
            return binding;
        }
        synchronized (EarthHumanCompat.class) {
            if (resolved) {
                return binding;
            }
            resolved = true;
            try {
                Class<?> apiClass = Class.forName(API_CLASS);
                Method version = apiClass.getMethod("apiVersion");
                int apiVersion = ((Number) version.invoke(null)).intValue();
                if (apiVersion < 1) {
                    warnOnce("Earth Human API is older than v1; integration is disabled", null);
                    return null;
                }
                Method recover = apiClass.getMethod("recover", ServerPlayer.class,
                        double.class, double.class, String[].class);
                Method snapshot = apiClass.getMethod("snapshot", ServerPlayer.class);
                Class<?> recoveryType = recover.getReturnType();
                Class<?> snapshotType = snapshot.getReturnType();
                binding = new ApiBinding(
                        recover,
                        recoveryType.getMethod("fatigueReduced"),
                        recoveryType.getMethod("bodyHealed"),
                        snapshot,
                        snapshotType.getMethod("fatigue"),
                        snapshotType.getMethod("bodyIntegrity"));
            } catch (ReflectiveOperationException | LinkageError | RuntimeException e) {
                warnOnce("Earth Human API v1 was not available; integration is disabled", e);
            }
            return binding;
        }
    }

    private static String[] bodyIds(BodyTarget... targets) {
        if (targets == null || targets.length == 0) {
            return new String[0];
        }
        String[] ids = new String[targets.length];
        for (int i = 0; i < targets.length; i++) {
            ids[i] = targets[i].id;
        }
        return ids;
    }

    private static double number(Object value) {
        return value instanceof Number number ? number.doubleValue() : 0.0D;
    }

    private static void warnOnce(String message, Throwable error) {
        if (warned) {
            return;
        }
        warned = true;
        if (error == null) {
            LOGGER.warn(message);
        } else {
            LOGGER.warn(message, error);
        }
    }

    public enum BodyTarget {
        HEAD("head"),
        TORSO("torso"),
        LEFT_ARM("left_arm"),
        RIGHT_ARM("right_arm"),
        LEFT_LEG("left_leg"),
        RIGHT_LEG("right_leg");

        private final String id;

        BodyTarget(String id) {
            this.id = id;
        }
    }

    public record RecoveryReport(double fatigueReduced, double bodyHealed) {
        private static final RecoveryReport EMPTY = new RecoveryReport(0.0D, 0.0D);

        public boolean changed() {
            return fatigueReduced > 0.01D || bodyHealed > 0.01D;
        }
    }

    public record HumanSnapshot(boolean linked, double fatigue, double bodyIntegrity) {
        private static final HumanSnapshot EMPTY = new HumanSnapshot(false, 0.0D, 1.0D);
    }

    private record ApiBinding(Method recover, Method recoveryFatigue, Method recoveryBody,
                              Method snapshot, Method snapshotFatigue, Method snapshotBodyIntegrity) {
    }
}
