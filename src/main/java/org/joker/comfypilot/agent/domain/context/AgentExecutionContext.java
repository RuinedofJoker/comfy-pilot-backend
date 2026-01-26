package org.joker.comfypilot.agent.domain.context;

import dev.langchain4j.model.chat.response.ChatResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.joker.comfypilot.agent.application.dto.AgentExecutionRequest;
import org.joker.comfypilot.agent.application.dto.AgentExecutionResponse;
import org.joker.comfypilot.agent.domain.callback.AgentCallback;
import org.joker.comfypilot.agent.domain.entity.AgentExecutionLog;
import org.joker.comfypilot.agent.domain.enums.ExecutionContextConnectType;
import org.joker.comfypilot.agent.domain.event.AgentEventPublisher;
import org.joker.comfypilot.agent.domain.service.Agent;
import org.joker.comfypilot.session.domain.context.WebSocketSessionContext;
import org.joker.comfypilot.tool.domain.service.Tool;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Agent执行上下文
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgentExecutionContext {

    /**
     * 执行Agent
     */
    private Agent agent;

    /**
     * Agent编码
     */
    private String agentCode;

    /**
     * 执行上下文连接类型
     */
    private ExecutionContextConnectType executionContextConnectType;

    /**
     * 执行上下文连接ID
     */
    private String connectSessionId;

    /**
     * 连接使用的WebSocket上下文
     * executionContextConnectType = WEBSOCKET时使用
     */
    private WebSocketSessionContext webSocketSessionContext;

    /**
     * 用户会话ID
     */
    private Long sessionId;

    /**
     * 用户会话编码
     */
    private String sessionCode;

    /**
     * 当前执行请求ID(一般是请求时的时间戳)
     */
    private volatile String requestId;

    /**
     * 所属用户ID
     */
    private Long userId;

    /**
     * 开始时间
     */
    private Long startTime;

    private AgentExecutionRequest request;

    private AgentExecutionResponse response;

    /**
     * Agent执行日志
     */
    private AgentExecutionLog executionLog;

    /**
     * 运行时Agent配置
     */
    private Map<String, Object> agentConfig;

    /**
     * 运行时AgentScope
     */
    private Map<String, Object> agentScope;

    /**
     * 客户端工具
     */
    private List<? extends Tool> clientTools;

    /**
     * 客户端工具名称
     */
    private Set<String> clientToolNames;

    /**
     * MCP工具
     */
    private List<? extends Tool> mcpTools;

    /**
     * MCP工具名称
     */
    private Set<String> mcpToolNames;

    /**
     * 输出回调
     */
    private AgentCallback agentCallback;

    /**
     * 事件发布器（用于发布和监听 Agent 执行过程中的事件）
     */
    private AgentEventPublisher eventPublisher;

    /**
     * 是否中断
     */
    private AtomicBoolean interrupted;

    /**
     * 最后一次LLM执行
     */
    private AtomicReference<CompletableFuture<ChatResponse>> lastLLMFuture;

    /**
     * 检查是否被中断
     */
    public boolean isInterrupted() {
        return interrupted.get();
    }

}
