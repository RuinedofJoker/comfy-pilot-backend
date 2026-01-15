package org.joker.comfypilot.agent.domain.repository;

import org.joker.comfypilot.agent.domain.entity.AgentExecution;
import org.joker.comfypilot.agent.domain.enums.ExecutionStatus;

import java.util.List;
import java.util.Optional;

/**
 * Agent执行记录仓储接口
 */
public interface AgentExecutionRepository {

    /**
     * 保存执行记录
     */
    AgentExecution save(AgentExecution execution);

    /**
     * 根据ID查询
     */
    Optional<AgentExecution> findById(Long id);

    /**
     * 根据会话ID查询
     */
    List<AgentExecution> findBySessionId(Long sessionId);

    /**
     * 根据Agent ID查询
     */
    List<AgentExecution> findByAgentId(Long agentId);

    /**
     * 根据状态查询
     */
    List<AgentExecution> findByStatus(ExecutionStatus status);

    /**
     * 查询会话的最新执行记录
     */
    Optional<AgentExecution> findLatestBySessionId(Long sessionId);
}
