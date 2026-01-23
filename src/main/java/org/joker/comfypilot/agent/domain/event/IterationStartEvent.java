package org.joker.comfypilot.agent.domain.event;

import lombok.Getter;
import lombok.Setter;
import org.joker.comfypilot.agent.domain.context.AgentExecutionContext;

/**
 * 迭代开始事件
 */
@Getter
@Setter
public class IterationStartEvent extends AgentEvent {

    /**
     * 当前迭代次数
     */
    private final int iteration;

    /**
     * 最大迭代次数
     */
    private final int maxIterations;

    public IterationStartEvent(AgentExecutionContext context, int iteration, int maxIterations) {
        super(AgentEventType.ITERATION_START, context);
        this.iteration = iteration;
        this.maxIterations = maxIterations;
    }
}
