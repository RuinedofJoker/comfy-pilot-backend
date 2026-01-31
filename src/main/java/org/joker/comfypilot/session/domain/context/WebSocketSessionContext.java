package org.joker.comfypilot.session.domain.context;

import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.joker.comfypilot.agent.domain.context.AgentExecutionContext;
import org.joker.comfypilot.common.config.JacksonConfig;
import org.joker.comfypilot.session.application.dto.WebSocketMessage;
import org.joker.comfypilot.session.application.dto.server2client.AgentPromptData;
import org.joker.comfypilot.session.domain.enums.AgentPromptType;
import org.joker.comfypilot.session.domain.enums.WebSocketMessageType;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Lock;

/**
 * WebSocket会话执行上下文
 * 管理单个WebSocket连接的执行状态
 */
@Data
@Builder
@Slf4j
public class WebSocketSessionContext {

    /**
     * WebSocket会话
     */
    private WebSocketSession webSocketSession;

    /**
     * 聊天会话编码
     */
    private String sessionCode;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 当前执行中的requestId
     */
    private AtomicReference<String> currentRequestId;

    /**
     * 创建时间
     */
    private volatile Long createTime;

    /**
     * 最后活跃时间
     */
    private Long lastActiveTime;

    /**
     * 当前Agent执行上下文
     */
    private AtomicReference<AgentExecutionContext> agentExecutionContext;

    /**
     * 消息发送锁
     */
    private Lock sendMessageLock;

    /**
     * 开始执行
     */
    public boolean startExecution(String requestId) {
        return currentRequestId.compareAndSet(null, requestId);
    }

    /**
     * 完成执行
     */
    public boolean completeExecution(String requestId) {
        if (currentRequestId.compareAndSet(requestId, null)) {
            agentExecutionContext.set(null);
            return true;
        }
        return false;
    }

    /**
     * 更新活跃时间
     */
    public void updateActiveTime() {
        this.lastActiveTime = System.currentTimeMillis();
    }

    public void sendMessage(WebSocketMessage<?> message) {
        try {
            if (webSocketSession.isOpen()) {
                String json = JacksonConfig.getObjectMapper().writeValueAsString(message);
                try {
                    getSendMessageLock().lock();
                    webSocketSession.sendMessage(new TextMessage(json));
                } finally {
                    getSendMessageLock().unlock();
                }
            }
        } catch (Exception e) {
            log.error("发送WebSocket消息失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 发送错误消息
     */
    public void sendErrorMessage(String error, String requestId) {
        WebSocketMessage<?> message = WebSocketMessage.builder()
                .type(WebSocketMessageType.AGENT_PROMPT.name())
                .sessionCode(sessionCode)
                .requestId(requestId)
                .data(AgentPromptData.builder().promptType(AgentPromptType.ERROR).message(error).build())
                .timestamp(System.currentTimeMillis())
                .build();

        sendMessage(message);
    }
}
