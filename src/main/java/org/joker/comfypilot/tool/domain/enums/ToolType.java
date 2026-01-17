package org.joker.comfypilot.tool.domain.enums;

import lombok.Getter;

/**
 * 工具类型枚举
 */
@Getter
public enum ToolType {

    /**
     * 大语言模型工具
     */
    LLM("llm", "大语言模型工具"),

    /**
     * 向量生成工具
     */
    EMBEDDING("embedding", "向量生成工具"),

    /**
     * 文本分类工具
     */
    CLASSIFICATION("classification", "文本分类工具"),

    /**
     * 情感分析工具
     */
    SENTIMENT_ANALYSIS("sentiment_analysis", "情感分析工具"),

    /**
     * 实体识别工具
     */
    ENTITY_RECOGNITION("entity_recognition", "实体识别工具"),

    /**
     * 重排序工具
     */
    RERANK("rerank", "重排序工具");

    private final String code;
    private final String description;

    ToolType(String code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * 根据 code 获取枚举
     */
    public static ToolType fromCode(String code) {
        for (ToolType type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("未知的工具类型: " + code);
    }
}
