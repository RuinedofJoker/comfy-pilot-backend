package org.joker.comfypilot.agent.application.service;

import lombok.RequiredArgsConstructor;
import org.joker.comfypilot.agent.application.dto.AgentExecutionDTO;
import org.joker.comfypilot.agent.domain.repository.AgentExecutionRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Agent执行应用服务
 */
@Service
@RequiredArgsConstructor
public class AgentExecutionApplicationService {

    private final AgentExecutionRepository agentExecutionRepository;

    /**
     * 获取执行记录
     */
    public AgentExecutionDTO getExecution(Long id) {
        // TODO: 实现查询逻辑
        return null;
    }

    /**
     * 获取会话的执行记录
     */
    public List<AgentExecutionDTO> getSessionExecutions(Long sessionId) {
        // TODO: 实现查询逻辑
        return List.of();
    }

    /**
     * 获取Agent的执行记录
     */
    public List<AgentExecutionDTO> getAgentExecutions(Long agentId) {
        // TODO: 实现查询逻辑
        return List.of();
    }
}
