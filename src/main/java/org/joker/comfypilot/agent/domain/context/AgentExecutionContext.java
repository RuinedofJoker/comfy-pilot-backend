package org.joker.comfypilot.agent.domain.context;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.joker.comfypilot.agent.application.dto.AgentExecutionRequest;
import org.joker.comfypilot.agent.application.dto.AgentExecutionResponse;
import org.joker.comfypilot.agent.domain.entity.AgentExecutionLog;
import org.joker.comfypilot.agent.domain.service.Agent;

import java.util.Map;

/**
 * Agent执行上下文
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgentExecutionContext {

    private Agent agent;

    private String agentCode;

    private Long sessionId;

    private Long userId;

    private AgentExecutionRequest request;

    private AgentExecutionResponse response;

    private AgentExecutionLog executionLog;

    private Map<String, Object> agentConfig;

    private Map<String, Object> agentScope;

}
