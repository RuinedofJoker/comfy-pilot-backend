package org.joker.comfypilot.model.application.service;

import org.joker.comfypilot.model.application.dto.ModelCapabilityRequest;
import org.joker.comfypilot.model.application.dto.ModelCapabilityResponse;
import org.joker.comfypilot.model.domain.entity.AiModel;
import org.joker.comfypilot.model.domain.enums.ModelCapability;

import java.util.List;

/**
 * 模型能力服务接口
 * 提供统一的模型能力调用入口
 */
public interface ModelCapabilityService {

    /**
     * 调用模型能力
     *
     * @param request 能力调用请求
     * @return 能力调用响应
     */
    ModelCapabilityResponse invoke(ModelCapabilityRequest request);

    /**
     * 获取支持指定能力的所有模型
     *
     * @param capability 能力类型
     * @return 支持该能力的模型列表
     */
    List<AiModel> getModelsForCapability(ModelCapability capability);
}
