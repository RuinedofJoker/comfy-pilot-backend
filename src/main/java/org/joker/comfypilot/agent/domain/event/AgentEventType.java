package org.joker.comfypilot.agent.domain.event;

import lombok.Getter;

/**
 * Agent 事件类型枚举
 */
@Getter
public enum AgentEventType {

    /**
     * LLM 调用前
     */
    BEFORE_LLM_CALL("LLM调用前"),

    /**
     * LLM 调用后
     */
    AFTER_LLM_CALL("LLM调用后"),

    /**
     * 消息添加前
     */
    BEFORE_MESSAGE_ADD("消息添加前"),

    /**
     * 消息添加后
     */
    AFTER_MESSAGE_ADD("消息添加后"),

    /**
     * 工具调用前
     */
    BEFORE_TOOL_CALL("工具调用前"),

    /**
     * 工具调用后
     */
    AFTER_TOOL_CALL("工具调用后"),

    /**
     * 迭代开始
     */
    ITERATION_START("迭代开始"),

    /**
     * 迭代结束
     */
    ITERATION_END("迭代结束");

    private final String description;

    AgentEventType(String description) {
        this.description = description;
    }
}
