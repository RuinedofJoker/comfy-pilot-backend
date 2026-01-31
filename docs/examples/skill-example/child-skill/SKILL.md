---
name: advanced-statistics
description: 高级统计分析子技能，提供回归分析、预测建模和异常检测等功能
metadata:
  author: comfy-pilot-team
  version: "1.0.0"
  parent: data-analysis
---

# 高级统计分析（子 Skill）

## 功能说明

作为 `data-analysis` skill 的子 skill，提供高级统计分析功能：

- **回归分析**：线性回归、多元回归、逻辑回归
- **预测建模**：基于历史数据的趋势预测
- **异常检测**：识别数据中的离群值和异常模式

## 使用场景

当需要进行深度数据分析时，Agent 可以调用本子 skill：

```
使用 advanced-statistics 对销售数据进行回归分析，
预测未来 3 个月的销售趋势。
```

## 主要功能

### 1. 线性回归

分析变量间的线性关系。

**输入：** 两列数据（自变量、因变量）

**输出：**
- 回归方程
- R² 值（拟合度）
- 预测结果

### 2. 趋势预测

基于历史数据预测未来值。

**输入：** 时间序列数据

**输出：**
- 未来 N 期的预测值
- 置信区间
- 预测准确度评估

### 3. 异常检测

识别数据中的异常点。

**方法：**
- Z-score 方法
- IQR（四分位距）方法
- 孤立森林算法

**输出：**
- 异常点列表
- 异常程度评分

## 配置参数

```yaml
regression:
  method: "linear"        # linear/polynomial/logistic
  degree: 1               # 多项式次数（仅用于 polynomial）

prediction:
  periods: 3              # 预测期数
  confidence: 0.95        # 置信水平

anomaly_detection:
  method: "zscore"        # zscore/iqr/isolation_forest
  threshold: 3.0          # Z-score 阈值
```

## 示例

### 回归分析示例

```python
# Agent 调用
result = perform_regression('sales.xlsx', x_col='advertising', y_col='sales')

# 输出
{
  "equation": "y = 2.5x + 100",
  "r_squared": 0.85,
  "predictions": [150, 175, 200]
}
```

### 异常检测示例

```python
# Agent 调用
anomalies = detect_anomalies('transactions.xlsx', method='zscore')

# 输出
{
  "anomaly_count": 5,
  "anomalies": [
    {"index": 45, "value": 50000, "score": 3.5},
    {"index": 78, "value": -1000, "score": -4.2}
  ]
}
```

## 与父 Skill 的关系

- **继承**：继承父 skill 的基本数据读取功能
- **扩展**：在基础分析上增加高级算法
- **独立调用**：可以单独调用，也可以作为父 skill 流程的一部分

## 性能考虑

高级分析比基本统计更耗时：

- 回归分析：2-5 秒
- 趋势预测：3-10 秒
- 异常检测：5-15 秒（取决于数据量）

## 依赖

除父 skill 的依赖外，还需要：

- scikit-learn
- statsmodels
- scipy

## 注意事项

1. 数据量建议 < 50000 行（性能考虑）
2. 确保数据质量（无缺失值、异常值已处理）
3. 回归分析前检查数据分布
4. 预测结果仅供参考，需结合业务判断
