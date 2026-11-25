package com.xxsx.earth.xuanxue;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record CultivationVisualPayload(int entityId, int actionId) implements CustomPacketPayload {
    public static final Type<CultivationVisualPayload> TYPE =
            new Type<>(XuanhuanEarth.id("cultivation_visual"));
    public static final StreamCodec<RegistryFriendlyByteBuf, CultivationVisualPayload> CODEC =
            new StreamCodec<>() {
                @Override
                public CultivationVisualPayload decode(RegistryFriendlyByteBuf buf) {
                    return new CultivationVisualPayload(buf.readVarInt(), buf.readVarInt());
                }

                @Override
                public void encode(RegistryFriendlyByteBuf buf, CultivationVisualPayload payload) {
                    buf.writeVarInt(payload.entityId());
                    buf.writeVarInt(payload.actionId());
                }
            };

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
