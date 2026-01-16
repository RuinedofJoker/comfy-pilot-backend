package org.joker.comfypilot.model.domain.enums;

import lombok.Getter;

/**
 * 模型类型枚举
 * 定义平台支持的各种AI模型类型
 */
@Getter
public enum ModelType {

    /**
     * 大语言模型（Large Language Model）
     * 用于文本生成、对话、问答等任务
     */
    LLM("llm", "大语言模型"),

    /**
     * 嵌入模型（Embedding Model）
     * 用于文本向量化、语义相似度计算等任务
     */
    EMBEDDING("embedding", "嵌入模型"),

    /**
     * 情感分类模型（Sentiment Classification Model）
     * 用于文本情感分析任务
     */
    SENTIMENT_CLASSIFICATION("sentiment_classification", "情感分类模型"),

    /**
     * Token分类模型（Token Classification Model）
     * 用于命名实体识别、词性标注等任务
     */
    TOKEN_CLASSIFICATION("token_classification", "Token分类模型"),

    /**
     * 图像生成模型（Image Generation Model）
     * 用于文生图、图生图等任务
     */
    IMAGE_GENERATION("image_generation", "图像生成模型"),

    /**
     * 语音识别模型（Speech Recognition Model）
     * 用于语音转文字任务
     */
    SPEECH_RECOGNITION("speech_recognition", "语音识别模型"),

    /**
     * 文本转语音模型（Text-to-Speech Model）
     * 用于文字转语音任务
     */
    TEXT_TO_SPEECH("text_to_speech", "文本转语音模型");

    /**
     * 模型类型编码
     */
    private final String code;

    /**
     * 模型类型描述
     */
    private final String description;

    ModelType(String code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * 根据编码获取枚举
     *
     * @param code 编码
     * @return 枚举值
     * @throws IllegalArgumentException 如果编码无效
     */
    public static ModelType fromCode(String code) {
        for (ModelType type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("未知的模型类型: " + code);
    }
}
