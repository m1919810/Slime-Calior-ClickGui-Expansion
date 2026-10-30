# slimefunhelper-calior-clickgui-expansion

为 [SlimefunHelper](https://github.com/m1919810/SlimefunHelper) 提供可选的窗口式 ClickGui 样式与组件样式的 Fabric 扩展。

## 功能

- 注册「窗口」ClickGui 样式及配套组件样式。
- 提供模块分类、搜索、模块设置等窗口界面。
- 支持通过 `click-gui.window-widget-style.setting` 配置窗口组件的外观。

安装后，在 SlimefunHelper 的 ClickGui 相关设置中启用「窗口 ClickGui」模块，并选择「窗口」样式。

## 环境要求

- Minecraft 1.21.1 至 1.21.11（`fabric.mod.json` 声明的版本范围；构建目标版本为 1.21.11）
- Java 21 或更高版本
- Fabric Loader 0.18.4 或更高版本
- 对应游戏版本的 Fabric API
- SlimefunHelper 1.2.1.2 或更高版本

## 构建与安装

在项目根目录运行：

```powershell
.\gradlew.bat build
```

构建完成后，将 `build/libs/slimefunhelper-calior-clickgui-expansion-1.0.0.jar` 与 SlimefunHelper、Fabric API 一起放入 Minecraft 的 `mods` 目录。`-sources.jar` 是源码包，不用于安装。

构建产物和 Gradle 项目使用 `slimefunhelper-calior-clickgui-expansion` 名称；Fabric 模组 ID 仍为 `slimefun-cailor-clickgui`，以保持现有插件标识和配置文件名兼容。

## 致谢

感谢 **独韵** 提供 GUI 设计。
