package org.joker.comfypilot.tool.infrastructure.service;

import com.fasterxml.jackson.databind.JsonNode;
import dev.langchain4j.agent.tool.ToolSpecification;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.joker.comfypilot.common.config.JacksonConfig;
import org.joker.comfypilot.common.exception.BusinessException;
import org.joker.comfypilot.tool.domain.service.Tool;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

/**
 * 服务端工具实现类
 * <p>
 * {@link Tool} 接口的默认实现，封装了工具的反射调用逻辑。
 * 通过 Jackson 进行 JSON 参数解析和结果序列化，支持复杂对象类型。
 * <p>
 * <b>核心功能：</b>
 * <ul>
 *   <li>自动解析 JSON 参数为 Java 方法参数</li>
 *   <li>通过反射调用工具方法</li>
 *   <li>序列化返回值为 JSON 字符串</li>
 *   <li>完善的异常处理和日志记录</li>
 * </ul>
 * <p>
 * <b>参数解析策略：</b>
 * <ol>
 *   <li>优先使用参数名匹配（需要 {@code -parameters} 编译选项）</li>
 *   <li>如果检测到编译器生成的参数名（arg0, arg1...），降级为索引匹配</li>
 *   <li>支持基本类型、包装类型、复杂对象、集合等所有 Jackson 支持的类型</li>
 * </ol>
 * <p>
 * <b>使用示例：</b>
 * <pre>{@code
 * // 工具定义
 * @Component
 * public class UserTool {
 *     @Tool("创建用户")
 *     public User createUser(@P("用户名") String name, @P("年龄") int age) {
 *         return new User(name, age);
 *     }
 * }
 *
 * // 工具执行
 * ExecutableTool tool = new ExecutableTool(
 *     "createUser",
 *     method,
 *     userToolInstance,
 *     toolSpecification
 * );
 *
 * String result = tool.executeTool("createUser", "{\"name\":\"张三\",\"age\":25}");
 * // 返回: {"name":"张三","age":25}
 * }</pre>
 * <p>
 * <b>注意事项：</b>
 * <ul>
 *   <li>确保 Maven/Gradle 配置了 {@code <parameters>true</parameters>} 编译选项</li>
 *   <li>工具方法必须是 public 的</li>
 *   <li>参数类型必须是 Jackson 可序列化/反序列化的</li>
 *   <li>返回值类型必须是 Jackson 可序列化的</li>
 * </ul>
 *
 * @see Tool
 * @see org.joker.comfypilot.tool.infrastructure.service.ToolRegistryImpl
 */
@Slf4j
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class ServerTool implements Tool {

    /**
     * 工具名称
     * <p>
     * 从 {@code @Tool} 注解中提取，如果注解未指定则使用方法名。
     */
    private String toolName;

    /**
     * 工具对应的反射方法
     * <p>
     * 用于通过反射调用工具的实际执行逻辑。
     */
    private Method method;

    /**
     * 工具所属的 Spring Bean 实例
     * <p>
     * 工具方法需要通过此实例调用（非静态方法）。
     */
    private Object instance;

    /**
     * LangChain4j 工具规范
     * <p>
     * 包含工具的完整元数据，用于传递给 LLM。
     */
    private ToolSpecification toolSpecification;

    /**
     * {@inheritDoc}
     */
    @Override
    public String toolName() {
        return toolName;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ToolSpecification toolSpecification() {
        return toolSpecification;
    }

    /**
     * {@inheritDoc}
     * <p>
     * <b>执行流程：</b>
     * <ol>
     *   <li>记录执行开始日志</li>
     *   <li>调用 {@link #buildArguments(Method, String)} 解析 JSON 参数</li>
     *   <li>通过反射调用工具方法</li>
     *   <li>序列化返回值为 JSON 字符串</li>
     *   <li>记录执行成功日志</li>
     * </ol>
     * <p>
     * <b>异常处理：</b>
     * <ul>
     *   <li>{@link IllegalArgumentException} - 参数类型不匹配或缺少必需参数</li>
     *   <li>{@link IllegalAccessException} - 方法访问权限不足</li>
     *   <li>{@link java.lang.reflect.InvocationTargetException} - 方法执行过程中抛出异常</li>
     *   <li>{@link Exception} - 其他未预期的异常</li>
     * </ul>
     * 所有异常都会被捕获、记录日志，并转换为 {@link BusinessException} 抛出。
     *
     * @param name      工具名称（用于日志记录）
     * @param arguments JSON 格式的参数字符串，例如：{@code {"a":10,"b":20}}
     * @return JSON 格式的执行结果
     * @throws BusinessException 工具执行失败时抛出，包含详细的错误信息
     */
    @Override
    public String executeTool(String toolCallId, String name, String arguments) {
        try {
            log.debug("开始执行工具: toolCallId={} toolName={}, arguments={}", toolCallId, name, arguments);

            // 1. 构建方法参数
            Object[] args = buildArguments(method, arguments);

            // 2. 反射调用方法
            Object result = method.invoke(instance, args);

            // 3. 序列化结果
            String resultJson = JacksonConfig.getObjectMapper().writeValueAsString(result);
            log.debug("工具执行成功: toolName={}, result={}", name, resultJson);

            return resultJson;

        } catch (IllegalArgumentException e) {
            // 参数类型不匹配
            log.error("工具参数错误: toolName={}, arguments={}, error={}",
                     name, arguments, e.getMessage(), e);
            throw new BusinessException("工具参数错误: " + e.getMessage());

        } catch (IllegalAccessException e) {
            // 方法访问权限问题
            log.error("工具方法访问失败: toolName={}, error={}", name, e.getMessage(), e);
            throw new BusinessException("工具方法访问失败: " + e.getMessage());

        } catch (java.lang.reflect.InvocationTargetException e) {
            // 方法执行过程中抛出异常
            Throwable cause = e.getCause();
            log.error("工具执行异常: toolName={}, arguments={}, error={}",
                     name, arguments, cause != null ? cause.getMessage() : e.getMessage(), e);
            throw new BusinessException("工具执行失败: " +
                                      (cause != null ? cause.getMessage() : e.getMessage()));

        } catch (Exception e) {
            // 其他未预期的异常
            log.error("工具执行失败: toolName={}, arguments={}, error={}",
                     name, arguments, e.getMessage(), e);
            throw new BusinessException("工具执行失败: " + e.getMessage());
        }
    }

    /**
     * 构建方法参数数组
     * <p>
     * 将 JSON 格式的参数字符串解析为 Java 方法参数数组。
     * 支持两种参数匹配策略：
     * <ol>
     *   <li><b>参数名匹配（推荐）：</b>通过参数名从 JSON 中获取对应值</li>
     *   <li><b>索引匹配（降级）：</b>当检测到编译器生成的参数名时，通过索引获取值</li>
     * </ol>
     * <p>
     * <b>参数名匹配示例：</b>
     * <pre>{@code
     * // 方法定义
     * public int add(int a, int b) { ... }
     *
     * // JSON 参数
     * {"a": 10, "b": 20}
     *
     * // 解析结果
     * Object[] args = [10, 20]
     * }</pre>
     * <p>
     * <b>索引匹配示例（降级策略）：</b>
     * <pre>{@code
     * // 如果参数名为 arg0, arg1（未启用 -parameters）
     * // JSON 参数（数组形式）
     * [10, 20]
     *
     * // 解析结果
     * Object[] args = [10, 20]
     * }</pre>
     * <p>
     * <b>支持的参数类型：</b>
     * <ul>
     *   <li>基本类型：int, long, double, boolean 等</li>
     *   <li>包装类型：Integer, Long, Double, Boolean 等</li>
     *   <li>字符串：String</li>
     *   <li>复杂对象：任何 Jackson 可反序列化的 POJO</li>
     *   <li>集合类型：List, Set, Map 等</li>
     * </ul>
     *
     * @param method        目标方法，用于获取参数信息
     * @param argumentsJson JSON 格式的参数字符串
     * @return 方法参数数组，顺序与方法参数定义一致
     * @throws IllegalArgumentException 参数缺失或格式错误时抛出
     * @throws Exception                JSON 解析失败或类型转换失败时抛出
     */
    private Object[] buildArguments(Method method, String argumentsJson) throws Exception {
        Parameter[] parameters = method.getParameters();

        // 如果方法没有参数，直接返回空数组
        if (parameters.length == 0) {
            return new Object[0];
        }

        JsonNode root = JacksonConfig.getObjectMapper().readTree(argumentsJson);
        Object[] args = new Object[parameters.length];

        for (int i = 0; i < parameters.length; i++) {
            Parameter p = parameters[i];
            String paramName = p.getName();

            // 检查参数名是否为编译器生成的默认名称（arg0, arg1, ...）
            if (paramName.matches("arg\\d+")) {
                log.warn("检测到编译器生成的参数名: {}，建议启用 -parameters 编译选项", paramName);

                // 尝试通过索引获取参数（假设 JSON 是数组或按顺序的对象）
                JsonNode valueNode = root.get(i);
                if (valueNode == null) {
                    throw new IllegalArgumentException(
                        String.format("""
                                无法获取第 %d 个参数的值。请确保：
                                1. Maven/Gradle 配置了 -parameters 编译选项
                                2. JSON 参数格式正确""", i)
                    );
                }
                args[i] = JacksonConfig.getObjectMapper().treeToValue(valueNode, p.getType());
            } else {
                // 使用参数名从 JSON 中获取值
                JsonNode valueNode = root.get(paramName);
                if (valueNode == null) {
                    throw new IllegalArgumentException(
                        String.format("缺少参数: %s (类型: %s)", paramName, p.getType().getSimpleName())
                    );
                }
                args[i] = JacksonConfig.getObjectMapper().treeToValue(valueNode, p.getType());
            }

            log.debug("参数解析: name={}, type={}, value={}", paramName, p.getType().getSimpleName(), args[i]);
        }

        return args;
    }
}
