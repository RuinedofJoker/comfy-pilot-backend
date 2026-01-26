package org.joker.comfypilot.agent.domain.event;

import dev.langchain4j.model.chat.response.ChatResponse;
import lombok.Getter;
import lombok.Setter;
import org.joker.comfypilot.agent.domain.context.AgentExecutionContext;

/**
 * 流式输出完成事件
 * 当整个 ReAct 循环完成时触发
 */
@Getter
@Setter
public class StreamCompleteEvent extends AgentEvent {

    /**
     * 返回消息
     */
    private final ChatResponse completeResponse;

    public StreamCompleteEvent(AgentExecutionContext context, ChatResponse completeResponse) {
        super(AgentEventType.STREAM_COMPLETE, context);
        this.completeResponse = completeResponse;
    }

}
