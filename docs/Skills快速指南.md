# Skills 快速指南

## 什么是 Skill

Skill 是可复用的 AI 能力模块，通过 `SKILL.md` 文件定义，可包含脚本、文档等资源。

## 快速开始

### 1. 配置目录

在 `application.yml` 中配置 skill 目录：

```yaml
skills:
  directories:
    - D:/code/comfy-pilot/skills
    - D:/code/custom-skills  # 可配置多个
```

### 2. 创建 Skill

在配置的目录下创建 skill 文件夹和 `SKILL.md`：

```
skills/
└── my-skill/
    ├── SKILL.md          # 必需：skill 定义
    ├── scripts/          # 可选：脚本文件
    ├── references/       # 可选：参考文档
    └── assets/           # 可选：资源文件
```

### 3. 编写 SKILL.md

```markdown
---
name: my-skill
description: 这个 skill 的功能描述（必需，最多 1024 字符）
license: MIT
metadata:
  author: your-name
  version: "1.0"
---

# My Skill

## 功能说明
详细描述 skill 的能力和使用场景。

## 使用方法
说明如何使用这个 skill。

## 示例
提供具体的使用示例。
```

**必需字段：**
- `name`: 小写字母、数字、连字符，1-64 字符
- `description`: 1-1024 字符

### 4. 启动应用

应用启动时自动扫描和注册所有 skill，查看日志确认：

```
Skills 注册完成，共注册 2 个技能，根技能数: 1
已注册的技能列表: [my-skill, another-skill]
```

## Skill 层级

支持嵌套结构，子目录的 skill 自动成为父 skill 的子 skill：

```
skills/
└── parent-skill/
    ├── SKILL.md           # 父 skill
    ├── child-skill-1/
    │   └── SKILL.md       # 子 skill
    └── child-skill-2/
        └── SKILL.md       # 子 skill
```

## Agent 使用

Agent 可通过工具方法访问 skill：

```java
// 获取所有根 skill 信息（XML 格式）
getSkillsInfo(null)

// 获取指定 skill 的子 skill
getSkillsInfo("parent-skill")

// 获取 skill 路径
getSkillPath("my-skill")

// 读取 skill 文件
readSkillFile("path/to/SKILL.md")

// 列出 skill 目录内容
listSkillDirectory("path/to/skill")
```

## 资源文件支持

Skills 工具支持读取多种文档类型：

### Excel 文件
```java
// 列出工作表
listExcelSheets("path/to/file.xlsx")

// 读取指定工作表
readSkillExcel("path/to/file.xlsx", "Sheet1")

// 读取所有工作表
readSkillExcelAllSheets("path/to/file.xlsx")
```

### PDF 文件
```java
// 读取全部文本
readSkillPdf("path/to/file.pdf")

// 读取指定页
readSkillPdfPages("path/to/file.pdf", 1, 5)

// 获取页数
getSkillPdfPageCount("path/to/file.pdf")
```

### Word 文件
```java
// 读取纯文本
readSkillWord("path/to/file.docx")

// 读取结构化内容
readSkillWordStructured("path/to/file.docx")
```

## 安全限制

所有文件操作仅限配置的 skill 目录，访问其他目录会抛出 `SecurityException`。

## 常见问题

**Q: Skill 未注册？**
- 检查 `application.yml` 配置
- 确认目录存在且包含 `SKILL.md`
- 查看启动日志中的警告

**Q: YAML 解析失败？**
- 确认 `---` 包裹 YAML 前置元数据
- 确认包含必需字段 `name` 和 `description`
- 检查 YAML 语法是否正确

**Q: 安全异常？**
- 确认访问路径在配置的 skill 目录下
- 使用 `getSkillPath()` 获取正确路径

## 参考

- 完整文档：`docs/Claude_Skills_完整指南.md`
- 工具文档：`common/tool/skills/README.md`
- 示例：`docs/examples/skill-example/`
