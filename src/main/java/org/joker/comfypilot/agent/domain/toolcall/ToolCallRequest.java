package org.joker.comfypilot.agent.domain.toolcall;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 工具调用请求
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ToolCallRequest {

    /**
     * 工具名称
     */
    private String toolName;

    /**
     * 工具参数（JSON字符串）
     */
    private String toolArgs;

    /**
     * 是否是客户端工具
     */
    private Boolean isClientTool;

    /**
     * 工具调用ID（用于追踪）
     */
    private String toolCallId;
}
