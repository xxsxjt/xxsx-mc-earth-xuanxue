# 超凡能量契约 v1（玄幻侧）

状态：源码实现、回归测试、静态门禁与资源打包已通过；游戏内运行与服务器重载尚未验证。

本契约与 `earth-series/earth_magic` 的 `AetherChunkField`/`ArcanaPower` 共享同一组不变量。单位、效率和字段含义必须由两侧回归测试共同约束；不建立强制运行时公共 Mod。

## 1. 物理层与超凡层

- 灵石的化学基质为多相固体：SiO2 80 wt%、KAlSi3O8 15 wt%、KAl2(AlSi3O10)(OH)2 4 wt%、Fe2O3 1 wt%。
- 固体质量、元素组成和相态不随充能改变。灵气不是化学元素；灵石保存的是独立的束缚场激发态。
- AEU 是游戏内公设单位：1 AEU = 1 kJ 可用功等价值。它不是现实发现，也不把既有 0–100 场值改称为能量。

## 2. 量的分离

| 量 | 含义 | 所有者 |
|---|---|---|
| field_level | 当前区块灵气玩法标度 0–100 | ArcanaChunkField |
| field_depletion | 当前区块枯竭标度 0–45 | ArcanaChunkField |
| field_flux | 本次可提取的场通量，等价于 `SpiritFieldLedger.availableField(field_level, depletion)` | SpiritStoneCharge 调用方 |
| charge_AEU | 灵石内部已储存场能 | 物品数据组件 |
| MP | 玩家共享法力 | earth_arcana.current_mana |

不得把 field_level 直接写成 AEU；不得把 charge 混入化学组成；不得把 MP 当化学燃料或环境浓度。

## 3. 数值不变量

    capacity_AEU = 120.0
    capture_efficiency = 0.80
    release_efficiency = 0.90
    AEU_PER_MANA = 10.0
    AEU_PER_FIELD_UNIT = 10.0
    talisman_sealed_charge_AEU = 24.0
    0 <= charge_AEU <= capacity_AEU

充能（全部以 AEU 计价）：

    field_coupling = clamp(usable_field_levels / 100, 0, 1)
    requested_aeu = aeu_budget * field_coupling^2
    aeu_spent = min(requested_aeu, (capacity - charge) / capture_efficiency)
    gained = aeu_spent * capture_efficiency
    loss = aeu_spent - gained
    charge += gained

释放：

    requested_aeu = requested_mana * AEU_PER_MANA / release_efficiency
    aeu_spent = min(charge, requested_aeu)
    mana_recovered = aeu_spent * release_efficiency / AEU_PER_MANA
    charge -= aeu_spent

效率必须在 0–1；多级效率相乘；输出能量不得超过输入；NaN/负数不得增加 charge 或法力。`capture` 的输入和输出都是 AEU；调用方必须先用 `SpiritFieldLedger` 把场等级换成耦合系数或 AEU，不允许把 0–100 的场等级直接当作 AEU 传进来。

## 3.1 场等级与 AEU

区块场仍是 0–100 的玩法标度，不是焦耳。`SpiritFieldLedger` 是唯一的换算入口：

    usable = max(0, field_level - depletion)
    aeu_available = usable * 10.0
    field_cost(aeu) = aeu / 10.0
    absorbed_aeu(exposure, coupling) = min(100, exposure) * 10.0 * coupling
    meditation_field_cost = field_cost(absorbed_aeu)

冥想与灵石充能都必须经过这一层。旧实现把冥想收益直接当作法力加到玩家身上，会让场等级和 MP 之间凭空放大，已改为“场 → AEU → 释放效率 → MP”。场扣除同样只按**实际吸收**的能量结算：暴露 100 等级、吸收率 8% 时扣除 8 个等级，而不是 100 个；否则高吸收率反而更快抽干区块。

`ArcanaChunkField` 提供两个扣除入口：`consumeField(aeu)` 按 AEU 换算后扣除，`consumeFieldLevels(levels)` 直接按等级扣除。机器与冥想使用后者，灵石与其它以 AEU 计价的效果使用前者。旧的 `Spirituality.consume(restoredMana)` 已删除，避免把法力值当作场等级二次换算。

## 4. 共享法力键

两个仓库都使用 `earth_arcana.*` 前缀。玄幻只写 `xuanhuan_*` 与玄幻专用路线字段；魔幻只写 `magic_*` 与魔幻专用字段。总上限按各项贡献相加，不能由任一侧覆盖对方贡献。旧的 `earth_online_arcana.*` 只作为迁移输入，迁移必须幂等。

关键键：

    earth_arcana.current_mana
    earth_arcana.base_mana
    earth_arcana.xuanhuan_mana_bonus
    earth_arcana.magic_mana_bonus
    earth_arcana.equipment_mana_bonus
    earth_arcana.temporary_mana_bonus
    earth_arcana.qi_absorption_rate
    earth_arcana.magic_attunement_rate
    earth_arcana.cultivation_level
    earth_arcana.magic_research_level

## 5. 服务端权限与存档

- `SpiritStoneItem.use` 只在服务端修改物品、玩家法力和区块枯竭度。
- 灵石不可堆叠，每件物品单独保存 `charge_aeu`、`matrix_grade`、`schema_version`。
- 数据组件必须登记到 `Registries.DATA_COMPONENT_TYPE`，并使用持久 Codec 与网络 StreamCodec。
- 读取/写入时把 charge 限制在 0–capacity；旧数据或缺失组件回退为空灵石。
- 区块枯竭通过 `Spirituality.consumeFieldLevels` / `consumeField` 进入 SavedData；退出重进后仍保留。

## 6. 测试向量（两侧必须一致）

| 用例 | 输入 | 期望 |
|---|---|---|
| NaN charge | clampCharge(NaN) | 0 |
| 超容量 | clampCharge(99999) | 120 |
| 空场 | capture(0,0,24,24) | gained 0, fieldSpent 0 |
| 首次吸收 | capture(0,100,24,24) | 0 < charge <= 120；gained = charge；fieldSpent = gained + loss |
| 满荷 | capture(120,100,24,24) | charge 120, fieldSpent 0 |
| 超量请求 | capture(0,100,10000,10000) | charge 120 |
| 空石释放 | release(0,10) | mana 0, aeuSpent 0 |
| 满石释放 | release(120,10) | 0 < mana <= 10；charge + aeuSpent = 120 |
| 场换算 | 100 可用等级 | 1000 AEU |
| 场扣除 | fieldCostOfAeu(10) | 1 个场等级 |
| 冥想收益 | meditationMana(100, 0.08) | 7.2 MP |
| 冥想场耗 | meditationFieldCost(100, 0.08) | 8 个场等级（只按吸收量扣除） |

## 7. 术式图与符箓

- 图 = 声明源 + 单条管线 + 显式汇。节点不再兼任 source，避免“第二个 source 其实是 buffer”的错误建模。
- 每个节点同时有 `costAeu` 与 `costMana`；合计为零的图被 `free_spell` 拒绝。
- `SpellGraphLoader` 从 `data/earth_xuanxue/spell_graph` 读取，只接受白名单枚举，文件内 `id` 必须与路径一致，坏图逐条拒绝并记录日志。它监听的是游戏总线的 `AddServerReloadListenersEvent`；若挂到 mod 总线则整个数据层永不加载。
- `SpellExecutor` 执行前先按 id 查 `SpellGraphLoader.find`，命中数据图就用数据图，否则回退到 `SpellCatalog` 内置图。数据包可以改数值，但不能引入新代码。
- 符案产出护体符时写入 `talisman_charge` 组件（24 AEU）；聚落箱子里的符同样在放置时封存。空符（无组件或 `charge_aeu` 为 0）在 `SpellExecutor.plan` 阶段即被 `talisman_empty` 拒绝，且不会被消耗。

## 8. 验收边界

- 已通过：Java 编译、单元回归、JAR 资源打包、两份 `spell_graph` JSON 进入 JAR；专用服务器在 MC 26.1.2 / NeoForge 26.1.2.124 上启动成功，日志确认 `Loaded 5 xuanhuan settlement definitions` 与 `Loaded 2 spell graph definitions`。
- 未通过/未执行：客户端启动、进入世界、右键充放能、死亡克隆、退出重进、多人同步、运行中数据包重载与坏图拒绝路径。
- 未完成：符文案的自动充能（目前只封存固定 24 AEU）；灵石的机器自动充能；配方级元素平衡在玄幻侧只有契约，没有运行校验。

## 9. 后续直接接手项

1. 在客户端进入世界实测右键充放能与死亡克隆、退出重进，并用 `/reload` 验证坏图被逐条拒绝且不影响已加载的合法图。
2. 由魔幻仓库实现同构的 JSON 图加载器与本方 `aether_*` 字段，不引用玄幻实现类。
3. 我的地球把 294 条旧化学表映射到目录，并补配方级元素平衡。
4. 为灵石增加自动充能设施；必须复用同一 `capture` 账本，不创建第二套公式。
