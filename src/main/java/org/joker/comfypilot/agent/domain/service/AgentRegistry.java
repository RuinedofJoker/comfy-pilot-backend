package org.joker.comfypilot.agent.domain.service;

import java.util.List;

/**
 * Agent注册中心接口
 * 负责管理所有Agent实例
 */
public interface AgentRegistry {

    /**
     * 获取所有Agent实例
     *
     * @return Agent实例列表
     */
    List<Agent> getAllAgents();

    /**
     * 根据Agent编码获取Agent实例
     *
     * @param agentCode Agent编码
     * @return Agent实例，如果不存在返回null
     */
    Agent getAgentByCode(String agentCode);

    /**
     * 检查Agent是否存在
     *
     * @param agentCode Agent编码
     * @return true-存在，false-不存在
     */
    boolean exists(String agentCode);
}
