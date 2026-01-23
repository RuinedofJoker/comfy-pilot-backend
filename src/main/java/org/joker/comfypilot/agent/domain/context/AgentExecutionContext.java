package org.joker.comfypilot.agent.domain.context;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.joker.comfypilot.agent.application.dto.AgentExecutionRequest;
import org.joker.comfypilot.agent.application.dto.AgentExecutionResponse;
import org.joker.comfypilot.agent.domain.callback.AgentCallback;
import org.joker.comfypilot.agent.domain.entity.AgentExecutionLog;
import org.joker.comfypilot.agent.domain.service.Agent;
import org.joker.comfypilot.session.domain.context.WebSocketSessionContext;
import org.joker.comfypilot.tool.domain.service.Tool;

import java.util.List;
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

    private String wsSessionId;

    private WebSocketSessionContext webSocketSessionContext;

    private Long sessionId;

    private String sessionCode;

    private String requestId;

    private Long userId;

    private AgentExecutionRequest request;

    private AgentExecutionResponse response;

    private AgentExecutionLog executionLog;

    private Map<String, Object> agentConfig;

    private Map<String, Object> agentScope;

    private List<? extends Tool> clientTools;

    private AgentCallback agentCallback;

    /**
     * 检查是否被中断
     */
    public boolean isInterrupted() {
        return agentCallback != null && agentCallback.isInterrupted();
    }

}
