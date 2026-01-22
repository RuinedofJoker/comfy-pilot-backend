package org.joker.comfypilot.session.domain.enums;

import lombok.Getter;

/**
 * 消息角色枚举
 */
@Getter
public enum MessageRole {

    /**
     * 用户消息
     */
    USER("用户"),

    /**
     * Agent提示词消息，构建消息历史时与USER消息一致
     */
    AGENT_PROMPT("Agent提示词"),

    /**
     * AI助手消息
     */
    ASSISTANT("助手"),

    /**
     * 摘要，AI助手消息一致
     */
    SUMMARY("摘要"),

    /**
     * 工具执行结果消息
     */
    TOOL_EXECUTION_RESULT("工具执行结果消息"),

    /**
     * 系统消息
     */
    SYSTEM("系统"),

    ;

    private final String description;

    MessageRole(String description) {
        this.description = description;
    }

}
