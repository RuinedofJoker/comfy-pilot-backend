package org.joker.comfypilot.agent.domain.event;

import lombok.Getter;
import lombok.Setter;
import org.joker.comfypilot.agent.domain.context.AgentExecutionContext;

/**
 * 工具调用通知事件
 * 当检测到工具调用请求时触发，用于通知客户端
 */
@Getter
@Setter
public class ToolCallNotifyEvent extends AgentEvent {

    /**
     * 当前迭代次数
     */
    private final int iteration;

    /**
     * 工具名称
     */
    private final String toolName;

    /**
     * 工具参数（JSON 字符串）
     */
    private final String toolArgs;

    /**
     * 工具调用 ID
     */
    private final String toolCallId;

    public ToolCallNotifyEvent(AgentExecutionContext context, int iteration,
                               String toolName, String toolArgs, String toolCallId) {
        super(AgentEventType.TOOL_CALL_NOTIFY, context);
        this.iteration = iteration;
        this.toolName = toolName;
        this.toolArgs = toolArgs;
        this.toolCallId = toolCallId;
    }
}
