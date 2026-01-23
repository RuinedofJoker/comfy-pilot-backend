package org.joker.comfypilot.agent.domain.event;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.model.chat.response.ChatResponse;
import lombok.Getter;
import lombok.Setter;
import org.joker.comfypilot.agent.domain.context.AgentExecutionContext;

/**
 * LLM 调用后事件
 * 可以查看 LLM 返回的消息
 */
@Getter
@Setter
public class AfterLlmCallEvent extends AgentEvent {

    /**
     * 当前迭代次数
     */
    private final int iteration;

    /**
     * LLM 响应
     */
    private final ChatResponse response;

    /**
     * AI 消息
     */
    private final AiMessage aiMessage;

    /**
     * 是否有工具调用
     */
    private final boolean hasToolCalls;

    public AfterLlmCallEvent(AgentExecutionContext context, int iteration, ChatResponse response) {
        super(AgentEventType.AFTER_LLM_CALL, context);
        this.iteration = iteration;
        this.response = response;
        this.aiMessage = response.aiMessage();
        this.hasToolCalls = aiMessage.hasToolExecutionRequests();
    }

    /**
     * 获取输入 token 数量
     */
    public Integer getInputTokenCount() {
        return response.tokenUsage() != null ? response.tokenUsage().inputTokenCount() : null;
    }

    /**
     * 获取输出 token 数量
     */
    public Integer getOutputTokenCount() {
        return response.tokenUsage() != null ? response.tokenUsage().outputTokenCount() : null;
    }

    /**
     * 获取总 token 数量
     */
    public Integer getTotalTokenCount() {
        return response.tokenUsage() != null ? response.tokenUsage().totalTokenCount() : null;
    }
}
