package org.joker.comfypilot.model.infrastructure.persistence.repository;

import lombok.RequiredArgsConstructor;
import org.joker.comfypilot.model.domain.entity.ModelProvider;
import org.joker.comfypilot.model.domain.enums.ProviderCode;
import org.joker.comfypilot.model.domain.enums.ProviderStatus;
import org.joker.comfypilot.model.domain.repository.ModelProviderRepository;
import org.joker.comfypilot.model.infrastructure.persistence.mapper.ModelProviderMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 模型提供商仓储实现
 */
@Repository
@RequiredArgsConstructor
public class ModelProviderRepositoryImpl implements ModelProviderRepository {

    private final ModelProviderMapper modelProviderMapper;

    @Override
    public ModelProvider save(ModelProvider provider) {
        // TODO: 实现保存逻辑
        return null;
    }

    @Override
    public Optional<ModelProvider> findById(Long id) {
        // TODO: 实现查询逻辑
        return Optional.empty();
    }

    @Override
    public Optional<ModelProvider> findByCode(ProviderCode code) {
        // TODO: 实现查询逻辑
        return Optional.empty();
    }

    @Override
    public List<ModelProvider> findByStatus(ProviderStatus status) {
        // TODO: 实现查询逻辑
        return List.of();
    }

    @Override
    public List<ModelProvider> findAllActive() {
        // TODO: 实现查询逻辑
        return List.of();
    }

    @Override
    public void deleteById(Long id) {
        // TODO: 实现删除逻辑
    }
}
