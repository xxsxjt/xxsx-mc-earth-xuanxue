import io

p = (r'D:\Codex\WORKSPACES\Projects\mc-agent\earth-series\earth_xuanxue\neoforge-26.1.2\src\main\java'
     r'\com\xxsx\earth\xuanxue\CultivationStatusPayload.java')
s = io.open(p, encoding='utf-8').read()

pairs = [
    # Record field.
    ('        boolean seated,\n        boolean openScreen) implements CustomPacketPayload {',
     '        boolean seated,\n        boolean openScreen,\n        int realm) implements CustomPacketPayload {'),
    # Decode.
    ('buf.readUtf(), buf.readBoolean(), buf.readBoolean());',
     'buf.readUtf(), buf.readBoolean(), buf.readBoolean(), buf.readVarInt());'),
    # Encode.
    ('                    buf.writeBoolean(payload.openScreen());',
     '                    buf.writeBoolean(payload.openScreen());\n'
     '                    buf.writeVarInt(payload.realm());'),
    # Empty.
    ('"spirituality.earth_xuanxue.source.natural", false, false);',
     '"spirituality.earth_xuanxue.source.natural", false, false, 0);'),
]

for old, new in pairs:
    if old in s:
        s = s.replace(old, new, 1)
        print('ok', old[:44].replace('\n', ' '))
    else:
        print('MISS', old[:44].replace('\n', ' '))

io.open(p, 'w', encoding='utf-8', newline='\n').write(s)

# Send the realm from the server.
p2 = (r'D:\Codex\WORKSPACES\Projects\mc-agent\earth-series\earth_xuanxue\neoforge-26.1.2\src\main\java'
      r'\com\xxsx\earth\xuanxue\CultivationNetwork.java')
s2 = io.open(p2, encoding='utf-8').read()
old2 = '''                player.getVehicle() instanceof MeditationSeatEntity,
                openScreen));'''
new2 = '''                player.getVehicle() instanceof MeditationSeatEntity,
                openScreen,
                ArcanaPower.getRealm(player)));'''
if old2 in s2:
    s2 = s2.replace(old2, new2, 1)
    io.open(p2, 'w', encoding='utf-8', newline='\n').write(s2)
    print('sync sends realm')
else:
    print('MISS sync')
