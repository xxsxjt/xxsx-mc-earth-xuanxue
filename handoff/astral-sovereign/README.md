# 镇岳星君：地球·玄学开发交接

本目录是地球：玄学仓库内的镇岳星君开发素材，位于 `handoff/astral-sovereign` 分支。它**尚未接入 `xuanhuan_earth`**：独立原型仍用 `astral_sovereign`，不会随 `neoforge-26.2/` 的玄学主模组构建或加载。

## 云端开工

在云端工作空间选择本仓库的 `handoff/astral-sovereign` 分支，然后阅读[接入说明](docs/integrate-xuanhuan-earth.md)。如需先独立验证 Boss 原型，使用 **JDK 25**：

```sh
git clone -b handoff/astral-sovereign https://github.com/xxsxjt/xxsx-mc-earth-xuanxue.git
cd xxsx-mc-earth-xuanxue/handoff/astral-sovereign
java -version
./gradlew --no-daemon build
```

Windows 使用 `.\gradlew.bat --no-daemon build`。独立原型基线是 **Minecraft 26.1.2 / NeoForge 26.1.2.124**。首次构建需要联网，Gradle Wrapper 已固定版本与发行包校验值；不依赖本机素材路径或其他模组仓库。产物为 `build/libs/astral_sovereign-1.0.0.jar`，不是发布版，也不代表通过了游戏内测试。

## 文件导航

| 路径 | 内容 |
|---|---|
| [`blockbench/`](blockbench/) | 两阶段定稿 `.bbmodel`，内嵌贴图，可直接编辑 |
| [`src/main/java/`](src/main/java/) | Boss、阶段状态、血条、方天星辰戟及客户端渲染原型 |
| [`src/main/resources/`](src/main/resources/) | 运行时贴图、物品定义、掉落表和中英文词条 |
| [`docs/integrate-xuanhuan-earth.md`](docs/integrate-xuanhuan-earth.md) | 接入 `xuanhuan_earth` 的迁移与验收清单 |

| 定稿 | 交付文件名 | SHA-256 |
|---|---|---|
| [`astral_sovereign_phase1.bbmodel`](blockbench/astral_sovereign_phase1.bbmodel) | `astral_sovereign_boss_p1_righthand_bedrock.bbmodel` | `4df1bf75259fe6ca267af51a5f5bc6db90bdfa63a08422db859740446783b20c` |
| [`astral_sovereign_phase2.bbmodel`](blockbench/astral_sovereign_phase2.bbmodel) | `astral_sovereign_boss_p2_righthand_v2_bedrock.bbmodel` | `e31cc907fbfbbf2699a256a872cb66ab4c578c3f3b04ca87ad4ebbdfecde1232` |

模型来自镇岳星君的原创设计交付，定稿字节未改动；对应的两张运行时贴图与内嵌 PNG 一致。原型保留原工程的 EarthSeries 署名和 `mod_license=MIT` 元数据；不能从该字段推定模型与贴图的其他许可条款。进入正式发行版前请核对适用的授权与署名。会话全文、录像、旧版本、ZIP 归档和服务端依赖未纳入本分支。
