package org.joker.comfypilot.model.application.service.impl;

import lombok.RequiredArgsConstructor;
import org.joker.comfypilot.common.exception.ResourceNotFoundException;
import org.joker.comfypilot.model.application.converter.ModelApiKeyDTOConverter;
import org.joker.comfypilot.model.application.dto.CreateApiKeyRequest;
import org.joker.comfypilot.model.application.dto.ModelApiKeyDTO;
import org.joker.comfypilot.model.application.dto.UpdateApiKeyRequest;
import org.joker.comfypilot.model.application.service.ModelApiKeyService;
import org.joker.comfypilot.model.application.util.ApiKeyUtil;
import org.joker.comfypilot.model.domain.entity.ModelApiKey;
import org.joker.comfypilot.model.domain.repository.ModelApiKeyRepository;
import org.joker.comfypilot.model.domain.repository.ModelProviderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 模型API密钥服务实现
 */
@Service
@RequiredArgsConstructor
public class ModelApiKeyServiceImpl implements ModelApiKeyService {

    private final ModelApiKeyRepository apiKeyRepository;
    private final ModelProviderRepository providerRepository;
    private final ModelApiKeyDTOConverter dtoConverter;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ModelApiKeyDTO createApiKey(CreateApiKeyRequest request) {
        // 验证提供商是否存在
        providerRepository.findById(request.getProviderId())
                .orElseThrow(() -> new ResourceNotFoundException("模型提供商", request.getProviderId()));

        // 加密API密钥
        String encryptedApiKey = ApiKeyUtil.encrypt(request.getApiKey());

        // 创建领域实体
        ModelApiKey apiKey = ModelApiKey.builder()
                .providerId(request.getProviderId())
                .keyName(request.getKeyName())
                .apiKey(encryptedApiKey)
                .isEnabled(true)
                .build();

        // 保存
        ModelApiKey savedApiKey = apiKeyRepository.save(apiKey);

        // 转换为DTO并脱敏显示
        ModelApiKeyDTO dto = dtoConverter.toDTO(savedApiKey);
        dto.setApiKey(ApiKeyUtil.mask(request.getApiKey()));

        return dto;
    }

    @Override
    public ModelApiKeyDTO getById(Long id) {
        ModelApiKey apiKey = apiKeyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("API密钥", id));

        // 转换为DTO并脱敏显示
        ModelApiKeyDTO dto = dtoConverter.toDTO(apiKey);
        String decryptedKey = ApiKeyUtil.decrypt(apiKey.getApiKey());
        dto.setApiKey(ApiKeyUtil.mask(decryptedKey));

        return dto;
    }

    @Override
    public List<ModelApiKeyDTO> listApiKeys() {
        List<ModelApiKey> apiKeys = apiKeyRepository.findAll();
        return apiKeys.stream()
                .map(apiKey -> {
                    ModelApiKeyDTO dto = dtoConverter.toDTO(apiKey);
                    String decryptedKey = ApiKeyUtil.decrypt(apiKey.getApiKey());
                    dto.setApiKey(ApiKeyUtil.mask(decryptedKey));
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<ModelApiKeyDTO> listByProviderId(Long providerId) {
        List<ModelApiKey> apiKeys = apiKeyRepository.findByProviderId(providerId);
        return apiKeys.stream()
                .map(apiKey -> {
                    ModelApiKeyDTO dto = dtoConverter.toDTO(apiKey);
                    String decryptedKey = ApiKeyUtil.decrypt(apiKey.getApiKey());
                    dto.setApiKey(ApiKeyUtil.mask(decryptedKey));
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ModelApiKeyDTO updateApiKey(Long id, UpdateApiKeyRequest request) {
        // 查询现有密钥
        ModelApiKey apiKey = apiKeyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("API密钥", id));

        // 更新字段
        if (request.getKeyName() != null) {
            apiKey.setKeyName(request.getKeyName());
        }

        // 保存
        ModelApiKey updatedApiKey = apiKeyRepository.save(apiKey);

        // 转换为DTO并脱敏显示
        ModelApiKeyDTO dto = dtoConverter.toDTO(updatedApiKey);
        String decryptedKey = ApiKeyUtil.decrypt(updatedApiKey.getApiKey());
        dto.setApiKey(ApiKeyUtil.mask(decryptedKey));

        return dto;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteApiKey(Long id) {
        // 查询密钥是否存在
        apiKeyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("API密钥", id));

        // 删除
        apiKeyRepository.deleteById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void enableApiKey(Long id) {
        ModelApiKey apiKey = apiKeyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("API密钥", id));

        apiKey.enable();
        apiKeyRepository.save(apiKey);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void disableApiKey(Long id) {
        ModelApiKey apiKey = apiKeyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("API密钥", id));

        apiKey.disable();
        apiKeyRepository.save(apiKey);
    }
}
