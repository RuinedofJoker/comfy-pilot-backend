package org.joker.comfypilot.agent.domain.event;

import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.model.chat.request.ChatRequest;
import lombok.Getter;
import lombok.Setter;
import org.joker.comfypilot.agent.domain.context.AgentExecutionContext;

import java.util.ArrayList;
import java.util.List;

/**
 * LLM 调用前事件
 * 可以修改消息列表、工具规范等
 */
@Getter
@Setter
public class BeforeLlmCallEvent extends AgentEvent {

    /**
     * 当前迭代次数
     */
    private final int iteration;

    /**
     * 原始 ChatRequest（只读）
     */
    private final ChatRequest originalRequest;

    /**
     * ChatRequest构建器
     */
    private final ChatRequest.Builder chatRequestBuilder;

    public BeforeLlmCallEvent(AgentExecutionContext context, int iteration, ChatRequest.Builder chatRequestBuilder, ChatRequest originalRequest) {
        super(AgentEventType.BEFORE_LLM_CALL, context);
        this.iteration = iteration;
        this.originalRequest = originalRequest;
        this.chatRequestBuilder = chatRequestBuilder;
    }

    /**
     * 构建修改后的 ChatRequest
     */
    public ChatRequest buildModifiedRequest() {
        return chatRequestBuilder
                .build();
    }
}
