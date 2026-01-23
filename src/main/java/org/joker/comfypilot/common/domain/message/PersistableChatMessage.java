package org.joker.comfypilot.common.domain.message;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.data.message.*;
import org.apache.commons.lang3.StringUtils;
import org.joker.comfypilot.common.exception.BusinessException;

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

    static String toJsonString(PersistableChatMessage persistableMsg) {
        if (persistableMsg == null) {
            return "";
        }
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(persistableMsg);
        } catch (JsonProcessingException e) {
            throw new BusinessException("PersistableChatMessage序列化失败", e);
        }
    }

    static PersistableChatMessage parseFromJsonString(String jsonString) {
        if (StringUtils.isBlank(jsonString)) {
            return null;
        }
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.readValue(jsonString, PersistableChatMessage.class);
        } catch (JsonProcessingException e) {
            throw new BusinessException("PersistableChatMessage反序列化失败", e);
        }
    }

    static String toJsonString(ChatMessage message) {
        if (message == null) {
            return "";
        }

        PersistableChatMessage persistableMsg = from(message);
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(persistableMsg);
        } catch (JsonProcessingException e) {
            throw new BusinessException("PersistableChatMessage序列化失败", e);
        }
    }

    static ChatMessage parseChatMessageFromJsonString(String jsonString) {
        if (StringUtils.isBlank(jsonString)) {
            return null;
        }
        ObjectMapper mapper = new ObjectMapper();
        try {
            PersistableChatMessage persistableMsg = mapper.readValue(jsonString, PersistableChatMessage.class);
            return toLangChain4j(persistableMsg);
        } catch (JsonProcessingException e) {
            throw new BusinessException("PersistableChatMessage反序列化失败", e);
        }
    }

}
