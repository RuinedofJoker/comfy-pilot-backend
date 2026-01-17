package org.joker.comfypilot.model.domain.service;

import org.joker.comfypilot.model.domain.entity.AiModel;
import org.joker.comfypilot.model.domain.enums.ModelCapability;

import java.util.Map;
import java.util.Set;

/**
 * 模型执行器接口
 * 负责实际调用模型进行推理
 */
public interface ModelExecutor {

    /**
     * 执行模型推理
     *
     * @param model 模型实体
     * @param capability 能力类型
     * @param parameters 请求参数
     * @return 执行结果
     */
    Map<String, Object> execute(AiModel model, ModelCapability capability, Map<String, Object> parameters);

    /**
     * 获取支持的能力类型集合
     *
     * @return 支持的能力类型
     */
    Set<ModelCapability> supportedCapabilities();

    /**
     * 检查是否支持指定模型
     *
     * @param model 模型实体
     * @return 是否支持
     */
    boolean supports(AiModel model);
}
