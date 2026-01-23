package org.joker.comfypilot.agent.domain.event;

import dev.langchain4j.agent.tool.ToolExecutionRequest;
import lombok.Getter;
import lombok.Setter;
import org.joker.comfypilot.agent.domain.context.AgentExecutionContext;

import java.util.List;

/**
 * 工具调用前事件
 * 可以修改工具调用参数或取消调用
 */
@Getter
@Setter
public class BeforeToolCallEvent extends AgentEvent {

    /**
     * 当前迭代次数
     */
    private final int iteration;

    /**
     * 工具调用请求列表（可修改）
     */
    private List<ToolExecutionRequest> toolExecutionRequests;

    public BeforeToolCallEvent(AgentExecutionContext context, int iteration,
                                List<ToolExecutionRequest> toolExecutionRequests) {
        super(AgentEventType.BEFORE_TOOL_CALL, context);
        this.iteration = iteration;
        this.toolExecutionRequests = toolExecutionRequests;
    }
}
