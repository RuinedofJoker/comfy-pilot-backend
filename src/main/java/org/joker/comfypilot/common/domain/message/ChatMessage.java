package org.joker.comfypilot.common.domain.message;

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

    static dev.langchain4j.data.message.ChatMessage from(ChatMessage message) {
        return switch (message) {
            case ChatSystemMessage chatSystemMessage -> chatSystemMessage.toSystemMessage();
            case ChatUserMessage chatUserMessage -> chatUserMessage.toUserMessage();
            case ChatAiMessage chatAiMessage -> chatAiMessage.toAiMessage();
            default -> ((ChatToolExecutionResultMessage) message).toToolExecutionResultMessage();
        };
    }

}
