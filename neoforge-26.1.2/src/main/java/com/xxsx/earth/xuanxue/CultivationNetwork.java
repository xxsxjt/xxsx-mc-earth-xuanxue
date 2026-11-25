package com.xxsx.earth.xuanxue;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public final class CultivationNetwork {
    private CultivationNetwork() {
    }

    public static void registerPayloads(RegisterPayloadHandlersEvent event) {
        event.registrar("0.7.0-alpha.5")
                .playToClient(CultivationStatusPayload.TYPE, CultivationStatusPayload.CODEC)
                .playToClient(CultivationVisualPayload.TYPE, CultivationVisualPayload.CODEC)
                .playToServer(CultivationActionPayload.TYPE, CultivationActionPayload.CODEC,
                        CultivationNetwork::handleAction);
    }

    public static void sync(ServerPlayer player, BlockPos pos, boolean openScreen) {
        if (!(player.level() instanceof ServerLevel level)) {
            return;
        }
        ArcanaChunkField.Reading reading = Spirituality.reading(level, pos);
        EarthHumanCompat.HumanSnapshot human = EarthHumanCompat.snapshot(player);
        CultivationFocus focus = ArcanaPower.getCultivationFocus(player);
        PacketDistributor.sendToPlayer(player, new CultivationStatusPayload(
                ArcanaPower.getCurrentMana(player),
                ArcanaPower.getMaxMana(player),
                reading.value(),
                reading.depletion(),
                (int) Math.min(Integer.MAX_VALUE, ArcanaPower.getQiMeditationCooldownTicks(player, level)),
                focus.id(),
                ArcanaPower.getCultivationFocusMask(player),
                XuanhuanJourney.reconcile(player),
                ArcanaPower.getFocusLevel(player, focus),
                ArcanaPower.getFocusXp(player, focus),
                ArcanaPower.getFocusXpNeeded(player, focus),
                (int) Math.min(Integer.MAX_VALUE, ArcanaPower.getSkillCooldownTicks(player, level)),
                human.linked(),
                human.fatigue(),
                human.bodyIntegrity(),
                reading.mainSourceKey(),
                player.getVehicle() instanceof MeditationSeatEntity,
                openScreen,
                ArcanaPower.getRealm(player)));
    }

    public static void broadcastVisual(ServerPlayer player, CultivationVisualAction action) {
        PacketDistributor.sendToPlayersTrackingEntityAndSelf(player,
                new CultivationVisualPayload(player.getId(), action.id()));
    }

    private static void handleAction(CultivationActionPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)
                    || !(player.level() instanceof ServerLevel level)) {
                return;
            }

            MeditationSeatEntity seat = player.getVehicle() instanceof MeditationSeatEntity meditationSeat
                    ? meditationSeat
                    : null;
            BlockPos pos = seat == null ? player.blockPosition() : seat.sourcePos();
            int action = payload.focusId();

            if (action == CultivationActionPayload.OPEN_PANEL) {
                sync(player, pos, true);
                return;
            }
            if (action == CultivationActionPayload.REFRESH_STATUS) {
                sync(player, pos, false);
                return;
            }
            if (action == CultivationActionPayload.STOP_RIDING) {
                if (seat != null) {
                    player.stopRiding();
                }
                sync(player, player.blockPosition(), false);
                return;
            }
            if (action == CultivationActionPayload.PRACTICE) {
                CultivationPractice.perform(level, pos, player,
                        seat == null ? CultivationPractice.Support.FREE : CultivationPractice.Support.CUSHION,
                        false);
                sync(player, pos, false);
                return;
            }
            if (action == CultivationActionPayload.ACTIVATE_SKILL) {
                CultivationSkill.activate(level, player);
                sync(player, pos, false);
                return;
            }
            if (action == CultivationActionPayload.BREAKTHROUGH) {
                RealmBreakthrough.attempt(level, player);
                sync(player, pos, false);
                return;
            }
            if (action < 0) {
                return;
            }

            CultivationFocus focus = CultivationFocus.byId(action);
            if (!focus.isUnlocked(player)) {
                player.sendSystemMessage(Component.translatable(
                        "message.earth_xuanxue.cultivation.focus.locked",
                        Component.translatable(focus.titleKey())));
                sync(player, pos, false);
                return;
            }
            if (ArcanaPower.setCultivationFocus(player, focus)) {
                level.playSound(null, pos, SoundEvents.AMETHYST_BLOCK_RESONATE,
                        SoundSource.PLAYERS, 0.55F, 0.85F + focus.id() * 0.08F);
                if (seat != null) {
                    seat.emitFocusChange(focus);
                } else {
                    CultivationPractice.emitFocusChange(level, pos, focus, false);
                }
            }
            sync(player, pos, false);
        });
    }
}
