package com.xxsx.earth.xuanxue;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record CultivationBookState(int chaptersRead, long nextStudyTick) {
    public static final int TOTAL_CHAPTERS = 4;
    public static final CultivationBookState EMPTY = new CultivationBookState(0, 0L);

    public static final Codec<CultivationBookState> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.optionalFieldOf("chapters_read", 0).forGetter(CultivationBookState::chaptersRead),
            Codec.LONG.optionalFieldOf("next_study_tick", 0L).forGetter(CultivationBookState::nextStudyTick)
    ).apply(instance, CultivationBookState::new));

    public CultivationBookState advance(long nextAllowedTick) {
        return new CultivationBookState(Math.min(TOTAL_CHAPTERS, chaptersRead + 1), Math.max(0L, nextAllowedTick));
    }

    public boolean completed() {
        return chaptersRead >= TOTAL_CHAPTERS;
    }
}
