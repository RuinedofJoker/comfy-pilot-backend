package org.joker.comfypilot.tool.infrastructure.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.agent.tool.ToolSpecification;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.joker.comfypilot.common.exception.BusinessException;
import org.joker.comfypilot.tool.domain.service.Tool;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

@Slf4j
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class ExecutableTool implements Tool {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private String toolName;
    private Method method;
    private Object instance;
    private ToolSpecification toolSpecification;

    @Override
    public String toolName() {
        return toolName;
    }

    @Override
    public Method method() {
        return method;
    }

    @Override
    public Object instance() {
        return instance;
    }

    @Override
    public ToolSpecification toolSpecification() {
        return toolSpecification;
    }

    @Override
    public String executeTool(String name, String arguments) {
        try {
            log.debug("开始执行工具: toolName={}, arguments={}", name, arguments);

            // 1. 构建方法参数
            Object[] args = buildArguments(method, arguments);

            // 2. 反射调用方法
            Object result = method.invoke(instance, args);

            // 3. 序列化结果
            String resultJson = OBJECT_MAPPER.writeValueAsString(result);
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
     *
     * @param method 目标方法
     * @param argumentsJson JSON 格式的参数
     * @return 方法参数数组
     * @throws Exception 参数解析失败
     */
    private Object[] buildArguments(Method method, String argumentsJson) throws Exception {
        Parameter[] parameters = method.getParameters();

        // 如果方法没有参数，直接返回空数组
        if (parameters.length == 0) {
            return new Object[0];
        }

        JsonNode root = OBJECT_MAPPER.readTree(argumentsJson);
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
                args[i] = OBJECT_MAPPER.treeToValue(valueNode, p.getType());
            } else {
                // 使用参数名从 JSON 中获取值
                JsonNode valueNode = root.get(paramName);
                if (valueNode == null) {
                    throw new IllegalArgumentException(
                        String.format("缺少参数: %s (类型: %s)", paramName, p.getType().getSimpleName())
                    );
                }
                args[i] = OBJECT_MAPPER.treeToValue(valueNode, p.getType());
            }

            log.debug("参数解析: name={}, type={}, value={}", paramName, p.getType().getSimpleName(), args[i]);
        }

        return args;
    }
}
