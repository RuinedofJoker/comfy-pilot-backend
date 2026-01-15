package org.joker.comfypilot.session.domain.enums;

import lombok.Getter;

/**
 * 消息角色枚举
 */
@Getter
public enum MessageRole {

    /**
     * 用户
     */
    USER("USER", "用户"),

    /**
     * 助手
     */
    ASSISTANT("ASSISTANT", "助手"),

    /**
     * 系统
     */
    SYSTEM("SYSTEM", "系统");

    private final String code;
    private final String description;

    MessageRole(String code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * 根据code获取枚举
     */
    public static MessageRole fromCode(String code) {
        for (MessageRole role : values()) {
            if (role.getCode().equals(code)) {
                return role;
            }
        }
        throw new IllegalArgumentException("Invalid MessageRole code: " + code);
    }
}
