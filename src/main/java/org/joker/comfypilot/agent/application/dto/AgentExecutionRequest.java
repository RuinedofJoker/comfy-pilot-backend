package org.joker.comfypilot.agent.application.dto;

import io.modelcontextprotocol.spec.McpSchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.joker.comfypilot.session.application.dto.client2server.UserMessageRequestData;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * Agent执行请求DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Agent执行请求")
public class AgentExecutionRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "会话ID")
    private Long sessionId;

    @Schema(description = "会话编码")
    private String sessionCode;

    @Schema(description = "用户输入内容")
    private String userMessage;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "请求ID")
    private String requestId;

    @Schema(description = "是否流式执行")
    private Boolean isStreamable;

    @Schema(description = "agent配置（json格式，格式内容是根据agentConfigDefinitions填写的）")
    private Map<String, Object> agentConfig;

    @Schema(description = "用户消息数据")
    private UserMessageRequestData userMessageData;

    /**
     * 客户端MCP工具列表
     */
    @Schema(description = "客户端MCP工具schema列表")
    private List<McpSchema.Tool> toolSchemas;
}
