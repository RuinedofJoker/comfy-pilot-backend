package org.joker.comfypilot.session.application.dto.client2server;

import io.modelcontextprotocol.spec.McpSchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 用户消息请求数据
 * 客户端 -> 服务端
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "用户消息请求数据")
public class UserMessageRequestData implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 当前请求ID
     */
    @Schema(description = "当前请求ID（UUID）")
    private String requestId;

    /**
     * 工具名称
     */
    @Schema(description = "工作流内容（JSON字符串）", example = "{}")
    private String workflowContent;

    /**
     * 客户端MCP工具列表
     */
    @Schema(description = "客户端MCP工具schema列表")
    private List<McpSchema.Tool> toolSchemas;

    /**
     * 是否是客户端工具
     */
    @Schema(description = "是否是客户端工具，如果不是只需要返回是否允许执行就行了", example = "true")
    private Boolean isClientTool;
}
