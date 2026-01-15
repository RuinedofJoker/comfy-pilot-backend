package org.joker.comfypilot.model.domain.repository;

import org.joker.comfypilot.model.domain.entity.AIModel;
import org.joker.comfypilot.model.domain.enums.ModelStatus;
import org.joker.comfypilot.model.domain.enums.ModelType;

import java.util.List;
import java.util.Optional;

/**
 * AI模型仓储接口
 */
public interface AIModelRepository {

    /**
     * 保存模型
     */
    AIModel save(AIModel model);

    /**
     * 根据ID查询
     */
    Optional<AIModel> findById(Long id);

    /**
     * 根据提供商ID查询
     */
    List<AIModel> findByProviderId(Long providerId);

    /**
     * 根据模型类型查询
     */
    List<AIModel> findByModelType(ModelType modelType);

    /**
     * 根据状态查询
     */
    List<AIModel> findByStatus(ModelStatus status);

    /**
     * 查询所有激活的模型
     */
    List<AIModel> findAllActive();

    /**
     * 删除模型
     */
    void deleteById(Long id);
}
