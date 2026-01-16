package org.joker.comfypilot.model.infrastructure.persistence.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.joker.comfypilot.model.domain.entity.ModelApiKey;
import org.joker.comfypilot.model.domain.repository.ModelApiKeyRepository;
import org.joker.comfypilot.model.infrastructure.persistence.converter.ModelApiKeyConverter;
import org.joker.comfypilot.model.infrastructure.persistence.mapper.ModelApiKeyMapper;
import org.joker.comfypilot.model.infrastructure.persistence.po.ModelApiKeyPO;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 模型API密钥仓储实现
 */
@Repository
@RequiredArgsConstructor
public class ModelApiKeyRepositoryImpl implements ModelApiKeyRepository {

    private final ModelApiKeyMapper mapper;
    private final ModelApiKeyConverter converter;

    @Override
    public ModelApiKey save(ModelApiKey apiKey) {
        ModelApiKeyPO po = converter.toPO(apiKey);
        if (po.getId() == null) {
            mapper.insert(po);
        } else {
            mapper.updateById(po);
        }
        return converter.toDomain(po);
    }

    @Override
    public Optional<ModelApiKey> findById(Long id) {
        ModelApiKeyPO po = mapper.selectById(id);
        return Optional.ofNullable(po).map(converter::toDomain);
    }

    @Override
    public List<ModelApiKey> findAll() {
        List<ModelApiKeyPO> poList = mapper.selectList(null);
        return poList.stream()
                .map(converter::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<ModelApiKey> findByProviderId(Long providerId) {
        LambdaQueryWrapper<ModelApiKeyPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ModelApiKeyPO::getProviderId, providerId);
        List<ModelApiKeyPO> poList = mapper.selectList(wrapper);
        return poList.stream()
                .map(converter::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<ModelApiKey> findByIsEnabled(Boolean isEnabled) {
        LambdaQueryWrapper<ModelApiKeyPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ModelApiKeyPO::getIsEnabled, isEnabled);
        List<ModelApiKeyPO> poList = mapper.selectList(wrapper);
        return poList.stream()
                .map(converter::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteById(Long id) {
        mapper.deleteById(id);
    }
}
