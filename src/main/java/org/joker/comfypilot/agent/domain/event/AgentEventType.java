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
    BEFORE_LLM_CALL("LLM调用前", BeforeLlmCallEvent.class),

    /**
     * LLM 调用后
     */
    AFTER_LLM_CALL("LLM调用后", AfterLlmCallEvent.class),

    /**
     * 消息添加前
     */
    BEFORE_MESSAGE_ADD("消息添加前", BeforeMessageAddEvent.class),

    /**
     * 消息添加后
     */
    AFTER_MESSAGE_ADD("消息添加后", AfterMessageAddEvent.class),

    /**
     * 工具调用前
     */
    BEFORE_TOOL_CALL("工具调用前", BeforeToolCallEvent.class),

    /**
     * 工具调用后
     */
    AFTER_TOOL_CALL("工具调用后", AfterToolCallEvent.class),

    /**
     * 迭代开始
     */
    ITERATION_START("迭代开始", IterationStartEvent.class),

    /**
     * 迭代结束
     */
    ITERATION_END("迭代结束", IterationEndEvent.class),

    /**
     * 流式输出片段
     */
    STREAM("流式输出", StreamEvent.class),

    /**
     * 流式输出完成
     */
    STREAM_COMPLETE("流式输出完成", StreamCompleteEvent.class),

    /**
     * 提示消息
     */
    PROMPT("提示消息", PromptEvent.class),

    /**
     * 工具调用通知
     */
    TOOL_CALL_NOTIFY("工具调用通知", ToolCallNotifyEvent.class);

    private final String description;

    /**
     * 事件类
     */
    private final Class<? extends AgentEvent> eventClass;

    AgentEventType(String description, Class<? extends AgentEvent> eventClass) {
        this.description = description;
        this.eventClass = eventClass;
    }
}
