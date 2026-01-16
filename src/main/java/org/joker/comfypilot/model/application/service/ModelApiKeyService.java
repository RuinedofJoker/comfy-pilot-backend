package org.joker.comfypilot.model.application.service;

import org.joker.comfypilot.model.application.dto.CreateApiKeyRequest;
import org.joker.comfypilot.model.application.dto.ModelApiKeyDTO;
import org.joker.comfypilot.model.application.dto.UpdateApiKeyRequest;

import java.util.List;

/**
 * 模型API密钥服务接口
 */
public interface ModelApiKeyService {

    /**
     * 创建API密钥
     *
     * @param request 创建请求
     * @return 密钥信息
     */
    ModelApiKeyDTO createApiKey(CreateApiKeyRequest request);

    /**
     * 查询密钥详情
     *
     * @param id 密钥ID
     * @return 密钥信息
     */
    ModelApiKeyDTO getById(Long id);

    /**
     * 查询密钥列表
     *
     * @return 密钥列表
     */
    List<ModelApiKeyDTO> listApiKeys();

    /**
     * 根据提供商ID查询密钥列表
     *
     * @param providerId 提供商ID
     * @return 密钥列表
     */
    List<ModelApiKeyDTO> listByProviderId(Long providerId);

    /**
     * 更新密钥
     *
     * @param id 密钥ID
     * @param request 更新请求
     * @return 密钥信息
     */
    ModelApiKeyDTO updateApiKey(Long id, UpdateApiKeyRequest request);

    /**
     * 删除密钥
     *
     * @param id 密钥ID
     */
    void deleteApiKey(Long id);

    /**
     * 启用密钥
     *
     * @param id 密钥ID
     */
    void enableApiKey(Long id);

    /**
     * 禁用密钥
     *
     * @param id 密钥ID
     */
    void disableApiKey(Long id);
}
