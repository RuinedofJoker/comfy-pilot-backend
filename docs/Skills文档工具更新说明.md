# Skills 文档读取工具更新说明

## 📋 更新概述

在原有的 Skills 工具基础上，新增了 `SkillsDocumentTools` 类，提供对 Excel、PDF、Word 等文档格式的读取支持。

## 🆕 新增内容

### 1. Maven 依赖

在 `pom.xml` 中添加了以下依赖：

```xml
<!-- Apache POI for Excel and Word -->
<dependency>
    <groupId>org.apache.poi</groupId>
    <artifactId>poi</artifactId>
    <version>5.3.0</version>
</dependency>
<dependency>
    <groupId>org.apache.poi</groupId>
    <artifactId>poi-ooxml</artifactId>
    <version>5.3.0</version>
</dependency>

<!-- Apache PDFBox for PDF -->
<dependency>
    <groupId>org.apache.pdfbox</groupId>
    <artifactId>pdfbox</artifactId>
    <version>3.0.3</version>
</dependency>
```

### 2. 新增类文件

**文件位置：** `org.joker.comfypilot.common.tool.skills.SkillsDocumentTools`

**类说明：** 提供文档读取工具方法，所有操作都限制在配置的 skills 目录下

### 3. 新增工具方法（8个）

#### Excel 操作（3个方法）

| 方法名 | 功能 | 参数 |
|--------|------|------|
| `listExcelSheets` | 列出工作表名称 | path |
| `readSkillExcel` | 读取特定工作表 | path, sheetName |
| `readSkillExcelAllSheets` | 读取所有工作表 | path |

#### PDF 操作（3个方法）

| 方法名 | 功能 | 参数 |
|--------|------|------|
| `readSkillPdf` | 读取全部文本 | path |
| `readSkillPdfPages` | 读取指定页面 | path, startPage, endPage |
| `getSkillPdfPageCount` | 获取总页数 | path |

#### Word 操作（2个方法）

| 方法名 | 功能 | 参数 |
|--------|------|------|
| `readSkillWord` | 读取纯文本 | path |
| `readSkillWordStructured` | 读取结构化内容 | path |

### 4. 文档

新增详细的使用文档：
- `DOCUMENT_TOOLS_README.md` - 完整的使用说明

## 🎯 功能特点

### 1. 安全性

✅ **路径校验**
- 所有操作都会校验路径必须在配置的 skills 目录下
- 使用与 `SkillsTools` 相同的安全机制
- 防止目录遍历攻击

✅ **只读操作**
- 仅支持读取，不支持写入、修改、删除
- 符合 Skills 的设计理念

### 2. 灵活性

✅ **多种读取方式**
- Excel: 可以读取单个工作表或所有工作表
- PDF: 可以读取全部或指定页面
- Word: 可以读取纯文本或结构化内容

✅ **结构化输出**
- 所有数据都以 JSON 格式返回
- 便于 Agent 解析和处理

### 3. 易用性

✅ **清晰的方法命名**
- `readSkillExcel` - 一看就知道是读取 Excel
- 统一的命名风格

✅ **详细的参数说明**
- 使用 `@P` 注解提供参数描述
- Agent 可以了解每个参数的作用

✅ **友好的错误提示**
- 明确的异常信息
- 帮助快速定位问题

## 📊 使用场景示例

### 场景 1：财务分析 Skill

```
skills/financial-analysis/
├── SKILL.md
├── scripts/
│   └── calculate_ratios.py
└── data/
    ├── benchmarks.xlsx      # 行业基准数据
    │   ├── Sheet1: 盈利能力基准
    │   ├── Sheet2: 流动性基准
    │   └── Sheet3: 杠杆基准
    └── manual.pdf           # 财务分析手册
```

**Agent 使用流程：**

```java
// 1. 获取技能路径
String path = getSkillPath("financial-analysis");

// 2. 读取 Excel 基准数据
String sheets = listExcelSheets(path + "/data/benchmarks.xlsx");
String data = readSkillExcel(path + "/data/benchmarks.xlsx", "盈利能力基准");

// 3. 读取 PDF 手册
String manual = readSkillPdf(path + "/data/manual.pdf");

// 4. 结合数据和手册进行财务分析
```

### 场景 2：品牌指南 Skill

```
skills/brand-guidelines/
├── SKILL.md
├── colors/
│   └── palette.xlsx         # 色板数据
├── fonts/
│   └── typography.xlsx      # 字体规范
└── references/
    └── brand-manual.pdf     # 品牌手册（100页）
```

**Agent 使用流程：**

```java
// 1. 读取色板数据
String colors = readSkillExcel(skillPath + "/colors/palette.xlsx", null);

// 2. 分页读取品牌手册（避免一次性加载大文件）
int pages = getSkillPdfPageCount(skillPath + "/references/brand-manual.pdf");
String chapter1 = readSkillPdfPages(pdfPath, 1, 20);  // 第一章
String chapter2 = readSkillPdfPages(pdfPath, 21, 40); // 第二章
```

### 场景 3：合同模板 Skill

```
skills/contract-templates/
├── SKILL.md
├── templates/
│   ├── service-agreement.docx
│   ├── nda.docx
│   └── license.docx
└── references/
    └── legal-guide.pdf
```

**Agent 使用流程：**

```java
// 1. 读取合同模板（保留结构）
String structured = readSkillWordStructured(path + "/templates/service-agreement.docx");

// 2. 识别标题和正文
// 根据 style 字段（Heading1, Normal 等）处理不同段落

// 3. 参考法律指南
String guide = readSkillPdf(path + "/references/legal-guide.pdf");
```

## 🔧 技术实现细节

### 1. Excel 解析

**支持的格式：**
- `.xlsx` (Excel 2007+)
- `.xls` (Excel 97-2003)

**数据类型处理：**
- 字符串 → String
- 数字 → Long 或 Double（自动判断）
- 布尔值 → Boolean
- 日期 → Date String
- 公式 → 计算结果
- 空单元格 → null

**特殊处理：**
- 数字格式化（避免科学计数法）
- 日期格式识别
- 公式求值

### 2. PDF 解析

**使用库：** Apache PDFBox 3.0.3

**功能：**
- 文本提取（保留换行和基本布局）
- 分页读取（节省内存）
- 页数统计

**限制：**
- 不支持 OCR（扫描版 PDF 无法提取文字）
- 不提取图片
- 不保留复杂格式（表格、多栏等）

### 3. Word 解析

**支持的格式：**
- `.docx` (Word 2007+)
- 不支持 `.doc` (Word 97-2003)

**两种读取模式：**

1. **纯文本模式** (`readSkillWord`)
   - 只提取文本内容
   - 每个段落占一行
   - 适合简单的内容提取

2. **结构化模式** (`readSkillWordStructured`)
   - 保留段落信息
   - 包含样式（Heading1, Normal 等）
   - 包含对齐方式
   - 适合需要保留文档结构的场景

### 4. 安全机制

**路径校验逻辑：**

```java
private void validatePathInSkillsDirectory(String path) {
    Path filePath = Paths.get(path).toAbsolutePath().normalize();
    
    // 检查是否在任意配置的 skills 目录下
    boolean inSkillsDir = false;
    for (Path configuredDir : skillsRegistry.getConfiguredDirectories()) {
        if (filePath.startsWith(configuredDir)) {
            inSkillsDir = true;
            break;
        }
    }
    
    if (!inSkillsDir) {
        throw new SecurityException("安全限制：只能访问配置的 Skills 目录下的文件");
    }
}
```

**特点：**
- 路径规范化（处理 `.` 和 `..`）
- 支持多个配置目录
- 抛出明确的异常信息

## 📈 性能考虑

### 内存使用

| 操作 | 内存占用 | 建议 |
|------|---------|------|
| Excel - 单工作表 | 中等 | 适合大多数场景 |
| Excel - 所有工作表 | 高 | 谨慎使用，文件不宜太大 |
| PDF - 全部读取 | 低-中 | < 100 页可直接读取 |
| PDF - 分页读取 | 低 | 推荐用于大文件 |
| Word - 纯文本 | 低 | 推荐 |
| Word - 结构化 | 中 | 按需使用 |

### 性能优化建议

1. **Excel**
   - 优先使用 `readSkillExcel` 读取单个工作表
   - 避免频繁调用 `readSkillExcelAllSheets`
   - 大文件考虑分工作表处理

2. **PDF**
   - 大文件使用 `readSkillPdfPages` 分页读取
   - 先用 `getSkillPdfPageCount` 了解总页数
   - 每次读取 10-20 页为宜

3. **Word**
   - 优先使用 `readSkillWord`（更快）
   - 只在需要结构时使用 `readSkillWordStructured`

## 🔄 与现有工具的关系

### 工具体系

```
Skills 工具体系
│
├── SkillsConfig          # 配置管理
├── Skill                 # 实体类
├── SkillsRegistry        # 注册器
│
├── SkillsTools           # 基础工具
│   ├── getSkillsInfo()          # 获取技能信息
│   ├── getAllSkillsInfo()       # 递归获取
│   ├── getSkillPath()           # 获取路径
│   ├── readSkillFile()          # 读取文本文件
│   ├── listSkillDirectory()    # 列出目录
│   ├── isSkillFile()            # 文件检查
│   └── isSkillDirectory()       # 目录检查
│
└── SkillsDocumentTools   # 文档工具（新增）
    ├── Excel 操作
    │   ├── listExcelSheets()
    │   ├── readSkillExcel()
    │   └── readSkillExcelAllSheets()
    ├── PDF 操作
    │   ├── readSkillPdf()
    │   ├── readSkillPdfPages()
    │   └── getSkillPdfPageCount()
    └── Word 操作
        ├── readSkillWord()
        └── readSkillWordStructured()
```

### 配合使用

```java
// 标准流程：先用基础工具探索，再用文档工具读取

// 1. 基础工具：查找技能
String skills = getSkillsInfo(null);

// 2. 基础工具：获取路径
String skillPath = getSkillPath("financial-analysis");

// 3. 基础工具：查看目录内容
String files = listSkillDirectory(skillPath + "/data");

// 4. 文档工具：读取文档
String excelData = readSkillExcel(skillPath + "/data/benchmarks.xlsx", "数据");

// 5. 基础工具：读取 SKILL.md
String instructions = readSkillFile(skillPath + "/SKILL.md");
```

## ✅ 测试建议

### 单元测试

建议测试以下场景：

1. **正常读取**
   - 读取各种格式的文件
   - 验证返回数据格式

2. **边界情况**
   - 空文件
   - 单元格/段落为空
   - 特殊字符处理

3. **错误处理**
   - 文件不存在
   - 工作表不存在
   - 页码超出范围
   - 路径安全检查

4. **性能测试**
   - 大文件处理
   - 多工作表处理
   - 内存使用情况

### 集成测试

```java
@Test
public void testReadExcelFromSkill() throws Exception {
    // 1. 准备测试 skill
    // 2. 读取 Excel
    String data = skillsDocumentTools.readSkillExcel(path, null);
    // 3. 验证数据
    assertNotNull(data);
}

@Test(expected = SecurityException.class)
public void testSecurityValidation() throws Exception {
    // 测试访问非 skills 目录的文件
    skillsDocumentTools.readSkillExcel("C:/temp/test.xlsx", null);
}
```

## 📚 相关文档

- **主文档**: `README.md` - Skills 工具总体说明
- **详细文档**: `DOCUMENT_TOOLS_README.md` - 文档工具详细使用说明
- **实现说明**: `Skills工具实现说明.md` - 技术实现细节
- **官方规范**: `Claude_Skills_完整指南.md` - Agent Skills 规范

## 🎉 总结

### 新增能力

✅ **Excel 读取** - 支持多工作表、多种数据类型  
✅ **PDF 读取** - 支持全量和分页读取  
✅ **Word 读取** - 支持纯文本和结构化读取  
✅ **安全可靠** - 严格的路径校验和错误处理  
✅ **易于使用** - 清晰的 API 和详细的文档  

### 适用场景

- 📊 **数据分析**: 读取 Excel 数据进行分析
- 📄 **文档处理**: 提取 PDF/Word 内容
- 🎨 **品牌管理**: 读取品牌指南中的资源
- 💼 **合同管理**: 处理合同模板
- 📈 **财务分析**: 读取财务数据和报告

### 下一步

建议后续可以考虑：

1. **图像读取** - base64 编码，支持 PNG/JPG 等
2. **CSV 解析** - 结构化的 CSV 数据读取
3. **压缩文件** - ZIP 文件的列表和解压
4. **更多格式** - PPT、Markdown 等

当前实现已经覆盖了最常用的文档格式，可以满足大多数 Skills 的需求！
