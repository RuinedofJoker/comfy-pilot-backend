package org.joker.comfypilot.session.domain.context;

import lombok.Builder;
import lombok.Data;
import org.joker.comfypilot.agent.domain.context.AgentExecutionContext;
import org.springframework.web.socket.WebSocketSession;

import java.util.concurrent.atomic.AtomicReference;

/**
 * WebSocket会话执行上下文
 * 管理单个WebSocket连接的执行状态
 */
@Data
@Builder
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
}
