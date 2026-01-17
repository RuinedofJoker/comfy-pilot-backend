package org.joker.comfypilot.agent.infrastructure.persistence.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.joker.comfypilot.agent.domain.entity.AgentConfig;
import org.joker.comfypilot.agent.domain.repository.AgentConfigRepository;
import org.joker.comfypilot.agent.infrastructure.persistence.converter.AgentConfigConverter;
import org.joker.comfypilot.agent.infrastructure.persistence.mapper.AgentConfigMapper;
import org.joker.comfypilot.agent.infrastructure.persistence.po.AgentConfigPO;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Agent配置仓储实现
 */
@Repository
@RequiredArgsConstructor
public class AgentConfigRepositoryImpl implements AgentConfigRepository {

    private final AgentConfigMapper agentConfigMapper;
    private final AgentConfigConverter agentConfigConverter;

    @Override
    public AgentConfig save(AgentConfig agentConfig) {
        AgentConfigPO po = agentConfigConverter.toPO(agentConfig);
        agentConfigMapper.insert(po);
        return agentConfigConverter.toDomain(po);
    }

    @Override
    public Optional<AgentConfig> findById(Long id) {
        AgentConfigPO po = agentConfigMapper.selectById(id);
        return Optional.ofNullable(po).map(agentConfigConverter::toDomain);
    }

    @Override
    public Optional<AgentConfig> findByAgentCode(String agentCode) {
        LambdaQueryWrapper<AgentConfigPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AgentConfigPO::getAgentCode, agentCode);
        AgentConfigPO po = agentConfigMapper.selectOne(wrapper);
        return Optional.ofNullable(po).map(agentConfigConverter::toDomain);
    }

    @Override
    public List<AgentConfig> findAll() {
        List<AgentConfigPO> poList = agentConfigMapper.selectList(null);
        return poList.stream()
                .map(agentConfigConverter::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public AgentConfig update(AgentConfig agentConfig) {
        AgentConfigPO po = agentConfigConverter.toPO(agentConfig);
        agentConfigMapper.updateById(po);
        return agentConfigConverter.toDomain(po);
    }

    @Override
    public void deleteById(Long id) {
        agentConfigMapper.deleteById(id);
    }
}
