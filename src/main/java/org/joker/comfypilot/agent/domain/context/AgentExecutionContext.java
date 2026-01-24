package org.joker.comfypilot.agent.domain.context;

import dev.langchain4j.data.message.ChatMessage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.joker.comfypilot.agent.application.dto.AgentExecutionRequest;
import org.joker.comfypilot.agent.application.dto.AgentExecutionResponse;
import org.joker.comfypilot.agent.domain.callback.AgentCallback;
import org.joker.comfypilot.agent.domain.entity.AgentExecutionLog;
import org.joker.comfypilot.agent.domain.event.AgentEventPublisher;
import org.joker.comfypilot.agent.domain.service.Agent;
import org.joker.comfypilot.session.domain.context.WebSocketSessionContext;
import org.joker.comfypilot.tool.domain.service.Tool;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

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

    private Set<String> clientToolNames;

    private AgentCallback agentCallback;

    /**
     * 事件发布器（用于发布和监听 Agent 执行过程中的事件）
     */
    private AgentEventPublisher eventPublisher;

    /**
     * 检查是否被中断
     */
    public boolean isInterrupted() {
        return agentCallback != null && agentCallback.isInterrupted();
    }

    /**
     * 获取当前需要加载的所有工具 TODO
     * @param serverToolSets 服务端工具集
     * @return 整合服务端和客户端的所有工具
     */
    public Map<String, ? extends Tool> allTools(List<Class<?>> serverToolSets) {
        return new HashMap<>();
    }

}
