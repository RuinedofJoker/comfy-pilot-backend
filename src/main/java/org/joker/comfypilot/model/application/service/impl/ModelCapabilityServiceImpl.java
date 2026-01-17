package org.joker.comfypilot.model.application.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.joker.comfypilot.common.exception.BusinessException;
import org.joker.comfypilot.model.application.dto.ModelCapabilityRequest;
import org.joker.comfypilot.model.application.dto.ModelCapabilityResponse;
import org.joker.comfypilot.model.application.service.ModelCapabilityService;
import org.joker.comfypilot.model.domain.entity.AiModel;
import org.joker.comfypilot.model.domain.enums.ModelCapability;
import org.joker.comfypilot.model.domain.service.CapabilityRouter;
import org.joker.comfypilot.model.domain.service.ModelExecutor;
import org.joker.comfypilot.model.domain.valueobject.CapabilityConstraints;
import org.joker.comfypilot.model.domain.valueobject.ModelExecutionMetadata;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 模型能力服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ModelCapabilityServiceImpl implements ModelCapabilityService {

    private final CapabilityRouter capabilityRouter;
    private final List<ModelExecutor> modelExecutors;

    @Override
    public ModelCapabilityResponse invoke(ModelCapabilityRequest request) {
        log.info("调用模型能力: capability={}", request.getCapability());

        // 1. 验证请求
        validateRequest(request);

        // 2. 选择模型
        AiModel model = selectModel(request);
        log.info("选中模型: modelIdentifier={}, modelName={}",
                 model.getModelIdentifier(), model.getModelName());

        // 3. 选择执行器
        ModelExecutor executor = selectExecutor(model);

        // 4. 执行推理
        LocalDateTime startTime = LocalDateTime.now();
        Map<String, Object> result;
        try {
            result = executor.execute(model, request.getCapability(), request.getParameters());
        } catch (Exception e) {
            log.error("模型执行失败: modelIdentifier={}, error={}",
                     model.getModelIdentifier(), e.getMessage(), e);
            throw new BusinessException("模型执行失败: " + e.getMessage());
        }
        LocalDateTime endTime = LocalDateTime.now();

        // 5. 构建响应
        ModelExecutionMetadata metadata = buildMetadata(model, startTime, endTime, true, null);

        return ModelCapabilityResponse.builder()
                .capability(request.getCapability())
                .result(result)
                .metadata(metadata)
                .build();
    }

    @Override
    public List<AiModel> getModelsForCapability(ModelCapability capability) {
        return capabilityRouter.getModelsForCapability(capability);
    }

    /**
     * 验证请求
     */
    private void validateRequest(ModelCapabilityRequest request) {
        if (request.getCapability() == null) {
            throw new BusinessException("能力类型不能为空");
        }
        if (request.getParameters() == null || request.getParameters().isEmpty()) {
            throw new BusinessException("请求参数不能为空");
        }
    }

    /**
     * 选择模型
     */
    private AiModel selectModel(ModelCapabilityRequest request) {
        CapabilityConstraints constraints = request.getConstraints();
        if (constraints == null) {
            constraints = CapabilityConstraints.builder().build();
        }

        AiModel model = capabilityRouter.selectModel(request.getCapability(), constraints);
        if (model == null) {
            throw new BusinessException("未找到支持该能力的模型: " + request.getCapability());
        }

        return model;
    }

    /**
     * 选择执行器
     */
    private ModelExecutor selectExecutor(AiModel model) {
        for (ModelExecutor executor : modelExecutors) {
            if (executor.supports(model)) {
                return executor;
            }
        }
        throw new BusinessException("未找到支持该模型的执行器: " + model.getModelIdentifier());
    }

    /**
     * 构建执行元数据
     */
    private ModelExecutionMetadata buildMetadata(AiModel model, LocalDateTime startTime,
                                                  LocalDateTime endTime, Boolean success,
                                                  String errorMessage) {
        long durationMs = java.time.Duration.between(startTime, endTime).toMillis();

        return ModelExecutionMetadata.builder()
                .modelIdentifier(model.getModelIdentifier())
                .modelName(model.getModelName())
                .startTime(startTime)
                .endTime(endTime)
                .durationMs(durationMs)
                .success(success)
                .errorMessage(errorMessage)
                .build();
    }
}
