import io

p = (r'D:\Codex\WORKSPACES\Projects\mc-agent\earth-series\earth_xuanxue\neoforge-26.1.2\src\main\java'
     r'\com\xxsx\earth\xuanxue\XuanhuanMachineBlock.java')
s = io.open(p, encoding='utf-8').read()

anchor = '''        recipes.add(r("crystal_qi_pill", Kind.ALCHEMY_FURNACE,
                item(XuanhuanEarth.SPIRIT_CRYSTAL_SHARD::get, 1), item(XuanhuanEarth.SPIRIT_SPRING_BOTTLE::get, 1),
                55, 18.0D, 160, out(XuanhuanEarth.QI_RECOVERY_PILL::get, 3)));'''

new = anchor + '''

        // The pill ladder: each tier needs the previous tier as a catalyst, so alchemy is a
        // progression rather than six unrelated recipes.
        recipes.add(r("foundation_pill", Kind.ALCHEMY_FURNACE,
                item(XuanhuanEarth.QI_RECOVERY_PILL::get, 2),
                item(XuanhuanEarth.SPIRIT_CRYSTAL_SHARD::get, 2),
                62, 22.0D, 200, out(XuanhuanEarth.FOUNDATION_PILL::get, 1)));
        recipes.add(r("ironbody_pill", Kind.ALCHEMY_FURNACE,
                item(XuanhuanEarth.QI_RECOVERY_PILL::get, 1),
                tagged(EarthMaterialTags.ARCANA_GEOLOGY_CATALYSTS, () -> Items.IRON_INGOT, 2),
                66, 24.0D, 220, out(XuanhuanEarth.IRONBODY_PILL::get, 1)));
        recipes.add(r("cloudbreath_pill", Kind.ALCHEMY_FURNACE,
                item(XuanhuanEarth.SPIRIT_SPRING_BOTTLE::get, 2),
                item(XuanhuanEarth.SPIRIT_GRASS::get, 3),
                58, 20.0D, 200, out(XuanhuanEarth.CLOUDBREATH_PILL::get, 1)));
        recipes.add(r("grainfast_pill", Kind.ALCHEMY_FURNACE,
                item(XuanhuanEarth.SPIRIT_GRASS::get, 4),
                item(() -> Items.WHEAT, 2),
                52, 16.0D, 180, out(XuanhuanEarth.GRAINFAST_PILL::get, 1)));
        recipes.add(r("nightsight_pill", Kind.ALCHEMY_FURNACE,
                item(XuanhuanEarth.SPIRIT_GRASS::get, 2),
                item(() -> Items.GOLDEN_CARROT, 1),
                56, 18.0D, 180, out(XuanhuanEarth.NIGHTSIGHT_PILL::get, 1)));
        recipes.add(r("breakthrough_pill", Kind.ALCHEMY_FURNACE,
                item(XuanhuanEarth.FOUNDATION_PILL::get, 2),
                item(XuanhuanEarth.SPIRIT_CRYSTAL_SHARD::get, 4),
                80, 34.0D, 320, out(XuanhuanEarth.BREAKTHROUGH_PILL::get, 1)));'''

if anchor in s:
    s = s.replace(anchor, new, 1)
    print('recipes added')
else:
    print('MISS anchor')

io.open(p, 'w', encoding='utf-8', newline='\n').write(s)
