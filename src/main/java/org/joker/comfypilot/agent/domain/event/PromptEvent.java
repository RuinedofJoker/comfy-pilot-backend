package org.joker.comfypilot.agent.domain.event;

import lombok.Getter;
import lombok.Setter;
import org.joker.comfypilot.agent.domain.context.AgentExecutionContext;
import org.joker.comfypilot.session.domain.enums.AgentPromptType;

/**
 * 提示消息事件
 * 用于发送 Agent 执行过程中的状态提示
 */
@Getter
@Setter
public class PromptEvent extends AgentEvent {

    /**
     * 当前迭代次数
     */
    private final int iteration;

    /**
     * 提示类型
     */
    private final AgentPromptType promptType;

    /**
     * 提示内容（可选，如果为 null 则使用默认提示）
     */
    private final String message;

    public PromptEvent(AgentExecutionContext context, int iteration, AgentPromptType promptType, String message) {
        super(AgentEventType.PROMPT, context);
        this.iteration = iteration;
        this.promptType = promptType;
        this.message = message;
    }
}
