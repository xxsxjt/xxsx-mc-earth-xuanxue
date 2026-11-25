import io

# Fix SpiritImplementItem compile errors.
p = (r'D:\Codex\WORKSPACES\Projects\mc-agent\earth-series\earth_xuanxue\neoforge-26.1.2\src\main\java'
     r'\com\xxsx\earth\xuanxue\SpiritImplementItem.java')
s = io.open(p, encoding='utf-8').read()

s = s.replace(
    'import net.minecraft.world.item.ItemStack;\n',
    'import net.minecraft.world.item.Item;\nimport net.minecraft.world.item.ItemStack;\n', 1)

# RecoverCore needs a ServerPlayer.
s = s.replace(
    '''        GATHER_ORB("gather_orb", 240.0D, 60.0D, 100.0D, 10.0D) {
            @Override
            public void apply(ServerLevel level, Player player) {
                double before = ArcanaPower.getCurrentMana(player);
                ArcanaPower.setCurrentMana(player, before + 18.0D);
                EarthHumanCompat.recoverCore(player, 3.0D, 0.10D);
            }
        },''',
    '''        GATHER_ORB("gather_orb", 240.0D, 60.0D, 100.0D, 10.0D) {
            @Override
            public void apply(ServerLevel level, Player player) {
                double before = ArcanaPower.getCurrentMana(player);
                ArcanaPower.setCurrentMana(player, before + 18.0D);
                if (player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
                    EarthHumanCompat.recoverCore(serverPlayer, 3.0D, 0.10D);
                }
            }
        },''', 1)

io.open(p, 'w', encoding='utf-8', newline='\n').write(s)
print('SpiritImplementItem patched')

# Add implementCast to XuanhuanEffects.
p2 = (r'D:\Codex\WORKSPACES\Projects\mc-agent\earth-series\earth_xuanxue\neoforge-26.1.2\src\main\java'
      r'\com\xxsx\earth\xuanxue\XuanhuanEffects.java')
s2 = io.open(p2, encoding='utf-8').read()
anchor = '    /** Pill swallowed: a small upward puff plus a jade ring at the feet. */'
addition = '''    /** Implement cast: a colour-coded burst matching the implement's element. */
    public static void implementCast(ServerLevel level, Vec3 origin,
                                     SpiritImplementItem.Implement kind) {
        groundRing(level, origin, 1.3F);
        switch (kind) {
            case WIND_FAN -> {
                level.sendParticles(XuanhuanParticles.WIND_RING.get(),
                        origin.x, origin.y + 0.4D, origin.z, 1, 0.0D, 0.0D, 0.0D, 0.0D);
                sparkBurst(level, origin.add(0.0D, 0.8D, 0.0D), 16, 0.14D);
            }
            case STONE_SEAL -> {
                runeCluster(level, origin, 4, 1.0D, false);
                level.sendParticles(XuanhuanParticles.EMBER_SPARK.get(),
                        origin.x, origin.y + 0.6D, origin.z, 12, 0.4D, 0.3D, 0.4D, 0.02D);
            }
            case GATHER_ORB -> qiRise(level, origin, 1.1D, 18, 0.03D);
            case LANTERN_ROD -> {
                runeCluster(level, origin, 3, 0.9D, true);
                level.sendParticles(XuanhuanParticles.QI_SPARK.get(),
                        origin.x, origin.y + 1.2D, origin.z, 10, 0.5D, 0.4D, 0.5D, 0.02D);
            }
        }
    }

'''
if anchor in s2:
    s2 = s2.replace(anchor, addition + anchor, 1)
    io.open(p2, 'w', encoding='utf-8', newline='\n').write(s2)
    print('implementCast added')
else:
    print('MISS effects anchor')
