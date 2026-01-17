package org.joker.comfypilot.session.infrastructure.websocket;

import lombok.extern.slf4j.Slf4j;
import org.joker.comfypilot.session.domain.context.WebSocketSessionContext;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * WebSocket会话管理器
 * 管理所有活跃的WebSocket连接
 */
@Slf4j
@Component
public class WebSocketSessionManager {

    /**
     * 存储所有活跃的WebSocket会话
     * Key: WebSocket Session ID
     * Value: WebSocketSessionContext
     */
    private final Map<String, WebSocketSessionContext> sessions = new ConcurrentHashMap<>();

    /**
     * 添加会话
     */
    public void addSession(String sessionId, WebSocketSession webSocketSession, Long userId) {
        WebSocketSessionContext context = WebSocketSessionContext.builder()
                .webSocketSession(webSocketSession)
                .userId(userId)
                .executing(new AtomicBoolean(false))
                .interrupted(new AtomicBoolean(false))
                .createTime(System.currentTimeMillis())
                .lastActiveTime(System.currentTimeMillis())
                .build();

        sessions.put(sessionId, context);
        log.info("WebSocket会话已添加: sessionId={}, userId={}", sessionId, userId);
    }

    /**
     * 获取会话上下文
     */
    public WebSocketSessionContext getContext(String sessionId) {
        return sessions.get(sessionId);
    }

    /**
     * 移除会话
     */
    public void removeSession(String sessionId) {
        WebSocketSessionContext context = sessions.remove(sessionId);
        if (context != null) {
            log.info("WebSocket会话已移除: sessionId={}", sessionId);
        }
    }

    /**
     * 更新会话的聊天会话编码
     */
    public void updateSessionCode(String sessionId, String sessionCode) {
        WebSocketSessionContext context = sessions.get(sessionId);
        if (context != null) {
            context.setSessionCode(sessionCode);
            context.updateActiveTime();
        }
    }

    /**
     * 请求中断会话执行
     */
    public void requestInterrupt(String sessionId) {
        WebSocketSessionContext context = sessions.get(sessionId);
        if (context != null) {
            context.requestInterrupt();
            log.info("请求中断会话执行: sessionId={}", sessionId);
        }
    }

    /**
     * 获取所有活跃会话数量
     */
    public int getActiveSessionCount() {
        return sessions.size();
    }
}
