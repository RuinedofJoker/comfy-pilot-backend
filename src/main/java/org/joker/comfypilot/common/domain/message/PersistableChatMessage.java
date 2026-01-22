package org.joker.comfypilot.common.domain.message;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import dev.langchain4j.data.message.*;

import java.io.Serializable;

/**
 * 可持久化的聊天消息接口
 * 用于在持久化格式和LangChain4j格式之间转换
 *
 * <p>支持的消息类型：
 * <ul>
 *   <li>ChatSystemMessage - 系统消息（设置AI角色和行为规则）</li>
 *   <li>ChatUserMessage - 用户消息（支持多模态内容）</li>
 *   <li>ChatAiMessage - AI消息（包含回复和工具调用请求）</li>
 *   <li>ChatToolExecutionResultMessage - 工具执行结果消息</li>
 * </ul>
 */
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type"
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = ChatSystemMessage.class, name = "system"),
        @JsonSubTypes.Type(value = ChatUserMessage.class, name = "user"),
        @JsonSubTypes.Type(value = ChatAiMessage.class, name = "ai"),
        @JsonSubTypes.Type(value = ChatToolExecutionResultMessage.class, name = "tool_execution_result")
})
public interface PersistableChatMessage extends Serializable {

    /**
     * 从LangChain4j的ChatMessage创建对应的PersistableChatMessage实现
     *
     * @param message LangChain4j的消息对象
     * @return 对应的PersistableChatMessage实现
     */
    static PersistableChatMessage from(ChatMessage message) {
        return switch (message) {
            case SystemMessage systemMessage -> ChatSystemMessage.from(systemMessage);
            case UserMessage userMessage -> ChatUserMessage.from(userMessage);
            case AiMessage aiMessage -> ChatAiMessage.from(aiMessage);
            case ToolExecutionResultMessage toolMessage -> ChatToolExecutionResultMessage.from(toolMessage);
            default -> throw new IllegalArgumentException("不支持的消息类型: " + message.getClass());
        };
    }

    /**
     * 转换为LangChain4j的ChatMessage
     *
     * @param message 可持久化的消息对象
     * @return LangChain4j的消息对象
     */
    static ChatMessage toLangChain4j(PersistableChatMessage message) {
        return switch (message) {
            case ChatSystemMessage systemMessage -> systemMessage.toSystemMessage();
            case ChatUserMessage userMessage -> userMessage.toUserMessage();
            case ChatAiMessage aiMessage -> aiMessage.toAiMessage();
            case ChatToolExecutionResultMessage toolMessage -> toolMessage.toToolExecutionResultMessage();
            default -> throw new IllegalArgumentException("不支持的消息类型: " + message.getClass());
        };
    }

}
