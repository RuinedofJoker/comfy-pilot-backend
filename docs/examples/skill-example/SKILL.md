---
name: data-analysis
description: 用于分析数据文件（Excel、CSV）并生成报告的技能，支持基本统计分析和可视化
license: MIT
compatibility: python>=3.8
metadata:
  author: comfy-pilot-team
  version: "1.0.0"
  category: data-analysis
  tags: ["excel", "csv", "statistics", "visualization"]
allowed-tools: ["readSkillExcel", "readSkillPdf", "listSkillDirectory"]
---

# 数据分析 Skill

## 功能概述

这个 skill 提供数据分析能力，包括：

- 读取和解析 Excel/CSV 文件
- 执行基本统计分析（均值、中位数、方差等）
- 生成数据可视化图表
- 创建分析报告

## 目录结构

```
data-analysis/
├── SKILL.md              # 本文件：skill 定义
├── scripts/
│   ├── analyze.py        # 数据分析脚本
│   └── visualize.py      # 可视化脚本
├── references/
│   ├── api-doc.md        # API 文档
│   └── examples.xlsx     # 示例数据
└── child-skill/
    └── SKILL.md          # 子 skill：高级统计分析
```

## 使用方法

### 1. 准备数据

将需要分析的数据文件（Excel、CSV）放到 skill 目录的 `data/` 子目录下。

### 2. 调用 Agent

```
请使用 data-analysis skill 分析 sales-data.xlsx 文件，
计算月度销售趋势并生成可视化图表。
```

### 3. 查看结果

Agent 会：
1. 读取数据文件
2. 执行统计分析
3. 调用可视化脚本生成图表
4. 返回分析报告

## 可用工具

本 skill 使用以下预批准工具：

- `readSkillExcel`: 读取 Excel 文件数据
- `readSkillPdf`: 读取 PDF 格式的参考文档
- `listSkillDirectory`: 列出目录中的数据文件

## 示例

### 基本统计分析

**输入：** `sales-data.xlsx`（包含销售记录）

**操作：**
```python
# Agent 会调用 scripts/analyze.py
python scripts/analyze.py sales-data.xlsx --stats basic
```

**输出：**
```json
{
  "total_sales": 1250000,
  "average_per_month": 104166.67,
  "best_month": "2024-03",
  "growth_rate": "15.2%"
}
```

### 趋势可视化

**输入：** 分析结果数据

**操作：**
```python
# Agent 会调用 scripts/visualize.py
python scripts/visualize.py sales-data.xlsx --chart line
```

**输出：** 生成 `sales-trend.png` 折线图

## 高级功能

本 skill 包含子 skill：`advanced-statistics`（高级统计分析），提供：

- 回归分析
- 预测建模
- 异常检测

使用时调用子 skill 名称即可。

## 配置选项

可在调用时指定参数：

```yaml
options:
  delimiter: ","          # CSV 分隔符（默认：逗号）
  encoding: "utf-8"       # 文件编码（默认：UTF-8）
  chart_type: "line"      # 图表类型：line/bar/pie
  output_format: "png"    # 输出格式：png/svg/pdf
```

## 依赖项

- Python 3.8+
- pandas
- matplotlib
- openpyxl

## 注意事项

1. 数据文件大小限制：< 100MB
2. 图表生成可能需要 5-10 秒
3. 仅支持结构化数据（表格格式）
4. 确保数据文件在 skill 目录下

## 错误处理

常见错误及解决方案：

| 错误 | 原因 | 解决方案 |
|------|------|----------|
| FileNotFoundError | 数据文件不存在 | 检查文件路径和名称 |
| ValueError | 数据格式不正确 | 确认数据为表格格式 |
| MemoryError | 文件过大 | 使用分块读取或减小数据量 |

## 更新日志

- **1.0.0** (2024-01-30)
  - 初始版本
  - 支持 Excel/CSV 读取
  - 基本统计分析
  - 折线图/柱状图可视化
