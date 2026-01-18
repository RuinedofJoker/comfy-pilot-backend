package org.joker.comfypilot.model.infrastructure.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.joker.comfypilot.common.exception.BusinessException;
import org.joker.comfypilot.model.domain.entity.AiModel;
import org.joker.comfypilot.model.domain.enums.ModelCapability;
import org.joker.comfypilot.model.domain.repository.AiModelRepository;
import org.joker.comfypilot.model.domain.service.CapabilityRouter;
import org.joker.comfypilot.model.domain.valueobject.CapabilityConstraints;
import org.joker.comfypilot.model.domain.valueobject.ModelConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 能力路由器实现
 * 负责根据能力类型和约束条件选择合适的模型
 */
@Slf4j
@Service
public class CapabilityRouterImpl implements CapabilityRouter {

    @Autowired
    private AiModelRepository modelRepository;
    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public AiModel selectModel(ModelCapability capability, CapabilityConstraints constraints) {
        log.debug("选择模型: capability={}, constraints={}", capability, constraints);

        // 1. 如果指定了模型标识符，直接使用
        if (constraints != null && StringUtils.hasText(constraints.getModelIdentifier())) {
            return selectByIdentifier(constraints.getModelIdentifier(), capability);
        }

        // 2. 获取支持该能力的所有模型
        List<AiModel> candidates = getModelsForCapability(capability, constraints);

        // 3. 如果没有候选模型，抛出异常
        if (candidates.isEmpty()) {
            throw new BusinessException("未找到支持该能力的模型: " + capability);
        }

        // 4. 返回优先级最高的模型
        return candidates.getFirst();
    }

    @Override
    public List<AiModel> getModelsForCapability(ModelCapability capability) {
        return getModelsForCapability(capability, null);
    }

    @Override
    public List<AiModel> getModelsForCapability(ModelCapability capability,
                                                 CapabilityConstraints constraints) {
        // 1. 获取所有启用的模型
        List<AiModel> allModels = modelRepository.findByIsEnabled(true);

        // 2. 过滤支持该能力的模型
        List<AiModel> supportedModels = allModels.stream()
                .filter(model -> supportsCapability(model, capability))
                .collect(Collectors.toList());

        // 3. 应用约束条件过滤
        if (constraints != null) {
            supportedModels = applyConstraints(supportedModels, constraints);
        }

        // 4. 按优先级排序
        supportedModels.sort(Comparator.comparingInt(this::getModelPriority).reversed());

        return supportedModels;
    }

    /**
     * 根据模型标识符选择模型
     */
    private AiModel selectByIdentifier(String modelIdentifier, ModelCapability capability) {
        AiModel model = modelRepository.findByModelIdentifier(modelIdentifier)
                .orElseThrow(() -> new BusinessException("模型不存在: " + modelIdentifier));

        if (!model.getIsEnabled()) {
            throw new BusinessException("模型未启用: " + modelIdentifier);
        }

        if (!supportsCapability(model, capability)) {
            throw new BusinessException("模型不支持该能力: " + modelIdentifier + ", " + capability);
        }

        return model;
    }

    /**
     * 检查模型是否支持指定能力
     */
    private boolean supportsCapability(AiModel model, ModelCapability capability) {
        ModelConfig config = parseModelConfig(model.getModelConfig());
        if (config == null || config.getCapabilities() == null) {
            return false;
        }
        return config.getCapabilities().contains(capability);
    }

    /**
     * 应用约束条件过滤
     */
    private List<AiModel> applyConstraints(List<AiModel> models, CapabilityConstraints constraints) {
        List<AiModel> filtered = new ArrayList<>(models);

        // 过滤接入方式
        if (constraints.getPreferredAccessType() != null) {
            filtered = filtered.stream()
                    .filter(model -> model.getAccessType().equals(constraints.getPreferredAccessType()))
                    .collect(Collectors.toList());

            // 如果过滤后为空，则不应用此约束
            if (filtered.isEmpty()) {
                filtered = new ArrayList<>(models);
            }
        }

        // 过滤成本限制
        if (constraints.getMaxCostPer1kTokens() != null) {
            filtered = filtered.stream()
                    .filter(model -> {
                        ModelConfig config = parseModelConfig(model.getModelConfig());
                        if (config == null || config.getCostPer1kTokens() == null) {
                            return true;
                        }
                        return config.getCostPer1kTokens() <= constraints.getMaxCostPer1kTokens();
                    })
                    .collect(Collectors.toList());
        }

        // 过滤最小优先级
        if (constraints.getMinPriority() != null) {
            filtered = filtered.stream()
                    .filter(model -> getModelPriority(model) >= constraints.getMinPriority())
                    .collect(Collectors.toList());
        }

        return filtered;
    }

    /**
     * 获取模型优先级
     */
    private int getModelPriority(AiModel model) {
        ModelConfig config = parseModelConfig(model.getModelConfig());
        if (config == null || config.getPriority() == null) {
            return 0;
        }
        return config.getPriority();
    }

    /**
     * 解析模型配置
     */
    private ModelConfig parseModelConfig(String modelConfigJson) {
        if (!StringUtils.hasText(modelConfigJson)) {
            return null;
        }

        try {
            return objectMapper.readValue(modelConfigJson, ModelConfig.class);
        } catch (Exception e) {
            log.warn("解析模型配置失败: {}", e.getMessage());
            return null;
        }
    }
}
