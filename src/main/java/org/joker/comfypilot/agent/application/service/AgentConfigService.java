package org.joker.comfypilot.agent.application.service;

import org.joker.comfypilot.agent.application.dto.AgentConfigDTO;
import org.joker.comfypilot.agent.application.dto.AgentRuntimeConfigDTO;

import java.util.List;
import java.util.Map;

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

    /**
     * 校验并转换Agent配置JSON
     * 根据AgentConfigDefinition定义校验配置JSON的格式和值，并转换为Map
     *
     * @param agentConfigDTO Agent配置DTO（包含agentConfigDefinitions）
     * @param agentConfigJson Agent配置JSON字符串
     * @return 校验并转换后的配置Map
     * @throws org.joker.comfypilot.common.exception.ValidationException 当配置不符合定义时抛出
     */
    Map<String, Object> validateAndParseAgentConfig(AgentConfigDTO agentConfigDTO, String agentConfigJson);
}
