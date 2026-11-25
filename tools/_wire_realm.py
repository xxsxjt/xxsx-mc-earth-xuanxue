import io

# 1. Add the breakthrough action id.
p = (r'D:\Codex\WORKSPACES\Projects\mc-agent\earth-series\earth_xuanxue\neoforge-26.1.2\src\main\java'
     r'\com\xxsx\earth\xuanxue\CultivationActionPayload.java')
s = io.open(p, encoding='utf-8').read()
old = '    public static final int ACTIVATE_SKILL = -5;'
new = old + '\n    public static final int BREAKTHROUGH = -6;'
if old in s:
    s = s.replace(old, new, 1)
    io.open(p, 'w', encoding='utf-8', newline='\n').write(s)
    print('action id added')
else:
    print('MISS action id')

# 2. Handle it on the server.
p2 = (r'D:\Codex\WORKSPACES\Projects\mc-agent\earth-series\earth_xuanxue\neoforge-26.1.2\src\main\java'
      r'\com\xxsx\earth\xuanxue\CultivationNetwork.java')
s2 = io.open(p2, encoding='utf-8').read()
old2 = '''            if (action == CultivationActionPayload.ACTIVATE_SKILL) {
                CultivationSkill.activate(level, player);
                sync(player, pos, false);
                return;
            }'''
new2 = old2 + '''
            if (action == CultivationActionPayload.BREAKTHROUGH) {
                RealmBreakthrough.attempt(level, player);
                sync(player, pos, false);
                return;
            }'''
if old2 in s2:
    s2 = s2.replace(old2, new2, 1)
    io.open(p2, 'w', encoding='utf-8', newline='\n').write(s2)
    print('handler added')
else:
    print('MISS handler')

# 3. Client request helper.
p3 = (r'D:\Codex\WORKSPACES\Projects\mc-agent\earth-series\earth_xuanxue\neoforge-26.1.2\src\main\java'
      r'\com\xxsx\earth\xuanxue\client\XuanhuanEarthClient.java')
s3 = io.open(p3, encoding='utf-8').read()
old3 = '''    public static void requestActivateTechnique() {'''
new3 = '''    public static void requestBreakthrough() {
        ClientPacketDistributor.sendToServer(
                new CultivationActionPayload(CultivationActionPayload.BREAKTHROUGH));
    }

    public static void requestActivateTechnique() {'''
if old3 in s3:
    s3 = s3.replace(old3, new3, 1)
    io.open(p3, 'w', encoding='utf-8', newline='\n').write(s3)
    print('client helper added')
else:
    print('MISS client helper')
