package org.joker.comfypilot.agent.domain.repository;

import org.joker.comfypilot.agent.domain.entity.AgentConfig;

import java.util.List;
import java.util.Optional;

/**
 * Agent配置仓储接口
 */
public interface AgentConfigRepository {

    /**
     * 保存Agent配置
     *
     * @param agentConfig Agent配置实体
     * @return 保存后的Agent配置实体
     */
    AgentConfig save(AgentConfig agentConfig);

    /**
     * 根据ID查询Agent配置
     *
     * @param id Agent ID
     * @return Agent配置实体
     */
    Optional<AgentConfig> findById(Long id);

    /**
     * 根据Agent编码查询Agent配置
     *
     * @param agentCode Agent编码
     * @return Agent配置实体
     */
    Optional<AgentConfig> findByAgentCode(String agentCode);

    /**
     * 查询所有Agent配置列表
     *
     * @return Agent配置列表
     */
    List<AgentConfig> findAll();

    /**
     * 更新Agent配置
     *
     * @param agentConfig Agent配置实体
     * @return 更新后的Agent配置实体
     */
    AgentConfig update(AgentConfig agentConfig);

    /**
     * 删除Agent配置（逻辑删除）
     *
     * @param id Agent ID
     */
    void deleteById(Long id);
}
