package org.joker.comfypilot.model.infrastructure.persistence.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.joker.comfypilot.model.domain.entity.AiModel;
import org.joker.comfypilot.model.domain.enums.ModelAccessType;
import org.joker.comfypilot.model.domain.repository.AiModelRepository;
import org.joker.comfypilot.model.infrastructure.persistence.converter.AiModelConverter;
import org.joker.comfypilot.model.infrastructure.persistence.mapper.AiModelMapper;
import org.joker.comfypilot.model.infrastructure.persistence.po.AiModelPO;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * AI模型仓储实现
 */
@Repository
@RequiredArgsConstructor
public class AiModelRepositoryImpl implements AiModelRepository {

    private final AiModelMapper mapper;
    private final AiModelConverter converter;

    @Override
    public AiModel save(AiModel model) {
        AiModelPO po = converter.toPO(model);
        if (po.getId() == null) {
            mapper.insert(po);
        } else {
            mapper.updateById(po);
        }
        return converter.toDomain(po);
    }

    @Override
    public Optional<AiModel> findById(Long id) {
        AiModelPO po = mapper.selectById(id);
        return Optional.ofNullable(po).map(converter::toDomain);
    }

    @Override
    public Optional<AiModel> findByModelIdentifier(String modelIdentifier) {
        LambdaQueryWrapper<AiModelPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AiModelPO::getModelIdentifier, modelIdentifier);
        AiModelPO po = mapper.selectOne(wrapper);
        return Optional.ofNullable(po).map(converter::toDomain);
    }

    @Override
    public List<AiModel> findAll() {
        List<AiModelPO> poList = mapper.selectList(null);
        return poList.stream()
                .map(converter::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<AiModel> findByAccessType(ModelAccessType accessType) {
        LambdaQueryWrapper<AiModelPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AiModelPO::getAccessType, accessType.getCode());
        List<AiModelPO> poList = mapper.selectList(wrapper);
        return poList.stream()
                .map(converter::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<AiModel> findByProviderId(Long providerId) {
        LambdaQueryWrapper<AiModelPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AiModelPO::getProviderId, providerId);
        List<AiModelPO> poList = mapper.selectList(wrapper);
        return poList.stream()
                .map(converter::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<AiModel> findByIsEnabled(Boolean isEnabled) {
        LambdaQueryWrapper<AiModelPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AiModelPO::getIsEnabled, isEnabled);
        List<AiModelPO> poList = mapper.selectList(wrapper);
        return poList.stream()
                .map(converter::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteById(Long id) {
        mapper.deleteById(id);
    }
}
