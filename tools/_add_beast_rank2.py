import io

p = (r'D:\Codex\WORKSPACES\Projects\mc-agent\earth-series\earth_xuanxue\neoforge-26.1.2\src\main\java'
     r'\com\xxsx\earth\xuanxue\entity\ContractableSpiritBeastEntity.java')
s = io.open(p, encoding='utf-8').read()

# Fields holding the unmodified attribute values, captured once the entity is built.
old_ctor = '''    protected ContractableSpiritBeastEntity(EntityType<? extends ContractableSpiritBeastEntity> type,
                                             Level level, Kind kind) {
        super(type, level);
        this.kind = kind;
        this.setTame(false, false);
    }'''

new_ctor = '''    /**
     * Unmodified attribute values, captured after construction. Rank bonuses are applied as
     * base + rank * step, so they must start from the template rather than accumulate.
     */
    private double baseHealth;
    private double baseAttack;
    private double baseArmor;

    protected ContractableSpiritBeastEntity(EntityType<? extends ContractableSpiritBeastEntity> type,
                                             Level level, Kind kind) {
        super(type, level);
        this.kind = kind;
        this.setTame(false, false);
        this.captureBaseAttributes();
    }

    private void captureBaseAttributes() {
        AttributeInstance health = getAttribute(Attributes.MAX_HEALTH);
        AttributeInstance attack = getAttribute(Attributes.ATTACK_DAMAGE);
        AttributeInstance armor = getAttribute(Attributes.ARMOR);
        this.baseHealth = health == null ? 20.0D : health.getBaseValue();
        this.baseAttack = attack == null ? 2.0D : attack.getBaseValue();
        this.baseArmor = armor == null ? 0.0D : armor.getBaseValue();
    }'''

if old_ctor in s:
    s = s.replace(old_ctor, new_ctor, 1)
    print('base fields added')
else:
    print('MISS ctor')

# Guard against zero base values on load (older saves, or capture before attributes exist).
old_apply = '''        double step = getRank() - 1;
        if (health != null) {'''
new_apply = '''        if (baseHealth <= 0.0D) {
            captureBaseAttributes();
        }
        double step = getRank() - 1;
        if (health != null) {'''
if old_apply in s:
    s = s.replace(old_apply, new_apply, 1)
    print('apply guard added')
else:
    print('MISS apply')

# Add the missing AttributeInstance import.
if 'import net.minecraft.world.entity.ai.attributes.AttributeInstance;' not in s:
    s = s.replace('import net.minecraft.world.entity.ai.attributes.AttributeSupplier;',
                  'import net.minecraft.world.entity.ai.attributes.AttributeInstance;\n'
                  'import net.minecraft.world.entity.ai.attributes.AttributeSupplier;', 1)
    print('import added')

io.open(p, 'w', encoding='utf-8', newline='\n').write(s)
