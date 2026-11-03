# Earth on Minecraft 生态联动契约

## 当前已验证项目

- `earth_on_minecraft`：现实材料与地质本体，当前审查版本 `0.1.16`（本体目标 MC 26.1.2 / NeoForge 26.1.2.124）。
- `earth_human`：独立人物状态模组，当前审查版本 `0.1.20`。
- `fantasy_earth`：独立可玩的《魔幻地球》，同时安装时与玄幻共享法力值。

所有联动保持可选；未安装其他项目时，玄幻路线必须仍可独立完成入门闭环。

> 版本线提醒：玄幻地球自 `0.7.0-alpha.9` 起目标版本线已对齐至 `MC 26.1.2 / NeoForge 26.1.2.124`（模块目录名仍沿用 `neoforge-26.2`，实际目标以 `neoforge-26.2/build.gradle` 为准），与上述生态本体处于同一 `26.1.2` 版本线。严禁将旧 `26.2` 预览构建产物与 `26.1.2` 模组混装，也不得把 `optional` 依赖当成跨 MC/NeoForge 版本兼容；同版本组合完整实测通过前，本节版本号仍代表源码与接口契约基线。

## 我的地球材料接口

设施和配方通过下列物品标签读取现实材料，不依赖本体 Java 类：

- `#earth_on_minecraft:spiritual_mineral_substrates`
- `#earth_on_minecraft:arcana_geology_catalysts`
- `#earth_on_minecraft:mana_conductors`
- `#earth_on_minecraft:aether_crystal_substrates`

标签为空时，对应增强路线不进入设施 JEI；原版回退配方继续可用。

## 共享 Arcana 玩家数据

玄幻与魔幻使用 `earth_arcana.*` 持久数据键共享当前法力、上限贡献、路线等级和人体增强。两边只累加自己的路线贡献，不清空另一个独立模组的数据。

`earth_human` 的 `ArcanaHumanBridge` 已读取修行、魔法研究、辟谷、呼吸、耐力和淬体相关键，并将其用于疲劳、温度压力、食物消耗和空气容量计算。

## 当前人体恢复边界

`earth_human` 0.1.20 提供 `EarthHumanApi v1`（`API_VERSION = 1`，`snapshot` / `recover` / `applyActivity` / `airQuality` / `detailedSnapshot`）。玄幻通过可选反射适配器调用人体快照和服务端恢复入口，由地球：人类负责配置倍率、当前部位上限、存档迁移、保存和同步。

API 缺失或版本过旧时，人体联动安全关闭；不得回退为直接读写 `earth_human.*` 私有 NBT。

## 兼容规则

1. 不编译依赖其他生态模组源码。
2. 现实材料优先走标签，玩家进度走共享持久键。
3. 所有增强配方保留原版回退入口。
4. 手册显示检测到的联动，不要求玩家理解模组加载细节。
5. 新增共享键或标签时，同时更新本契约和中英玩家提示。
6. 单模验收是发布门禁：不安装任何生态模组时，也必须能创建世界并完成手册描述的基础流程。
