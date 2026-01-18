package org.joker.comfypilot.model.application.service.impl;

import org.joker.comfypilot.common.exception.BusinessException;
import org.joker.comfypilot.common.exception.ResourceNotFoundException;
import org.joker.comfypilot.model.application.converter.ModelProviderDTOConverter;
import org.joker.comfypilot.model.application.dto.CreateProviderRequest;
import org.joker.comfypilot.model.application.dto.ModelProviderDTO;
import org.joker.comfypilot.model.application.dto.UpdateProviderRequest;
import org.joker.comfypilot.model.application.service.ModelProviderService;
import org.joker.comfypilot.model.domain.entity.ModelProvider;
import org.joker.comfypilot.model.domain.enums.ProviderType;
import org.joker.comfypilot.model.domain.repository.ModelProviderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 模型提供商服务实现
 */
@Service
public class ModelProviderServiceImpl implements ModelProviderService {

    @Autowired
    private ModelProviderRepository providerRepository;
    @Autowired
    private ModelProviderDTOConverter dtoConverter;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ModelProviderDTO createProvider(CreateProviderRequest request) {
        // 验证提供商类型是否有效
        ProviderType providerType = validateProviderType(request.getProviderType());

        // 创建领域实体
        ModelProvider provider = ModelProvider.builder()
                .providerName(request.getProviderName())
                .providerType(providerType)
                .apiBaseUrl(request.getApiBaseUrl())
                .description(request.getDescription())
                .isEnabled(true)
                .build();

        // 保存
        ModelProvider savedProvider = providerRepository.save(provider);

        return dtoConverter.toDTO(savedProvider);
    }

    @Override
    public ModelProviderDTO getById(Long id) {
        ModelProvider provider = providerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("模型提供商", id));
        return dtoConverter.toDTO(provider);
    }

    @Override
    public List<ModelProviderDTO> listProviders() {
        List<ModelProvider> providers = providerRepository.findAll();
        return providers.stream()
                .map(dtoConverter::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ModelProviderDTO updateProvider(Long id, UpdateProviderRequest request) {
        // 查询现有提供商
        ModelProvider provider = providerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("模型提供商", id));

        // 更新字段
        if (request.getProviderName() != null) {
            provider.setProviderName(request.getProviderName());
        }
        if (request.getApiBaseUrl() != null) {
            provider.setApiBaseUrl(request.getApiBaseUrl());
        }
        if (request.getDescription() != null) {
            provider.setDescription(request.getDescription());
        }

        // 保存
        ModelProvider updatedProvider = providerRepository.save(provider);

        return dtoConverter.toDTO(updatedProvider);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteProvider(Long id) {
        // 查询提供商是否存在
        ModelProvider provider = providerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("模型提供商", id));

        // 检查是否被模型引用
        if (providerRepository.isReferencedByModels(id)) {
            throw new BusinessException("无法删除提供商：存在关联的模型");
        }

        // 删除
        providerRepository.deleteById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void enableProvider(Long id) {
        ModelProvider provider = providerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("模型提供商", id));

        provider.enable();
        providerRepository.save(provider);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void disableProvider(Long id) {
        ModelProvider provider = providerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("模型提供商", id));

        provider.disable();
        providerRepository.save(provider);
    }

    /**
     * 验证提供商类型是否有效
     */
    private ProviderType validateProviderType(String code) {
        try {
            return ProviderType.fromCode(code);
        } catch (IllegalArgumentException e) {
            throw new BusinessException("无效的提供商类型: " + code);
        }
    }
}
