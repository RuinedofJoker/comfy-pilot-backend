package org.joker.comfypilot.tool.domain.valueobject;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 工具执行元数据
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ToolExecutionMetadata {

    /**
     * 工具类型
     */
    private String toolType;

    /**
     * 工具名称
     */
    private String toolName;

    /**
     * 执行时间（毫秒）
     */
    private Long executionTimeMs;

    /**
     * 使用的模型
     */
    private String modelUsed;

    /**
     * 使用的 Token 数量
     */
    private Integer tokenUsed;

    /**
     * 输入 Token 数量
     */
    private Integer inputTokens;

    /**
     * 输出 Token 数量
     */
    private Integer outputTokens;
}
