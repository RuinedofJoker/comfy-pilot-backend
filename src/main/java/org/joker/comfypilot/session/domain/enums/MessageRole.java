package org.joker.comfypilot.session.domain.enums;

/**
 * 消息角色枚举
 */
public enum MessageRole {

    /**
     * 用户消息
     */
    USER("用户"),

    /**
     * AI助手消息
     */
    ASSISTANT("助手"),

    /**
     * 系统消息
     */
    SYSTEM("系统");

    private final String description;

    MessageRole(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
