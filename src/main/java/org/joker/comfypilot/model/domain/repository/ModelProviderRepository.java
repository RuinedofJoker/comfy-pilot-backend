package org.joker.comfypilot.model.domain.repository;

import org.joker.comfypilot.model.domain.entity.ModelProvider;
import org.joker.comfypilot.model.domain.enums.ProviderCode;
import org.joker.comfypilot.model.domain.enums.ProviderStatus;

import java.util.List;
import java.util.Optional;

/**
 * 模型提供商仓储接口
 */
public interface ModelProviderRepository {

    /**
     * 保存提供商
     */
    ModelProvider save(ModelProvider provider);

    /**
     * 根据ID查询
     */
    Optional<ModelProvider> findById(Long id);

    /**
     * 根据代码查询
     */
    Optional<ModelProvider> findByCode(ProviderCode code);

    /**
     * 根据状态查询
     */
    List<ModelProvider> findByStatus(ProviderStatus status);

    /**
     * 查询所有激活的提供商
     */
    List<ModelProvider> findAllActive();

    /**
     * 删除提供商
     */
    void deleteById(Long id);
}
