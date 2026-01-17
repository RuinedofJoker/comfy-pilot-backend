package org.joker.comfypilot.tool.application.service;

import org.joker.comfypilot.tool.domain.enums.ToolType;
import org.joker.comfypilot.tool.domain.valueobject.ToolExecutionResult;

import java.util.Map;

/**
 * 工具执行器接口
 */
public interface ToolExecutor {

    /**
     * 执行工具（按类型）
     *
     * @param type 工具类型
     * @param parameters 工具参数
     * @return 执行结果
     */
    ToolExecutionResult execute(ToolType type, Map<String, Object> parameters);

    /**
     * 执行工具（按名称）
     *
     * @param toolName 工具名称
     * @param parameters 工具参数
     * @return 执行结果
     */
    ToolExecutionResult execute(String toolName, Map<String, Object> parameters);
}
