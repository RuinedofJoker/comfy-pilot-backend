package org.joker.comfypilot.common.domain.message;

import dev.langchain4j.data.message.SystemMessage;
import lombok.Data;

@Data
public class ChatSystemMessage implements ChatMessage {

    private String content;

    public SystemMessage toSystemMessage() {
        return SystemMessage.from(content);
    }

    public static ChatSystemMessage fromSystemMessage(SystemMessage systemMessage) {
        ChatSystemMessage chatSystemMessage = new ChatSystemMessage();
        chatSystemMessage.content = systemMessage.text();
        return chatSystemMessage;
    }

}
