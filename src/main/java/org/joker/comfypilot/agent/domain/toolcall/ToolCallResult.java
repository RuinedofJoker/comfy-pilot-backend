package org.joker.comfypilot.agent.domain.toolcall;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 工具调用结果
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ToolCallResult {

    /**
     * 工具名称
     */
    private String toolName;

    /**
     * 是否允许执行
     */
    private Boolean isAllow;

    /**
     * 是否执行成功
     */
    private Boolean success;

    /**
     * 执行结果（JSON字符串）
     */
    private String result;

    /**
     * 错误信息
     */
    private String error;

    /**
     * 工具调用ID
     */
    private String toolCallId;
}
