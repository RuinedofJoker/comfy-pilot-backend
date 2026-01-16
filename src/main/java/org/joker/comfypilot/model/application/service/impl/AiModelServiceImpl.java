package org.joker.comfypilot.model.application.service.impl;

import lombok.RequiredArgsConstructor;
import org.joker.comfypilot.common.exception.BusinessException;
import org.joker.comfypilot.common.exception.ResourceNotFoundException;
import org.joker.comfypilot.model.application.converter.AiModelDTOConverter;
import org.joker.comfypilot.model.application.dto.AiModelDTO;
import org.joker.comfypilot.model.application.dto.CreateModelRequest;
import org.joker.comfypilot.model.application.dto.UpdateModelRequest;
import org.joker.comfypilot.model.application.service.AiModelService;
import org.joker.comfypilot.model.domain.entity.AiModel;
import org.joker.comfypilot.model.domain.enums.ModelAccessType;
import org.joker.comfypilot.model.domain.enums.ModelSource;
import org.joker.comfypilot.model.domain.enums.ModelType;
import org.joker.comfypilot.model.domain.repository.AiModelRepository;
import org.joker.comfypilot.model.domain.repository.ModelProviderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * AI模型服务实现
 */
@Service
@RequiredArgsConstructor
public class AiModelServiceImpl implements AiModelService {

    private final AiModelRepository modelRepository;
    private final ModelProviderRepository providerRepository;
    private final AiModelDTOConverter dtoConverter;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AiModelDTO createModel(CreateModelRequest request) {
        // 验证接入方式是否有效
        ModelAccessType accessType = validateAccessType(request.getAccessType());

        // 验证模型类型是否有效
        ModelType modelType = validateModelType(request.getModelType());

        // 验证模型标识符是否唯一
        modelRepository.findByModelIdentifier(request.getModelIdentifier())
                .ifPresent(existing -> {
                    throw new BusinessException("模型标识符已存在: " + request.getModelIdentifier());
                });

        // 如果是远程API接入，验证提供商是否存在
        if (ModelAccessType.REMOTE_API.equals(accessType)) {
            if (request.getProviderId() == null) {
                throw new BusinessException("远程API接入方式必须指定提供商");
            }
            providerRepository.findById(request.getProviderId())
                    .orElseThrow(() -> new ResourceNotFoundException("模型提供商", request.getProviderId()));
        }

        // 创建领域实体
        AiModel model = AiModel.builder()
                .modelName(request.getModelName())
                .modelIdentifier(request.getModelIdentifier())
                .accessType(accessType)
                .modelType(modelType)
                .modelSource(ModelSource.REMOTE_API)  // 通过API创建的模型标记为远程API来源
                .providerId(request.getProviderId())
                .modelConfig(request.getModelConfig())
                .description(request.getDescription())
                .isEnabled(true)
                .build();

        // 调用领域实体的验证方法
        model.validate();

        // 保存
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
        // 查询现有模型
        AiModel model = modelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("AI模型", id));

        // 检查是否可以编辑
        // 代码预定义的模型只能编辑基本信息（modelName, description）
        // 远程API创建的模型可以完全编辑
        if (!model.canFullEdit()) {
            // 代码预定义的模型，只允许更新基本信息
            if (request.getModelConfig() != null) {
                throw new BusinessException("代码预定义的模型不允许修改模型配置");
            }
        }

        // 更新字段
        if (request.getModelName() != null) {
            model.setModelName(request.getModelName());
        }
        if (request.getModelConfig() != null) {
            model.setModelConfig(request.getModelConfig());
        }
        if (request.getDescription() != null) {
            model.setDescription(request.getDescription());
        }

        // 保存
        AiModel updatedModel = modelRepository.save(model);

        return dtoConverter.toDTO(updatedModel);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteModel(Long id) {
        // 查询模型是否存在
        AiModel model = modelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("AI模型", id));

        // 检查是否可以删除
        // 只有远程API创建的模型才能删除，代码预定义的模型不能删除
        if (!model.canDelete()) {
            throw new BusinessException("代码预定义的模型不允许删除");
        }

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

    /**
     * 验证接入方式是否有效
     */
    private ModelAccessType validateAccessType(String code) {
        try {
            return ModelAccessType.fromCode(code);
        } catch (IllegalArgumentException e) {
            throw new BusinessException("无效的接入方式: " + code);
        }
    }

    /**
     * 验证模型类型是否有效
     */
    private ModelType validateModelType(String code) {
        try {
            return ModelType.fromCode(code);
        } catch (IllegalArgumentException e) {
            throw new BusinessException("无效的模型类型: " + code);
        }
    }
}
