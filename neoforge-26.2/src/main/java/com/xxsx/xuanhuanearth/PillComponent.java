package com.xxsx.xuanhuanearth;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.item.ItemStack;

import java.util.Locale;

/**
 * X-041b pill quality component ({@code xuanhuan_earth:pill}).
 * Shares the five-grade scale (crude, common, fine, masterwork, heirloom) with Earth quality items.
 */
public record PillComponent(int schemaVersion, String pill, PillGrade grade, double purity, String batchId) {
    public static final int CURRENT_SCHEMA = 1;

    public enum PillGrade {
        CRUDE("crude", 0, -0.10D, 0.60D, 0.10D),
        COMMON("common", 1, 0.00D, 0.85D, 0.03D),
        FINE("fine", 2, 0.05D, 1.00D, 0.00D),
        MASTERWORK("masterwork", 3, 0.10D, 1.15D, 0.00D),
        HEIRLOOM("heirloom", 4, 0.15D, 1.30D, 0.00D);

        private final String serialized;
        private final int tier;
        private final double gradeBonus;
        private final double durationMultiplier;
        private final double nauseaChance;

        PillGrade(String serialized, int tier, double gradeBonus, double durationMultiplier, double nauseaChance) {
            this.serialized = serialized;
            this.tier = tier;
            this.gradeBonus = gradeBonus;
            this.durationMultiplier = durationMultiplier;
            this.nauseaChance = nauseaChance;
        }

        public String serialized() {
            return serialized;
        }

        public int tier() {
            return tier;
        }

        public double gradeBonus() {
            return gradeBonus;
        }

        public double durationMultiplier() {
            return durationMultiplier;
        }

        public double nauseaChance() {
            return nauseaChance;
        }

        public static DataResult<PillGrade> parse(String raw) {
            if (raw == null) {
                return DataResult.error(() -> "invalid_grade: null");
            }
            String norm = raw.trim().toLowerCase(Locale.ROOT);
            for (PillGrade g : values()) {
                if (g.serialized.equals(norm)) {
                    return DataResult.success(g);
                }
            }
            return DataResult.error(() -> "invalid_grade: " + raw);
        }

        public static PillGrade fromQuality(double pillQuality) {
            double q = Double.isFinite(pillQuality) ? Math.max(0.0D, Math.min(1.0D, pillQuality)) : 0.0D;
            if (q < 0.40D) {
                return CRUDE;
            }
            if (q < 0.60D) {
                return COMMON;
            }
            if (q < 0.80D) {
                return FINE;
            }
            if (q < 0.95D) {
                return MASTERWORK;
            }
            return HEIRLOOM;
        }

        public static final Codec<PillGrade> CODEC = Codec.STRING.comapFlatMap(PillGrade::parse, PillGrade::serialized);
    }

    public static final Codec<PillComponent> CODEC = RecordCodecBuilder.<PillComponent>create(instance -> instance.group(
            Codec.intRange(1, 1).optionalFieldOf("schema_version", CURRENT_SCHEMA).forGetter(PillComponent::schemaVersion),
            Codec.string(1, 64).fieldOf("pill").forGetter(PillComponent::pill),
            PillGrade.CODEC.optionalFieldOf("grade", PillGrade.COMMON).forGetter(PillComponent::grade),
            Codec.doubleRange(0.0D, 1.0D).optionalFieldOf("purity", 0.85D).forGetter(PillComponent::purity),
            Codec.string(0, 32).optionalFieldOf("batch_id", "").forGetter(PillComponent::batchId)
    ).apply(instance, PillComponent::new));

    public PillComponent {
        schemaVersion = 1;
        pill = pill == null || pill.isBlank() ? "unknown" : pill;
        grade = grade == null ? PillGrade.COMMON : grade;
        purity = Double.isFinite(purity) ? Math.max(0.0D, Math.min(1.0D, purity)) : 0.85D;
        batchId = batchId == null ? "" : (batchId.length() > 32 ? batchId.substring(0, 32) : batchId);
    }

    public static PillComponent ofDefault(String pillSlug) {
        return new PillComponent(CURRENT_SCHEMA, pillSlug, PillGrade.COMMON, 0.85D, "");
    }

    public static PillComponent fromQuality(String pillSlug, double quality, String batchId) {
        double clamped = Double.isFinite(quality) ? Math.max(0.0D, Math.min(1.0D, quality)) : 0.50D;
        return new PillComponent(CURRENT_SCHEMA, pillSlug, PillGrade.fromQuality(clamped), clamped, batchId);
    }

    public PillComponent withGrade(PillGrade nextGrade) {
        return new PillComponent(schemaVersion, pill, nextGrade, purity, batchId);
    }

    public static PillComponent read(ItemStack stack, String fallbackSlug) {
        PillComponent existing = stack.get(XuanhuanEarth.PILL_COMPONENT.get());
        return existing != null ? existing : ofDefault(fallbackSlug);
    }
}
