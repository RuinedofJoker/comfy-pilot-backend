package org.joker.comfypilot.model.domain.enums;

import lombok.Getter;

/**
 * 模型提供商类型枚举
 */
@Getter
public enum ProviderType {

    /**
     * OpenAI
     */
    OPENAI("openai", "OpenAI"),

    /**
     * Anthropic
     */
    ANTHROPIC("anthropic", "Anthropic"),

    /**
     * 阿里云
     */
    ALIYUN("aliyun", "阿里云"),

    /**
     * 自定义
     */
    CUSTOM("custom", "自定义");

    private final String code;
    private final String name;

    ProviderType(String code, String name) {
        this.code = code;
        this.name = name;
    }

    /**
     * 根据code获取枚举
     *
     * @param code 代码
     * @return 枚举值
     */
    public static ProviderType fromCode(String code) {
        for (ProviderType type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("未知的提供商类型: " + code);
    }
}
