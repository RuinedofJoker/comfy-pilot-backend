package org.joker.comfypilot.model.infrastructure.persistence.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.joker.comfypilot.model.domain.entity.ModelProvider;
import org.joker.comfypilot.model.domain.enums.ProviderType;
import org.joker.comfypilot.model.domain.repository.ModelProviderRepository;
import org.joker.comfypilot.model.infrastructure.persistence.converter.ModelProviderConverter;
import org.joker.comfypilot.model.infrastructure.persistence.mapper.AiModelMapper;
import org.joker.comfypilot.model.infrastructure.persistence.mapper.ModelProviderMapper;
import org.joker.comfypilot.model.infrastructure.persistence.po.AiModelPO;
import org.joker.comfypilot.model.infrastructure.persistence.po.ModelProviderPO;
import org.springframework.stereotype.Repository;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 模型提供商仓储实现
 */
@Repository
public class ModelProviderRepositoryImpl implements ModelProviderRepository {

    @Autowired
    private ModelProviderMapper mapper;
    @Autowired
    private AiModelMapper aiModelMapper;
    @Autowired
    private ModelProviderConverter converter;

    @Override
    public ModelProvider save(ModelProvider provider) {
        ModelProviderPO po = converter.toPO(provider);
        if (po.getId() == null) {
            mapper.insert(po);
        } else {
            mapper.updateById(po);
        }
        return converter.toDomain(po);
    }

    @Override
    public Optional<ModelProvider> findById(Long id) {
        ModelProviderPO po = mapper.selectById(id);
        return Optional.ofNullable(po).map(converter::toDomain);
    }

    @Override
    public List<ModelProvider> findAll() {
        List<ModelProviderPO> poList = mapper.selectList(null);
        return poList.stream()
                .map(converter::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<ModelProvider> findByProviderType(ProviderType providerType) {
        LambdaQueryWrapper<ModelProviderPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ModelProviderPO::getProviderType, providerType.getCode());
        List<ModelProviderPO> poList = mapper.selectList(wrapper);
        return poList.stream()
                .map(converter::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<ModelProvider> findByIsEnabled(Boolean isEnabled) {
        LambdaQueryWrapper<ModelProviderPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ModelProviderPO::getIsEnabled, isEnabled);
        List<ModelProviderPO> poList = mapper.selectList(wrapper);
        return poList.stream()
                .map(converter::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteById(Long id) {
        mapper.deleteById(id);
    }

    @Override
    public boolean isReferencedByModels(Long providerId) {
        LambdaQueryWrapper<AiModelPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AiModelPO::getProviderId, providerId);
        return aiModelMapper.selectCount(wrapper) > 0;
    }
}
