package com.xxsx.xuanhuanearth;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.WeatherData;

import java.util.UUID;

/**
 * X-072 Divination Coins: once per game day (earth_arcana.divination_day), casts one of the 64 King Wen
 * hexagrams deterministically from worldSeed ^ uuidHash ^ day, forecasts weather in the next 24000 ticks,
 * and grants 2400 ticks of xuanhuan_earth:auspicious on even hexagrams.
 */
public class DivinationCoinsItem extends GuidedXuanhuanItem {
    public static final int AUSPICIOUS_TICKS = 2400;

    public DivinationCoinsItem(Properties properties, String hintKey) {
        super(properties, hintKey);
    }

    public static int computeHexagram(long worldSeed, UUID playerUuid, long day) {
        long uuidHash = playerUuid.getMostSignificantBits() ^ playerUuid.getLeastSignificantBits();
        long mixed = mix(worldSeed ^ uuidHash ^ day);
        return Math.floorMod(mixed, 64);
    }

    private static long mix(long value) {
        value ^= value >>> 33;
        value *= 0xff51afd7ed558ccdL;
        value ^= value >>> 33;
        value *= 0xc4ceb9fe1a85ec53L;
        value ^= value >>> 33;
        return value;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        if (!(level instanceof ServerLevel serverLevel)) {
            return InteractionResult.SUCCESS_SERVER;
        }
        long day = Math.floorDiv(serverLevel.getGameTime(), 24000L);
        long lastDay = ArcanaPower.getDivinationDay(player);
        if (lastDay == day) {
            player.sendSystemMessage(Component.translatable("message.xuanhuan_earth.divination.already_cast_today")
                    .withStyle(ChatFormatting.YELLOW));
            return InteractionResult.FAIL;
        }
        ArcanaPower.setDivinationDay(player, day);

        int hexagram = computeHexagram(serverLevel.getSeed(), player.getUUID(), day);
        boolean auspicious = (hexagram % 2) == 0;

        boolean willRain = serverLevel.isRaining();
        boolean willThunder = serverLevel.isThundering();
        WeatherData weatherData = serverLevel.getWeatherData();
        if (weatherData != null) {
            int clearTime = weatherData.getClearWeatherTime();
            int rainTime = weatherData.getRainTime();
            int thunderTime = weatherData.getThunderTime();
            if (!willRain) {
                if (clearTime > 0) {
                    willRain = clearTime <= 24000;
                } else {
                    willRain = rainTime > 0 && rainTime <= 24000;
                }
            }
            if (!willThunder) {
                if (clearTime > 0) {
                    willThunder = clearTime <= 24000 && thunderTime <= 24000;
                } else {
                    willThunder = thunderTime > 0 && thunderTime <= 24000;
                }
            }
        }

        String omenKey = auspicious
                ? "message.xuanhuan_earth.divination.omen.auspicious"
                : "message.xuanhuan_earth.divination.omen.ominous";
        String weatherKey = willThunder
                ? "message.xuanhuan_earth.divination.weather.thunder"
                : (willRain ? "message.xuanhuan_earth.divination.weather.rain"
                        : "message.xuanhuan_earth.divination.weather.clear");

        player.sendSystemMessage(Component.translatable(
                "message.xuanhuan_earth.divination.result",
                Component.translatable("hexagram.xuanhuan_earth." + hexagram),
                Component.translatable(omenKey),
                Component.translatable(weatherKey))
                .withStyle(auspicious ? ChatFormatting.GOLD : ChatFormatting.GRAY));

        if (auspicious) {
            player.addEffect(new MobEffectInstance(XuanhuanEarth.AUSPICIOUS, AUSPICIOUS_TICKS, 0, true, true, true));
        }
        serverLevel.playSound(null, player.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME,
                SoundSource.PLAYERS, 0.7F, auspicious ? 1.2F : 0.85F);
        return InteractionResult.SUCCESS_SERVER;
    }
}
