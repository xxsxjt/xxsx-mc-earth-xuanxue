package com.xxsx.earth.xuanxue;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record CultivationStatusPayload(
        double currentMana,
        double maxMana,
        int fieldValue,
        double depletion,
        int remainingTicks,
        int focusId,
        int unlockedMask,
        int journeyMask,
        int focusLevel,
        int focusXp,
        int focusXpNeeded,
        int skillRemainingTicks,
        boolean earthHumanLinked,
        double fatigue,
        double bodyIntegrity,
        String sourceKey,
        boolean seated,
        boolean openScreen,
        int realm) implements CustomPacketPayload {
    public static final Type<CultivationStatusPayload> TYPE =
            new Type<>(XuanhuanEarth.id("cultivation_status"));
    public static final StreamCodec<RegistryFriendlyByteBuf, CultivationStatusPayload> CODEC =
            new StreamCodec<>() {
                @Override
                public CultivationStatusPayload decode(RegistryFriendlyByteBuf buf) {
                    return new CultivationStatusPayload(
                            buf.readDouble(), buf.readDouble(), buf.readVarInt(), buf.readDouble(),
                            buf.readVarInt(), buf.readVarInt(), buf.readVarInt(), buf.readVarInt(),
                            buf.readVarInt(), buf.readVarInt(), buf.readVarInt(), buf.readVarInt(),
                            buf.readBoolean(),
                            buf.readDouble(), buf.readDouble(), buf.readUtf(), buf.readBoolean(), buf.readBoolean(), buf.readVarInt());
                }

                @Override
                public void encode(RegistryFriendlyByteBuf buf, CultivationStatusPayload payload) {
                    buf.writeDouble(payload.currentMana());
                    buf.writeDouble(payload.maxMana());
                    buf.writeVarInt(payload.fieldValue());
                    buf.writeDouble(payload.depletion());
                    buf.writeVarInt(payload.remainingTicks());
                    buf.writeVarInt(payload.focusId());
                    buf.writeVarInt(payload.unlockedMask());
                    buf.writeVarInt(payload.journeyMask());
                    buf.writeVarInt(payload.focusLevel());
                    buf.writeVarInt(payload.focusXp());
                    buf.writeVarInt(payload.focusXpNeeded());
                    buf.writeVarInt(payload.skillRemainingTicks());
                    buf.writeBoolean(payload.earthHumanLinked());
                    buf.writeDouble(payload.fatigue());
                    buf.writeDouble(payload.bodyIntegrity());
                    buf.writeUtf(payload.sourceKey());
                    buf.writeBoolean(payload.seated());
                    buf.writeBoolean(payload.openScreen());
                    buf.writeVarInt(payload.realm());
                }
            };

    public static CultivationStatusPayload empty() {
        return new CultivationStatusPayload(0.0D, 20.0D, 0, 0.0D, 0,
                CultivationFocus.CIRCULATION.id(), 0, 0, 0, 0, 0, 0, false, 0.0D, 1.0D,
                "spirituality.earth_xuanxue.source.natural", false, false, 0);
    }

    public boolean isUnlocked(CultivationFocus focus) {
        return (unlockedMask & (1 << focus.id())) != 0;
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
