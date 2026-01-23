package org.joker.comfypilot.agent.domain.event;

import lombok.Getter;
import lombok.Setter;
import org.joker.comfypilot.agent.domain.context.AgentExecutionContext;

/**
 * 流式输出事件
 * 当 LLM 输出部分内容时触发
 */
@Getter
@Setter
public class StreamEvent extends AgentEvent {

    /**
     * 当前迭代次数
     */
    private final int iteration;

    /**
     * 输出的部分内容
     */
    private final String chunk;

    public StreamEvent(AgentExecutionContext context, int iteration, String chunk) {
        super(AgentEventType.STREAM, context);
        this.iteration = iteration;
        this.chunk = chunk;
    }
}
