package org.joker.comfypilot.tool.domain.service;

import org.joker.comfypilot.tool.domain.enums.ToolType;

import java.util.List;

/**
 * 工具注册中心接口
 */
public interface ToolRegistry {

    /**
     * 注册工具
     *
     * @param tool 工具实例
     */
    void register(Tool tool);

    /**
     * 根据类型获取工具
     *
     * @param type 工具类型
     * @return 工具实例，如果不存在返回 null
     */
    Tool getTool(ToolType type);

    /**
     * 根据名称获取工具
     *
     * @param name 工具名称
     * @return 工具实例，如果不存在返回 null
     */
    Tool getTool(String name);

    /**
     * 获取所有工具
     *
     * @return 所有工具列表
     */
    List<Tool> getAllTools();

    /**
     * 检查工具是否存在
     *
     * @param type 工具类型
     * @return true-存在，false-不存在
     */
    boolean exists(ToolType type);

    /**
     * 检查工具是否存在
     *
     * @param name 工具名称
     * @return true-存在，false-不存在
     */
    boolean exists(String name);
}
