package org.joker.comfypilot.common.domain.message;

import com.fasterxml.jackson.annotation.JsonTypeName;
import dev.langchain4j.data.message.ToolExecutionResultMessage;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 工具执行结果消息
 * 用于将工具执行结果返回给AI
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonTypeName("tool_execution_result")
public class ChatToolExecutionResultMessage implements PersistableChatMessage {

    private static final long serialVersionUID = 1L;

    /**
     * 工具调用请求ID（与ChatAiMessage中的toolRequest.id对应）
     */
    private String id;

    /**
     * 工具名称
     */
    private String toolName;

    /**
     * 工具执行结果（文本格式）
     */
    private String toolExecutionResult;

    /**
     * 转换为LangChain4j的ToolExecutionResultMessage
     */
    public ToolExecutionResultMessage toToolExecutionResultMessage() {
        return ToolExecutionResultMessage.from(id, toolName, toolExecutionResult);
    }

    /**
     * 从LangChain4j的ToolExecutionResultMessage创建ChatToolExecutionResultMessage
     */
    public static ChatToolExecutionResultMessage from(ToolExecutionResultMessage message) {
        return new ChatToolExecutionResultMessage(
                message.id(),
                message.toolName(),
                message.text()
        );
    }

}
