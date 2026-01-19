package org.joker.comfypilot.model.application.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.joker.comfypilot.common.exception.BusinessException;
import org.joker.comfypilot.common.exception.ResourceNotFoundException;
import org.joker.comfypilot.model.application.converter.AiModelDTOConverter;
import org.joker.comfypilot.model.application.dto.AiModelDTO;
import org.joker.comfypilot.model.application.dto.CreateModelRequest;
import org.joker.comfypilot.model.application.dto.UpdateModelRequest;
import org.joker.comfypilot.model.application.service.AiModelService;
import org.joker.comfypilot.model.domain.entity.AiModel;
import org.joker.comfypilot.model.domain.enums.ModelAccessType;
import org.joker.comfypilot.model.domain.enums.ModelCallingType;
import org.joker.comfypilot.model.domain.enums.ModelType;
import org.joker.comfypilot.model.domain.enums.ProviderType;
import org.joker.comfypilot.model.domain.repository.AiModelRepository;
import org.joker.comfypilot.model.domain.repository.ModelProviderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * AI模型服务实现
 */
@Service
public class AiModelServiceImpl implements AiModelService {

    @Autowired
    private AiModelRepository modelRepository;
    @Autowired
    private ModelProviderRepository providerRepository;
    @Autowired
    private AiModelDTOConverter dtoConverter;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AiModelDTO createModel(CreateModelRequest request) {
        // 1. 生成或验证模型标识符
        String modelIdentifier = request.getModelIdentifier();
        if (modelIdentifier == null || modelIdentifier.trim().isEmpty()) {
            // 自动生成模型标识符：使用模型名称转换为小写并替换空格为下划线
            modelIdentifier = request.getModelName().toLowerCase().replaceAll("\\s+", "_");
        }

        // 验证模型标识符是否唯一
        final String finalModelIdentifier = modelIdentifier;
        modelRepository.findByModelIdentifier(finalModelIdentifier)
                .ifPresent(existing -> {
                    throw new BusinessException("模型标识符已存在: " + finalModelIdentifier);
                });

        // 2. 转换 modelCallingType 字符串为枚举
        ModelCallingType modelCallingType = parseModelCallingType(request.getModelCallingType());

        // 3. 如果提供了模型提供商，验证提供商是否存在
        if (request.getProviderId() != null) {
            providerRepository.findById(request.getProviderId())
                    .orElseThrow(() -> new ResourceNotFoundException("模型提供商不存在", request.getProviderId()));
        }

        // 4. 转换 providerType 字符串为枚举（可选）
        ProviderType providerType = parseProviderType(request.getProviderType());

        // 5. TODO: 根据 modelCallingType 确定 accessType 和 modelType
        ModelAccessType accessType = null;
        ModelType modelType = null;
        // TODO: 实现根据 modelCallingType 自动设置 accessType 和 modelType 的逻辑

        // 6. 处理 modelConfig，将 JSON 字符串转换为 Map
        Map<String, Object> modelConfig = parseModelConfig(request.getModelConfig());

        // 7. 创建领域实体
        AiModel model = AiModel.builder()
                .modelName(request.getModelName())
                .modelIdentifier(finalModelIdentifier)
                .modelCallingType(modelCallingType)
                .apiBaseUrl(request.getApiBaseUrl())
                .accessType(accessType)
                .modelType(modelType)
                .providerId(request.getProviderId())
                .providerType(providerType)
                .modelConfig(modelConfig)
                .description(request.getDescription())
                .isEnabled(request.getIsEnabled())
                .build();

        // 8. 调用领域实体的验证方法
        model.validate();

        // 9. 保存
        AiModel savedModel = modelRepository.save(model);

        return dtoConverter.toDTO(savedModel);
    }

    @Override
    public AiModelDTO getById(Long id) {
        AiModel model = modelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("AI模型", id));
        return dtoConverter.toDTO(model);
    }

    @Override
    public List<AiModelDTO> listModels() {
        List<AiModel> models = modelRepository.findAll();
        return models.stream()
                .map(dtoConverter::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AiModelDTO updateModel(Long id, UpdateModelRequest request) {
        // 1. 查询现有模型
        AiModel model = modelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("AI模型", id));

        // 2. 转换 modelCallingType 字符串为枚举
        ModelCallingType modelCallingType = parseModelCallingType(request.getModelCallingType());

        // 3. 如果提供了模型提供商，验证提供商是否存在
        if (request.getProviderId() != null) {
            providerRepository.findById(request.getProviderId())
                    .orElseThrow(() -> new ResourceNotFoundException("模型提供商不存在", request.getProviderId()));
        }

        // 4. 转换 providerType 字符串为枚举（可选）
        ProviderType providerType = parseProviderType(request.getProviderType());

        // 5. TODO: 根据 modelCallingType 确定 accessType 和 modelType
        ModelAccessType accessType = null;
        ModelType modelType = null;
        // TODO: 实现根据 modelCallingType 自动设置 accessType 和 modelType 的逻辑

        // 6. 处理 modelConfig，将 JSON 字符串转换为 Map
        Map<String, Object> modelConfig = parseModelConfig(request.getModelConfig());

        // 7. 更新字段
        model.setModelName(request.getModelName());
        model.setModelCallingType(modelCallingType);
        model.setApiBaseUrl(request.getApiBaseUrl());
        model.setAccessType(accessType);
        model.setModelType(modelType);
        model.setProviderId(request.getProviderId());
        model.setProviderType(providerType);
        model.setModelConfig(modelConfig);
        model.setDescription(request.getDescription());
        model.setIsEnabled(request.getIsEnabled());

        // 8. 保存
        AiModel updatedModel = modelRepository.save(model);

        return dtoConverter.toDTO(updatedModel);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteModel(Long id) {
        // 查询模型是否存在
        AiModel model = modelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("AI模型", id));

        // 删除
        modelRepository.deleteById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void enableModel(Long id) {
        AiModel model = modelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("AI模型", id));

        model.enable();
        modelRepository.save(model);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void disableModel(Long id) {
        AiModel model = modelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("AI模型", id));

        model.disable();
        modelRepository.save(model);
    }

    @Override
    public AiModelDTO getByModelIdentifier(String modelIdentifier) {
        AiModel model = modelRepository.findByModelIdentifier(modelIdentifier)
                .orElseThrow(() -> new ResourceNotFoundException("AI模型", modelIdentifier));
        return dtoConverter.toDTO(model);
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 解析模型调用方式字符串为枚举
     */
    private ModelCallingType parseModelCallingType(String code) {
        if (code == null || code.trim().isEmpty()) {
            throw new BusinessException("模型调用方式不能为空");
        }
        try {
            return ModelCallingType.fromCode(code);
        } catch (IllegalArgumentException e) {
            throw new BusinessException("无效的模型调用方式: " + code);
        }
    }

    /**
     * 解析提供协议类型字符串为枚举（可选）
     */
    private ProviderType parseProviderType(String code) {
        if (code == null || code.trim().isEmpty()) {
            return null;
        }
        try {
            return ProviderType.fromCode(code);
        } catch (IllegalArgumentException e) {
            throw new BusinessException("无效的提供协议类型: " + code);
        }
    }

    /**
     * 解析模型配置 JSON 字符串为 Map
     */
    private Map<String, Object> parseModelConfig(String json) {
        if (json == null || json.trim().isEmpty()) {
            return Map.of(); // 返回空 Map
        }
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.findAndRegisterModules();
            return objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {});
        } catch (JsonProcessingException e) {
            throw new BusinessException("无效的模型配置 JSON 格式: " + e.getMessage());
        }
    }
}
