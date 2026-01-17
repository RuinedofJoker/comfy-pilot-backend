package org.joker.comfypilot.model.domain.enums;

import lombok.Getter;

/**
 * 模型能力类型枚举
 * 定义系统支持的所有模型能力类型
 */
@Getter
public enum ModelCapability {

    /**
     * 文本生成/对话（LLM）
     */
    TEXT_GENERATION("text_generation", "文本生成/对话"),

    /**
     * 向量生成（Embedding）
     */
    EMBEDDING("embedding", "向量生成"),

    /**
     * 文本分类
     */
    CLASSIFICATION("classification", "文本分类"),

    /**
     * 情感分析
     */
    SENTIMENT_ANALYSIS("sentiment_analysis", "情感分析"),

    /**
     * 实体识别
     */
    ENTITY_RECOGNITION("entity_recognition", "实体识别"),

    /**
     * 重排序
     */
    RERANK("rerank", "重排序");

    private final String code;
    private final String description;

    ModelCapability(String code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * 根据 code 获取枚举
     *
     * @param code 能力代码
     * @return 枚举值
     */
    public static ModelCapability fromCode(String code) {
        for (ModelCapability capability : values()) {
            if (capability.code.equals(code)) {
                return capability;
            }
        }
        throw new IllegalArgumentException("未知的模型能力类型: " + code);
    }
}
