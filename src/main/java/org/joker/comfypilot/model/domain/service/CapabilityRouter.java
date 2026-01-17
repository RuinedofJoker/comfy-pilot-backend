package org.joker.comfypilot.model.domain.service;

import org.joker.comfypilot.model.domain.entity.AiModel;
import org.joker.comfypilot.model.domain.enums.ModelCapability;
import org.joker.comfypilot.model.domain.valueobject.CapabilityConstraints;

import java.util.List;

/**
 * 能力路由器接口
 * 负责根据能力类型和约束条件选择合适的模型
 */
public interface CapabilityRouter {

    /**
     * 根据能力类型和约束条件选择最佳模型
     *
     * @param capability 能力类型
     * @param constraints 约束条件
     * @return 选中的模型
     */
    AiModel selectModel(ModelCapability capability, CapabilityConstraints constraints);

    /**
     * 获取支持指定能力的所有模型
     *
     * @param capability 能力类型
     * @return 支持该能力的模型列表
     */
    List<AiModel> getModelsForCapability(ModelCapability capability);

    /**
     * 获取支持指定能力且满足约束条件的模型列表
     *
     * @param capability 能力类型
     * @param constraints 约束条件
     * @return 符合条件的模型列表（按优先级排序）
     */
    List<AiModel> getModelsForCapability(ModelCapability capability, CapabilityConstraints constraints);
}
