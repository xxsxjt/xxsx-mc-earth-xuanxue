import io

p = (r'D:\Codex\WORKSPACES\Projects\mc-agent\earth-series\earth_xuanxue\neoforge-26.1.2\src\main\java'
     r'\com\xxsx\earth\xuanxue\ArcanaPower.java')
s = io.open(p, encoding='utf-8').read()

# 1. Realm key constant.
old = '    public static final String CULTIVATION_LEVEL = "earth_arcana.cultivation_level";'
new = (old + '\n'
       '    public static final String REALM = "earth_arcana.realm";\n'
       '    public static final String REALM_MANA_BONUS = "earth_arcana.realm_mana_bonus";')
if old in s:
    s = s.replace(old, new, 1)
    print('keys added')
else:
    print('MISS keys')

# 2. Accessors next to the cultivation level getter.
anchor = '''    public static double getQiAbsorptionRate(Player player) {'''
addition = '''    public static int getRealm(Player player) {
        return Math.max(0, Math.min(RealmBreakthrough.MAX_REALM,
                data(player).getIntOr(REALM, 0)));
    }

    public static void setRealm(Player player, int realm) {
        data(player).putInt(REALM, Math.max(0, Math.min(RealmBreakthrough.MAX_REALM, realm)));
    }

    /** Adds to the permanent realm contribution without touching the magic or equipment pools. */
    public static void addXuanhuanManaBonus(Player player, double amount) {
        if (!Double.isFinite(amount) || amount <= 0.0D) {
            return;
        }
        CompoundTag tag = data(player);
        tag.putDouble(XUANHUAN_MANA_BONUS,
                Math.max(0.0D, tag.getDoubleOr(XUANHUAN_MANA_BONUS, 0.0D)) + amount);
    }

    public static double getQiAbsorptionRate(Player player) {'''
if anchor in s:
    s = s.replace(anchor, addition, 1)
    print('accessors added')
else:
    print('MISS accessor anchor')

io.open(p, 'w', encoding='utf-8', newline='\n').write(s)
