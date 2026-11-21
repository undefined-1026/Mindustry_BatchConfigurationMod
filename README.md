# Batch Configuration · 批量配置

一个 Mindustry Java 模组：先选中一个“样板建筑”，再框选一片区域，把样板建筑的配置（物品/液体配置、逻辑处理器代码、炮塔弹药等）批量复制到区域内所有**同类型、同队伍**的建筑上。

适用于需要大范围铺设统一配置的场景，例如：统一设置工厂产出、统一炮塔弹药、统一处理器逻辑等。

- 模组内部名：`batch-config`
- 主类：`mod.BatchConfigurationMod`
- 最低游戏版本：`160.3`
- 支持平台：PC / Android（Java 模组）

> 本模组在 `mod.hjson` 中设置了 `hidden: true`，不会出现在游戏内的模组浏览器列表中，需要手动安装 jar。

## 功能特性

- **样板选取**：面向任意可配置建筑按选取键，即可把它设为配置样板，任意距离都可以。
- **框选批量配置**：按住框选键拖动鼠标拉出矩形范围，松开后一次性配置范围内所有符合条件的建筑。
- **同类过滤**：只对与样板**方块类型相同**且**队伍相同**的建筑生效，不会误改其他建筑。
- **处理器支持**：样板为逻辑处理器时，会复制其**代码与连接**（单人模式下使用压缩数据直接写入）。
- **多人限流**：作为客户端联机时，配置不会瞬间全部下发，而是进入队列，按“配置间隔”逐个发送，避免刷屏和服务器压力。
- **视觉反馈**：被配置的建筑会闪烁一次方块图标；框选过程中显示选框、样板位置以及 `宽x高 (面积)` 提示。
- **可配置项**：配置间隔与特效不透明度均可在游戏设置中调整。

## 按键操作

按键可在 **设置 → 控制** 中修改（分类：`批量配置`）。

| 按键绑定 | 默认键 | 作用 |
| --- | --- | --- |
| 选择设置副本 (`select_target`) | `H` | 轻按并松开：把鼠标指向的建筑设为样板 |
| 框选配置范围 (`batch_config`) | `H` | 按住拖动：拉出批量配置范围；松开：执行配置 |

两者默认都是 `H`，因此常规操作是：

1. 鼠标指向要复制的建筑，按一下 `H` → 该建筑成为样板（闪烁一次）。
2. 按住 `H` 从区域一角拖到另一角 → 显示选框与尺寸。
3. 松开 `H` → 框内所有同类型、同队伍的建筑被配置为样板配置，同时清除样板。

### 使用小技巧

- 把两个按键改成**不同按键**后，样板会保持选中，可以连续框选多次，不必重复选取样板。
- 选取样板时按 `清除建筑`、`重新建造选择`、`拾取`、`取消选择` 等原版按键，可取消当前样板。
- 框选范围受原版蓝图最大尺寸限制（`maxSchematicSize`）。

## 设置项

设置 → `批量配置` 分类：

| 设置 | 默认值 | 范围 | 说明 |
| --- | --- | --- | --- |
| 配置间隔 | `10 tick` | `1 ~ 60 tick`，步长 3 | 仅联机作为客户端时生效，控制队列中每个配置的下发间隔 |
| 特效不透明度 | `40%` | `0 ~ 100%`，步长 5 | 配置成功时方块图标闪烁效果的强度/颜色深浅 |

## 多人游戏说明

- **主机/单人**：配置立即逐个应用。
- **客户端**：配置进入队列，按“配置间隔”定时发送给服务器，由服务器执行 `configure`。
- 框选与样板选取完全在本地完成，不改变原版操作逻辑。
- 逻辑处理器在客户端模式下同样按队列下发压缩数据。

## 已知限制

- **处理器连接位置**：由于游戏没有提供“绝对位置”配置 API，处理器只能被配置为与样板**相同的连接布局**；在单人模式下可直接复制代码与连接，跨位置使用需自行保证连接一致。
- 只对**可配置**（`configurable`）且属于玩家队伍的方块生效；不可配置方块无法作为样板。
- 只对样板**同类型**方块生效，不会跨方块类型复制。
- 模组仅在客户端加载事件后初始化 UI 设置分类，专用服务器上不显示设置项（不影响核心功能）。

## 构建

需要 **JDK 17**。依赖的 Mindustry API 已放在 `libs/dependencies.jar`，以 `compileOnly` 方式参与编译，不会打进 jar。

### 桌面版（仅用于本地测试）

```bash
# Windows
gradlew.bat jar
# Linux / macOS
./gradlew jar
```

产物：`build/libs/BatchConfigurationDesktop.jar`，可直接放入 Mindustry 的 `mods/` 目录。该版本**不能**在 Android 上运行。

### 全平台版本（Android + 桌面）

需要安装 Android SDK，并满足：

1. 设置环境变量 `ANDROID_HOME`（或 `ANDROID_SDK_ROOT`）。
2. 安装任意包含 `android.jar` 的平台。
3. 将 `build-tools` 下的 `d8` 加入 `PATH`。

```bash
gradlew.bat deploy   # Windows
./gradlew deploy     # Linux / macOS
```

产物：`build/libs/BatchConfiguration.jar`，同时兼容 Android 与桌面。

## 项目结构

```
BatchConfiguration/
├─ mod.hjson                        # 模组元数据（名称、作者、版本、主类等）
├─ build.gradle.kts                 # Gradle 构建脚本（jar / jarAndroid / deploy）
├─ gradlew, gradlew.bat             # Gradle Wrapper
├─ libs/dependencies.jar            # Mindustry v160 编译依赖（compileOnly）
├─ icon.png                         # 模组图标
├─ assets/
│  ├─ bundles/bundle_zh_CN.properties  # 中文文本（设置项、按键、分类）
│  └─ sprites/frog.png              # 占位素材
└─ src/
   ├─ mod/BatchConfigurationMod.java  # 模组入口：注册设置分类并初始化
   └─ mod/Main.java                   # 核心逻辑：选取、框选、队列、特效、选框绘制
```

## 实现简述

- `BatchConfigurationMod` 监听 `ClientLoadEvent`，在主线程 `Core.app.post` 中注册设置分类，然后调用 `Main.init()`。
- `Main` 通过 `Events.run(Trigger.update)` 每帧处理按键与队列，通过 `Events.run(Trigger.draw)` 绘制选框。
- 框选时先用 `indexer.eachBlock` 按矩形中心与半径粗筛，再逐块校验坐标、方块类型与队伍，保证只改动目标建筑。
- 联机客户端使用 `Queue<Config>` + `Interval` 节流；`Config` 对象由 `Pool` 复用，避免频繁分配。
- 处理器单独走 `LogicBlock.compress` / `readCompressed` 路径，其余方块统一 `configure`。

## 作者

- Bilibili：arknights player
- GitHub：undefined-1026

## 致谢

项目基于 [Mindustry Java Mod Template](https://github.com/Anuken/MindustryJavaModTemplate) 搭建。