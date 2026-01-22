package org.joker.comfypilot.common.domain.message;

import dev.langchain4j.agent.tool.ToolExecutionRequest;
import dev.langchain4j.data.message.AiMessage;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ChatAiMessage implements ChatMessage {

    private String content;

    private List<String> toolRequests;

    public AiMessage toAiMessage() {
        AiMessage.Builder builder = AiMessage.builder()
                .text(content);
        if (toolRequests != null && !toolRequests.isEmpty()) {
            List<ToolExecutionRequest> toolExecutionRequests = new ArrayList<>(toolRequests.size() / 3);
            for (int i = 0; i < toolRequests.size(); i+=3) {
                ToolExecutionRequest toolExecutionRequest = ToolExecutionRequest.builder()
                        .id(toolRequests.get(i))
                        .name(toolRequests.get(i + 1))
                        .arguments(toolRequests.get(i + 2))
                        .build();
                toolExecutionRequests.add(toolExecutionRequest);
            }
            builder.toolExecutionRequests(toolExecutionRequests);
        }
        return builder.build();
    }

    public static ChatAiMessage fromAiMessage(AiMessage aiMessage) {
        ChatAiMessage chatAiMessage = new ChatAiMessage();
        chatAiMessage.content = aiMessage.text();
        if (aiMessage.hasToolExecutionRequests()) {
            List<String> toolExecutionRequests = new ArrayList<>(aiMessage.toolExecutionRequests().size() * 3);
            aiMessage.toolExecutionRequests().forEach(toolExecutionRequest -> {
                toolExecutionRequests.add(toolExecutionRequest.id());
                toolExecutionRequests.add(toolExecutionRequest.name());
                toolExecutionRequests.add(toolExecutionRequest.arguments());
            });
            chatAiMessage.toolRequests = toolExecutionRequests;
        }
        return chatAiMessage;
    }

}
