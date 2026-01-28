package org.joker.comfypilot.common.enums;

import lombok.Getter;

/**
 * 消息角色枚举
 */
@Getter
public enum MessageRole {

    /**
     * 系统消息
     */
    SYSTEM("系统"),

    /**
     * 用户消息
     */
    USER("用户"),

    /**
     * Agent提示词消息，构建消息历史时与USER消息一致，页面上不回显
     */
    AGENT_PROMPT("Agent提示词"),

    /**
     * AI助手消息
     */
    ASSISTANT("助手"),

    /**
     * AI助手提示词消息，构建消息历史时与ASSISTANT消息一致，页面上不回显
     */
    ASSISTANT_PROMPT("助手提示词"),

    /**
     * 工具执行结果消息，在带工具执行的AI助手消息时出现
     */
    TOOL_EXECUTION_RESULT("工具执行结果消息"),

    /**
     * Agent消息，不计入模型记忆，只用作页面回显
     */
    AGENT_MESSAGE("Agent消息"),

    /**
     * Agent报错，不计入模型记忆，只用作页面回显
     */
    AGENT_ERROR("Agent报错"),

    /**
     * 用户命令，不计入模型记忆，只用作页面回显
     */
    USER_ORDER("用户命令"),

    /**
     * Agent计划，不计入模型记忆，只用作页面回显
     */
    AGENT_PLAN("Agent计划"),

    /**
     * Agent状态，不计入模型记忆，只用作页面回显
     */
    AGENT_STATUS("Agent状态"),

    /**
     * 终端输出，不计入模型记忆，只用作页面回显
     */
    AGENT_TERMINAL("终端输出"),

    ;

    private final String description;

    MessageRole(String description) {
        this.description = description;
    }

}
