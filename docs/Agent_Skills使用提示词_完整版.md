# Agent Skills 使用指南（完整版）

## 什么是 Skills

Skills（技能）是封装了专业知识、操作流程和可执行代码的能力包，能够让你在特定领域表现出专业水平。每个 skill 都包含：
- 详细的使用说明（SKILL.md）
- 可选的脚本代码（scripts/）
- 参考文档（REFERENCE.md）
- 数据资源（Excel、PDF、Word 等）
- 其他资源文件

## Skills 的渐进式披露原则

为了优化 token 使用，请遵循渐进式披露原则：

1. **第一阶段：发现技能** - 只加载技能的名称和描述（~100 tokens）
2. **第二阶段：读取指令** - 需要时读取完整的 SKILL.md（~500-2000 tokens）
3. **第三阶段：访问资源** - 必要时读取 scripts、references、数据文件等

**重要：** 不要一次性读取所有内容，按需加载！

---

## 可用的工具方法

### 一、基础工具（SkillsTools）

#### 1. getSkillsInfo(skillName)
获取技能信息（XML 格式）

**参数：**
- `skillName`（可选）：
  - 为空或 null：返回所有根技能
  - 指定名称：返回该技能的直接子技能

**返回格式：**
```xml
<available_skills description="...">
<agent_skill fullPath="D:\...\SKILL.md">技能描述</agent_skill>
</available_skills>
```

---

#### 2. getAllSkillsInfo(skillName)
递归获取所有层级的技能信息

**参数：** 同上，但会递归包含所有子技能

**使用建议：** 谨慎使用，会消耗较多 tokens

---

#### 3. getSkillPath(skillName)
获取技能所在目录的绝对路径

**返回：** 技能目录的绝对路径字符串

**使用场景：** 在读取技能文件前，先获取正确路径

---

#### 4. readSkillFile(path)
读取 skills 目录下的文本文件内容（SKILL.md、脚本、参考文档等）

**限制：** 只能读取配置的 skills 目录下的文件

---

#### 5. listSkillDirectory(path)
列出 skills 目录下的目录内容（JSON 格式）

**用途：** 了解技能包含哪些文件和目录

---

#### 6. isSkillFile(path) / isSkillDirectory(path)
检查路径是否为文件或目录

---

### 二、文档工具（SkillsDocumentTools）⭐ 新增

#### Excel 操作

##### 7. listExcelSheets(path)
列出 Excel 文件中的所有工作表名称

**返回：** JSON 数组
```json
["Sheet1", "数据", "图表"]
```

**使用场景：** 在读取 Excel 前，先了解有哪些工作表

---

##### 8. readSkillExcel(path, sheetName)
读取 Excel 文件的特定工作表内容

**参数：**
- `path`: Excel 文件路径
- `sheetName`: 工作表名称（可选，为空时读取第一个）

**返回：** JSON 格式
```json
{
  "sheetName": "数据",
  "totalRows": 100,
  "data": [
    ["姓名", "年龄", "城市"],
    ["张三", 25, "北京"],
    ["李四", 30, "上海"]
  ]
}
```

**数据类型：**
- 字符串 → String
- 数字 → Long（整数）或 Double（小数）
- 布尔值 → Boolean
- 日期 → Date String
- 公式 → 计算结果
- 空单元格 → null

---

##### 9. readSkillExcelAllSheets(path)
读取 Excel 文件的所有工作表

**返回：** JSON 数组，包含所有工作表数据

**注意：** 大文件慎用，建议先用 `listExcelSheets` 查看后按需读取

---

#### PDF 操作

##### 10. readSkillPdf(path)
读取 PDF 文件的全部文本内容

**返回：** 纯文本字符串

**适用：** 小于 50 页的 PDF

---

##### 11. readSkillPdfPages(path, startPage, endPage)
读取 PDF 文件的指定页面文本内容

**参数：**
- `startPage`: 起始页码（从 1 开始）
- `endPage`: 结束页码（包含）

**使用场景：** 大文件分页读取，每次读取 10-20 页

---

##### 12. getSkillPdfPageCount(path)
获取 PDF 文件的总页数

**返回：** 整数

**用途：** 在分页读取前，先了解总页数

---

#### Word 操作

##### 13. readSkillWord(path)
读取 Word 文档（.docx）的纯文本内容

**返回：** 纯文本，每个段落占一行

**适用：** 大多数场景

---

##### 14. readSkillWordStructured(path)
读取 Word 文档的结构化内容（包含段落和样式）

**返回：** JSON 格式
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

**使用场景：** 需要保留文档结构（标题层级）时

---

## 标准使用流程

### 基础流程

```
步骤 1：发现技能
↓
调用 getSkillsInfo(null) 查看所有可用的根技能
↓
步骤 2：选择技能
↓
根据用户需求选择最相关的技能
↓
步骤 3：获取技能路径
↓
调用 getSkillPath(skillName) 获取路径
↓
步骤 4：读取技能说明
↓
调用 readSkillFile(path + "/SKILL.md") 读取完整说明
↓
步骤 5：查看资源
↓
调用 listSkillDirectory(path) 查看可用资源
↓
步骤 6：访问资源（按需）
↓
- 文本文件：readSkillFile()
- Excel：listExcelSheets() → readSkillExcel()
- PDF：getSkillPdfPageCount() → readSkillPdf/Pages()
- Word：readSkillWord() 或 readSkillWordStructured()
↓
步骤 7：使用技能
↓
按照 SKILL.md 中的指令执行任务
```

---

## 实际使用示例

### 示例 1：财务分析 Skill

```python
# 1. 查看可用技能
skills = getSkillsInfo(null)
# 发现 "financial-analysis" 技能

# 2. 获取技能路径
skill_path = getSkillPath("financial-analysis")
# 返回: D:\code\comfy-pilot\skills\financial-analysis

# 3. 读取技能说明
skill_md = readSkillFile(skill_path + "/SKILL.md")
# 了解如何使用这个技能

# 4. 查看数据目录
files = listSkillDirectory(skill_path + "/data")
# 发现有 benchmarks.xlsx 文件

# 5. 查看 Excel 工作表
sheets = listExcelSheets(skill_path + "/data/benchmarks.xlsx")
# 返回: ["盈利能力基准", "流动性基准", "杠杆基准"]

# 6. 读取基准数据
profitability = readSkillExcel(
    skill_path + "/data/benchmarks.xlsx", 
    "盈利能力基准"
)

# 7. 读取参考手册
manual = readSkillPdf(skill_path + "/references/manual.pdf")

# 8. 使用数据进行财务分析
# 按照 SKILL.md 的指令，结合基准数据和手册进行分析
```

---

### 示例 2：品牌指南 Skill

```python
# 1. 获取品牌指南技能路径
skill_path = getSkillPath("brand-guidelines")

# 2. 读取技能说明
skill_md = readSkillFile(skill_path + "/SKILL.md")

# 3. 读取色板数据（Excel）
colors = readSkillExcel(skill_path + "/colors/palette.xlsx", null)
# 获取所有品牌颜色的 RGB 值

# 4. 读取字体规范（Excel）
fonts = readSkillExcel(skill_path + "/fonts/typography.xlsx", null)

# 5. 读取品牌手册（PDF 分页读取）
pdf_path = skill_path + "/references/brand-manual.pdf"
page_count = getSkillPdfPageCount(pdf_path)
print(f"品牌手册共 {page_count} 页")

# 分章节读取
logo_guide = readSkillPdfPages(pdf_path, 1, 10)      # Logo 使用指南
color_guide = readSkillPdfPages(pdf_path, 11, 20)    # 色彩规范
typo_guide = readSkillPdfPages(pdf_path, 21, 30)     # 字体规范

# 6. 应用品牌指南
# 根据读取的规范，确保用户的设计符合品牌要求
```

---

### 示例 3：合同模板 Skill

```python
# 1. 获取合同模板技能路径
skill_path = getSkillPath("contract-templates")

# 2. 查看有哪些模板
files = listSkillDirectory(skill_path + "/templates")
# 发现: service-agreement.docx, nda.docx, license.docx

# 3. 读取服务协议模板（结构化）
contract = readSkillWordStructured(
    skill_path + "/templates/service-agreement.docx"
)

# 4. 解析段落结构
for para in contract["paragraphs"]:
    if para["style"] == "Heading1":
        print(f"章节: {para['text']}")
    else:
        print(f"内容: {para['text']}")

# 5. 读取法律指南
legal_guide = readSkillPdf(skill_path + "/references/legal-guide.pdf")

# 6. 生成定制合同
# 根据模板和法律指南，为用户生成定制化的合同
```

---

## 最佳实践

### ✅ 应该做的

1. **渐进式加载**
   - 先用 `getSkillsInfo` 了解有哪些技能
   - 再用 `getSkillPath` 获取路径
   - 最后按需读取具体文件

2. **先探索再读取**
   - Excel: 先 `listExcelSheets`，再 `readSkillExcel`
   - PDF: 先 `getSkillPdfPageCount`，再决定读取方式
   - 目录: 先 `listSkillDirectory`，了解有哪些文件

3. **合理选择读取方式**
   - 小 Excel（< 10 工作表）: 可以用 `readSkillExcel` 逐个读取
   - 大 Excel（> 10 工作表）: 避免用 `readSkillExcelAllSheets`
   - 小 PDF（< 50 页）: 用 `readSkillPdf` 一次读取
   - 大 PDF（> 50 页）: 用 `readSkillPdfPages` 分页读取
   - Word: 优先用 `readSkillWord`，需要结构时用 `readSkillWordStructured`

4. **完整阅读 SKILL.md**
   - 始终先读取 SKILL.md 了解使用方法
   - 严格按照技能说明操作

5. **利用参考文档**
   - 当需要详细信息时，查阅 REFERENCE.md 或其他参考文档

---

### ❌ 不应该做的

1. **不要盲目读取**
   - 不要在不了解技能的情况下直接读取所有文件
   - 不要使用 `getAllSkillsInfo(null)` 除非真的需要查看所有技能

2. **不要过度加载**
   - 避免一次性读取大量数据
   - 避免读取不相关的文件

3. **不要猜测路径**
   - 始终使用 `getSkillPath()` 获取正确路径
   - 不要手动拼接路径

4. **不要忽略错误**
   - 注意处理 SecurityException（路径不在 skills 目录）
   - 注意处理 IOException（文件不存在、工作表不存在等）

---

## 文档格式支持说明

### Excel (.xlsx, .xls)
- ✅ 读取多个工作表
- ✅ 支持各种数据类型（字符串、数字、日期、布尔、公式）
- ✅ 自动格式化数字（避免科学计数法）
- ❌ 不支持图表、图片
- ❌ 不保留单元格格式（颜色、字体等）

### PDF (.pdf)
- ✅ 提取文本内容
- ✅ 保留基本换行和布局
- ✅ 支持分页读取
- ❌ 不支持 OCR（扫描版 PDF）
- ❌ 不提取图片
- ❌ 不保留复杂格式（表格、多栏）

### Word (.docx)
- ✅ 提取纯文本
- ✅ 提取结构化内容（段落、样式）
- ✅ 保留段落层级
- ❌ 仅支持 .docx（不支持 .doc）
- ❌ 不提取图片、表格
- ❌ 不保留复杂格式

---

## 安全限制

### 路径限制
所有文件操作都必须在配置的 skills 目录下：

```
✅ 允许：D:/code/comfy-pilot/skills/xxx/data.xlsx
❌ 禁止：C:/Windows/System32/config.xlsx
         会抛出 SecurityException
```

### 操作限制
- ✅ 只读操作（读取文件）
- ❌ 禁止写入
- ❌ 禁止删除
- ❌ 禁止修改

---

## 错误处理

### 常见错误

#### SecurityException
```
安全限制：只能访问配置的 Skills 目录下的文件
```
**解决：** 使用 `getSkillPath()` 获取正确路径

#### IOException - 文件不存在
```
Excel/PDF/Word 文件不存在: xxx
```
**解决：** 检查文件路径，使用 `listSkillDirectory()` 确认

#### IOException - 工作表不存在
```
工作表不存在: Sheet1
```
**解决：** 先用 `listExcelSheets()` 查看可用工作表

#### IllegalArgumentException - 无效页码
```
无效的页码范围: 10 - 5
```
**解决：** 确保 startPage <= endPage 且都 >= 1

---

## 性能建议

### 文件大小建议

| 文件类型 | 大小 | 建议的读取方式 |
|---------|------|---------------|
| Excel | < 1MB | `readSkillExcel` 单工作表 |
| Excel | 1-10MB | 先 `listExcelSheets` 再按需读取 |
| Excel | > 10MB | 避免 `readSkillExcelAllSheets` |
| PDF | < 50 页 | `readSkillPdf` 全部读取 |
| PDF | 50-200 页 | `readSkillPdfPages` 分页读取 |
| PDF | > 200 页 | 每次读取 10-20 页 |
| Word | 任意 | `readSkillWord` 或 `readSkillWordStructured` |

---

## 何时使用 Skills

### 适合使用 Skills 的场景

在处理用户请求前，如果任务涉及以下领域，建议先查看是否有相关技能：

1. **专业分析**
   - 财务分析、数据分析
   - 法律文档、技术规范
   - 行业研究报告

2. **数据处理**
   - 读取 Excel 数据进行计算
   - 提取 PDF 内容进行总结
   - 解析 Word 模板

3. **文档生成**
   - 基于模板生成报告
   - 套用品牌规范
   - 填充合同模板

4. **标准化流程**
   - 遵循特定的操作规范
   - 应用行业标准
   - 确保一致性

---

## 完整工作流示例

```python
# 完整的技能使用流程

# 1. 发现阶段 - 查看有哪些技能
print("查看可用技能...")
skills_xml = getSkillsInfo(null)
# 发现相关技能：financial-analysis

# 2. 准备阶段 - 获取技能信息
print("获取技能路径...")
skill_path = getSkillPath("financial-analysis")

print("读取技能说明...")
skill_md = readSkillFile(skill_path + "/SKILL.md")
# 仔细阅读，了解这个技能如何使用

print("查看技能资源...")
files = listSkillDirectory(skill_path)
# 发现有 data/ 和 references/ 目录

# 3. 数据准备阶段 - 读取所需资源
print("查看数据文件...")
data_files = listSkillDirectory(skill_path + "/data")

print("读取 Excel 数据...")
excel_path = skill_path + "/data/benchmarks.xlsx"
sheets = listExcelSheets(excel_path)
print(f"工作表列表: {sheets}")

benchmark_data = readSkillExcel(excel_path, "行业基准")

print("读取参考手册...")
pdf_path = skill_path + "/references/manual.pdf"
pages = getSkillPdfPageCount(pdf_path)
print(f"手册共 {pages} 页")

# 分章节读取
intro = readSkillPdfPages(pdf_path, 1, 5)
methods = readSkillPdfPages(pdf_path, 6, 15)

# 4. 执行阶段 - 使用技能处理任务
print("执行财务分析...")
# 根据 SKILL.md 的指令，结合读取的数据和手册
# 进行专业的财务分析

# 5. 输出阶段 - 返回结果
print("分析完成！")
# 生成分析报告
```

---

## 总结

记住三个关键原则：

1. **渐进式披露** - 先概览，再详读，最后访问资源
2. **按需加载** - 只读取你需要的内容
3. **遵循指令** - 严格按照 SKILL.md 的说明操作

Skills 是你的专业工具箱，现在它不仅包含文本说明，还能访问：
- 📊 Excel 数据表格
- 📄 PDF 参考文档
- 📝 Word 文档模板

善用这些能力，可以显著提升你在特定领域的专业表现！

---

## 快速参考

### 基础操作
```
getSkillsInfo(null)              # 查看所有技能
getSkillPath("skill-name")       # 获取技能路径
readSkillFile(path)              # 读取文本文件
listSkillDirectory(path)         # 列出目录
```

### Excel 操作
```
listExcelSheets(path)                    # 列出工作表
readSkillExcel(path, sheetName)          # 读取工作表
readSkillExcelAllSheets(path)            # 读取所有工作表
```

### PDF 操作
```
getSkillPdfPageCount(path)               # 获取页数
readSkillPdf(path)                       # 读取全部
readSkillPdfPages(path, start, end)      # 读取指定页
```

### Word 操作
```
readSkillWord(path)                      # 读取纯文本
readSkillWordStructured(path)            # 读取结构化内容
```
