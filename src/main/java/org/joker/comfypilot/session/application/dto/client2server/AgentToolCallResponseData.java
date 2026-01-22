package org.joker.comfypilot.session.application.dto.client2server;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Agent工具调用响应数据
 * 客户端 -> 服务端
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Agent工具调用响应数据")
public class AgentToolCallResponseData implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 工具名称
     */
    @Schema(description = "工具名称", example = "readFile")
    private String toolName;

    /**
     * 工具执行结果（JSON字符串或文本）
     */
    @Schema(description = "工具执行结果", example = "{\"content\": \"file content\"}")
    private String result;

    /**
     * 用户是否允许执行工具
     */
    @Schema(description = "是否允许执行", example = "true")
    private Boolean isAllow;

    /**
     * 是否执行成功
     */
    @Schema(description = "是否执行成功", example = "true")
    private Boolean success;

    /**
     * 错误信息（如果执行失败）
     */
    @Schema(description = "错误信息", example = "File not found")
    private String error;
}
