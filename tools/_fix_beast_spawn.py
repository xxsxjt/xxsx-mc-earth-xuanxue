import io

p = (r'D:\Codex\WORKSPACES\Projects\mc-agent\earth-series\earth_xuanxue\neoforge-26.1.2\src\main\java'
     r'\com\xxsx\earth\xuanxue\settlement\XuanhuanSettlementAnchorBlockEntity.java')
s = io.open(p, encoding='utf-8').read()

old = '''        if (type == XuanhuanSettlementFeature.Type.SPIRIT_SPRING_VILLAGE
                || type == XuanhuanSettlementFeature.Type.SECT_OUTPOST
                || type == XuanhuanSettlementFeature.Type.SPIRIT_BEAST_KEEPER_CAMP
                || type == XuanhuanSettlementFeature.Type.MOUNTAIN_SPIRIT_SHRINE) {
            spawnBeast(level, pos.offset(3, 1, 3), randomBeast(random));
        }'''

new = '''        if (type == XuanhuanSettlementFeature.Type.SPIRIT_SPRING_VILLAGE
                || type == XuanhuanSettlementFeature.Type.SECT_OUTPOST
                || type == XuanhuanSettlementFeature.Type.SPIRIT_BEAST_KEEPER_CAMP
                || type == XuanhuanSettlementFeature.Type.MOUNTAIN_SPIRIT_SHRINE) {
            // A fixed corner can be water, a wall or a paved tile, so search outward instead
            // of trusting one hard-coded offset.
            spawnBeastNear(level, pos, randomBeast(random));
        }'''

if old in s:
    s = s.replace(old, new, 1)
    print('beast call replaced')
else:
    print('MISS beast call')

old_spawn = '''    private static void spawnBeast(ServerLevel level, BlockPos pos,
                                   EntityType<? extends ContractableSpiritBeastEntity> type) {
        ContractableSpiritBeastEntity beast = type.create(level, EntitySpawnReason.STRUCTURE);
        if (beast == null) {
            return;
        }
        BlockPos spawnPos = findOpenSpawn(level, pos);
        beast.snapTo(spawnPos.getX() + 0.5D, spawnPos.getY(), spawnPos.getZ() + 0.5D,
                level.getRandom().nextFloat() * 360.0F, 0.0F);
        beast.setPersistenceRequired();
        level.addFreshEntity(beast);
    }'''

new_spawn = '''    private static void spawnBeastNear(ServerLevel level, BlockPos anchor,
                                       EntityType<? extends ContractableSpiritBeastEntity> type) {
        // Ring search outward from the anchor so the beast lands on open ground.
        for (int radius = 2; radius <= 6; radius++) {
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    if (Math.max(Math.abs(dx), Math.abs(dz)) != radius) {
                        continue;
                    }
                    BlockPos candidate = findOpenSpawn(level, anchor.offset(dx, 1, dz));
                    if (!level.isEmptyBlock(candidate)
                            || level.isEmptyBlock(candidate.below())) {
                        continue;
                    }
                    ContractableSpiritBeastEntity beast = type.create(level, EntitySpawnReason.STRUCTURE);
                    if (beast == null) {
                        return;
                    }
                    beast.snapTo(candidate.getX() + 0.5D, candidate.getY(), candidate.getZ() + 0.5D,
                            level.getRandom().nextFloat() * 360.0F, 0.0F);
                    beast.setPersistenceRequired();
                    level.addFreshEntity(beast);
                    return;
                }
            }
        }
    }'''

if old_spawn in s:
    s = s.replace(old_spawn, new_spawn, 1)
    print('spawn method replaced')
else:
    print('MISS spawn method')

io.open(p, 'w', encoding='utf-8', newline='\n').write(s)
