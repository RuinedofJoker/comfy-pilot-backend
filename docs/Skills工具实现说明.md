# Skills 工具实现说明

## 实现概述

已成功在 `comfy-pilot-backend` 项目的 `org.joker.comfypilot.common.tool.skills` 包下实现了完整的 Skills 管理工具。

## 实现的功能

### 1. 配置管理 ✅

**文件：** `SkillsConfig.java`

- 使用 `@ConfigurationProperties` 读取 `application.yml` 中的配置
- 支持配置多个 skills 目录（数组形式）
- 配置路径：`skills.directories`

**配置示例：**
```yaml
skills:
  directories:
    - D:/code/comfy-pilot/skills
    - D:/code/custom-skills
```

### 2. Skill 实体类 ✅

**文件：** `Skill.java`

实现了完整的 Skill 数据结构：

- **必需字段：**
  - `name`: 技能名称
  - `description`: 技能描述
  - `skillPath`: 技能所在目录的绝对路径

- **可选字段：**
  - `license`: 许可证信息
  - `compatibility`: 兼容性要求
  - `metadata`: 自定义元数据（Map）
  - `allowedTools`: 预批准的工具列表

- **层级关系：**
  - `parent`: 父技能引用
  - `children`: 子技能列表
  - `isRoot()`: 判断是否为根技能
  - `getDepth()`: 获取层级深度

### 3. Skills 注册器 ✅

**文件：** `SkillsRegistry.java`

实现了完整的注册和管理功能：

**启动时注册：**
- 实现 `CommandLineRunner`，在应用启动时自动执行
- 扫描所有配置的 skills 目录
- 递归查找所有 `SKILL.md` 文件
- 解析 YAML 前置元数据（使用 SnakeYAML）
- 建立父子层级关系
- 检测并警告重复的技能名称

**核心方法：**
- `scanAndRegisterSkills()`: 递归扫描并注册技能
- `parseSkillMd()`: 解析 SKILL.md 文件
- `parseYamlFrontMatter()`: 解析 YAML 前置元数据
- `getSkillByName()`: 根据名称获取技能
- `getRootSkills()`: 获取所有根技能
- `getAllSkills()`: 获取所有已注册的技能
- `getConfiguredDirectories()`: 获取配置的目录列表

**数据结构：**
- `skillMap`: ConcurrentHashMap，存储所有技能（线程安全）
- `rootSkills`: 存储所有根技能
- `configuredDirectories`: 存储配置的目录（用于安全校验）

### 4. Skills 工具类 ✅

**文件：** `SkillsTools.java`

实现了所有要求的工具方法，使用 `@Tool` 注解暴露给 Agent：

#### 方法 1：getSkillsInfo ✅
获取一级 skills 或指定 skill 的直接子 skills（官方 XML 格式）

**参数：**
- `skillName`（可选）：
  - 为空时返回所有根技能
  - 不为空时返回该技能的直接子技能

**返回格式：**
```xml
<available_skills description="Skills the agent can use. Use the Read tool with the provided absolute path to fetch full contents.">
<agent_skill fullPath="D:\code\comfy-pilot\skills\analyzing-financial-statements\SKILL.md">分析财务报表并计算关键财务比率</agent_skill>
</available_skills>
```

#### 方法 2：getAllSkillsInfo ✅
递归获取所有 skills 或指定 skill 下的所有层级的 skills

**参数：**
- `skillName`（可选）：
  - 为空时返回所有技能（递归包含所有子技能）
  - 不为空时返回该技能及其所有子技能

#### 方法 3：getSkillPath ✅
根据 skill 名称获取其所在目录的绝对路径

**参数：**
- `skillName`（必需）：技能名称

**返回：** 技能目录的绝对路径字符串

#### 方法 4-7：文件系统操作方法 ✅

参考 `ServerFileSystemTools` 实现，只读操作：

- **isSkillDirectory**: 检查路径是否为目录
- **isSkillFile**: 检查路径是否为文件
- **readSkillFile**: 读取文件内容（UTF-8 编码）
- **listSkillDirectory**: 列出目录内容（返回 JSON 格式）

**安全校验：**
- 所有文件操作都会校验路径必须在配置的 skills 目录下
- 使用 `validatePathInSkillsDirectory()` 方法进行安全检查
- 路径规范化（绝对路径、处理 `.` 和 `..`）
- 不在配置目录下的访问会抛出 `SecurityException`

**文件信息格式：**
复用 `org.joker.comfypilot.common.tool.filesystem.FileInfo` 类：
```json
{
  "name": "SKILL.md",
  "path": "D:/code/comfy-pilot/skills/.../SKILL.md",
  "isDirectory": false,
  "size": 2048,
  "lastModified": 1738233600000
}
```

### 5. 配置文件更新 ✅

**文件：** `application.yml`

已添加 skills 配置部分：
```yaml
skills:
  directories:
    - D:/code/comfy-pilot/skills
```

### 6. 示例 Skills ✅

已创建示例技能用于测试：

**父技能：** `analyzing-financial-statements`
- 路径：`D:/code/comfy-pilot/skills/analyzing-financial-statements/SKILL.md`
- 描述：分析财务报表并计算关键财务比率

**子技能：** `profitability-analysis`
- 路径：`D:/code/comfy-pilot/skills/analyzing-financial-statements/sub-skill-example/SKILL.md`
- 描述：专注于盈利能力分析的子技能

### 7. 文档 ✅

已创建完整的文档：

1. **README.md**：详细的使用说明
   - 功能介绍
   - 配置方法
   - SKILL.md 格式规范
   - 使用示例
   - 故障排查
   - 技术细节

2. **本文档**：实现说明

## 技术特点

### 1. 符合官方规范

完全遵循 Agent Skills 官方规范：
- https://agentskills.io/specification
- https://agentskills.io/what-are-skills

### 2. 渐进式披露架构

支持官方的渐进式披露模式：
- 第一阶段：加载技能元数据（name, description）
- 第二阶段：按需读取完整的 SKILL.md
- 第三阶段：按需访问 scripts、references 等资源

### 3. 层级结构支持

完整支持父子技能的层级关系：
- 自动识别目录结构
- 建立父子引用关系
- 支持任意层级深度
- 提供深度查询功能

### 4. 安全性

实现了完善的安全机制：
- 路径校验（只能访问配置的目录）
- 路径规范化（防止目录遍历攻击）
- 只读操作（不支持写入、删除等操作）
- 异常处理和错误提示

### 5. 线程安全

- 使用 `ConcurrentHashMap` 存储技能映射
- 注册过程在启动时完成，之后为只读
- 所有公共方法都是线程安全的

### 6. 易于扩展

- 清晰的类结构和职责划分
- 详细的代码注释
- 便于添加新的工具方法
- 支持自定义元数据字段

## 使用流程

### 1. 配置 Skills 目录

在 `application.yml` 中配置：
```yaml
skills:
  directories:
    - D:/code/comfy-pilot/skills
```

### 2. 创建 Skill

在配置的目录下创建技能：
```
skills/
└── my-skill/
    └── SKILL.md
```

SKILL.md 格式：
```markdown
---
name: my-skill
description: 我的技能描述
---

# 技能内容
...
```

### 3. 启动应用

应用启动时会自动：
1. 扫描配置的目录
2. 解析所有 SKILL.md
3. 注册技能
4. 建立层级关系
5. 输出注册日志

启动日志示例：
```
开始扫描和注册 Skills...
扫描 Skills 目录: D:\code\comfy-pilot\skills
注册技能: name=analyzing-financial-statements, path=..., parent=无
注册技能: name=profitability-analysis, path=..., parent=analyzing-financial-statements
Skills 注册完成，共注册 2 个技能，根技能数: 1
已注册的技能列表: [analyzing-financial-statements, profitability-analysis]
```

### 4. Agent 使用

Agent 可以通过以下工具方法访问 skills：

```java
// 1. 获取所有根技能
String xml = getSkillsInfo(null);

// 2. 获取特定技能的子技能
String xml = getSkillsInfo("analyzing-financial-statements");

// 3. 递归获取所有技能
String xml = getAllSkillsInfo(null);

// 4. 获取技能路径
String path = getSkillPath("analyzing-financial-statements");

// 5. 读取 SKILL.md
String content = readSkillFile(path + "/SKILL.md");

// 6. 列出技能目录
String files = listSkillDirectory(path);
```

## 测试建议

### 1. 基本功能测试

启动应用，检查日志：
```
Skills 注册完成，共注册 X 个技能
```

### 2. 工具方法测试

可以通过 Agent 调用或者创建单元测试：

```java
@Autowired
private SkillsTools skillsTools;

@Test
public void testGetSkillsInfo() {
    String xml = skillsTools.getSkillsInfo(null);
    assertNotNull(xml);
    assertTrue(xml.contains("available_skills"));
}
```

### 3. 安全测试

测试访问非 skills 目录的文件：
```java
@Test(expected = SecurityException.class)
public void testSecurityValidation() {
    skillsTools.readSkillFile("C:/Windows/System32/config");
}
```

### 4. 层级结构测试

创建多层级的 skills，验证父子关系是否正确建立。

## 依赖说明

所有依赖都已包含在项目中，无需额外添加：

- **Spring Boot Starter**: 提供核心功能
- **SnakeYAML**: 解析 YAML（Spring Boot 自带）
- **Lombok**: 简化代码
- **LangChain4j**: 提供 @Tool 注解

## 后续优化建议

1. **性能优化**
   - 对大量 skills 的场景，可以考虑缓存机制
   - 延迟加载 SKILL.md 内容（仅在需要时读取）

2. **功能增强**
   - 支持 skills 热重载（不重启应用）
   - 支持 skills 版本管理
   - 提供 skills 验证工具（格式检查）

3. **监控和管理**
   - 提供 REST API 查询 skills 信息
   - 添加 Actuator endpoint 监控 skills 状态
   - 提供管理界面（可选）

4. **扩展功能**
   - 支持远程 skills 仓库
   - 支持 skills 包管理（类似 npm）
   - 集成 CI/CD 自动部署 skills

## 总结

已完整实现所有要求的功能：

✅ 支持在 application.yml 中配置多个 skills 目录（数组形式）
✅ 启动时自动扫描并注册所有 skills
✅ 递归查找 SKILL.md 文件
✅ 解析 YAML 前置元数据（name, description 等）
✅ 建立并维护技能的层级关系
✅ 提供获取 skills 信息的方法（官方 XML 格式）
  - 一级查询（根技能或指定技能的子技能）
  - 递归查询（所有层级）
✅ 提供根据名称获取技能路径的方法
✅ 提供文件系统操作方法（只读，带安全校验）
  - isDirectory、isFile、readFile、listDirectory
✅ 路径安全校验（只能访问配置的 skills 目录）
✅ 完整的文档和示例

系统已经可以正常使用，只需启动应用即可看到 skills 注册的日志输出。
