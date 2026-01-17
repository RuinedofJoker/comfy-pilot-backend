package org.joker.comfypilot.agent.domain.repository;

import org.joker.comfypilot.agent.domain.entity.AgentExecutionLog;

import java.util.List;
import java.util.Optional;

/**
 * Agent执行日志仓储接口
 */
public interface AgentExecutionLogRepository {

    /**
     * 保存执行日志
     *
     * @param log 执行日志实体
     * @return 保存后的执行日志实体
     */
    AgentExecutionLog save(AgentExecutionLog log);

    /**
     * 根据ID查询执行日志
     *
     * @param id 日志ID
     * @return 执行日志实体
     */
    Optional<AgentExecutionLog> findById(Long id);

    /**
     * 根据Agent ID查询执行日志列表
     *
     * @param agentId Agent ID
     * @return 执行日志列表
     */
    List<AgentExecutionLog> findByAgentId(Long agentId);

    /**
     * 根据会话ID查询执行日志列表
     *
     * @param sessionId 会话ID
     * @return 执行日志列表
     */
    List<AgentExecutionLog> findBySessionId(Long sessionId);

    /**
     * 更新执行日志
     *
     * @param log 执行日志实体
     * @return 更新后的执行日志实体
     */
    AgentExecutionLog update(AgentExecutionLog log);
}
