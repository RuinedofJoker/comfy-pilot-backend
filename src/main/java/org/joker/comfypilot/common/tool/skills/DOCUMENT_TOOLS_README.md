# Skills 文档读取工具说明

## 概述

`SkillsDocumentTools` 提供了读取 Excel、PDF、Word 文档的工具方法，用于访问 Skills 资源中的文档类型文件。

## 支持的文档格式

- **Excel**: `.xlsx`, `.xls`
- **PDF**: `.pdf`
- **Word**: `.docx`

## 可用的工具方法

### Excel 操作

#### 1. listExcelSheets
列出 Excel 文件中的所有工作表名称

**参数：**
- `path` (必需): Excel 文件的完整路径

**返回：** JSON 数组格式的工作表名称列表

**示例：**
```java
String sheets = listExcelSheets("D:/code/comfy-pilot/skills/financial-analysis/data/ratios.xlsx");
// 返回: ["财务数据","比率计算","趋势分析"]
```

---

#### 2. readSkillExcel
读取 Excel 文件的特定工作表内容

**参数：**
- `path` (必需): Excel 文件的完整路径
- `sheetName` (可选): 工作表名称，为空时读取第一个工作表

**返回：** JSON 格式的工作表数据

**返回结构：**
```json
{
  "sheetName": "Sheet1",
  "totalRows": 100,
  "data": [
    ["姓名", "年龄", "城市"],
    ["张三", 25, "北京"],
    ["李四", 30, "上海"]
  ]
}
```

**示例：**
```java
// 读取第一个工作表
String data = readSkillExcel(path, null);

// 读取指定工作表
String data = readSkillExcel(path, "财务数据");
```

**数据说明：**
- 文本单元格返回字符串
- 数字单元格返回数字（整数返回 long，小数返回 double）
- 布尔单元格返回 boolean
- 日期单元格返回日期字符串
- 公式单元格返回计算结果
- 空单元格返回 null

---

#### 3. readSkillExcelAllSheets
读取 Excel 文件的所有工作表内容

**参数：**
- `path` (必需): Excel 文件的完整路径

**返回：** JSON 数组，包含所有工作表的数据

**返回结构：**
```json
[
  {
    "sheetName": "Sheet1",
    "totalRows": 10,
    "data": [[...]]
  },
  {
    "sheetName": "Sheet2",
    "totalRows": 20,
    "data": [[...]]
  }
]
```

**使用建议：**
- 适合需要处理多个工作表的场景
- 如果文件很大，建议先用 `listExcelSheets` 查看工作表列表，再按需读取

---

### PDF 操作

#### 4. readSkillPdf
读取 PDF 文件的全部文本内容

**参数：**
- `path` (必需): PDF 文件的完整路径

**返回：** PDF 文档的文本内容（纯文本）

**示例：**
```java
String content = readSkillPdf("D:/code/comfy-pilot/skills/legal/templates/contract.pdf");
```

**注意事项：**
- 只提取文本内容，不包含图片
- 保留文本的基本布局和换行
- 对于扫描版 PDF（图片格式），无法提取文字

---

#### 5. readSkillPdfPages
读取 PDF 文件的指定页面文本内容

**参数：**
- `path` (必需): PDF 文件的完整路径
- `startPage` (必需): 起始页码（从 1 开始）
- `endPage` (必需): 结束页码（包含）

**返回：** 指定页面的文本内容

**示例：**
```java
// 读取第 1-5 页
String content = readSkillPdfPages(path, 1, 5);

// 读取第 10 页
String content = readSkillPdfPages(path, 10, 10);
```

**使用场景：**
- PDF 文件很大时，按页读取节省内存
- 只需要读取特定章节时
- 分页处理大型文档

---

#### 6. getSkillPdfPageCount
获取 PDF 文件的总页数

**参数：**
- `path` (必需): PDF 文件的完整路径

**返回：** PDF 文档的总页数（整数）

**示例：**
```java
int pageCount = getSkillPdfPageCount(path);
// 返回: 50
```

**使用场景：**
- 在分页读取前，先了解总页数
- 显示文档信息

---

### Word 操作

#### 7. readSkillWord
读取 Word 文档的纯文本内容

**参数：**
- `path` (必需): Word 文件的完整路径（仅支持 .docx）

**返回：** Word 文档的纯文本内容，每个段落占一行

**示例：**
```java
String content = readSkillWord("D:/code/comfy-pilot/skills/templates/report.docx");
```

**注意事项：**
- 仅支持 .docx 格式（Office 2007 及以后）
- 不支持旧版 .doc 格式
- 提取纯文本，不包含图片、表格格式

---

#### 8. readSkillWordStructured
读取 Word 文档的结构化内容（包含段落和样式信息）

**参数：**
- `path` (必需): Word 文件的完整路径

**返回：** JSON 格式的结构化内容

**返回结构：**
```json
{
  "totalParagraphs": 10,
  "paragraphs": [
    {
      "text": "第一章 概述",
      "style": "Heading1",
      "alignment": "LEFT"
    },
    {
      "text": "这是正文内容...",
      "style": "Normal",
      "alignment": "LEFT"
    }
  ]
}
```

**使用场景：**
- 需要保留文档结构时（标题、正文区分）
- 需要根据样式处理不同段落
- 生成目录或提取特定样式的内容

---

## 使用流程示例

### 场景 1：读取财务分析 Skill 中的 Excel 数据

```java
// 1. 获取技能路径
String skillPath = getSkillPath("financial-analysis");

// 2. 构建 Excel 文件路径
String excelPath = skillPath + "/data/benchmarks.xlsx";

// 3. 查看有哪些工作表
String sheetsJson = listExcelSheets(excelPath);
// 返回: ["行业基准","历史数据","说明"]

// 4. 读取特定工作表
String data = readSkillExcel(excelPath, "行业基准");

// 5. 解析并使用数据
// 根据返回的 JSON 数据进行财务分析
```

---

### 场景 2：读取品牌指南 Skill 中的 PDF 文档

```java
// 1. 获取技能路径
String skillPath = getSkillPath("brand-guidelines");

// 2. 构建 PDF 文件路径
String pdfPath = skillPath + "/references/brand-manual.pdf";

// 3. 先检查页数
int totalPages = getSkillPdfPageCount(pdfPath);
System.out.println("总共 " + totalPages + " 页");

// 4. 分页读取（避免一次性加载大文件）
String chapter1 = readSkillPdfPages(pdfPath, 1, 10);
String chapter2 = readSkillPdfPages(pdfPath, 11, 20);

// 或者读取全部
String fullContent = readSkillPdf(pdfPath);
```

---

### 场景 3：读取合同模板 Skill 中的 Word 文档

```java
// 1. 获取技能路径
String skillPath = getSkillPath("contract-templates");

// 2. 构建 Word 文件路径
String wordPath = skillPath + "/templates/service-agreement.docx";

// 3. 读取结构化内容（保留标题层级）
String structured = readSkillWordStructured(wordPath);

// 4. 或读取纯文本
String plainText = readSkillWord(wordPath);
```

---

## 安全限制

所有文档读取工具都有严格的安全限制：

### ✅ 允许的操作
- 读取配置的 skills 目录下的文档
- 提取文本内容
- 解析结构化数据

### ❌ 禁止的操作
- 访问 skills 目录之外的文件（会抛出 `SecurityException`）
- 修改文件内容
- 创建或删除文件

### 路径校验
所有方法都会自动进行路径校验：
```java
// ✅ 正确 - 在 skills 目录下
readSkillExcel("D:/code/comfy-pilot/skills/data/file.xlsx");

// ❌ 错误 - 不在 skills 目录下
readSkillExcel("C:/Windows/System32/config.xlsx");
// 抛出 SecurityException
```

---

## 性能建议

### Excel 文件
- **小文件（< 1MB）**: 可以直接用 `readSkillExcel` 读取
- **中等文件（1-10MB）**: 先用 `listExcelSheets` 查看，再按需读取特定工作表
- **大文件（> 10MB）**: 避免使用 `readSkillExcelAllSheets`，逐个工作表读取

### PDF 文件
- **小文件（< 50 页）**: 可以直接用 `readSkillPdf` 读取全部
- **大文件（> 50 页）**: 使用 `readSkillPdfPages` 分页读取
- **超大文件（> 200 页）**: 建议每次读取 10-20 页

### Word 文件
- **一般情况**: 使用 `readSkillWord` 读取纯文本即可
- **需要结构**: 使用 `readSkillWordStructured`，但会增加处理时间

---

## 错误处理

### 常见错误

#### 1. SecurityException
```
安全限制：只能访问配置的 Skills 目录下的文件
```
**原因：** 尝试访问不在 skills 目录下的文件
**解决：** 使用 `getSkillPath()` 获取正确路径

#### 2. IOException - 文件不存在
```
Excel/PDF/Word 文件不存在: xxx
```
**原因：** 文件路径错误或文件已被删除
**解决：** 检查文件路径，使用 `listSkillDirectory()` 确认文件存在

#### 3. IOException - 工作表不存在
```
工作表不存在: Sheet1
```
**原因：** Excel 文件中没有指定名称的工作表
**解决：** 先用 `listExcelSheets()` 查看可用的工作表

#### 4. IllegalArgumentException - 无效的页码范围
```
无效的页码范围: 10 - 5
```
**原因：** PDF 页码范围错误（起始页大于结束页，或小于 1）
**解决：** 检查页码参数，确保 startPage <= endPage 且都 >= 1

---

## 最佳实践

### 1. 渐进式读取
```java
// ✅ 好的做法：先了解文件结构
String sheets = listExcelSheets(path);
// 然后选择需要的工作表
String data = readSkillExcel(path, "数据表");

// ❌ 不好的做法：直接读取所有
String allData = readSkillExcelAllSheets(path); // 可能很大
```

### 2. 合理使用缓存
```java
// 如果需要多次访问同一文件，考虑缓存结果
String content = readSkillPdf(path);
// 保存 content，后续直接使用，避免重复读取
```

### 3. 错误处理
```java
try {
    String data = readSkillExcel(path, sheetName);
    // 处理数据
} catch (SecurityException e) {
    // 路径安全问题
    log.error("访问受限: {}", e.getMessage());
} catch (IOException e) {
    // 文件读取问题
    log.error("文件读取失败: {}", e.getMessage());
}
```

### 4. 文件大小检查
```java
// 在读取前，先检查文件大小
File file = new File(path);
long sizeInMB = file.length() / (1024 * 1024);

if (sizeInMB > 10) {
    // 大文件，使用分页读取
    int pageCount = getSkillPdfPageCount(path);
    // 分批处理
} else {
    // 小文件，直接读取
    String content = readSkillPdf(path);
}
```

---

## 依赖说明

文档读取功能依赖以下库（已添加到 pom.xml）：

- **Apache POI 5.3.0**: 读取 Excel 和 Word
- **Apache PDFBox 3.0.3**: 读取 PDF

这些依赖在应用启动时自动加载，无需额外配置。

---

## 与其他 Skills 工具配合使用

```java
// 完整的工作流示例

// 1. 查看可用技能
String skills = getSkillsInfo(null);

// 2. 获取特定技能路径
String skillPath = getSkillPath("financial-analysis");

// 3. 列出技能目录内容
String files = listSkillDirectory(skillPath + "/data");

// 4. 读取文档
String excelData = readSkillExcel(skillPath + "/data/benchmarks.xlsx", "数据");
String pdfContent = readSkillPdf(skillPath + "/references/manual.pdf");

// 5. 读取 SKILL.md 了解如何使用这些数据
String skillMd = readSkillFile(skillPath + "/SKILL.md");
```

---

## 总结

文档读取工具提供了强大的文档处理能力：

✅ **支持常用格式**: Excel、PDF、Word  
✅ **安全可靠**: 严格的路径校验  
✅ **灵活读取**: 支持全量和分页读取  
✅ **结构化输出**: JSON 格式，易于处理  
✅ **详细错误信息**: 便于调试和问题定位  

合理使用这些工具，可以大大增强 Skills 的能力！
