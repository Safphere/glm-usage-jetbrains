# GitHub 仓库创建和推送指南

## ⚠️ 重要安全提醒

**请不要分享你的 GitHub Personal Access Token！**

Token 一旦泄露，请立即：
1. 访问 https://github.com/settings/tokens
2. 删除已泄露的 Token
3. 重新生成新的 Token

## 步骤 1: 创建 GitHub 仓库

### 选项 A: 通过 GitHub 网页界面（推荐）

1. 登录 GitHub 账号（mrcatai）
2. 访问组织页面：https://github.com/safphere
3. 点击 "New" 按钮创建新仓库
4. 填写仓库信息：
   - **Repository name**: `glm-usage-jetbrains`
   - **Description**: `为 JetBrains IDE 提供 GLM/智谱 AI API 实时用量监控插件`
   - **Visibility**: 选择 `Public`（开源）
   - **Initialize this repository with**: 不要勾选任何选项（保持空白）
5. 点击 "Create repository"

### 选项 B: 通过 GitHub CLI（命令行）

如果已安装 GitHub CLI：

```bash
# 登录 GitHub（如果尚未登录）
gh auth login

# 在 safphere 组织下创建仓库
gh repo create safphere/glm-usage-jetbrains \
  --public \
  --description="为 JetBrains IDE 提供 GLM/智谱 AI API 实时用量监控插件" \
  --remote=origin
```

## 步骤 2: 生成本地提交

在项目根目录执行以下命令：

```bash
# 添加所有文件到暂存区
git add .

# 创建初始提交
git commit -m "Initial commit: GLM/智谱 AI 实时用量监控插件"
```

## 步骤 3: 添加远程仓库并推送

### 方法 1: 使用 HTTPS（推荐）

```bash
# 添加远程仓库
git remote add origin https://github.com/safphere/glm-usage-jetbrains.git

# 推送到 GitHub（首次推送需要 -u 参数）
git push -u origin main
```

**注意**：如果使用 HTTPS 方式，推送时会提示输入用户名和密码。
- **用户名**：你的 GitHub 用户名（mrcatai）
- **密码**：你的 GitHub Personal Access Token（不是登录密码）

### 方法 2: 使用 SSH

如果使用 SSH 方式，需要先配置 SSH 密钥：

```bash
# 添加远程仓库（SSH 方式）
git remote add origin git@github.com:safphere/glm-usage-jetbrains.git

# 推送到 GitHub
git push -u origin main
```

## 步骤 4: 验证推送

访问 https://github.com/safphere/glm-usage-jetbrains 查看代码是否已成功推送。

## 步骤 5: 配置仓库设置（可选但推荐）

1. **设置默认分支**：确保默认分支是 `main`
2. **添加仓库话题**：添加 `jetbrains`, `plugin`, `glm`, `zhipu-ai`, `api-monitoring` 等话题
3. **配置分支保护规则**（可选）：
   - 进入 Settings → Branches
   - 添加分支保护规则 for `main` 分支
   - 可选：要求 Pull Request 审查
4. **启用 Issues 和 Discussions**：便于用户反馈
5. **配置仓库主页**：
   - 添加仓库描述
   - 添加网站链接（如果有）
   - 添加社交媒体链接

## 步骤 6: 创建发布（Release）

当准备发布新版本时：

```bash
# 创建标签
git tag -a v0.1.0 -m "Initial release"

# 推送标签到 GitHub
git push origin v0.1.0
```

然后在 GitHub 网页上：
1. 进入 "Releases" 页面
2. 点击 "Draft a new release"
3. 选择刚刚推送的标签 `v0.1.0`
4. 填写发布说明
5. 上传构建好的插件包（可选）
6. 点击 "Publish release"

## 常见问题解决

### 问题 1: 推送时提示 "Authentication failed"

**解决方案**：
- 确认使用的是 Personal Access Token 而不是密码
- 确认 Token 有 `repo` 权限
- 重新生成 Token: https://github.com/settings/tokens

### 问题 2: 提示 "remote origin already exists"

**解决方案**：
```bash
# 先删除已存在的 remote
git remote remove origin

# 然后重新添加
git remote add origin https://github.com/safphere/glm-usage-jetbrains.git
```

### 问题 3: 提示 "src refspec main does not match any"

**解决方案**：
```bash
# 确保有提交记录
git log

# 如果没有，先创建提交
git add .
git commit -m "Initial commit"
```

### 问题 4: 提示 "Updates were rejected"

**解决方案**：
```bash
# 强制推送（谨慎使用）
git push -f origin main

# 或者先拉取远程更改
git pull --rebase origin main
git push origin main
```

## 安全最佳实践

1. **不要提交敏感信息**：
   - API Keys
   - Tokens
   - 密码
   - 私钥

2. **使用环境变量**：在本地开发中使用环境变量存储敏感信息

3. **配置 .gitignore**：确保 `.gitignore` 文件已正确配置

4. **定期轮换 Token**：定期更新 GitHub Personal Access Token

5. **使用 SSH 方式**：相比 HTTPS，SSH 方式更安全便捷

## 获取帮助

如果遇到问题：
1. 查看 GitHub 文档：https://docs.github.com/
2. 查看 Git 文档：https://git-scm.com/doc
3. 提交 Issue 到本仓库

## 快速命令汇总

```bash
# 一次性执行所有步骤（复制粘贴即可）
git add .
git commit -m "Initial commit: GLM/智谱 AI 实时用量监控插件"
git remote add origin https://github.com/safphere/glm-usage-jetbrains.git
git push -u origin main
```

---

**完成以上步骤后，您的插件就成功发布到 GitHub 了！**
