package org.joker.comfypilot.session.infrastructure.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.data.message.ChatMessage;
import lombok.extern.slf4j.Slf4j;
import org.joker.comfypilot.agent.domain.callback.AgentCallback;
import org.joker.comfypilot.agent.domain.context.AgentExecutionContext;
import org.joker.comfypilot.agent.infrastructure.memory.ChatMemoryChatMemoryStore;
import org.joker.comfypilot.common.exception.BusinessException;
import org.joker.comfypilot.common.util.SpringContextUtil;
import org.joker.comfypilot.session.application.dto.WebSocketMessage;
import org.joker.comfypilot.session.application.dto.server2client.AgentPromptData;
import org.joker.comfypilot.session.application.dto.server2client.AgentToolCallRequestData;
import org.joker.comfypilot.session.domain.context.WebSocketSessionContext;
import org.joker.comfypilot.session.domain.enums.AgentPromptType;
import org.joker.comfypilot.session.domain.enums.WebSocketMessageType;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.util.List;
import java.util.function.Consumer;

/**
 * WebSocket流式输出回调实现
 */
@Slf4j
public class WebSocketAgentCallback implements AgentCallback {

    private final WebSocketSession webSocketSession;
    private final WebSocketSessionContext sessionContext;
    private final AgentExecutionContext agentExecutionContext;
    private final String sessionCode;
    private final String requestId;
    private final ObjectMapper objectMapper;

    private final ChatMemoryChatMemoryStore chatMemoryChatMemoryStore;

    public WebSocketAgentCallback(WebSocketSession webSocketSession, WebSocketSessionContext sessionContext, AgentExecutionContext agentExecutionContext, String sessionCode, String requestId, ObjectMapper objectMapper) {
        this.webSocketSession = webSocketSession;
        this.sessionContext = sessionContext;
        this.agentExecutionContext = agentExecutionContext;
        this.sessionCode = sessionCode;
        this.requestId = requestId;
        this.objectMapper = objectMapper;

        this.chatMemoryChatMemoryStore = SpringContextUtil.getBean(ChatMemoryChatMemoryStore.class);
    }

    @Override
    public void onPrompt(AgentPromptType promptType, String message) {
        log.debug("Agent提示: sessionCode={}, promptType={}, message={}", sessionCode, promptType, message);

        // 构建提示数据
        AgentPromptData promptData = AgentPromptData.builder()
                .promptType(promptType)
                .message(message != null ? message : promptType.getDefaultMessage())
                .build();

        WebSocketMessage<AgentPromptData> wsMessage = WebSocketMessage.<AgentPromptData>builder()
                .type(WebSocketMessageType.AGENT_PROMPT.name())
                .sessionCode(sessionCode)
                .requestId(requestId)
                .data(promptData)
                .timestamp(System.currentTimeMillis())
                .build();

        sendWebSocketMessage(wsMessage);
    }

    @Override
    public void onStream(String chunk) {
        log.debug("Agent流式输出: sessionCode={}, chunk={}", sessionCode, chunk);
        sendMessage(WebSocketMessageType.AGENT_STREAM, chunk);
    }

    @Override
    public void onToolCall(boolean isClientTool, String toolCallId, String toolName, String toolArgs) {
        log.info("Agent调用工具: sessionCode={}, tool={}, args={}, isClientTool={}", sessionCode, toolName, toolArgs, isClientTool);

        // 构建工具调用请求数据
        AgentToolCallRequestData requestData = AgentToolCallRequestData.builder()
                .toolCallId(toolCallId)
                .toolName(toolName)
                .toolArgs(toolArgs)
                .isClientTool(isClientTool)
                .build();

        WebSocketMessage<AgentToolCallRequestData> message = WebSocketMessage.<AgentToolCallRequestData>builder()
                .type(WebSocketMessageType.AGENT_TOOL_CALL_REQUEST.name())
                .sessionCode(sessionCode)
                .requestId(requestId)
                .content(toolName)
                .data(requestData)
                .timestamp(System.currentTimeMillis())
                .build();

        sendWebSocketMessage(message);
    }

    @Override
    public void onComplete(String fullContent) {
        log.info("Agent执行完成: sessionCode={}", sessionCode);

        // 非流式调用需要返回
        throw new BusinessException("暂不支持非流式调用");
    }

    @Override
    public void onStreamComplete(String fullContent) {
        log.info("Agent流式执行完成: sessionCode={}", sessionCode);

        // 流式调用不需要返回
        sendMessage(WebSocketMessageType.AGENT_COMPLETE, null);

        // 标记执行完成
        sessionContext.completeExecution();
    }

    @Override
    public boolean isInterrupted() {
        return sessionContext.isInterrupted();
    }

    @Override
    public void addMemoryMessage(ChatMessage message, Consumer<ChatMessage> successCallback, Consumer<ChatMessage> failCallback) {
        if (chatMemoryChatMemoryStore.addMessage(agentExecutionContext.getWsSessionId(), message)) {
            if (successCallback != null) {
                successCallback.accept(message);
            }
        } else {
            if (failCallback != null) {
                failCallback.accept(message);
            }
            throw new BusinessException("当前会话已关闭");
        }
    }

    @Override
    public List<ChatMessage> getMemoryMessages() {
        List<ChatMessage> messages = chatMemoryChatMemoryStore.getMessages(agentExecutionContext.getWsSessionId());
        if (messages == null) {
            throw new BusinessException("当前会话已关闭");
        }
        return messages;
    }

    /**
     * 发送简单消息
     */
    private void sendMessage(WebSocketMessageType type, String content) {
        WebSocketMessage<?> message = WebSocketMessage.builder()
                .type(type.name())
                .sessionCode(sessionCode)
                .requestId(requestId)
                .content(content)
                .timestamp(System.currentTimeMillis())
                .build();

        sendWebSocketMessage(message);
    }

    /**
     * 发送WebSocket消息
     */
    private void sendWebSocketMessage(WebSocketMessage<?> message) {
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
