package org.joker.comfypilot.common.domain.message;

import dev.langchain4j.data.message.ToolExecutionResultMessage;
import lombok.Data;

@Data
public class ChatToolExecutionResultMessage implements ChatMessage {

    private String id;
    private String toolName;
    private String toolExecutionResult;

    public ToolExecutionResultMessage toToolExecutionResultMessage() {
        return ToolExecutionResultMessage.from(id, toolName, toolExecutionResult);
    }

    public static ChatToolExecutionResultMessage fromToolExecutionResultMessage(ToolExecutionResultMessage toolExecutionResultMessage) {
        ChatToolExecutionResultMessage chatToolExecutionResultMessage = new ChatToolExecutionResultMessage();
        chatToolExecutionResultMessage.id = toolExecutionResultMessage.id();
        chatToolExecutionResultMessage.toolName = toolExecutionResultMessage.toolName();
        chatToolExecutionResultMessage.toolExecutionResult = toolExecutionResultMessage.text();
        return chatToolExecutionResultMessage;
    }

}
