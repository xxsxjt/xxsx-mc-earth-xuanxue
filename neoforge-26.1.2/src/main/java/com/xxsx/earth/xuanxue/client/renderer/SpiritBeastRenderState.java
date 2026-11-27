package com.xxsx.earth.xuanxue.client.renderer;

import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;

/**
 * Extra animation inputs shared by every spirit beast.
 *
 * <p>{@link LivingEntityRenderState} already carries walk, head rotation and death time,
 * but it cannot express "this beast is sprinting", "it just got hit" or "it is mid-leap".
 * Those are extracted here so models stay pure functions of the state.</p>
 */
public final class SpiritBeastRenderState extends LivingEntityRenderState {
    public float affinity;
    public int companionMode;
    public boolean aggressive;
    public float attackProgress;
    public boolean sitting;
    public float runAmount;
    public float hurtTime;
    public float swimAmount;
    public boolean inWater;
    public boolean onGround;
    public float leapProgress;
    public float tailWag;
    public float breathPhase;
    public int variant;
}
