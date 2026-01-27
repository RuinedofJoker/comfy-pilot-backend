package org.joker.comfypilot.common.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.joker.comfypilot.agent.domain.context.AgentExecutionContext;
import org.joker.comfypilot.session.domain.enums.AgentPromptType;

/**
 * WorkflowAgent onPrompt事件
 */
@Data
@AllArgsConstructor
public class WorkflowAgentOnPromptEvent {

    /**
     * Agent执行上下文
     */
    private AgentExecutionContext executionContext;

    /**
     * Prompt类型
     */
    private AgentPromptType promptType;

    /**
     * Prompt消息内容
     */
    private String message;

}
