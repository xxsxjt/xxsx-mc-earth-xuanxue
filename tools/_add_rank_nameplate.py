import io

p = (r'D:\Codex\WORKSPACES\Projects\mc-agent\earth-series\earth_xuanxue\neoforge-26.1.2\src\main\java'
     r'\com\xxsx\earth\xuanxue\entity\ContractableSpiritBeastEntity.java')
s = io.open(p, encoding='utf-8').read()

# Show rank in the beast's displayed name so progress is visible without opening anything.
anchor = '''    public void setCompanionMode(CompanionMode mode) {'''
addition = '''    @Override
    public Component getName() {
        // Rank is part of the beast's identity: the name plate shows it so the owner can see
        // training progress at a glance.
        Component base = super.getName();
        if (!this.isTame()) {
            return base;
        }
        return Component.empty()
                .append(base)
                .append(Component.literal(" "))
                .append(rankName());
    }

    public void setCompanionMode(CompanionMode mode) {'''

if anchor in s:
    s = s.replace(anchor, addition, 1)
    io.open(p, 'w', encoding='utf-8', newline='\n').write(s)
    print('getName overridden')
else:
    print('MISS anchor')
