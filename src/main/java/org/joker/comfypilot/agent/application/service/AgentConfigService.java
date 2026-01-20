package org.joker.comfypilot.agent.application.service;

import org.joker.comfypilot.agent.application.dto.AgentConfigDTO;
import org.joker.comfypilot.agent.application.dto.AgentRuntimeConfigDTO;

import java.util.List;

/**
 * Agent配置服务接口
 */
public interface AgentConfigService {

    /**
     * 获取所有Agent配置
     *
     * @return Agent配置列表
     */
    List<AgentConfigDTO> getAllAgents();

    /**
     * 获取所有已启用的Agent配置
     *
     * @return 已启用的Agent配置列表
     */
    List<AgentRuntimeConfigDTO> getEnabledRuntimeAgents();

    /**
     * 根据ID获取Agent配置
     *
     * @param id Agent ID
     * @return Agent配置
     */
    AgentConfigDTO getAgentById(Long id);

    /**
     * 根据CODE获取Agent配置
     *
     * @param agentCode Agent CODE
     * @return Agent配置
     */
    AgentConfigDTO getAgentByCode(String agentCode);

    /**
     * 根据编码获取Agent配置
     *
     * @param agentCode Agent编码
     * @return Agent配置
     */
    AgentRuntimeConfigDTO getRuntimeAgentByCode(String agentCode);

    /**
     * 启用Agent
     *
     * @param id Agent ID
     */
    void enableAgent(Long id);

    /**
     * 禁用Agent
     *
     * @param id Agent ID
     */
    void disableAgent(Long id);
}
