package org.joker.comfypilot.agent.domain.event;

import dev.langchain4j.data.message.ChatMessage;
import lombok.Getter;
import lombok.Setter;
import org.joker.comfypilot.agent.domain.context.AgentExecutionContext;

/**
 * 消息添加前事件
 * 可以修改消息内容或取消添加
 */
@Getter
@Setter
public class BeforeMessageAddEvent extends AgentEvent {

    /**
     * 当前迭代次数
     */
    private final int iteration;

    /**
     * 要添加的消息（可修改）
     */
    private ChatMessage message;

    /**
     * 消息类型描述
     */
    private final String messageType;

    public BeforeMessageAddEvent(AgentExecutionContext context, int iteration, ChatMessage message) {
        super(AgentEventType.BEFORE_MESSAGE_ADD, context);
        this.iteration = iteration;
        this.message = message;
        this.messageType = message.type().toString();
    }
}
