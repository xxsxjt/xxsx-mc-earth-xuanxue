import io

p = (r'D:\Codex\WORKSPACES\Projects\mc-agent\earth-series\earth_xuanxue\neoforge-26.1.2\src\main\java'
     r'\com\xxsx\earth\xuanxue\client\CultivationScreen.java')
s = io.open(p, encoding='utf-8').read()

old = '''        if (primaryActionButton != null) {
            boolean seated = status.seated();'''
new = '''        if (breakthroughButton != null) {
            boolean maxed = status.realm() >= RealmBreakthrough.MAX_REALM;
            breakthroughButton.active = !maxed;
            breakthroughButton.setMessage(Component.translatable(
                    "screen.earth_xuanxue.cultivation.breakthrough")
                    .copy()
                    .append(Component.literal("  "))
                    .append(Component.translatable(RealmBreakthrough.realmNameKey(status.realm()))));
        }
        if (primaryActionButton != null) {
            boolean seated = status.seated();'''

if old in s:
    s = s.replace(old, new, 1)
    io.open(p, 'w', encoding='utf-8', newline='\n').write(s)
    print('updateButtons extended')
else:
    print('MISS')
