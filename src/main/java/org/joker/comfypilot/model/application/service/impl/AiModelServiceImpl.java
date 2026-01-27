package org.joker.comfypilot.model.application.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.extern.slf4j.Slf4j;
import org.joker.comfypilot.common.config.JacksonConfig;
import org.joker.comfypilot.common.exception.BusinessException;
import org.joker.comfypilot.common.exception.ResourceNotFoundException;
import org.joker.comfypilot.model.application.converter.AiModelDTOConverter;
import org.joker.comfypilot.model.application.converter.AiModelSimpleDTOConverter;
import org.joker.comfypilot.model.application.dto.AiModelDTO;
import org.joker.comfypilot.model.application.dto.AiModelSimpleDTO;
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
import org.joker.comfypilot.model.domain.service.ModelTemplate;
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
@Slf4j
public class AiModelServiceImpl implements AiModelService {

    @Autowired
    private AiModelRepository modelRepository;
    @Autowired
    private ModelProviderRepository providerRepository;
    @Autowired
    private AiModelDTOConverter dtoConverter;
    @Autowired
    private AiModelSimpleDTOConverter simpleDtoConverter;

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

        // 5. 根据 modelCallingType 确定 accessType 和 modelType
        ModelAccessType accessType;
        ModelType modelType = switch (modelCallingType) {
            case API_LLM -> {
                accessType = ModelAccessType.REMOTE_API;
                yield ModelType.LLM;
            }
            case API_EMBEDDING -> {
                accessType = ModelAccessType.REMOTE_API;
                yield ModelType.EMBEDDING;
            }
            case SENTENCE_TRANSFORMERS_EMBEDDING -> {
                accessType = ModelAccessType.LOCAL;
                yield ModelType.EMBEDDING;
            }
            default -> throw new BusinessException("不支持的模型调用方式: " + modelCallingType);
        };

        // 6. 处理 modelConfig，将 JSON 字符串转换为 Map
        Map<String, Object> modelConfig = parseModelConfig(request.getModelConfig());

        // 7. 创建领域实体
        AiModel model = AiModel.builder()
                .modelName(request.getModelName())
                .modelDisplayName(request.getModelDisplayName())
                .modelIdentifier(finalModelIdentifier)
                .modelCallingType(modelCallingType)
                .apiBaseUrl(request.getApiBaseUrl())
                .apiKey(request.getApiKey())
                .accessType(accessType)
                .modelType(modelType)
                .providerId(request.getProviderId())
                .providerType(providerType)
                .modelConfig(modelConfig)
                .description(request.getDescription())
                .isEnabled(request.getIsEnabled())
                .build();

        // 8. 保存
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

        // 5. 根据 modelCallingType 确定 accessType 和 modelType
        ModelAccessType accessType;
        ModelType modelType = switch (modelCallingType) {
            case API_LLM -> {
                accessType = ModelAccessType.REMOTE_API;
                yield ModelType.LLM;
            }
            case API_EMBEDDING -> {
                accessType = ModelAccessType.REMOTE_API;
                yield ModelType.EMBEDDING;
            }
            case SENTENCE_TRANSFORMERS_EMBEDDING -> {
                accessType = ModelAccessType.LOCAL;
                yield ModelType.EMBEDDING;
            }
            default -> throw new BusinessException("不支持的模型调用方式: " + modelCallingType);
        };

        // 6. 处理 modelConfig，将 JSON 字符串转换为 Map
        Map<String, Object> modelConfig = parseModelConfig(request.getModelConfig());

        // 7. 更新字段
        model.setModelName(request.getModelName());
        model.setModelDisplayName(request.getModelDisplayName());
        model.setModelCallingType(modelCallingType);
        model.setApiBaseUrl(request.getApiBaseUrl());
        model.setApiKey(request.getApiKey());
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

    @Override
    public List<AiModelSimpleDTO> listEnabledModels(String modelCallingType) {
        ModelCallingType callingType = ModelCallingType.fromCode(modelCallingType);
        List<AiModel> enabledModels = modelRepository.findByIsEnabled(true);
        return enabledModels.stream()
                .filter(aiModel -> callingType.equals(aiModel.getModelCallingType()))
                .map(simpleDtoConverter::toSimpleDTO)
                .collect(Collectors.toList());
    }

    @Override
    public String getModelConfigFormat(String modelCallingType) {
        // 1. 解析模型调用方式
        ModelCallingType callingType = parseModelCallingType(modelCallingType);

        // 2. 获取对应的ModelTemplate类
        Class<? extends ModelTemplate> modelClass = callingType.getModelClass();

        // 3. 实例化ModelTemplate（使用null作为AiModel参数，因为只需要获取配置格式）
        try {
            ModelTemplate template = modelClass.getDeclaredConstructor(AiModel.class).newInstance((AiModel) null);

            // 4. 调用configFormat方法获取配置格式
            Map<String, Object> configFormat = template.configFormat();

            // 5. 将Map转换为JSON字符串
            ObjectMapper objectMapper = JacksonConfig.getObjectMapper();
            objectMapper.configure(SerializationFeature.INDENT_OUTPUT, true);
            return objectMapper.writeValueAsString(configFormat);
        } catch (Exception e) {
            log.error("获取模型配置格式失败", e);
            throw new BusinessException("获取模型配置格式失败: " + e.getMessage());
        }
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
            ObjectMapper objectMapper = JacksonConfig.getObjectMapper();
            return objectMapper.readValue(json, new TypeReference<>() {
            });
        } catch (JsonProcessingException e) {
            throw new BusinessException("无效的模型配置 JSON 格式: " + e.getMessage());
        }
    }
}
