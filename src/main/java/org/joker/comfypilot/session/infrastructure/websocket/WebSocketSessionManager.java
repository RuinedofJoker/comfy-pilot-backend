package org.joker.comfypilot.session.infrastructure.websocket;

import lombok.extern.slf4j.Slf4j;
import org.joker.comfypilot.session.domain.context.WebSocketSessionContext;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
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
    private final Map<String, WebSocketSessionContext> sessions = new ConcurrentHashMap<>(1024);

    private final Map<String, ConcurrentLinkedQueue<Runnable>> sessionRemoveCallbacks = new ConcurrentHashMap<>(1024);

    /**
     * 添加会话
     */
    public void addSession(String wsSessionId, WebSocketSession webSocketSession, Long userId, String sessionCode) {
        WebSocketSessionContext context = WebSocketSessionContext.builder()
                .webSocketSession(webSocketSession)
                .userId(userId)
                .sessionCode(sessionCode)
                .executing(new AtomicBoolean(false))
                .interrupted(new AtomicBoolean(false))
                .createTime(System.currentTimeMillis())
                .lastActiveTime(System.currentTimeMillis())
                .build();

        sessionRemoveCallbacks.put(wsSessionId, new ConcurrentLinkedQueue<>());
        sessions.put(wsSessionId, context);
        log.info("WebSocket会话已添加: wsSessionId={}, userId={}", wsSessionId, userId);
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
        ConcurrentLinkedQueue<Runnable> removedCallbacks = sessionRemoveCallbacks.remove(sessionId);
        if (removedCallbacks != null) {
            Runnable polled = removedCallbacks.poll();
            while (polled != null) {
                polled.run();
                polled = removedCallbacks.poll();
            }
        }
    }

    /**
     * 添加会话移除时触发的回调
     */
    public void addRemovedCallback(String sessionId, Runnable callback) {
        if (sessionId == null || callback == null) {
            return;
        }
        if (!sessions.containsKey(sessionId)) {
            callback.run();
            return;
        }
        ConcurrentLinkedQueue<Runnable> removedCallbacks = sessionRemoveCallbacks.get(sessionId);
        if (removedCallbacks == null) {
            callback.run();
            return;
        }
        removedCallbacks.offer(callback);
        if (!sessions.containsKey(sessionId)) {
            callback = removedCallbacks.poll();
            while (callback != null) {
                callback.run();
                callback = removedCallbacks.poll();
            }
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

}
