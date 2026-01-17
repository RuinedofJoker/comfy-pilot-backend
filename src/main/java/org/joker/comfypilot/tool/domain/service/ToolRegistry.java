package org.joker.comfypilot.tool.domain.service;

import dev.langchain4j.agent.tool.ToolSpecification;

import java.util.List;

/**
 * 工具注册中心接口
 * 管理 langchain4j 工具实例（带 @Tool 注解方法的 Bean）
 */
public interface ToolRegistry {

    /**
     * 获取所有工具实例
     * 用于 Agent 构建时注入工具
     *
     * @return 所有工具实例数组
     */
    Object[] getAllTools();

    /**
     * 根据类名获取工具实例
     *
     * @param className 工具类名
     * @return 工具实例，如果不存在返回 null
     */
    Object getToolByClassName(String className);

    /**
     * 获取所有工具实例列表
     *
     * @return 所有工具实例列表
     */
    List<Object> getToolList();

    /**
     * 检查工具是否存在
     *
     * @param className 工具类名
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
     * 根据类名获取工具的 ToolSpecification 列表
     *
     * @param className 工具类名
     * @return 该工具类的所有 ToolSpecification 列表，如果不存在返回空列表
     */
    List<ToolSpecification> getToolSpecificationsByClassName(String className);

    /**
     * 根据类名和方法名获取工具的 ToolSpecification
     *
     * @param className 工具类名
     * @return 该工具类下的工具方法对应的 ToolSpecification，如果不存在返回null
     */
    ToolSpecification getToolSpecificationByMethodName(String className, String methodName);
}
