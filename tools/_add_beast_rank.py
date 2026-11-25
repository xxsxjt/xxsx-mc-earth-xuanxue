import io

p = (r'D:\Codex\WORKSPACES\Projects\mc-agent\earth-series\earth_xuanxue\neoforge-26.1.2\src\main\java'
     r'\com\xxsx\earth\xuanxue\entity\ContractableSpiritBeastEntity.java')
s = io.open(p, encoding='utf-8').read()

# 1. Rank data accessors alongside affinity.
old_data = '''    private static final EntityDataAccessor<Byte> DATA_MODE = SynchedEntityData.defineId(
            ContractableSpiritBeastEntity.class, EntityDataSerializers.BYTE);'''

new_data = old_data + '''
    /** Beast cultivation rank, 1-5. Separate from affinity: trust versus training. */
    public static final int MAX_RANK = 5;
    private static final int[] RANK_XP = {0, 20, 55, 110, 190, 300};
    private static final EntityDataAccessor<Integer> DATA_RANK = SynchedEntityData.defineId(
            ContractableSpiritBeastEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_RANK_XP = SynchedEntityData.defineId(
            ContractableSpiritBeastEntity.class, EntityDataSerializers.INT);'''

if old_data in s:
    s = s.replace(old_data, new_data, 1)
    print('data accessors added')
else:
    print('MISS data')

# 2. Initialise them.
old_def = '''        builder.define(DATA_AFFINITY, 0);
        builder.define(DATA_MODE, (byte) CompanionMode.FOLLOW.id());'''
new_def = '''        builder.define(DATA_AFFINITY, 0);
        builder.define(DATA_MODE, (byte) CompanionMode.FOLLOW.id());
        builder.define(DATA_RANK, 1);
        builder.define(DATA_RANK_XP, 0);'''
if old_def in s:
    s = s.replace(old_def, new_def, 1)
    print('defaults added')
else:
    print('MISS defaults')

# 3. Persist.
old_save = '''        output.putInt("SpiritAffinity", getAffinity());
        output.putInt("CompanionMode", getCompanionMode().id());'''
new_save = '''        output.putInt("SpiritAffinity", getAffinity());
        output.putInt("CompanionMode", getCompanionMode().id());
        output.putInt("BeastRank", getRank());
        output.putInt("BeastRankXp", getRankXp());'''
if old_save in s:
    s = s.replace(old_save, new_save, 1)
    print('save added')
else:
    print('MISS save')

old_load = '''        setAffinity(input.getIntOr("SpiritAffinity", 0));
        setCompanionMode(CompanionMode.byId(input.getIntOr("CompanionMode", 0)));'''
new_load = '''        setAffinity(input.getIntOr("SpiritAffinity", 0));
        setCompanionMode(CompanionMode.byId(input.getIntOr("CompanionMode", 0)));
        setRank(input.getIntOr("BeastRank", 1));
        setRankXp(input.getIntOr("BeastRankXp", 0));
        applyRankAttributes();'''
if old_load in s:
    s = s.replace(old_load, new_load, 1)
    print('load added')
else:
    print('MISS load')

# 4. Rank accessors + growth logic, inserted before getAffinity.
old_affinity = '''    public int getAffinity() {'''
growth = '''    public int getRank() {
        return this.entityData.get(DATA_RANK);
    }

    public void setRank(int rank) {
        this.entityData.set(DATA_RANK, Math.max(1, Math.min(MAX_RANK, rank)));
    }

    public int getRankXp() {
        return this.entityData.get(DATA_RANK_XP);
    }

    public void setRankXp(int xp) {
        this.entityData.set(DATA_RANK_XP, Math.max(0, xp));
    }

    public int getRankXpNeeded() {
        int rank = getRank();
        return rank >= MAX_RANK ? 0 : RANK_XP[rank];
    }

    /**
     * Feeds growth experience to a contracted beast. Rank drives health, attack and armor,
     * so a beast that is kept and used actually becomes stronger.
     */
    public boolean addRankExperience(int amount) {
        if (amount <= 0 || getRank() >= MAX_RANK) {
            return false;
        }
        int previous = getRank();
        int xp = getRankXp() + amount;
        int rank = previous;
        while (rank < MAX_RANK && xp >= RANK_XP[rank]) {
            xp -= RANK_XP[rank];
            rank++;
        }
        setRank(rank);
        setRankXp(rank >= MAX_RANK ? 0 : xp);
        applyRankAttributes();
        if (rank > previous) {
            setHealth(getMaxHealth());
        }
        return rank > previous;
    }

    /** Applies the stat bonus for the current rank. Idempotent: recomputed from rank. */
    public void applyRankAttributes() {
        AttributeInstance health = getAttribute(Attributes.MAX_HEALTH);
        AttributeInstance attack = getAttribute(Attributes.ATTACK_DAMAGE);
        AttributeInstance armor = getAttribute(Attributes.ARMOR);
        double step = getRank() - 1;
        if (health != null) {
            health.setBaseValue(baseHealth + step * 3.0D);
        }
        if (attack != null) {
            attack.setBaseValue(baseAttack + step * 0.8D);
        }
        if (armor != null) {
            armor.setBaseValue(baseArmor + step * 1.2D);
        }
    }

    public Component rankName() {
        return Component.translatable("beast_rank.earth_xuanxue." + getRank());
    }

    public int getAffinity() {'''
if old_affinity in s:
    s = s.replace(old_affinity, growth, 1)
    print('growth methods added')
else:
    print('MISS affinity anchor')

io.open(p, 'w', encoding='utf-8', newline='\n').write(s)
