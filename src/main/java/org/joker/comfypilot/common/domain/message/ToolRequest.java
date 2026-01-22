package org.joker.comfypilot.common.domain.message;

import dev.langchain4j.agent.tool.ToolExecutionRequest;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 工具调用请求
 * 用于封装AI发起的工具调用信息
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ToolRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 工具调用请求ID
     */
    private String id;

    /**
     * 工具名称
     */
    private String name;

    /**
     * 工具参数（JSON格式）
     */
    private String arguments;

    /**
     * 从LangChain4j的ToolExecutionRequest创建
     */
    public static ToolRequest from(ToolExecutionRequest request) {
        return new ToolRequest(
                request.id(),
                request.name(),
                request.arguments()
        );
    }

    /**
     * 转换为LangChain4j的ToolExecutionRequest
     */
    public ToolExecutionRequest toToolExecutionRequest() {
        return ToolExecutionRequest.builder()
                .id(id)
                .name(name)
                .arguments(arguments)
                .build();
    }

}
