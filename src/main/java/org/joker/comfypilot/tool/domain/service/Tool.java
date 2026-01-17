package org.joker.comfypilot.tool.domain.service;

import org.joker.comfypilot.tool.domain.enums.ToolType;
import org.joker.comfypilot.tool.domain.valueobject.ToolExecutionResult;

import java.util.Map;

/**
 * 工具接口
 * 所有工具都需要实现此接口
 */
public interface Tool {

    /**
     * 获取工具类型
     *
     * @return 工具类型
     */
    ToolType getType();

    /**
     * 获取工具名称
     *
     * @return 工具名称
     */
    String getName();

    /**
     * 获取工具描述
     *
     * @return 工具描述
     */
    String getDescription();

    /**
     * 获取参数 Schema (JSON Schema 格式)
     *
     * @return 参数 Schema
     */
    Map<String, Object> getParameterSchema();

    /**
     * 执行工具
     *
     * @param parameters 工具参数
     * @return 执行结果
     */
    ToolExecutionResult execute(Map<String, Object> parameters);
}
