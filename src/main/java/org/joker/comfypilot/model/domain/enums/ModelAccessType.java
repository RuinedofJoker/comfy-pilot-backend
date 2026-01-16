package org.joker.comfypilot.model.domain.enums;

import lombok.Getter;

/**
 * 模型接入方式枚举
 */
@Getter
public enum ModelAccessType {

    /**
     * 远程API接入
     * 需要提供商信息和API密钥
     */
    REMOTE_API("remote_api", "远程API接入"),

    /**
     * 本地接入
     * 通过代码方式实现
     */
    LOCAL("local", "本地接入");

    private final String code;
    private final String description;

    ModelAccessType(String code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * 根据code获取枚举
     *
     * @param code 代码
     * @return 枚举值
     */
    public static ModelAccessType fromCode(String code) {
        for (ModelAccessType type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("未知的接入方式: " + code);
    }
}
