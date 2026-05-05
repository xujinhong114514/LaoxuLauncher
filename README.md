# Laoxu Minecraft Launcher

> 基于 HMCL 的 Minecraft 启动器，支持 Java 版 + 基岩版

[![License](https://img.shields.io/badge/License-GPLv3-blue.svg)](LICENSE)
[![Java](https://img.shields.io/badge/Java-17+-orange.svg)](https://www.java.com/)
[![Release](https://img.shields.io/badge/Release-v2.0.0-green.svg)](https://github.com/yourname/LaoxuLauncher/releases)

---

## 📖 简介

**Laoxu Minecraft Launcher** 是一个基于 [HMCL (Hello Minecraft! Launcher)](https://github.com/HMCL-dev/HMCL) 开发的 Minecraft 启动器。

### 特色功能

- ✅ **Java 版** - 完整保留 HMCL 所有功能
- ✅ **基岩版** - 新增基岩版启动支持（Windows 10/11）
- ✅ **自动检测** - 自动扫描所有磁盘分区的 XboxGames 文件夹
- ✅ **一键启动** - 直接运行 Minecraft.Windows.exe

### 与 HMCL 的区别

| 功能 | HMCL | Laoxu Launcher |
|------|------|----------------|
| Java 版 | ✅ | ✅（完整保留） |
| 基岩版 | ❌ | ✅（新增） |
| 模组管理 | ✅ | ✅ |
| 账户系统 | ✅ | ✅ |
| 版本隔离 | ✅ | ✅ |

---

## 🚀 快速开始

### 系统要求

- **操作系统**: Windows 10/11（64位）
- **Java**: Java 17 或更高版本
- **基岩版**: 已安装 Minecraft for Windows（从 Microsoft Store 或 Xbox 应用安装）

### 下载与运行

1. 从 [Releases](https://github.com/yourname/LaoxuLauncher/releases) 下载最新版本
2. 双击运行 `LaoxuLauncher.exe` 或 `java -jar LaoxuLauncher.jar`
3. 在启动器顶部选择 Java 版或基岩版
4. 点击启动按钮开始游戏

### 首次使用

**Java 版**：
- 点击「下载」按钮安装新游戏
- 或从「实例列表」选择已有游戏

**基岩版**：
- 确保已安装 Minecraft for Windows
- 点击「基岩版」按钮，启动器会自动扫描
- 检测成功后显示启动按钮

---

## 🎮 基岩版支持说明

### 检测逻辑

启动器会扫描所有磁盘分区（C:、D:、E: 等），查找以下路径：
{磁盘}:\XboxGames\Minecraft for Windows\Content\Minecraft.Windows.exe

### 支持的安装方式

- ✅ Microsoft Store 安装
- ✅ Xbox 应用安装
- ✅ 任意分区（C:、D:、E: 等）

### 注意事项

- 基岩版仅支持 Windows 10/11
- 需要已安装 Minecraft for Windows
- 如果检测不到，请确认游戏已安装并位于 XboxGames 文件夹

---

## 📁 项目结构
LaoxuLauncher/
├── HMCL/ # HMCL 源码
│ └── src/main/java/org/jackhuang/hmcl/
│ ├── bedrock/ # 新增：基岩版模块
│ │ ├── BedrockLauncher.java
│ │ └── BedrockVersion.java
│ ├── ui/
│ │ ├── bedrock/ # 新增：基岩版 UI
│ │ │ └── BedrockVersionPage.java
│ │ └── main/
│ │ └── MainPage.java # 修改：添加基岩版切换
│ └── Metadata.java # 修改：品牌名称
└── docs/ # 文档


---

## 🛠️ 开发与编译

### 环境要求

- JDK 21+
- Gradle 9.4+

### 编译步骤

```bash
# 克隆仓库
git clone https://github.com/xujinhong114514/LaoxuLauncher.git
cd LaoxuLauncher

# 编译
./gradlew clean build

# 运行
java -jar HMCL/build/libs/HMCL-*.jar
