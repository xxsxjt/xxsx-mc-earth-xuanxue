package com.xxsx.earth.xuanxue;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record CultivationActionPayload(int focusId) implements CustomPacketPayload {
    public static final int STOP_RIDING = -1;
    public static final int OPEN_PANEL = -2;
    public static final int PRACTICE = -3;
    public static final int REFRESH_STATUS = -4;
    public static final int ACTIVATE_SKILL = -5;
    public static final int BREAKTHROUGH = -6;
    public static final Type<CultivationActionPayload> TYPE =
            new Type<>(XuanhuanEarth.id("cultivation_action"));
    public static final StreamCodec<RegistryFriendlyByteBuf, CultivationActionPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT.cast(), CultivationActionPayload::focusId,
            CultivationActionPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
