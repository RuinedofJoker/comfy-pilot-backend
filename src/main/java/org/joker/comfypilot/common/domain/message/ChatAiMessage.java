package org.joker.comfypilot.common.domain.message;

import com.fasterxml.jackson.annotation.JsonTypeName;
import dev.langchain4j.data.message.AiMessage;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * AI消息
 * 包含AI的回复内容和可能的工具调用请求
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonTypeName("ai")
public class ChatAiMessage implements PersistableChatMessage {

    private static final long serialVersionUID = 1L;

    /**
     * AI回复的文本内容
     */
    private String content;

    /**
     * 工具调用请求列表
     */
    private List<ToolRequest> toolRequests;

    /**
     * 转换为LangChain4j的AiMessage
     */
    public AiMessage toAiMessage() {
        AiMessage.Builder builder = AiMessage.builder().text(content);

        if (toolRequests != null && !toolRequests.isEmpty()) {
            builder.toolExecutionRequests(
                toolRequests.stream()
                    .map(ToolRequest::toToolExecutionRequest)
                    .toList()
            );
        }

        return builder.build();
    }

    /**
     * 从LangChain4j的AiMessage创建ChatAiMessage
     */
    public static ChatAiMessage from(AiMessage aiMessage) {
        List<ToolRequest> requests = null;

        if (aiMessage.hasToolExecutionRequests()) {
            requests = aiMessage.toolExecutionRequests().stream()
                    .map(ToolRequest::from)
                    .toList();
        }

        return new ChatAiMessage(aiMessage.text(), requests);
    }

}
