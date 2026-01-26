package org.joker.comfypilot.agent.domain.event;

import lombok.Getter;
import lombok.Setter;
import org.joker.comfypilot.agent.domain.context.AgentExecutionContext;

/**
 * 迭代结束事件
 */
@Getter
@Setter
public class IterationEndEvent extends AgentEvent {

    /**
     * 当前迭代次数
     */
    private final int iteration;

    /**
     * 是否有工具调用
     */
    private final boolean hasToolCalls;

    /**
     * 是否继续下一轮迭代
     */
    private final boolean willContinue;

    private final boolean isSuccess;

    private final boolean cancelled;

    private final Throwable exception;

    public IterationEndEvent(AgentExecutionContext context, int iteration,
                            boolean hasToolCalls, boolean willContinue,
                             boolean isSuccess, boolean cancelled, Throwable exception
    ) {
        super(AgentEventType.ITERATION_END, context);
        this.iteration = iteration;
        this.hasToolCalls = hasToolCalls;
        this.willContinue = willContinue;
        this.isSuccess = isSuccess;
        this.cancelled = cancelled;
        this.exception = exception;
    }
}
