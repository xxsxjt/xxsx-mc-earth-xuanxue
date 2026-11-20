# 把镇岳星君接入 `xuanhuan_earth`

原型和目标模组已在**同一仓库**。以 `handoff/astral-sovereign` 分支取用样例，先在开发分支工作，不要把这个独立原型整体并入运行时。交接分支以当时 GitHub `main` 的 `0313dfe` 为基础，没有包含维护者尚未推送的改动。提交集成 PR 前，以确认后的目标分支为基线，只提交移植所需的运行时改动。

## 迁移范围

1. **核对构建基线。** 目标模块是仓库根目录的 `neoforge-26.2/`；实际 Minecraft 和 NeoForge 版本看该分支的构建配置，不按目录名推断。使用 JDK 25。若它仍通过相对路径依赖 `earth_lore`，把 [Earth Lore](https://github.com/xxsxjt/xxsx-mc-earth-lore) 检出为本仓库的同级 `earth_lore/`，按目标 `build.gradle` 要求的版本生成 JAR；不要提交依赖 JAR，也不要用服务端目录替代依赖。
2. **迁移实体与物品。** 保留目标模组 ID `xuanhuan_earth` 和 Java 根包 `com.xxsx.xuanhuanearth`。接入目标已有的实体、属性、物品与创造栏注册入口；不要复制原型的 `@Mod`、独立构建、创造栏或模组描述。计划使用 `xuanhuan_earth:astral_sovereign`、`xuanhuan_earth:astral_sovereign_spawn_egg`、`xuanhuan_earth:astral_fangtian_halberd`，首轮不加入自然生成。如果确实要兼容装过独立原型的存档，另行验证旧 ID 的迁移。
3. **迁移客户端和资源。** 渲染器与模型层接入现有的 `XuanhuanEarthClient`，不要让公共代码引用客户端类型。贴图和物品定义归 `assets/xuanhuan_earth/`，掉落表归 `data/xuanhuan_earth/`；中英文词条追加到现有文件，逐项变更资源标识与引用，不替换修士模型。不要把 `.bbmodel` 放进运行时 JAR。
4. **核对两阶段表现与掉落。** 独立原型用一个 Java 模型层、部件显隐和两套贴图；不会直接加载两份 `.bbmodel` 的关键帧。检查右手持戟、星环与飞剑的阶段切换，不把原型表现当成定稿已经逐帧还原。原型掉落为方天星辰戟和下界之星，需按玄学进度重新评估。评审飞剑的距离和视线、武器技能是否重复命中、Boss 的阶段和血条能否正确跨存档加载。

## 验收

使用现有 Gradle Wrapper，从目标模块运行 `./gradlew --no-daemon build`，检查实体属性注册、资源路径、模型层、翻译、掉落表及客户端代码隔离。原型独立构建成功不等于目标模组集成通过。游戏内由验收者在隔离存档用刷怪蛋或 `/summon xuanhuan_earth:astral_sovereign` 验证；未获本轮授权时不要启动客户端或服务端。
