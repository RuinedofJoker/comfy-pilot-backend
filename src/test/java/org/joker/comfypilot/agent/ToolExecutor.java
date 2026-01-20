package org.joker.comfypilot.agent;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.agent.tool.ToolExecutionRequest;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public class ToolExecutor {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final Object toolInstance;
    private final Map<String, Method> methodMap;

    public ToolExecutor(Class<?> toolClass) {
        try {
            this.toolInstance = toolClass.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new BusinessException(e);
        }

        this.methodMap = Arrays.stream(toolClass.getDeclaredMethods())
                .filter(m -> m.isAnnotationPresent(Tool.class))
                .collect(Collectors.toMap(Method::getName, m -> m));
    }

    public String execute(ToolExecutionRequest request) {
        try {
            String toolName = request.name();
            String argumentsJson = request.arguments();

            Method method = methodMap.get(toolName);
            if (method == null) {
                throw new IllegalArgumentException("Unknown tool: " + toolName);
            }

            Object[] args = buildArguments(method, argumentsJson);
            Object result = method.invoke(toolInstance, args);

            return OBJECT_MAPPER.writeValueAsString(result);

        } catch (Exception e) {
            throw new BusinessException("Tool execution failed", e);
        }
    }

    private Object[] buildArguments(Method method, String argumentsJson) throws Exception {
        Parameter[] parameters = method.getParameters();
        JsonNode root = OBJECT_MAPPER.readTree(argumentsJson);

        Object[] args = new Object[parameters.length];
        for (int i = 0; i < parameters.length; i++) {
            Parameter p = parameters[i];
            JsonNode valueNode = root.get(p.getName());
            args[i] = OBJECT_MAPPER.treeToValue(valueNode, p.getType());
        }
        return args;
    }
}
