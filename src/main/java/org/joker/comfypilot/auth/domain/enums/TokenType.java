package org.joker.comfypilot.auth.domain.enums;

import lombok.Getter;

/**
 * Token类型枚举
 */
@Getter
public enum TokenType {

    /**
     * 访问令牌
     */
    ACCESS("ACCESS", "访问令牌"),

    /**
     * 刷新令牌
     */
    REFRESH("REFRESH", "刷新令牌");

    private final String code;
    private final String description;

    TokenType(String code, String description) {
        this.code = code;
        this.description = description;
    }
}
