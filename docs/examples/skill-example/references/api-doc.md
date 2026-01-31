# 数据分析 API 文档

## 概述

本文档说明数据分析 skill 提供的主要功能接口。

## 分析函数

### basic_statistics()

执行基本统计分析。

**参数：**
- `data_file` (str): 数据文件路径（支持 .xlsx 和 .csv）

**返回值：**
```json
{
  "row_count": 100,
  "column_count": 5,
  "columns": ["date", "product", "sales", "quantity", "region"],
  "numeric_summary": {
    "sales": {
      "count": 100,
      "mean": 12500.5,
      "std": 2345.67,
      "min": 5000,
      "max": 20000
    }
  }
}
```

### trend_analysis()

分析时间序列趋势。

**参数：**
- `data_file` (str): 数据文件路径
- `date_column` (str): 日期列名
- `value_column` (str): 数值列名

**返回值：**
```json
{
  "trend": "increasing",
  "total": 1250000,
  "average": 12500,
  "max_value": 25000,
  "min_value": 5000
}
```

## 使用示例

### 示例 1：基本分析

```python
from analyze import basic_statistics

result = basic_statistics('data/sales.xlsx')
print(result)
```

### 示例 2：趋势分析

```python
from analyze import trend_analysis

result = trend_analysis('data/sales.xlsx', 'date', 'sales')
print(result)
```

## 错误码

| 代码 | 说明 | 解决方案 |
|------|------|----------|
| E001 | 文件不存在 | 检查文件路径 |
| E002 | 格式不支持 | 使用 .xlsx 或 .csv 格式 |
| E003 | 列不存在 | 检查列名是否正确 |
| E004 | 数据为空 | 确保文件包含数据 |

## 配置

在使用前，可以设置以下环境变量：

- `MAX_ROWS`: 最大处理行数（默认：100000）
- `ENCODING`: 文件编码（默认：utf-8）
- `DECIMAL_PLACES`: 小数位数（默认：2）

## 性能

- 小文件（< 1MB）：< 1 秒
- 中等文件（1-10MB）：1-5 秒
- 大文件（10-100MB）：5-30 秒

## 限制

- 最大文件大小：100MB
- 最大行数：100 万行
- 最大列数：1000 列
- 支持的文件格式：Excel (.xlsx), CSV (.csv)
