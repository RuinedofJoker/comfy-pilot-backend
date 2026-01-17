package org.joker.comfypilot.tool.domain.valueobject;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 工具执行结果
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ToolExecutionResult {

    /**
     * 是否执行成功
     */
    private boolean success;

    /**
     * 执行结果数据
     */
    private Map<String, Object> data;

    /**
     * 错误信息（如果失败）
     */
    private String errorMessage;

    /**
     * 执行元数据
     */
    private ToolExecutionMetadata metadata;

    /**
     * 创建成功结果
     */
    public static ToolExecutionResult success(Map<String, Object> data, ToolExecutionMetadata metadata) {
        return ToolExecutionResult.builder()
                .success(true)
                .data(data)
                .metadata(metadata)
                .build();
    }

    /**
     * 创建失败结果
     */
    public static ToolExecutionResult failure(String errorMessage) {
        return ToolExecutionResult.builder()
                .success(false)
                .errorMessage(errorMessage)
                .build();
    }
}
