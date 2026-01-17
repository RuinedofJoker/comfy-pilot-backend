package org.joker.comfypilot.agent.infrastructure.persistence.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.joker.comfypilot.agent.domain.entity.AgentExecutionLog;
import org.joker.comfypilot.agent.domain.repository.AgentExecutionLogRepository;
import org.joker.comfypilot.agent.infrastructure.persistence.converter.AgentExecutionLogConverter;
import org.joker.comfypilot.agent.infrastructure.persistence.mapper.AgentExecutionLogMapper;
import org.joker.comfypilot.agent.infrastructure.persistence.po.AgentExecutionLogPO;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Agent执行日志仓储实现
 */
@Repository
@RequiredArgsConstructor
public class AgentExecutionLogRepositoryImpl implements AgentExecutionLogRepository {

    private final AgentExecutionLogMapper agentExecutionLogMapper;
    private final AgentExecutionLogConverter agentExecutionLogConverter;

    @Override
    public AgentExecutionLog save(AgentExecutionLog log) {
        AgentExecutionLogPO po = agentExecutionLogConverter.toPO(log);
        agentExecutionLogMapper.insert(po);
        return agentExecutionLogConverter.toDomain(po);
    }

    @Override
    public Optional<AgentExecutionLog> findById(Long id) {
        AgentExecutionLogPO po = agentExecutionLogMapper.selectById(id);
        return Optional.ofNullable(po).map(agentExecutionLogConverter::toDomain);
    }

    @Override
    public List<AgentExecutionLog> findByAgentId(Long agentId) {
        LambdaQueryWrapper<AgentExecutionLogPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AgentExecutionLogPO::getAgentId, agentId)
                .orderByDesc(AgentExecutionLogPO::getCreateTime);
        List<AgentExecutionLogPO> poList = agentExecutionLogMapper.selectList(wrapper);
        return poList.stream()
                .map(agentExecutionLogConverter::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<AgentExecutionLog> findBySessionId(Long sessionId) {
        LambdaQueryWrapper<AgentExecutionLogPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AgentExecutionLogPO::getSessionId, sessionId)
                .orderByAsc(AgentExecutionLogPO::getCreateTime);
        List<AgentExecutionLogPO> poList = agentExecutionLogMapper.selectList(wrapper);
        return poList.stream()
                .map(agentExecutionLogConverter::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public AgentExecutionLog update(AgentExecutionLog log) {
        AgentExecutionLogPO po = agentExecutionLogConverter.toPO(log);
        agentExecutionLogMapper.updateById(po);
        return agentExecutionLogConverter.toDomain(po);
    }
}
