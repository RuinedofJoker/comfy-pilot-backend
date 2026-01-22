package org.joker.comfypilot.tool.domain.service;

import dev.langchain4j.agent.tool.ToolSpecification;
import org.joker.comfypilot.tool.infrastructure.service.ClientTool;
import org.joker.comfypilot.tool.infrastructure.service.ServerTool;

import java.lang.reflect.Method;

/**
 * 工具接口
 * <p>
 * 定义了 Agent 可调用的工具的统一抽象。每个工具实例封装了：
 * <ul>
 *   <li>工具的元数据（名称、规范）</li>
 *   <li>工具的执行逻辑（反射方法、Bean 实例）</li>
 *   <li>工具的调用接口（executeTool）</li>
 * </ul>
 * <p>
 * 工具通过 {@link dev.langchain4j.agent.tool.Tool @Tool} 注解标记在 Spring Bean 的方法上，
 * 由 {@link org.joker.comfypilot.tool.infrastructure.service.ToolRegistryImpl ToolRegistry} 自动扫描注册。
 * <p>
 * <b>使用示例：</b>
 * <pre>{@code
 * // 1. 定义工具
 * @Component
 * public class CalculatorTool {
 *     @Tool("计算两个数的和")
 *     public int add(@P("第一个数") int a, @P("第二个数") int b) {
 *         return a + b;
 *     }
 * }
 *
 * // 2. 获取并执行工具
 * Tool tool = toolRegistry.getToolByName("add");
 * String result = tool.executeTool("add", "{\"a\":10,\"b\":20}");
 * // 返回: "30"
 * }</pre>
 *
 * @see ServerTool
 * @see org.joker.comfypilot.tool.infrastructure.service.ToolRegistryImpl
 */
public interface Tool {

    String SERVER_TOOL_PREFIX = "comfy_pilot_server_tool_";

    /**
     * 获取工具名称
     * <p>
     * 工具名称由 LangChain4j 从 {@code @Tool} 注解中提取，
     * 如果注解未指定名称，则使用方法名。
     *
     * @return 工具名称
     */
    String toolName();

    /**
     * 获取工具的 LangChain4j 规范
     * <p>
     * {@link ToolSpecification} 包含工具的完整元数据：
     * <ul>
     *   <li>工具名称</li>
     *   <li>工具描述</li>
     *   <li>参数定义（名称、类型、描述、是否必需）</li>
     * </ul>
     * 这些信息会传递给 LLM，帮助其决定何时调用哪个工具。
     *
     * @return LangChain4j 工具规范
     */
    ToolSpecification toolSpecification();

    /**
     * 执行工具
     * <p>
     * 通过反射调用工具方法，并返回 JSON 格式的执行结果。
     * <p>
     * <b>执行流程：</b>
     * <ol>
     *   <li>解析 JSON 参数为 Java 对象数组</li>
     *   <li>通过反射调用工具方法</li>
     *   <li>将返回值序列化为 JSON 字符串</li>
     * </ol>
     * <p>
     * <b>参数格式：</b>
     * <pre>{@code
     * // 示例 1：简单参数
     * {"a": 10, "b": 20}
     *
     * // 示例 2：复杂对象
     * {"user": {"name": "张三", "age": 25}, "action": "create"}
     * }</pre>
     *
     * @param name      工具名称（用于日志记录）
     * @param arguments JSON 格式的参数字符串
     * @return JSON 格式的执行结果
     * @throws org.joker.comfypilot.common.exception.BusinessException 工具执行失败时抛出
     */
    String executeTool(String name, String arguments);

    default boolean isServerTool() {
        return ServerTool.class.equals(getClass());
    }

    default boolean isClientTool() {
        return ClientTool.class.equals(getClass());
    }

}
