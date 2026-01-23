package org.joker.comfypilot.agent.domain.event;

import dev.langchain4j.data.message.ToolExecutionResultMessage;
import lombok.Getter;
import lombok.Setter;
import org.joker.comfypilot.agent.domain.context.AgentExecutionContext;

import java.util.List;

/**
 * 工具调用后事件
 * 可以查看工具调用结果
 */
@Getter
@Setter
public class AfterToolCallEvent extends AgentEvent {

    /**
     * 当前迭代次数
     */
    private final int iteration;

    /**
     * 工具调用结果列表
     */
    private final List<ToolExecutionResultMessage> toolResults;

    public AfterToolCallEvent(AgentExecutionContext context, int iteration,
                              List<ToolExecutionResultMessage> toolResults) {
        super(AgentEventType.AFTER_TOOL_CALL, context);
        this.iteration = iteration;
        this.toolResults = toolResults;
    }
}
