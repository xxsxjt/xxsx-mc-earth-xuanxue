import io

p = (r'D:\Codex\WORKSPACES\Projects\mc-agent\earth-series\earth_xuanxue\neoforge-26.1.2\src\main\java'
     r'\com\xxsx\earth\xuanxue\settlement\XuanhuanSettlementAnchorBlockEntity.java')
s = io.open(p, encoding='utf-8').read()

old_save = '''        output.putBoolean("Initialized", initialized);
        output.putInt("WarmupTicks", warmupTicks);
    }'''

new_save = '''        output.putBoolean("Initialized", initialized);
        output.putInt("WarmupTicks", warmupTicks);
        // Flattened as x,y,z triples so the roster survives a reload.
        int[] flat = new int[spawnOffsets.length * 3];
        for (int i = 0; i < spawnOffsets.length; i++) {
            flat[i * 3] = spawnOffsets[i][0];
            flat[i * 3 + 1] = spawnOffsets[i][1];
            flat[i * 3 + 2] = spawnOffsets[i][2];
        }
        output.putIntArray("SpawnOffsets", flat);
    }'''

old_load = '''        initialized = input.getBooleanOr("Initialized", false);
        warmupTicks = input.getIntOr("WarmupTicks", 0);
    }'''

new_load = '''        initialized = input.getBooleanOr("Initialized", false);
        warmupTicks = input.getIntOr("WarmupTicks", 0);
        int[] flat = input.getIntArray("SpawnOffsets").orElse(new int[0]);
        spawnOffsets = new int[flat.length / 3][3];
        for (int i = 0; i + 2 < flat.length; i += 3) {
            spawnOffsets[i / 3] = new int[]{flat[i], flat[i + 1], flat[i + 2]};
        }
    }'''

for old, new, label in ((old_save, new_save, 'save'), (old_load, new_load, 'load')):
    if old in s:
        s = s.replace(old, new, 1)
        print(label, 'ok')
    else:
        print(label, 'MISS')

io.open(p, 'w', encoding='utf-8', newline='\n').write(s)
