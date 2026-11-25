import io

p = (r'D:\Codex\WORKSPACES\Projects\mc-agent\earth-series\earth_xuanxue\neoforge-26.1.2\src\main\java'
     r'\com\xxsx\earth\xuanxue\entity\ContractableSpiritBeastEntity.java')
s = io.open(p, encoding='utf-8').read()

# 1. Feeding a spirit crystal to an owned beast advances its rank.
old = '''        } else if (this.isOwnedBy(player)) {
            if (stack.is(XuanhuanEarth.SPIRIT_GRASS.get()) && getHealth() < getMaxHealth()) {'''
new = '''        } else if (this.isOwnedBy(player)) {
            // Feeding a crystal trains the beast. Grass only heals; crystals raise its rank,
            // which is what actually makes it stronger.
            if (stack.is(XuanhuanEarth.SPIRIT_CRYSTAL_SHARD.get()) && getRank() < MAX_RANK) {
                if (!this.level().isClientSide()) {
                    stack.consume(1, player);
                    boolean promoted = addRankExperience(12);
                    if (this.level() instanceof ServerLevel serverLevel) {
                        XuanhuanEffects.qiRise(serverLevel,
                                this.position().add(0.0D, this.getBbHeight() * 0.5D, 0.0D),
                                0.7D, 12, 0.03D);
                    }
                    if (promoted) {
                        player.sendSystemMessage(Component.translatable(
                                "message.earth_xuanxue.beast.rank_up",
                                getDisplayName(), rankName(), getRank()));
                    } else {
                        player.sendSystemMessage(Component.translatable(
                                "message.earth_xuanxue.beast.rank_progress",
                                getDisplayName(), rankName(),
                                getRankXp(), getRankXpNeeded()));
                    }
                }
                return this.level().isClientSide() ? InteractionResult.SUCCESS : InteractionResult.SUCCESS_SERVER;
            }
            if (stack.is(XuanhuanEarth.SPIRIT_GRASS.get()) && getHealth() < getMaxHealth()) {'''
if old in s:
    s = s.replace(old, new, 1)
    print('feeding added')
else:
    print('MISS feeding')

# 2. Combat also trains the beast: award growth xp when it kills something.
old_tick = '''    private void applyOwnerSupport(ServerPlayer owner) {'''
new_tick = '''    @Override
    public boolean doHurtTarget(net.minecraft.server.level.ServerLevel level,
                                net.minecraft.world.entity.Entity target) {
        boolean hit = super.doHurtTarget(level, target);
        // Fighting is the other half of training, so a beast that hunts with its owner
        // progresses without needing to be hand-fed everything.
        if (hit && this.isTame() && getRank() < MAX_RANK && this.random.nextFloat() < 0.35F) {
            boolean promoted = addRankExperience(3);
            if (promoted && this.getOwner() instanceof ServerPlayer owner) {
                owner.sendSystemMessage(Component.translatable(
                        "message.earth_xuanxue.beast.rank_up",
                        getDisplayName(), rankName(), getRank()));
            }
        }
        return hit;
    }

    private void applyOwnerSupport(ServerPlayer owner) {'''
if old_tick in s:
    s = s.replace(old_tick, new_tick, 1)
    print('combat xp added')
else:
    print('MISS combat')

io.open(p, 'w', encoding='utf-8', newline='\n').write(s)
