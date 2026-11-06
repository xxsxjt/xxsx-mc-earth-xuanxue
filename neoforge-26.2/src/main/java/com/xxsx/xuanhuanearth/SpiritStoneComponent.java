package com.xxsx.xuanhuanearth;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;

/** Persistent per-stack state of a spirit stone. */
public record SpiritStoneComponent(double chargeAeu, double capacityAeu, int matrixGrade,
                                   double stability, String fieldSignature, BlockPos linkedArray,
                                   int schemaVersion) {
    public static final int CURRENT_SCHEMA = SpiritStoneCharge.SCHEMA_VERSION;
    public static final double CRACKED_CAPACITY_AEU = 60.0D;
    public static final double MAX_CORE_CAPACITY_AEU = 480.0D;

    public static final Codec<SpiritStoneComponent> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.DOUBLE.fieldOf("charge_aeu").forGetter(SpiritStoneComponent::chargeAeu),
            Codec.DOUBLE.optionalFieldOf("capacity_aeu", SpiritStoneCharge.CAPACITY_AEU).forGetter(SpiritStoneComponent::capacityAeu),
            Codec.INT.optionalFieldOf("matrix_grade", 1).forGetter(SpiritStoneComponent::matrixGrade),
            Codec.DOUBLE.optionalFieldOf("stability", 1.0D).forGetter(SpiritStoneComponent::stability),
            Codec.string(0, 32).optionalFieldOf("field_signature", "").forGetter(SpiritStoneComponent::fieldSignature),
            BlockPos.CODEC.optionalFieldOf("linked_array", BlockPos.ZERO).forGetter(SpiritStoneComponent::linkedArray),
            Codec.INT.optionalFieldOf("schema_version", CURRENT_SCHEMA).forGetter(SpiritStoneComponent::schemaVersion)
    ).apply(instance, SpiritStoneComponent::new));
    public static final SpiritStoneComponent EMPTY = new SpiritStoneComponent(
            0.0D, SpiritStoneCharge.CAPACITY_AEU, 1, 1.0D, "", BlockPos.ZERO, CURRENT_SCHEMA);

    public SpiritStoneComponent {
        capacityAeu = Double.isFinite(capacityAeu)
                ? Math.max(CRACKED_CAPACITY_AEU, Math.min(MAX_CORE_CAPACITY_AEU, capacityAeu))
                : SpiritStoneCharge.CAPACITY_AEU;
        chargeAeu = Double.isFinite(chargeAeu) ? Math.max(0.0D, Math.min(capacityAeu, chargeAeu)) : 0.0D;
        matrixGrade = Math.max(1, Math.min(3, matrixGrade));
        stability = Double.isFinite(stability) ? Math.max(0.0D, Math.min(1.0D, stability)) : 1.0D;
        fieldSignature = fieldSignature == null ? ""
                : (fieldSignature.length() > 32 ? fieldSignature.substring(0, 32) : fieldSignature);
        linkedArray = linkedArray == null ? BlockPos.ZERO : linkedArray;
        schemaVersion = Math.max(1, schemaVersion);
    }

    public SpiritStoneComponent(double chargeAeu, int matrixGrade, int schemaVersion) {
        this(chargeAeu, SpiritStoneCharge.CAPACITY_AEU, matrixGrade, 1.0D, "", BlockPos.ZERO, schemaVersion);
    }

    public static SpiritStoneComponent read(ItemStack stack) {
        SpiritStoneComponent value = stack.get(XuanhuanEarth.SPIRIT_STONE_CHARGE);
        return value == null ? EMPTY : value;
    }

    public SpiritStoneComponent withCharge(double charge) {
        return new SpiritStoneComponent(charge, capacityAeu, matrixGrade, stability, fieldSignature, linkedArray, CURRENT_SCHEMA);
    }

    public SpiritStoneComponent withStoneState(double nextCharge, double nextCapacity, double nextStability, String signature) {
        return new SpiritStoneComponent(nextCharge, nextCapacity, matrixGrade, nextStability,
                signature == null || signature.isEmpty() ? fieldSignature : signature, linkedArray, CURRENT_SCHEMA);
    }

    public SpiritStoneComponent withCoreState(double nextCharge, double coreCapacity, BlockPos arrayPos) {
        return new SpiritStoneComponent(nextCharge, coreCapacity, matrixGrade, stability, fieldSignature, arrayPos, CURRENT_SCHEMA);
    }

    public static String computeSignature(Level level, BlockPos pos, String sourceKey) {
        ChunkPos cp = ChunkPos.containing(pos);
        String raw = level.dimension().identifier() + ":" + cp.x() + ":" + cp.z() + ":" + (sourceKey == null ? "" : sourceKey);
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256").digest(raw.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash).substring(0, 16);
        } catch (Exception ignored) {
            return Integer.toHexString(raw.hashCode());
        }
    }
}
