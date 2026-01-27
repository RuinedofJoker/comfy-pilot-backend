package org.joker.comfypilot.agent.infrastructure.persistence.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.joker.comfypilot.agent.domain.entity.UserAgentConfig;
import org.joker.comfypilot.agent.domain.repository.UserAgentConfigRepository;
import org.joker.comfypilot.agent.infrastructure.persistence.converter.UserAgentConfigConverter;
import org.joker.comfypilot.agent.infrastructure.persistence.mapper.UserAgentConfigMapper;
import org.joker.comfypilot.agent.infrastructure.persistence.po.UserAgentConfigPO;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 用户Agent配置仓储实现
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class UserAgentConfigRepositoryImpl implements UserAgentConfigRepository {

    private final UserAgentConfigMapper mapper;
    private final UserAgentConfigConverter converter;

    @Override
    public UserAgentConfig saveOrUpdate(UserAgentConfig userAgentConfig) {
        UserAgentConfigPO po = converter.toPO(userAgentConfig);
        mapper.insertOrUpdate(po);
        return converter.toDomain(po);
    }

    @Override
    public Optional<UserAgentConfig> findById(Long id) {
        UserAgentConfigPO po = mapper.selectById(id);
        return Optional.ofNullable(po).map(converter::toDomain);
    }

    @Override
    public Optional<UserAgentConfig> findByUserIdAndAgentCode(Long userId, String agentCode) {
        LambdaQueryWrapper<UserAgentConfigPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserAgentConfigPO::getUserId, userId)
               .eq(UserAgentConfigPO::getAgentCode, agentCode)
               .eq(UserAgentConfigPO::getIsDeleted, 0);

        UserAgentConfigPO po = mapper.selectOne(wrapper);
        return Optional.ofNullable(po).map(converter::toDomain);
    }

    @Override
    public List<UserAgentConfig> findByUserId(Long userId) {
        LambdaQueryWrapper<UserAgentConfigPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserAgentConfigPO::getUserId, userId)
               .eq(UserAgentConfigPO::getIsDeleted, 0)
               .orderByDesc(UserAgentConfigPO::getUpdateTime);

        return mapper.selectList(wrapper).stream()
                .map(converter::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public UserAgentConfig update(UserAgentConfig userAgentConfig) {
        UserAgentConfigPO po = converter.toPO(userAgentConfig);
        po.setUpdateTime(LocalDateTime.now());

        mapper.updateById(po);

        return converter.toDomain(po);
    }

    @Override
    public void deleteById(Long id) {
        UserAgentConfigPO po = new UserAgentConfigPO();
        po.setId(id);
        po.setIsDeleted(id);
        po.setUpdateTime(LocalDateTime.now());

        mapper.updateById(po);
    }
}
