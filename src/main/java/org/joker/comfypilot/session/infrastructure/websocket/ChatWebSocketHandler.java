package org.joker.comfypilot.session.infrastructure.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.joker.comfypilot.common.constant.AuthConstants;
import org.joker.comfypilot.session.application.dto.WebSocketMessage;
import org.joker.comfypilot.session.application.service.ChatSessionService;
import org.joker.comfypilot.session.domain.context.WebSocketSessionContext;
import org.joker.comfypilot.session.domain.enums.WebSocketMessageType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.Map;

/**
 * 聊天WebSocket处理器
 */
@Slf4j
@Component
public class ChatWebSocketHandler extends TextWebSocketHandler {

    @Autowired
    private WebSocketSessionManager sessionManager;
    @Lazy
    @Autowired
    private ChatSessionService chatSessionService;
    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String wsSessionId = session.getId();

        // 从WebSocket会话属性中获取已认证的用户ID和sessionCode
        Long userId = (Long) session.getAttributes().get(AuthConstants.USER_ID_ATTRIBUTE);
        String sessionCode = (String) session.getAttributes().get(AuthConstants.SESSION_CODE_ATTRIBUTE);

        if (userId == null) {
            log.error("WebSocket连接建立失败: 未找到用户ID");
            session.close();
            return;
        }
        if (StringUtils.isBlank(sessionCode)) {
            log.error("WebSocket连接建立失败: 未找到sessionCode");
            session.close();
            return;
        }

        sessionManager.addSession(wsSessionId, session, userId, sessionCode);

        log.info("WebSocket连接已建立: sessionId={}, userId={}", wsSessionId, userId);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String wsSessionId = session.getId();
        WebSocketSessionContext context = sessionManager.getContext(wsSessionId);

        if (context == null) {
            log.warn("未找到WebSocket会话上下文: wsSessionId={}", wsSessionId);
            return;
        }

        context.updateActiveTime();

        try {
            // 解析消息
            WebSocketMessage wsMessage = objectMapper.readValue(message.getPayload(), WebSocketMessage.class);
            String messageType = wsMessage.getType();

            log.info("收到WebSocket消息: wsSessionId={}, type={}", wsSessionId, messageType);

            // 根据消息类型处理
            if (WebSocketMessageType.USER_MESSAGE.name().equals(messageType)) {
                handleUserMessage(session, context, wsMessage);
            } else if (WebSocketMessageType.USER_RESPONSE.name().equals(messageType)) {
                handleUserResponse(session, context, wsMessage);
            } else if (WebSocketMessageType.INTERRUPT.name().equals(messageType)) {
                handleInterrupt(session, context, wsMessage);
            } else if (WebSocketMessageType.PING.name().equals(messageType)) {
                handlePing(session);
            } else {
                log.warn("未知的消息类型: {}", messageType);
            }

        } catch (Exception e) {
            log.error("处理WebSocket消息失败: wsSessionId={}, error={}", wsSessionId, e.getMessage(), e);
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
     * 处理用户消息
     */
    private void handleUserMessage(WebSocketSession session, WebSocketSessionContext context, WebSocketMessage wsMessage) {
        String sessionCode = wsMessage.getSessionCode();
        String content = wsMessage.getContent();
        Map<String, Object> data = wsMessage.getData();

        if (sessionCode == null || content == null) {
            sendErrorMessage(session, "会话编码、消息内容不能为空");
            return;
        }

        // 检查是否可以执行
        if (!context.canExecute()) {
            sendErrorMessage(session, "当前会话正在执行中,请稍后再试");
            return;
        }

        // 异步执行Agent（传递agentCode）
        chatSessionService.sendMessageAsync(sessionCode, content, data, context, session);
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
     * 处理用户响应消息
     */
    private void handleUserResponse(WebSocketSession session, WebSocketSessionContext context, WebSocketMessage wsMessage) {
        String content = wsMessage.getContent();

        if (content == null || content.isEmpty()) {
            sendErrorMessage(session, "用户响应内容不能为空");
            return;
        }

        // 检查是否正在等待用户输入
        if (!context.isWaitingForUserInput()) {
            log.warn("收到用户响应但未在等待状态: sessionId={}", session.getId());
            sendErrorMessage(session, "当前不在等待用户输入状态");
            return;
        }

        // 提供用户响应，完成Future
        context.provideUserResponse(content);
        log.info("已接收用户响应: sessionId={}, content={}", session.getId(), content);
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
