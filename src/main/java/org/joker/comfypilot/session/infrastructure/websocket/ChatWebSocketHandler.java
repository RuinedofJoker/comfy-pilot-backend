package org.joker.comfypilot.session.infrastructure.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.joker.comfypilot.session.application.dto.WebSocketMessage;
import org.joker.comfypilot.session.application.service.ChatSessionService;
import org.joker.comfypilot.session.domain.context.WebSocketSessionContext;
import org.joker.comfypilot.session.domain.enums.WebSocketMessageType;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

/**
 * 聊天WebSocket处理器
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ChatWebSocketHandler extends TextWebSocketHandler {

    private final WebSocketSessionManager sessionManager;
    private final ChatSessionService chatSessionService;
    private final ObjectMapper objectMapper;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String sessionId = session.getId();

        // TODO: 从session中获取认证的用户ID
        // 当前暂时使用固定值,后续需要集成认证模块
        Long userId = 1L;

        sessionManager.addSession(sessionId, session, userId);

        log.info("WebSocket连接已建立: sessionId={}, userId={}", sessionId, userId);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String sessionId = session.getId();
        WebSocketSessionContext context = sessionManager.getContext(sessionId);

        if (context == null) {
            log.warn("未找到WebSocket会话上下文: sessionId={}", sessionId);
            return;
        }

        context.updateActiveTime();

        try {
            // 解析消息
            WebSocketMessage wsMessage = objectMapper.readValue(message.getPayload(), WebSocketMessage.class);
            String messageType = wsMessage.getType();

            log.info("收到WebSocket消息: sessionId={}, type={}", sessionId, messageType);

            // 根据消息类型处理
            if (WebSocketMessageType.START_SESSION.name().equals(messageType)) {
                handleStartSession(session, context, wsMessage);
            } else if (WebSocketMessageType.USER_MESSAGE.name().equals(messageType)) {
                handleUserMessage(session, context, wsMessage);
            } else if (WebSocketMessageType.INTERRUPT.name().equals(messageType)) {
                handleInterrupt(session, context, wsMessage);
            } else if (WebSocketMessageType.PING.name().equals(messageType)) {
                handlePing(session);
            } else {
                log.warn("未知的消息类型: {}", messageType);
            }

        } catch (Exception e) {
            log.error("处理WebSocket消息失败: sessionId={}, error={}", sessionId, e.getMessage(), e);
            sendErrorMessage(session, "处理消息失败: " + e.getMessage());
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        String sessionId = session.getId();
        sessionManager.removeSession(sessionId);
        log.info("WebSocket连接已关闭: sessionId={}, status={}", sessionId, status);
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        String sessionId = session.getId();
        log.error("WebSocket传输错误: sessionId={}, error={}", sessionId, exception.getMessage(), exception);
        sessionManager.removeSession(sessionId);
    }

    /**
     * 处理开始会话消息
     */
    private void handleStartSession(WebSocketSession session, WebSocketSessionContext context, WebSocketMessage wsMessage) {
        try {
            // 从消息中获取agentId
            Long agentId = ((Number) wsMessage.getData().get("agentId")).longValue();
            String title = (String) wsMessage.getData().get("title");

            // 创建聊天会话
            String sessionCode = chatSessionService.createSession(context.getUserId(), agentId, title);

            // 更新上下文
            sessionManager.updateSessionCode(session.getId(), sessionCode);

            // 发送会话创建成功消息
            WebSocketMessage response = WebSocketMessage.builder()
                    .type(WebSocketMessageType.SESSION_CREATED.name())
                    .sessionCode(sessionCode)
                    .timestamp(System.currentTimeMillis())
                    .build();

            sendMessage(session, response);

            log.info("会话已创建: sessionCode={}, agentId={}", sessionCode, agentId);

        } catch (Exception e) {
            log.error("创建会话失败: {}", e.getMessage(), e);
            sendErrorMessage(session, "创建会话失败: " + e.getMessage());
        }
    }

    /**
     * 处理用户消息
     */
    private void handleUserMessage(WebSocketSession session, WebSocketSessionContext context, WebSocketMessage wsMessage) {
        String sessionCode = wsMessage.getSessionCode();
        String content = wsMessage.getContent();

        if (sessionCode == null || content == null) {
            sendErrorMessage(session, "会话编码和消息内容不能为空");
            return;
        }

        // 检查是否可以执行
        if (!context.canExecute()) {
            sendErrorMessage(session, "当前会话正在执行中,请稍后再试");
            return;
        }

        // 异步执行Agent
        chatSessionService.sendMessageAsync(sessionCode, content, context, session);
    }

    /**
     * 处理中断消息
     */
    private void handleInterrupt(WebSocketSession session, WebSocketSessionContext context, WebSocketMessage wsMessage) {
        sessionManager.requestInterrupt(session.getId());

        WebSocketMessage response = WebSocketMessage.builder()
                .type(WebSocketMessageType.EXECUTION_INTERRUPTED.name())
                .content("执行已中断")
                .timestamp(System.currentTimeMillis())
                .build();

        sendMessage(session, response);
        log.info("执行已中断: sessionId={}", session.getId());
    }

    /**
     * 处理心跳消息
     */
    private void handlePing(WebSocketSession session) {
        WebSocketMessage response = WebSocketMessage.builder()
                .type(WebSocketMessageType.PONG.name())
                .timestamp(System.currentTimeMillis())
                .build();

        sendMessage(session, response);
    }

    /**
     * 发送消息
     */
    public void sendMessage(WebSocketSession session, WebSocketMessage message) {
        try {
            if (session.isOpen()) {
                String json = objectMapper.writeValueAsString(message);
                session.sendMessage(new TextMessage(json));
            }
        } catch (Exception e) {
            log.error("发送WebSocket消息失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 发送错误消息
     */
    private void sendErrorMessage(WebSocketSession session, String error) {
        WebSocketMessage message = WebSocketMessage.builder()
                .type(WebSocketMessageType.ERROR.name())
                .error(error)
                .timestamp(System.currentTimeMillis())
                .build();

        sendMessage(session, message);
    }
}
