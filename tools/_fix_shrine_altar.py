import io

p = (r'D:\Codex\WORKSPACES\Projects\mc-agent\earth-series\earth_xuanxue\neoforge-26.1.2\src\main\java'
     r'\com\xxsx\earth\xuanxue\settlement\XuanhuanSettlementFeature.java')
s = io.open(p, encoding='utf-8').read()

old = '''        level.setBlock(center.offset(0, 3, 0), XuanhuanEarth.SPIRIT_ARRAY_CORE.get().defaultBlockState(), FLAGS);
        level.setBlock(center.offset(0, 2, 0), XuanhuanEarth.SPIRIT_VEIN_NODE.get().defaultBlockState(), FLAGS);'''

new = '''        // The altar is a complete formal array on the top step, not a lone core: a core with
        // no soil ring and no corner focus can never form, so it would be a dead block.
        formalArray(level, center.offset(0, 2, 0));'''

if old in s:
    s = s.replace(old, new, 1)
    io.open(p, 'w', encoding='utf-8', newline='\n').write(s)
    print('shrine altar fixed')
else:
    print('MISS')
