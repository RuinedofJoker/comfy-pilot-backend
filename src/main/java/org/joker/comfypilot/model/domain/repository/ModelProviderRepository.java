package org.joker.comfypilot.model.domain.repository;

import org.joker.comfypilot.model.domain.entity.ModelProvider;
import org.joker.comfypilot.model.domain.enums.ProviderType;

import java.util.List;
import java.util.Optional;

/**
 * 模型提供商仓储接口
 */
public interface ModelProviderRepository {

    /**
     * 保存提供商
     *
     * @param provider 提供商实体
     * @return 保存后的实体
     */
    ModelProvider save(ModelProvider provider);

    /**
     * 根据ID查询
     *
     * @param id 提供商ID
     * @return 提供商实体
     */
    Optional<ModelProvider> findById(Long id);

    /**
     * 查询所有提供商
     *
     * @return 提供商列表
     */
    List<ModelProvider> findAll();

    /**
     * 根据提供商类型查询
     *
     * @param providerType 提供商类型
     * @return 提供商列表
     */
    List<ModelProvider> findByProviderType(ProviderType providerType);

    /**
     * 根据启用状态查询
     *
     * @param isEnabled 是否启用
     * @return 提供商列表
     */
    List<ModelProvider> findByIsEnabled(Boolean isEnabled);

    /**
     * 删除提供商
     *
     * @param id 提供商ID
     */
    void deleteById(Long id);

    /**
     * 检查提供商是否被模型引用
     *
     * @param providerId 提供商ID
     * @return true-被引用，false-未被引用
     */
    boolean isReferencedByModels(Long providerId);

    /**
     * 检查提供商是否有API密钥
     *
     * @param providerId 提供商ID
     * @return true-有密钥，false-无密钥
     */
    boolean hasApiKeys(Long providerId);
}
