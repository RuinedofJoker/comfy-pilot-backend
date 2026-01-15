package org.joker.comfypilot.agent.domain.repository;

import org.joker.comfypilot.agent.domain.entity.AgentConfig;
import org.joker.comfypilot.agent.domain.enums.AgentStatus;
import org.joker.comfypilot.agent.domain.enums.AgentType;

import java.util.List;
import java.util.Optional;

/**
 * Agent配置仓储接口
 */
public interface AgentConfigRepository {

    /**
     * 保存Agent配置
     */
    AgentConfig save(AgentConfig agentConfig);

    /**
     * 根据ID查询
     */
    Optional<AgentConfig> findById(Long id);

    /**
     * 根据类型查询
     */
    List<AgentConfig> findByType(AgentType type);

    /**
     * 根据状态查询
     */
    List<AgentConfig> findByStatus(AgentStatus status);

    /**
     * 查询所有激活的Agent
     */
    List<AgentConfig> findAllActive();

    /**
     * 删除Agent配置
     */
    void deleteById(Long id);
}
