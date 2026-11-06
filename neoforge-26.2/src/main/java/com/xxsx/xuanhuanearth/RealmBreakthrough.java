package com.xxsx.xuanhuanearth;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;

/**
 * Realm breakthrough: the step from one cultivation stage to the next.
 *
 * <p>The ten route levels cap out, which leaves a player with nothing to aim for. A realm is
 * a stage above the routes: reaching the cap on all learned routes and consuming a
 * breakthrough pill raises the realm, which multiplies mana and unlocks a higher ceiling on
 * every route. It is gated on a rare pill so it stays an event rather than a grind.</p>
 */
public final class RealmBreakthrough {
    public static final int MAX_REALM = 5;
    /** Route levels required before each realm can be entered. */
    private static final int[] REQUIRED_LEVEL = {0, 4, 6, 8, 10};

    private RealmBreakthrough() {
    }

    public static int requiredLevel(int nextRealm) {
        if (nextRealm < 1 || nextRealm > MAX_REALM) {
            return Integer.MAX_VALUE;
        }
        return REQUIRED_LEVEL[nextRealm - 1];
    }

    public static String realmNameKey(int realm) {
        return "realm.xuanhuan_earth." + Math.max(0, Math.min(MAX_REALM, realm));
    }

    /** Attempts to advance the realm. Returns the new realm, or -1 when it failed. */
    public static int attempt(ServerLevel level, ServerPlayer player) {
        int realm = ArcanaPower.getRealm(player);
        if (realm >= MAX_REALM) {
            player.sendSystemMessage(Component.translatable("message.xuanhuan_earth.realm.max")
                    .withStyle(ChatFormatting.YELLOW));
            return -1;
        }
        int next = realm + 1;
        if (!XuanhuanLoreIntegration.knowsRealm(player, next)) {
            player.sendSystemMessage(Component.translatable("lore.earth_lore.fail.lore_locked")
                    .withStyle(ChatFormatting.RED));
            return -1;
        }
        int required = requiredLevel(next);

        // Every learned route must be at the required level.
        for (CultivationFocus focus : CultivationFocus.values()) {
            if (ArcanaPower.getFocusLevel(player, focus) <= 0) {
                continue;
            }
            if (ArcanaPower.getFocusLevel(player, focus) < required) {
                player.sendSystemMessage(Component.translatable(
                        "message.xuanhuan_earth.realm.route_low",
                        Component.translatable(focus.titleKey()), required,
                        ArcanaPower.getFocusLevel(player, focus))
                        .withStyle(ChatFormatting.YELLOW));
                return -1;
            }
        }

        ItemStack pill = findPill(player);
        if (pill.isEmpty()) {
            player.sendSystemMessage(Component.translatable(
                    "message.xuanhuan_earth.realm.needs_pill",
                    Component.translatable("item.xuanhuan_earth.breakthrough_pill"))
                    .withStyle(ChatFormatting.YELLOW));
            return -1;
        }
        if (!player.getAbilities().instabuild) {
            pill.shrink(1);
        }

        ArcanaPower.setRealm(player, next);
        // A breakthrough refills mana and grants a permanent contribution bump.
        ArcanaPower.setCurrentMana(player, ArcanaPower.getMaxMana(player));
        ArcanaPower.addXuanhuanManaBonus(player, 12.0D);

        player.swing(InteractionHand.MAIN_HAND, true);
        CultivationNetwork.broadcastVisual(player, CultivationVisualAction.MEDITATION);
        XuanhuanEffects.realmBreakthrough(level, player.position());
        level.playSound(null, player.blockPosition(), SoundEvents.END_PORTAL_SPAWN,
                SoundSource.PLAYERS, 0.7F, 1.1F);
        player.sendSystemMessage(Component.translatable("message.xuanhuan_earth.realm.advanced",
                Component.translatable(realmNameKey(next)), next,
                ArcanaPower.format(ArcanaPower.getMaxMana(player)))
                .withStyle(ChatFormatting.LIGHT_PURPLE));
        return next;
    }

    private static ItemStack findPill(ServerPlayer player) {
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            ItemStack stack = player.getInventory().getItem(slot);
            if (stack.is(XuanhuanEarth.BREAKTHROUGH_PILL.get())) {
                return stack;
            }
        }
        return ItemStack.EMPTY;
    }

    /** Multiplier applied to route-based bonuses at the current realm. */
    public static double realmMultiplier(int realm) {
        return 1.0D + Math.max(0, Math.min(MAX_REALM, realm)) * 0.12D;
    }
}
