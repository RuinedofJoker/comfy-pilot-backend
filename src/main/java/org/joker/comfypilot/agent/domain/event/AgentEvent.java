package org.joker.comfypilot.agent.domain.event;

import lombok.Data;
import org.joker.comfypilot.agent.domain.context.AgentExecutionContext;

import java.time.LocalDateTime;

/**
 * Agent 事件基类
 * 所有 Agent 事件都继承此类
 */
@Data
public abstract class AgentEvent {

    /**
     * 事件类型
     */
    private final AgentEventType eventType;

    /**
     * 事件发生时间
     */
    private final LocalDateTime timestamp;

    /**
     * 执行上下文
     */
    private final AgentExecutionContext context;

    /**
     * 是否已取消（如果为 true，则中断后续执行）
     */
    private boolean cancelled = false;

    protected AgentEvent(AgentEventType eventType, AgentExecutionContext context) {
        this.eventType = eventType;
        this.context = context;
        this.timestamp = LocalDateTime.now();
    }

    /**
     * 取消事件（中断后续执行）
     */
    public void cancel() {
        this.cancelled = true;
    }
}
