<div align="center">
  <img src="https://i.meee.com.tw/gBbvMrH.png" alt="GLM Usage Monitor Logo" width="120" height="120">
  <h1>GLM/智谱 AI 实时用量监控</h1>
  <p>为 JetBrains IDE 提供 GLM/智谱 AI API 实时用量监控插件</p>
  
  ![Version](https://img.shields.io/badge/version-0.1.6-blue.svg)
  ![JetBrains Plugin](https://img.shields.io/badge/JetBrains-Plugin-orange.svg)
  ![License](https://img.shields.io/badge/License-MIT-blue.svg)
  ![Kotlin](https://img.shields.io/badge/Kotlin-1.9.25-blue.svg)
</div>

## ✨ 功能特性

- 📊 **实时数据监控**: 在 IDE 状态栏实时显示 Token 使用量和 API 调用次数
- ⚠️ **配额预警**: MCP 额度（月度）和 Token 限流（5小时）配额预警，超过 80% 标红提醒
- 📈 **调用趋势**: 24 小时调用趋势图表（Sparkline），直观展示使用模式
- 🔍 **悬停面板**: 鼠标悬停查看详细数据面板，包含完整统计信息
- 🔄 **便捷操作**: 点击刷新数据、快捷键支持（Ctrl+Shift+G）
- ⚙️ **灵活配置**: 支持智谱 AI 和 Z.ai 双平台，可自定义 API 地址

## 📸 截图展示

### 主界面 - 状态栏实时显示与悬停面板
<p align="center">
  <img src="https://i.meee.com.tw/eUJl9KP.png" alt="主界面显示效果" width="30%">
</p>

### 设置界面
<p align="center">
  <img src="https://i.meee.com.tw/yQIo2J7.png" alt="设置界面1" width="45%">
  <img src="https://i.meee.com.tw/j3EYtaZ.png" alt="设置界面2" width="45%">
</p>

## 🚀 快速开始

### 安装方式

#### 方式一：通过 JetBrains Marketplace（推荐）
1. 打开 IDE 设置：`File → Settings → Plugins`
2. 搜索：`GLM/智谱 AI 实时用量监控`
3. 点击安装并重启 IDE

#### 方式二：手动安装
1. 下载最新版本的插件 ZIP 包
2. 打开 IDE 设置：`File → Settings → Plugins`
3. 点击齿轮图标 → `Install Plugin from Disk...`
4. 选择下载的 ZIP 文件并重启 IDE

### 配置 API Token

1. 获取 API Token：
   - 智谱 AI: https://open.bigmodel.cn/
   - Z.ai: https://z.ai/

2. 配置 Token（三种方式）：
   - **方式一**：点击状态栏的 `⚙️ 配置 GLM` → 输入 Token
   - **方式二**：`Tools → 配置 GLM/智谱 AI Token`
   - **方式三**：设置环境变量 `ANTHROPIC_AUTH_TOKEN`

3. 配置完成，插件将自动开始监控

## ⚙️ 高级配置

打开设置：`File → Settings → Tools → GLM/智谱 AI 用量监控`

- **API Key**: 您的智谱 AI 或 Z.ai API Token
- **Base URL**: API 基础地址（默认: https://open.bigmodel.cn/api/anthropic）
- **Timeout**: 请求超时时间（毫秒，默认: 30000）
- **Use Mock Data**: 启用模拟数据用于测试

### 支持的 API Key 格式
- `sk-...` 格式（智谱 AI）
- `id.secret` 格式（Z.ai）

## ⌨️ 快捷键

- `Ctrl + Shift + G`: 查看详细用量信息

## 🔧 开发构建

### 环境要求
- JDK 17 或更高版本
- IntelliJ IDEA 2023.2.5 或更高版本
- Kotlin 1.9.25

### 构建步骤

```bash
# 克隆仓库
git clone https://github.com/safphere/glm-usage-jetbrains.git
cd glm-usage-jetbrains

# 构建插件
./gradlew build

# 构建结果在 build/distributions/
```

### 本地运行测试

```bash
# 运行测试
./gradlew test

# 启动 IDE 实例运行插件
./gradlew runIde
```

## 📝 使用技巧

1. **悬停查看详情**: 将鼠标悬停在状态栏的用量信息上，查看完整数据面板
2. **点击刷新**: 点击状态栏组件可立即刷新数据
3. **配额预警**: 当配额使用超过 80% 时，状态栏文字会变红提醒
4. **24小时趋势**: 悬停面板中的 Sparkline 图表显示最近 24 小时的调用趋势
5. **峰值信息**: 悬停面板显示调用峰值和时间点

## 🤝 贡献指南

欢迎提交 Issue 和 Pull Request！

1. Fork 本仓库
2. 创建特性分支：`git checkout -b feature/AmazingFeature`
3. 提交更改：`git commit -m 'Add some AmazingFeature'`
4. 推送到分支：`git push origin feature/AmazingFeature`
5. 开启 Pull Request

## 📄 开源协议

本项目基于 [MIT 协议](LICENSE) 开源。

## 🙏 致谢

- 灵感来源于 [glm-usage-vscode](https://github.com/your-repo/glm-usage-vscode)
- 感谢 [智谱 AI](https://open.bigmodel.cn/) 和 [Z.ai](https://z.ai/) 提供优秀的 API 服务

## 📧 联系方式

- **作者**: Safphere
- **组织**: [Safphere](https://github.com/safphere)
- **项目地址**: https://github.com/safphere/glm-usage-jetbrains

## 🔄 更新日志

### v0.1.6
- ✨ 初始版本发布
- 📊 实时用量监控
- ⚠️ 配额预警功能
- 📈 24小时调用趋势
- 🔍 悬停数据面板
- ⚙️ 灵活配置支持

---

<div align="center">

**如果觉得这个插件有帮助，请给个 ⭐️ Star 支持一下！**

</div>
