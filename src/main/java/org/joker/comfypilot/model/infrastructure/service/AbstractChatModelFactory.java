package org.joker.comfypilot.model.infrastructure.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.joker.comfypilot.common.exception.BusinessException;

import java.util.Map;

/**
 * ChatModel 工厂抽象基类
 * <p>
 * 提供公共的配置解析和类型转换逻辑
 */
@Slf4j
public abstract class AbstractChatModelFactory {

    protected static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    /**
     * 默认超时时间（秒）
     */
    protected static final int DEFAULT_TIMEOUT_SECONDS = 60;

    /**
     * 默认温度参数
     */
    protected static final double DEFAULT_TEMPERATURE = 0.7;

    /**
     * 解析模型配置 JSON
     * <p>
     * 从 JSON 字符串中提取模型配置参数，并使用 agentConfig 覆盖
     *
     * @param modelConfigJson 模型配置 JSON 字符串
     * @param agentConfig     Agent配置，用于覆盖模型默认配置
     * @return 模型配置对象
     */
    protected ModelConfig parseModelConfig(String modelConfigJson, Map<String, Object> agentConfig) {
        try {
            // 1. 解析模型默认配置
            String apiKey = null;
            Double temperature = DEFAULT_TEMPERATURE;
            Integer maxTokens = null;
            Double topP = null;
            Integer timeoutSeconds = DEFAULT_TIMEOUT_SECONDS;
            String apiBaseUrl = null;

            if (modelConfigJson != null && !modelConfigJson.isBlank()) {
                JsonNode root = OBJECT_MAPPER.readTree(modelConfigJson);
                apiKey = getStringValue(root, "apiKey");
                temperature = getDoubleValue(root, "temperature", DEFAULT_TEMPERATURE);
                maxTokens = getIntegerValue(root, "maxTokens");
                topP = getDoubleValue(root, "topP", null);
                timeoutSeconds = getIntegerValue(root, "timeout", DEFAULT_TIMEOUT_SECONDS);
                apiBaseUrl = getStringValue(root, "apiBaseUrl");
            }

            // 2. 使用 agentConfig 覆盖模型配置
            if (agentConfig != null && !agentConfig.isEmpty()) {
                log.debug("应用 Agent 配置覆盖: {}", agentConfig);

                // 覆盖 temperature
                if (agentConfig.containsKey("temperature")) {
                    temperature = convertToDouble(agentConfig.get("temperature"), "temperature");
                }

                // 覆盖 maxTokens
                if (agentConfig.containsKey("maxTokens")) {
                    maxTokens = convertToInteger(agentConfig.get("maxTokens"), "maxTokens");
                }

                // 覆盖 topP
                if (agentConfig.containsKey("topP")) {
                    topP = convertToDouble(agentConfig.get("topP"), "topP");
                }

                // 覆盖 timeout
                if (agentConfig.containsKey("timeout")) {
                    timeoutSeconds = convertToInteger(agentConfig.get("timeout"), "timeout");
                }
            }

            return new ModelConfig(apiKey, temperature, maxTokens, topP, timeoutSeconds, apiBaseUrl);

        } catch (Exception e) {
            log.error("解析模型配置失败: modelConfig={}, agentConfig={}", modelConfigJson, agentConfig, e);
            throw new BusinessException("模型配置格式错误: " + e.getMessage());
        }
    }

    /**
     * 从 JSON 节点中获取字符串值
     */
    protected String getStringValue(JsonNode root, String fieldName) {
        JsonNode node = root.get(fieldName);
        return (node != null && !node.isNull()) ? node.asText() : null;
    }

    /**
     * 从 JSON 节点中获取整数值
     */
    protected Integer getIntegerValue(JsonNode root, String fieldName) {
        JsonNode node = root.get(fieldName);
        return (node != null && !node.isNull()) ? node.asInt() : null;
    }

    /**
     * 从 JSON 节点中获取整数值（带默认值）
     */
    protected Integer getIntegerValue(JsonNode root, String fieldName, Integer defaultValue) {
        JsonNode node = root.get(fieldName);
        return (node != null && !node.isNull()) ? node.asInt() : defaultValue;
    }

    /**
     * 从 JSON 节点中获取双精度浮点数值（带默认值）
     */
    protected Double getDoubleValue(JsonNode root, String fieldName, Double defaultValue) {
        JsonNode node = root.get(fieldName);
        return (node != null && !node.isNull()) ? node.asDouble() : defaultValue;
    }

    /**
     * 将 Object 转换为 Double
     * <p>
     * 支持 Number、String 类型的转换
     *
     * @param value     待转换的值
     * @param fieldName 字段名（用于错误提示）
     * @return Double 值
     */
    protected Double convertToDouble(Object value, String fieldName) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        if (value instanceof String) {
            try {
                return Double.parseDouble((String) value);
            } catch (NumberFormatException e) {
                throw new BusinessException(
                        String.format("Agent配置参数 %s 格式错误，无法转换为数字: %s", fieldName, value));
            }
        }
        throw new BusinessException(
                String.format("Agent配置参数 %s 类型错误，期望 Number 或 String，实际: %s",
                        fieldName, value.getClass().getSimpleName()));
    }

    /**
     * 将 Object 转换为 Integer
     * <p>
     * 支持 Number、String 类型的转换
     *
     * @param value     待转换的值
     * @param fieldName 字段名（用于错误提示）
     * @return Integer 值
     */
    protected Integer convertToInteger(Object value, String fieldName) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        if (value instanceof String) {
            try {
                return Integer.parseInt((String) value);
            } catch (NumberFormatException e) {
                throw new BusinessException(
                        String.format("Agent配置参数 %s 格式错误，无法转换为整数: %s", fieldName, value));
            }
        }
        throw new BusinessException(
                String.format("Agent配置参数 %s 类型错误，期望 Number 或 String，实际: %s",
                        fieldName, value.getClass().getSimpleName()));
    }

    /**
     * 模型配置内部类
     * <p>
     * 封装从 JSON 配置中解析出的模型参数
     */
    protected record ModelConfig(
            String apiKey,
            Double temperature,
            Integer maxTokens,
            Double topP,
            Integer timeoutSeconds,
            String apiBaseUrl
    ) {
    }
}
