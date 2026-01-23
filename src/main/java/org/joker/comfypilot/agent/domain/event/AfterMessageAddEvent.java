package org.joker.comfypilot.agent.domain.event;

import dev.langchain4j.data.message.ChatMessage;
import lombok.Getter;
import lombok.Setter;
import org.joker.comfypilot.agent.domain.context.AgentExecutionContext;

/**
 * 消息添加后事件
 * 用于保存到数据库等后续处理
 */
@Getter
@Setter
public class AfterMessageAddEvent extends AgentEvent {

    /**
     * 当前迭代次数
     */
    private final int iteration;

    /**
     * 已添加的消息
     */
    private final ChatMessage message;

    /**
     * 消息类型描述
     */
    private final String messageType;

    /**
     * 是否添加成功
     */
    private final boolean success;

    public AfterMessageAddEvent(AgentExecutionContext context, int iteration, ChatMessage message, boolean success) {
        super(AgentEventType.AFTER_MESSAGE_ADD, context);
        this.iteration = iteration;
        this.message = message;
        this.messageType = message.type().toString();
        this.success = success;
    }
}
