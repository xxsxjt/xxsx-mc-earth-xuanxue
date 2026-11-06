package com.xxsx.xuanhuanearth;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/**
 * X-024 Cultivation Journal: displays the four routes (level, xp/xpNeeded, cap, realm bottleneck)
 * and opens the cultivation screen.
 */
public class CultivationJournalItem extends GuidedXuanhuanItem {
    public CultivationJournalItem(Properties properties, String hintKey) {
        super(properties, hintKey);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
            int realm = ArcanaPower.getRealm(player);
            player.sendSystemMessage(Component.translatable(
                    "message.xuanhuan_earth.journal.header",
                    Component.translatable(RealmBreakthrough.realmNameKey(realm)),
                    realm).withStyle(ChatFormatting.GOLD));
            for (CultivationFocus focus : CultivationFocus.values()) {
                int lv = ArcanaPower.getFocusLevel(player, focus);
                int xp = ArcanaPower.getFocusXp(player, focus);
                int needed = ArcanaPower.getFocusXpNeeded(player, focus);
                boolean atCap = lv >= ArcanaPower.MAX_CULTIVATION_LEVEL;
                String progress = lv <= 0 ? "-" : (atCap ? "MAX" : (xp + "/" + needed));
                player.sendSystemMessage(Component.translatable(
                        "message.xuanhuan_earth.journal.route",
                        Component.translatable(focus.titleKey()),
                        lv,
                        progress).withStyle(lv > 0 ? ChatFormatting.AQUA : ChatFormatting.DARK_GRAY));
            }
            if (realm < RealmBreakthrough.MAX_REALM) {
                int next = realm + 1;
                player.sendSystemMessage(Component.translatable(
                        "message.xuanhuan_earth.journal.bottleneck",
                        Component.translatable(RealmBreakthrough.realmNameKey(next)),
                        RealmBreakthrough.requiredLevel(next)).withStyle(ChatFormatting.LIGHT_PURPLE));
            }
            CultivationNetwork.sync(serverPlayer, player.blockPosition(), true);
        }
        return level.isClientSide() ? InteractionResult.SUCCESS : InteractionResult.SUCCESS_SERVER;
    }
}
