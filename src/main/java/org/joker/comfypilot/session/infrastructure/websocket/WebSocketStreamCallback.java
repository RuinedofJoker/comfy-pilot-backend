package org.joker.comfypilot.session.infrastructure.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.joker.comfypilot.agent.domain.callback.StreamCallback;
import org.joker.comfypilot.session.application.dto.WebSocketMessage;
import org.joker.comfypilot.session.domain.context.WebSocketSessionContext;
import org.joker.comfypilot.session.domain.enums.WebSocketMessageType;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

/**
 * WebSocket流式输出回调实现
 */
@Slf4j
@RequiredArgsConstructor
public class WebSocketStreamCallback implements StreamCallback {

    private final WebSocketSession webSocketSession;
    private final WebSocketSessionContext sessionContext;
    private final String sessionCode;
    private final ObjectMapper objectMapper;

    @Override
    public void onThinking() {
        log.debug("Agent开始思考: sessionCode={}", sessionCode);
        sendMessage(WebSocketMessageType.AGENT_THINKING, "Agent正在思考中...");
    }

    @Override
    public void onStream(String chunk) {
        log.debug("Agent流式输出: sessionCode={}, chunk={}", sessionCode, chunk);
        sendMessage(WebSocketMessageType.AGENT_STREAM, chunk);
    }

    @Override
    public void onToolCall(String toolName, String toolArgs) {
        log.info("Agent调用工具: sessionCode={}, tool={}, args={}", sessionCode, toolName, toolArgs);

        WebSocketMessage message = WebSocketMessage.builder()
                .type(WebSocketMessageType.AGENT_TOOL_CALL.name())
                .sessionCode(sessionCode)
                .content(toolName)
                .data(java.util.Map.of("toolName", toolName, "toolArgs", toolArgs))
                .timestamp(System.currentTimeMillis())
                .build();

        sendWebSocketMessage(message);
    }

    @Override
    public void onRequestInput(String prompt) {
        log.info("Agent请求用户输入: sessionCode={}, prompt={}", sessionCode, prompt);
        sendMessage(WebSocketMessageType.AGENT_REQUEST_INPUT, prompt);
    }

    @Override
    public void onComplete(String fullContent) {
        log.info("Agent执行完成: sessionCode={}", sessionCode);
        sendMessage(WebSocketMessageType.AGENT_COMPLETE, fullContent);

        // 标记执行完成
        sessionContext.completeExecution();
    }

    @Override
    public void onError(String error) {
        log.error("Agent执行错误: sessionCode={}, error={}", sessionCode, error);

        WebSocketMessage message = WebSocketMessage.builder()
                .type(WebSocketMessageType.ERROR.name())
                .sessionCode(sessionCode)
                .error(error)
                .timestamp(System.currentTimeMillis())
                .build();

        sendWebSocketMessage(message);

        // 标记执行完成
        sessionContext.completeExecution();
    }

    @Override
    public boolean isInterrupted() {
        return sessionContext.isInterrupted();
    }

    /**
     * 发送简单消息
     */
    private void sendMessage(WebSocketMessageType type, String content) {
        WebSocketMessage message = WebSocketMessage.builder()
                .type(type.name())
                .sessionCode(sessionCode)
                .content(content)
                .timestamp(System.currentTimeMillis())
                .build();

        sendWebSocketMessage(message);
    }

    /**
     * 发送WebSocket消息
     */
    private void sendWebSocketMessage(WebSocketMessage message) {
        try {
            if (webSocketSession.isOpen()) {
                String json = objectMapper.writeValueAsString(message);
                webSocketSession.sendMessage(new TextMessage(json));
            } else {
                log.warn("WebSocket连接已关闭,无法发送消息: sessionCode={}", sessionCode);
            }
        } catch (Exception e) {
            log.error("发送WebSocket消息失败: sessionCode={}, error={}", sessionCode, e.getMessage(), e);
        }
    }
}
