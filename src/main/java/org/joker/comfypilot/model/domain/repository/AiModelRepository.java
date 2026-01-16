package org.joker.comfypilot.model.domain.repository;

import org.joker.comfypilot.model.domain.entity.AiModel;
import org.joker.comfypilot.model.domain.enums.ModelAccessType;

import java.util.List;
import java.util.Optional;

/**
 * AI模型仓储接口
 */
public interface AiModelRepository {

    /**
     * 保存模型
     *
     * @param model 模型实体
     * @return 保存后的实体
     */
    AiModel save(AiModel model);

    /**
     * 根据ID查询
     *
     * @param id 模型ID
     * @return 模型实体
     */
    Optional<AiModel> findById(Long id);

    /**
     * 根据模型标识符查询
     *
     * @param modelIdentifier 模型标识符
     * @return 模型实体
     */
    Optional<AiModel> findByModelIdentifier(String modelIdentifier);

    /**
     * 查询所有模型
     *
     * @return 模型列表
     */
    List<AiModel> findAll();

    /**
     * 根据接入方式查询
     *
     * @param accessType 接入方式
     * @return 模型列表
     */
    List<AiModel> findByAccessType(ModelAccessType accessType);

    /**
     * 根据提供商ID查询
     *
     * @param providerId 提供商ID
     * @return 模型列表
     */
    List<AiModel> findByProviderId(Long providerId);

    /**
     * 根据启用状态查询
     *
     * @param isEnabled 是否启用
     * @return 模型列表
     */
    List<AiModel> findByIsEnabled(Boolean isEnabled);

    /**
     * 删除模型
     *
     * @param id 模型ID
     */
    void deleteById(Long id);
}
