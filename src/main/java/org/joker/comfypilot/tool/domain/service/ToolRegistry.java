package org.joker.comfypilot.tool.domain.service;

import dev.langchain4j.agent.tool.ToolSpecification;

import java.util.List;

/**
 * 工具注册中心接口
 * 管理 langchain4j 工具实例（带 @Tool 注解方法的 Bean）
 */
public interface ToolRegistry {

    /**
     * 检查工具是否存在
     *
     * @param className 工具名
     * @return true-存在，false-不存在
     */
    boolean exists(String className);

    /**
     * 获取所有工具的 ToolSpecification 列表
     * 用于 Agent 需要工具规范而非实例的场景
     *
     * @return 所有工具的 ToolSpecification 列表
     */
    List<ToolSpecification> getAllToolSpecifications();

    /**
     * 根据工具名获取工具
     *
     * @param toolName 工具名
     * @return 该工具
     */
    Tool getToolByName(String toolName);

    /**
     * 根据 Class 获取该类下所有的 Tool
     *
     * @param clazz 工具类的 Class 对象
     * @return 该类下所有的 Tool 列表
     */
    List<Tool> getToolsByClass(Class<?> clazz);

}
