package org.joker.comfypilot.model.infrastructure.executor;

import lombok.extern.slf4j.Slf4j;
import org.joker.comfypilot.common.exception.BusinessException;
import org.joker.comfypilot.model.domain.entity.AiModel;
import org.joker.comfypilot.model.domain.enums.ModelAccessType;
import org.joker.comfypilot.model.domain.enums.ModelCapability;
import org.joker.comfypilot.model.domain.service.ModelExecutor;

import java.util.Map;
import java.util.Set;

/**
 * 远程API执行器抽象类
 * 提供远程API调用的通用逻辑
 */
@Slf4j
public abstract class RemoteApiExecutor implements ModelExecutor {

    @Override
    public Map<String, Object> execute(AiModel model, ModelCapability capability,
                                       Map<String, Object> parameters) {
        log.info("执行远程API调用: model={}, capability={}",
                 model.getModelIdentifier(), capability);

        // 1. 验证模型
        validateModel(model);

        // 2. 验证能力
        if (!supportedCapabilities().contains(capability)) {
            throw new BusinessException("执行器不支持该能力: " + capability);
        }

        // 3. 执行具体的API调用
        return doExecute(model, capability, parameters);
    }

    @Override
    public boolean supports(AiModel model) {
        // 只支持远程API接入的模型
        if (!ModelAccessType.REMOTE_API.equals(model.getAccessType())) {
            return false;
        }

        // 检查提供商类型是否匹配
        return supportsProviderType(model);
    }

    /**
     * 验证模型
     */
    protected void validateModel(AiModel model) {
        if (model == null) {
            throw new BusinessException("模型不能为空");
        }

        if (!model.getIsEnabled()) {
            throw new BusinessException("模型未启用: " + model.getModelIdentifier());
        }

        if (model.getProviderId() == null) {
            throw new BusinessException("远程API模型必须指定提供商");
        }
    }

    /**
     * 执行具体的API调用（由子类实现）
     */
    protected abstract Map<String, Object> doExecute(AiModel model, ModelCapability capability,
                                                     Map<String, Object> parameters);

    /**
     * 检查是否支持指定提供商类型（由子类实现）
     */
    protected abstract boolean supportsProviderType(AiModel model);
}
