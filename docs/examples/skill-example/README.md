# Skill 示例说明

这是一个完整的 skill 示例，演示了如何组织和配置一个实际的 skill。

## 目录结构

```
skill-example/
├── SKILL.md              # 主 skill 定义（必需）
├── README.md             # 本文件：示例说明
├── scripts/              # 脚本目录（可选）
│   └── analyze.py        # 数据分析脚本
├── references/           # 参考文档目录（可选）
│   └── api-doc.md        # API 文档
└── child-skill/          # 子 skill 目录（可选）
    └── SKILL.md          # 子 skill 定义
```

## 使用说明

### 1. 复制到 Skills 目录

将 `skill-example` 文件夹复制到配置的 skills 目录下：

```
D:/code/comfy-pilot/skills/
└── data-analysis/        # 重命名为实际的 skill 名称
    ├── SKILL.md
    ├── scripts/
    ├── references/
    └── child-skill/
```

### 2. 配置 application.yml

确保 `application.yml` 中配置了 skills 目录：

```yaml
skills:
  directories:
    - D:/code/comfy-pilot/skills
```

### 3. 启动应用

启动应用后查看日志：

```
Skills 注册完成，共注册 2 个技能，根技能数: 1
已注册的技能列表: [data-analysis, advanced-statistics]
```

### 4. Agent 使用

Agent 可以通过以下方式使用这个 skill：

```java
// 获取 skill 信息
String info = getSkillsInfo(null);  // 获取所有根 skill

// 获取子 skill
String childInfo = getSkillsInfo("data-analysis");

// 读取 SKILL.md
String path = getSkillPath("data-analysis");
String content = readSkillFile(path + "/SKILL.md");

// 读取脚本
String script = readSkillFile(path + "/scripts/analyze.py");

// 列出目录
String files = listSkillDirectory(path);
```

## 关键要点

### SKILL.md 格式

1. **YAML 前置元数据**必须用 `---` 包裹
2. **必需字段**：`name` 和 `description`
3. **可选字段**：`license`、`compatibility`、`metadata`、`allowed-tools`

### 层级关系

- 父 skill：`data-analysis`（根目录的 `SKILL.md`）
- 子 skill：`advanced-statistics`（`child-skill/SKILL.md`）
- 层级通过目录结构自动建立

### 资源组织

- `scripts/`：放置可执行脚本
- `references/`：放置参考文档
- `assets/`：放置图片、模板等资源
- `data/`：放置示例数据（自定义）

## 自定义

### 修改 Skill 名称

1. 修改目录名称：`skill-example` → `your-skill-name`
2. 修改 `SKILL.md` 中的 `name` 字段
3. 重启应用

### 添加新功能

1. 在 `scripts/` 下添加新脚本
2. 在 `SKILL.md` 中更新说明
3. 在 `references/` 中添加相关文档

### 添加子 Skill

1. 创建新的子目录
2. 在子目录中创建 `SKILL.md`
3. 重启应用，自动识别层级关系

## 注意事项

1. **这是示例代码**：脚本不能直接运行，需要安装依赖并调整代码
2. **路径分隔符**：Windows 使用 `\` 或 `/`，推荐使用 `/`
3. **文件编码**：建议使用 UTF-8
4. **名称规范**：skill 名称只能包含小写字母、数字、连字符

## 参考文档

- [Skills 快速指南](../Skills快速指南.md)
- [完整工具文档](../../src/main/java/org/joker/comfypilot/common/tool/skills/README.md)
- [Claude Skills 规范](../Claude_Skills_完整指南.md)
