package com.xxsx.xuanhuanearth.client;

import com.xxsx.xuanhuanearth.CultivationVisualAction;
import com.xxsx.xuanhuanearth.CultivationVisualPayload;
import com.xxsx.xuanhuanearth.particle.XuanhuanParticles;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.neoforged.neoforge.client.event.RenderPlayerEvent;

import java.util.HashMap;
import java.util.Map;

/**
 * Client-side player pose driver for cultivation actions.
 *
 * <p>Poses are authored as three overlapping phases — wind-up, release and settle — instead
 * of a single sine pulse. That gives a readable "gather, then cast, then recover" beat even
 * for actions that only last a second, and it keeps the hands clear of the body silhouette
 * during the release frame.</p>
 */
public final class CultivationPlayerAnimations {
    private static final Map<Integer, ActiveAction> ACTIVE = new HashMap<>();
    private static ClientLevel activeLevel;
    private static int auraTicker;

    private CultivationPlayerAnimations() {
    }

    public static void start(CultivationVisualPayload payload) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) {
            return;
        }
        ensureLevel(minecraft.level);
        CultivationVisualAction action = CultivationVisualAction.byId(payload.actionId());
        ACTIVE.put(payload.entityId(), new ActiveAction(action, minecraft.level.getGameTime()));
    }

    public static void tick() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) {
            ACTIVE.clear();
            activeLevel = null;
            return;
        }
        ensureLevel(minecraft.level);
        long now = minecraft.level.getGameTime();
        ACTIVE.entrySet().removeIf(entry -> entry.getValue().isExpired(now));
        if (++auraTicker % 3 == 0) {
            emitAura(minecraft, now);
        }
    }

    /** Small client-side wisps around any player currently in a cultivation action. */
    private static void emitAura(Minecraft minecraft, long now) {
        if (minecraft.level == null) {
            return;
        }
        for (Map.Entry<Integer, ActiveAction> entry : ACTIVE.entrySet()) {
            var entity = minecraft.level.getEntity(entry.getKey());
            if (entity == null) {
                continue;
            }
            float progress = entry.getValue().progress(now, 0.0F);
            if (progress < 0.05F || progress > 0.95F) {
                continue;
            }
            double x = entity.getX() + (minecraft.level.getRandom().nextDouble() - 0.5D) * 0.9D;
            double y = entity.getY() + 0.15D + minecraft.level.getRandom().nextDouble() * 1.5D;
            double z = entity.getZ() + (minecraft.level.getRandom().nextDouble() - 0.5D) * 0.9D;
            minecraft.level.addParticle(
                    entry.getValue().action() == CultivationVisualAction.BODY_TEMPERING
                            ? XuanhuanParticles.EMBER_SPARK.get()
                            : XuanhuanParticles.QI_MOTE.get(),
                    x, y, z, 0.0D, 0.012D, 0.0D);
        }
    }

    public static void renderPlayer(RenderPlayerEvent.Pre<?> event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) {
            return;
        }
        ActiveAction active = ACTIVE.get(event.getRenderState().id);
        if (active == null) {
            return;
        }
        float progress = active.progress(minecraft.level.getGameTime(), event.getPartialTick());
        if (progress < 0.0F || progress >= 1.0F) {
            return;
        }
        apply(event.getRenderer().getModel(), active.action(), progress);
    }

    /** Wind-up in 0-0.25, hold 0.25-0.7, settle after. */
    private static float windUp(float progress) {
        return Mth.clamp(progress / 0.25F, 0.0F, 1.0F);
    }

    private static float release(float progress) {
        return Mth.clamp((progress - 0.25F) / 0.45F, 0.0F, 1.0F);
    }

    private static float settle(float progress) {
        return Mth.clamp((progress - 0.7F) / 0.3F, 0.0F, 1.0F);
    }

    private static void apply(PlayerModel model, CultivationVisualAction action, float progress) {
        float pulse = Mth.sin(progress * Mth.PI);
        float rise = windUp(progress);
        float cast = release(progress);
        float fade = settle(progress);

        switch (action) {
            case MEDITATION -> {
                model.rightArm.xRot = Mth.lerp(rise, 0.0F, -0.95F) - pulse * 0.18F + fade * 0.2F;
                model.leftArm.xRot = Mth.lerp(rise, 0.0F, -0.95F) - pulse * 0.18F + fade * 0.2F;
                model.rightArm.yRot = Mth.lerp(rise, 0.0F, -0.34F);
                model.leftArm.yRot = Mth.lerp(rise, 0.0F, 0.34F);
                model.rightArm.zRot = Mth.lerp(rise, 0.0F, 0.18F);
                model.leftArm.zRot = Mth.lerp(rise, 0.0F, -0.18F);
                model.body.xRot += 0.06F * pulse;
                model.head.xRot += 0.10F * pulse;
                model.rightLeg.xRot = -1.30F * rise * (1.0F - fade);
                model.leftLeg.xRot = -1.30F * rise * (1.0F - fade);
            }
            case RECOVERY -> {
                model.rightArm.xRot = Mth.lerp(rise, 0.0F, -1.28F) + pulse * 0.16F;
                model.leftArm.xRot = Mth.lerp(rise, 0.0F, -1.28F) + pulse * 0.16F;
                model.rightArm.yRot = -0.42F * rise;
                model.leftArm.yRot = 0.42F * rise;
                model.head.xRot += 0.10F * pulse;
                model.body.xRot -= 0.04F * pulse;
            }
            case CIRCULATION -> {
                // Hands sweep from chest height outward, then close again.
                float sweep = Mth.lerp(cast, -0.58F, 0.62F);
                model.rightArm.xRot = -1.05F - pulse * 0.42F;
                model.leftArm.xRot = -1.05F - pulse * 0.42F;
                model.rightArm.yRot = sweep;
                model.leftArm.yRot = -sweep;
                model.rightArm.zRot = 0.28F * rise * (1.0F - cast);
                model.leftArm.zRot = -0.28F * rise * (1.0F - cast);
                model.head.yRot += Mth.sin(progress * Mth.TWO_PI) * 0.08F;
            }
            case BODY_TEMPERING -> {
                float flare = Mth.lerp(cast, 0.72F, 0.36F);
                model.rightArm.xRot = Mth.lerp(rise, 0.0F, -1.18F);
                model.leftArm.xRot = Mth.lerp(rise, 0.0F, -1.18F);
                model.rightArm.zRot = flare - pulse * 0.18F;
                model.leftArm.zRot = -flare + pulse * 0.18F;
                model.body.xRot -= 0.08F * pulse;
                model.rightLeg.xRot = -0.18F * rise;
                model.leftLeg.xRot = 0.18F * rise;
            }
            case FETAL_BREATH -> {
                model.rightArm.xRot = Mth.lerp(rise, 0.0F, -0.78F) - pulse * 0.22F;
                model.leftArm.xRot = Mth.lerp(rise, 0.0F, -0.78F) - pulse * 0.22F;
                model.rightArm.yRot = -0.28F * rise;
                model.leftArm.yRot = 0.28F * rise;
                model.head.xRot += 0.16F * pulse;
                model.body.xRot -= 0.05F * pulse;
            }
            case BIGU -> {
                model.rightArm.xRot = Mth.lerp(rise, 0.0F, -0.72F);
                model.leftArm.xRot = Mth.lerp(rise, 0.0F, -0.72F);
                model.rightArm.yRot = -0.50F + pulse * 0.08F;
                model.leftArm.yRot = 0.50F - pulse * 0.08F;
                model.body.xRot += 0.04F * pulse;
                model.head.xRot += 0.06F * rise;
            }
            case TALISMAN -> {
                // Raise the talisman overhead, snap it down, then let the arm drop.
                float raise = Mth.lerp(cast, -1.65F, -0.55F);
                model.rightArm.xRot = raise - fade * 0.35F;
                model.rightArm.yRot = -0.36F + cast * 0.72F;
                model.rightArm.zRot = 0.18F + pulse * 0.34F;
                model.leftArm.xRot = -0.55F * rise;
                model.leftArm.yRot = 0.34F * rise;
                model.head.xRot -= 0.12F * pulse;
                model.body.yRot += 0.10F * (cast - fade);
            }
            case IMPLEMENT_CAST -> {
                // Two-handed seal / sword-qi sweep: form seal at chest, sweep right arm outward.
                float sweep = Mth.lerp(cast, -0.45F, 0.78F);
                model.rightArm.xRot = Mth.lerp(rise, 0.0F, -1.42F) + cast * 0.48F;
                model.rightArm.yRot = sweep * (1.0F - fade * 0.5F);
                model.rightArm.zRot = 0.28F * pulse;
                model.leftArm.xRot = Mth.lerp(rise, 0.0F, -1.15F) - pulse * 0.15F;
                model.leftArm.yRot = 0.42F * rise * (1.0F - fade * 0.4F);
                model.leftArm.zRot = -0.22F * rise;
                model.body.yRot += 0.14F * (cast - fade);
                model.head.xRot -= 0.08F * pulse;
            }
        }
        copyPose(model.rightArm, model.rightSleeve);
        copyPose(model.leftArm, model.leftSleeve);
        copyPose(model.body, model.jacket);
    }

    private static void copyPose(ModelPart source, ModelPart target) {
        target.x = source.x;
        target.y = source.y;
        target.z = source.z;
        target.xRot = source.xRot;
        target.yRot = source.yRot;
        target.zRot = source.zRot;
        target.xScale = source.xScale;
        target.yScale = source.yScale;
        target.zScale = source.zScale;
    }

    private static void ensureLevel(ClientLevel level) {
        if (activeLevel != level) {
            ACTIVE.clear();
            activeLevel = level;
        }
    }

    private record ActiveAction(CultivationVisualAction action, long startTick) {
        boolean isExpired(long now) {
            return now - startTick >= action.durationTicks();
        }

        float progress(long now, float partialTick) {
            return (now + partialTick - startTick) / action.durationTicks();
        }
    }
}
