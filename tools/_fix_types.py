import io

base = r'D:\Codex\WORKSPACES\Projects\mc-agent\earth-series\earth_xuanxue\neoforge-26.1.2\src\main\java\com\xxsx\earth\xuanxue\settlement'

# --- Fix: spirit grass is an item, not a block. Use the spirit soil bed only.
p = base + r'\XuanhuanSettlementFeature.java'
s = io.open(p, encoding='utf-8').read()
old = '''                } else if ((x * z) % 5 == 0) {
                    level.setBlock(center.offset(x, 0, z),
                            XuanhuanEarth.SPIRIT_GRASS.get().defaultBlockState(), FLAGS);
                }'''
new = '''                } else if ((x * z) % 5 == 0) {
                    level.setBlock(center.offset(x, 0, z), palette.accent(), FLAGS);
                }'''
if old in s:
    s = s.replace(old, new, 1)
    print('grass fix ok')
else:
    print('MISS grass')
io.open(p, 'w', encoding='utf-8', newline='\n').write(s)

# --- Add rosters for the two new settlement types.
p2 = base + r'\XuanhuanSettlementAnchorBlockEntity.java'
s2 = io.open(p2, encoding='utf-8').read()
old_roster = '''            case SECT_OUTPOST -> List.of(
                    CultivationSettlerEntity.Role.SECT_STEWARD,
                    CultivationSettlerEntity.Role.SECT_STEWARD,
                    CultivationSettlerEntity.Role.SPIRIT_SPRING_KEEPER);
        };'''
new_roster = '''            case SECT_OUTPOST -> List.of(
                    CultivationSettlerEntity.Role.SECT_STEWARD,
                    CultivationSettlerEntity.Role.SECT_STEWARD,
                    CultivationSettlerEntity.Role.SPIRIT_SPRING_KEEPER);
            case MOUNTAIN_SPIRIT_SHRINE -> List.of(
                    CultivationSettlerEntity.Role.SPIRIT_SPRING_KEEPER,
                    CultivationSettlerEntity.Role.SECT_STEWARD,
                    CultivationSettlerEntity.Role.WANDERING_MERCHANT);
            case SPIRIT_BEAST_KEEPER_CAMP -> List.of(
                    CultivationSettlerEntity.Role.SPIRIT_SPRING_KEEPER,
                    CultivationSettlerEntity.Role.WANDERING_MERCHANT,
                    CultivationSettlerEntity.Role.WANDERING_MERCHANT);
        };'''
if old_roster in s2:
    s2 = s2.replace(old_roster, new_roster, 1)
    print('roster ok')
else:
    print('MISS roster')

old_spawn = '''        if (type == XuanhuanSettlementFeature.Type.SPIRIT_SPRING_VILLAGE
                || type == XuanhuanSettlementFeature.Type.SECT_OUTPOST) {'''
new_spawn = '''        if (type == XuanhuanSettlementFeature.Type.SPIRIT_SPRING_VILLAGE
                || type == XuanhuanSettlementFeature.Type.SECT_OUTPOST
                || type == XuanhuanSettlementFeature.Type.SPIRIT_BEAST_KEEPER_CAMP
                || type == XuanhuanSettlementFeature.Type.MOUNTAIN_SPIRIT_SHRINE) {'''
if old_spawn in s2:
    s2 = s2.replace(old_spawn, new_spawn, 1)
    print('spawn ok')
else:
    print('MISS spawn')

io.open(p2, 'w', encoding='utf-8', newline='\n').write(s2)
