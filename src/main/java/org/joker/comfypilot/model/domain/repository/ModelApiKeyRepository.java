package org.joker.comfypilot.model.domain.repository;

import org.joker.comfypilot.model.domain.entity.ModelApiKey;

import java.util.List;
import java.util.Optional;

/**
 * 模型API密钥仓储接口
 */
public interface ModelApiKeyRepository {

    /**
     * 保存API密钥
     *
     * @param apiKey 密钥实体
     * @return 保存后的实体
     */
    ModelApiKey save(ModelApiKey apiKey);

    /**
     * 根据ID查询
     *
     * @param id 密钥ID
     * @return 密钥实体
     */
    Optional<ModelApiKey> findById(Long id);

    /**
     * 查询所有密钥
     *
     * @return 密钥列表
     */
    List<ModelApiKey> findAll();

    /**
     * 根据提供商ID查询
     *
     * @param providerId 提供商ID
     * @return 密钥列表
     */
    List<ModelApiKey> findByProviderId(Long providerId);

    /**
     * 根据启用状态查询
     *
     * @param isEnabled 是否启用
     * @return 密钥列表
     */
    List<ModelApiKey> findByIsEnabled(Boolean isEnabled);

    /**
     * 删除密钥
     *
     * @param id 密钥ID
     */
    void deleteById(Long id);
}
