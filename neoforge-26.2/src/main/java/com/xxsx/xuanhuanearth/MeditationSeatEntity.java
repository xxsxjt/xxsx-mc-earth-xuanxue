package com.xxsx.xuanhuanearth;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import com.xxsx.xuanhuanearth.particle.XuanhuanParticles;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;

public class MeditationSeatEntity extends Entity {
    private BlockPos sourcePos = BlockPos.ZERO;

    public MeditationSeatEntity(EntityType<? extends MeditationSeatEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
        this.blocksBuilding = false;
    }

    public void bindTo(BlockPos pos) {
        this.sourcePos = pos.immutable();
        this.setPos(pos.getX() + 0.5D, pos.getY() + 0.05D, pos.getZ() + 0.5D);
    }

    public boolean isBoundTo(BlockPos pos) {
        return this.sourcePos.equals(pos);
    }

    public BlockPos sourcePos() {
        return this.sourcePos;
    }

    public void emitFocusChange(CultivationFocus focus) {
        if (!(this.level() instanceof ServerLevel level)) {
            return;
        }
        CultivationPractice.emitFocusChange(level, sourcePos, focus, true);
    }

    @Override
    public void tick() {
        super.tick();
        this.noPhysics = true;
        this.setDeltaMovement(Vec3.ZERO);
        if (this.level().isClientSide()) {
            return;
        }

        if (!(this.level() instanceof ServerLevel serverLevel)) {
            this.discard();
            return;
        }

        BlockState cushionState = serverLevel.getBlockState(this.sourcePos);
        if (!(cushionState.getBlock() instanceof MeditationCushionBlock)) {
            this.discard();
            return;
        }

        if (!(this.getFirstPassenger() instanceof ServerPlayer player)) {
            if (cushionState.getValue(MeditationCushionBlock.OCCUPIED)) {
                serverLevel.setBlock(this.sourcePos,
                        cushionState.setValue(MeditationCushionBlock.OCCUPIED, false),
                        Block.UPDATE_CLIENTS);
            }
            this.discard();
            return;
        }

        Direction facing = cushionState.getValue(MeditationCushionBlock.FACING);
        player.setYRot(facing.toYRot());
        player.setYHeadRot(facing.toYRot());
        player.setYBodyRot(facing.toYRot());

        double x = this.sourcePos.getX() + 0.5D;
        double y = this.sourcePos.getY() + 0.28D;
        double z = this.sourcePos.getZ() + 0.5D;
        if (this.tickCount % 10 == 0) {
            serverLevel.sendParticles(XuanhuanParticles.QI_MOTE.get(), x, y, z, 3, 0.34D, 0.10D, 0.34D, 0.01D);
        }
        if (this.tickCount % 40 == 0) {
            serverLevel.sendParticles(XuanhuanParticles.QI_SPARK.get(), x, y + 0.25D, z, 1, 0.16D, 0.20D, 0.16D, 0.005D);
        }
        if (this.tickCount % 20 == 0
                && MeditationCushionBlock.performSeatedMeditation(serverLevel, this.sourcePos, player, true)) {
            XuanhuanEffects.groundRing(serverLevel, new Vec3(x, y, z), 1.0F);
            serverLevel.sendParticles(XuanhuanParticles.QI_MOTE.get(), x, y + 0.35D, z, 8, 0.42D, 0.32D, 0.42D, 0.015D);
            serverLevel.playSound(null, this.sourcePos, SoundEvents.AMETHYST_BLOCK_CHIME,
                    SoundSource.PLAYERS, 0.65F, 1.25F);
        }
        if (this.tickCount % 20 == 0) {
            CultivationNetwork.sync(player, this.sourcePos, false);
        }
    }

    @Override
    public Vec3 getPassengerRidingPosition(Entity passenger) {
        return new Vec3(this.getX(), this.getY() + 0.13D, this.getZ());
    }

    @Override
    public boolean isPickable() {
        return false;
    }

    @Override
    public boolean shouldBeSaved() {
        return false;
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        return false;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        this.sourcePos = new BlockPos(
                input.getIntOr("SourceX", 0),
                input.getIntOr("SourceY", 0),
                input.getIntOr("SourceZ", 0));
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        output.putInt("SourceX", this.sourcePos.getX());
        output.putInt("SourceY", this.sourcePos.getY());
        output.putInt("SourceZ", this.sourcePos.getZ());
    }
}
