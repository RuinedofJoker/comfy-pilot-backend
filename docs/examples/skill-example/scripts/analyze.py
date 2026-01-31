#!/usr/bin/env python3
"""
数据分析脚本示例

这是一个示例脚本，说明如何在 skill 中组织分析代码。
实际使用时，Agent 会读取此脚本并理解其功能。
"""

import sys
from pathlib import Path

import pandas as pd


def basic_statistics(data_file):
    """
    执行基本统计分析

    Args:
        data_file: Excel 或 CSV 文件路径

    Returns:
        dict: 统计结果
    """
    # 读取数据
    if data_file.endswith(".xlsx"):
        df = pd.read_excel(data_file)
    else:
        df = pd.read_csv(data_file)

    # 计算统计指标
    stats = {
        "row_count": len(df),
        "column_count": len(df.columns),
        "columns": list(df.columns),
        "numeric_summary": df.describe().to_dict(),
    }

    return stats


def trend_analysis(data_file, date_column, value_column):
    """
    分析时间序列趋势

    Args:
        data_file: 数据文件路径
        date_column: 日期列名
        value_column: 数值列名

    Returns:
        dict: 趋势分析结果
    """
    df = (
        pd.read_excel(data_file)
        if data_file.endswith(".xlsx")
        else pd.read_csv(data_file)
    )

    # 转换日期列
    df[date_column] = pd.to_datetime(df[date_column])
    df = df.sort_values(date_column)

    # 计算趋势
    result = {
        "trend": "increasing"
        if df[value_column].is_monotonic_increasing
        else "decreasing",
        "total": df[value_column].sum(),
        "average": df[value_column].mean(),
        "max_value": df[value_column].max(),
        "min_value": df[value_column].min(),
    }

    return result


if __name__ == "__main__":
    if len(sys.argv) < 2:
        print("Usage: python analyze.py <data_file> [--stats basic|trend]")
        sys.exit(1)

    data_file = sys.argv[1]
    mode = "basic"

    if "--stats" in sys.argv:
        mode = sys.argv[sys.argv.index("--stats") + 1]

    if mode == "basic":
        result = basic_statistics(data_file)
    elif mode == "trend":
        result = trend_analysis(data_file, "date", "value")
    else:
        print(f"Unknown mode: {mode}")
        sys.exit(1)

    import json

    print(json.dumps(result, indent=2, ensure_ascii=False))
