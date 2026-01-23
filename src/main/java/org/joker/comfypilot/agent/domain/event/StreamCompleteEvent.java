package org.joker.comfypilot.agent.domain.event;

import lombok.Getter;
import lombok.Setter;
import org.joker.comfypilot.agent.domain.context.AgentExecutionContext;

/**
 * 流式输出完成事件
 * 当整个 ReAct 循环完成时触发
 */
@Getter
@Setter
public class StreamCompleteEvent extends AgentEvent {

    /**
     * 完整内容（可能为 null）
     */
    private final String fullContent;

    /**
     * 是否成功完成
     */
    private final boolean success;

    /**
     * 错误信息（如果失败）
     */
    private final String errorMessage;

    public StreamCompleteEvent(AgentExecutionContext context, String fullContent, boolean success, String errorMessage) {
        super(AgentEventType.STREAM_COMPLETE, context);
        this.fullContent = fullContent;
        this.success = success;
        this.errorMessage = errorMessage;
    }

    /**
     * 成功完成的构造方法
     */
    public static StreamCompleteEvent success(AgentExecutionContext context, String fullContent) {
        return new StreamCompleteEvent(context, fullContent, true, null);
    }

    /**
     * 失败的构造方法
     */
    public static StreamCompleteEvent failure(AgentExecutionContext context, String errorMessage) {
        return new StreamCompleteEvent(context, null, false, errorMessage);
    }
}
