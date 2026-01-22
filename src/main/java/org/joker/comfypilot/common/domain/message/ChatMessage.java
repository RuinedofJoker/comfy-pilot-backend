package org.joker.comfypilot.common.domain.message;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.ToolExecutionResultMessage;
import dev.langchain4j.data.message.UserMessage;

public interface ChatMessage {

    static ChatMessage to(dev.langchain4j.data.message.ChatMessage message) {
        return switch (message) {
            case SystemMessage systemMessage -> ChatSystemMessage.fromSystemMessage(systemMessage);
            case UserMessage userMessage -> ChatUserMessage.fromUserMessage(userMessage);
            case AiMessage aiMessage -> ChatAiMessage.fromAiMessage(aiMessage);
            default ->
                    ChatToolExecutionResultMessage.fromToolExecutionResultMessage((ToolExecutionResultMessage) message);
        };
    }

    static dev.langchain4j.data.message.ChatMessage from(ChatMessage chatMessage) {
        return switch (chatMessage) {
            case ChatSystemMessage chatSystemMessage -> chatSystemMessage.toSystemMessage();
            case ChatUserMessage chatUserMessage -> chatUserMessage.toUserMessage();
            case ChatAiMessage chatAiMessage -> chatAiMessage.toAiMessage();
            default -> ((ChatToolExecutionResultMessage) chatMessage).toToolExecutionResultMessage();
        };
    }

    static String toJSONString(ChatMessage chatMessage) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            String json = mapper.writeValueAsString(chatMessage);
            String prefix;
            switch (chatMessage) {
                case ChatSystemMessage ignored -> prefix = "SYSTEM";
                case ChatUserMessage ignored -> prefix = "USER";
                case ChatAiMessage ignored -> prefix = "AI";
                default -> prefix = "TOOL_EXECUTION_RESULT";
            }
            return prefix + json;
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    static ChatMessage fromJSONString(String json) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            if (json == null) {
                return null;
            } else if (json.startsWith("SYSTEM")) {
                return mapper.readValue(json.substring("SYSTEM".length()), ChatSystemMessage.class);
            } else if (json.startsWith("USER")) {
                return mapper.readValue(json.substring("USER".length()), ChatUserMessage.class);
            } else if (json.startsWith("AI")) {
                return mapper.readValue(json.substring("AI".length()), ChatAiMessage.class);
            } else if (json.startsWith("TOOL_EXECUTION_RESULT")) {
                return mapper.readValue(json.substring("TOOL_EXECUTION_RESULT".length()), ChatToolExecutionResultMessage.class);
            } else {
                return null;
            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

}
