package org.joker.comfypilot.model.domain.valueobject;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.joker.comfypilot.model.domain.enums.ModelCapability;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * 模型配置值对象
 * 封装模型的能力配置、参数配置等信息
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModelConfig implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 模型支持的能力列表
     */
    private List<ModelCapability> capabilities;

    /**
     * 模型参数配置
     * 如：max_tokens, temperature, top_p 等
     */
    private Map<String, Object> parameters;

    /**
     * 模型优先级（数值越大优先级越高）
     * 用于同能力多模型时的选择
     */
    private Integer priority;

    /**
     * 每1000个token的成本（美元）
     * 用于成本控制和优化
     */
    private Double costPer1kTokens;

    /**
     * 检查是否支持指定能力
     *
     * @param capability 能力类型
     * @return 是否支持
     */
    public boolean supportsCapability(ModelCapability capability) {
        return capabilities != null && capabilities.contains(capability);
    }

    /**
     * 获取指定参数的值
     *
     * @param key 参数名
     * @return 参数值
     */
    public Object getParameter(String key) {
        return parameters != null ? parameters.get(key) : null;
    }

    /**
     * 获取指定参数的值（带默认值）
     *
     * @param key 参数名
     * @param defaultValue 默认值
     * @return 参数值
     */
    public Object getParameter(String key, Object defaultValue) {
        Object value = getParameter(key);
        return value != null ? value : defaultValue;
    }
}
