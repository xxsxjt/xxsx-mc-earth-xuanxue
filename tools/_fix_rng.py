import io

p = (r'D:\Codex\WORKSPACES\Projects\mc-agent\earth-series\earth_xuanxue\neoforge-26.1.2\src\main\java'
     r'\com\xxsx\earth\xuanxue\settlement\XuanhuanSettlementFeature.java')
s = io.open(p, encoding='utf-8').read()

old = '''        RandomSource random = context.random();
        SettlementStyle.Theme theme = type.themeFor(level, center);'''

new = '''        // The feature RNG is seeded per placement, so two sites generated in the same tick
        // can receive the same roll. Mix the world seed with the exact origin instead so
        // every settlement gets its own palette and layout.
        long mixed = level.getSeed()
                ^ ((long) center.getX() * 0x9E3779B97F4A7C15L)
                ^ ((long) center.getY() * 0xC2B2AE3D27D4EB4FL)
                ^ ((long) center.getZ() * 0x165667B19E3779F9L)
                ^ type.ordinal();
        RandomSource random = RandomSource.create(mixed);
        SettlementStyle.Theme theme = type.themeFor(level, center);'''

if old in s:
    s = s.replace(old, new, 1)
    io.open(p, 'w', encoding='utf-8', newline='\n').write(s)
    print('seeded rng applied')
else:
    print('MISS')
