package org.joker.comfypilot.session.infrastructure.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.data.message.ChatMessage;
import lombok.extern.slf4j.Slf4j;
import org.joker.comfypilot.agent.domain.callback.AgentCallback;
import org.joker.comfypilot.agent.domain.context.AgentExecutionContext;
import org.joker.comfypilot.agent.infrastructure.memory.ChatMemoryChatMemoryStore;
import org.joker.comfypilot.common.enums.MessageRole;
import org.joker.comfypilot.common.exception.BusinessException;
import org.joker.comfypilot.common.util.SpringContextUtil;
import org.joker.comfypilot.session.application.dto.ChatMessageDTO;
import org.joker.comfypilot.session.application.dto.WebSocketMessage;
import org.joker.comfypilot.session.application.dto.server2client.AgentCompleteResponseData;
import org.joker.comfypilot.session.application.dto.server2client.AgentPromptData;
import org.joker.comfypilot.session.application.dto.server2client.AgentToolCallRequestData;
import org.joker.comfypilot.session.application.service.ChatSessionService;
import org.joker.comfypilot.session.domain.context.WebSocketSessionContext;
import org.joker.comfypilot.session.domain.enums.AgentPromptType;
import org.joker.comfypilot.session.domain.enums.WebSocketMessageType;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

/**
 * WebSocket输出回调实现
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
    private final ChatSessionService chatSessionService;

    private final StringBuffer chunkBuffer = new StringBuffer();
    private volatile long lastSendChunkTime = 0;

    public WebSocketAgentCallback(WebSocketSession webSocketSession, WebSocketSessionContext sessionContext, AgentExecutionContext agentExecutionContext, String sessionCode, String requestId, ObjectMapper objectMapper) {
        this.webSocketSession = webSocketSession;
        this.sessionContext = sessionContext;
        this.agentExecutionContext = agentExecutionContext;
        this.sessionCode = sessionCode;
        this.requestId = requestId;
        this.objectMapper = objectMapper;

        this.chatMemoryChatMemoryStore = SpringContextUtil.getBean(ChatMemoryChatMemoryStore.class);
        this.chatSessionService = SpringContextUtil.getBean(ChatSessionService.class);
    }

    @Override
    public void onPrompt(AgentPromptType promptType, String message, boolean needSave) {
        log.debug("Agent提示: sessionCode={}, promptType={}, message={}", sessionCode, promptType, message);

        if (AgentPromptType.TERMINAL_OUTPUT_END.equals(promptType) && !chunkBuffer.isEmpty()) {
            sendMessage(WebSocketMessageType.AGENT_STREAM, chunkBuffer.toString());
            chunkBuffer.setLength(0);
            lastSendChunkTime = System.currentTimeMillis();
        }

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

        if (needSave) {
            if (AgentPromptType.TODO_WRITE.equals(promptType)) {
                ChatMessageDTO agentPlanChatMessage = ChatMessageDTO.builder()
                        .sessionId(agentExecutionContext.getSessionId())
                        .sessionCode(sessionCode)
                        .requestId(agentExecutionContext.getRequestId())
                        .role(MessageRole.AGENT_PLAN.name())
                        .metadata(new HashMap<>())
                        .content(message)
                        .chatContent(null)
                        .build();
                chatSessionService.saveMessage(agentPlanChatMessage);
            } else if (AgentPromptType.STATUS_UPDATE.equals(promptType)) {
                ChatMessageDTO agentPlanChatMessage = ChatMessageDTO.builder()
                        .sessionId(agentExecutionContext.getSessionId())
                        .sessionCode(sessionCode)
                        .requestId(agentExecutionContext.getRequestId())
                        .role(MessageRole.AGENT_STATUS.name())
                        .metadata(new HashMap<>())
                        .content(message)
                        .chatContent(null)
                        .build();
                chatSessionService.saveMessage(agentPlanChatMessage);
            } else if (AgentPromptType.AGENT_MESSAGE_BLOCK.equals(promptType)) {
                ChatMessageDTO agentMessageChatMessage = ChatMessageDTO.builder()
                        .sessionId(agentExecutionContext.getSessionId())
                        .sessionCode(sessionCode)
                        .requestId(agentExecutionContext.getRequestId())
                        .role(MessageRole.AGENT_MESSAGE.name())
                        .metadata(new HashMap<>())
                        .content(message)
                        .chatContent(null)
                        .build();
                chatSessionService.saveMessage(agentMessageChatMessage);
            } else if (AgentPromptType.ERROR.equals(promptType)) {
                ChatMessageDTO agentMessageChatMessage = ChatMessageDTO.builder()
                        .sessionId(agentExecutionContext.getSessionId())
                        .sessionCode(sessionCode)
                        .requestId(agentExecutionContext.getRequestId())
                        .role(MessageRole.AGENT_ERROR.name())
                        .metadata(new HashMap<>())
                        .content(message)
                        .chatContent(null)
                        .build();
                chatSessionService.saveMessage(agentMessageChatMessage);
            }
        }
    }

    @Override
    public void onStream(String chunk) {
        chunkBuffer.append(chunk);
        if (System.currentTimeMillis() - lastSendChunkTime > 200 || chunkBuffer.length() >= 100) {
            sendMessage(WebSocketMessageType.AGENT_STREAM, chunkBuffer.toString());
            chunkBuffer.setLength(0);
            lastSendChunkTime = System.currentTimeMillis();
        }
    }

    @Override
    public void onToolCall(boolean isClientTool, boolean isMcpTool, String toolCallId, String toolName, String toolArgs) {
        log.info("Agent调用工具: sessionCode={}, tool={}, args={}, isClientTool={}, isMcpTool={}", sessionCode, toolName, toolArgs, isClientTool, isMcpTool);

        // 构建工具调用请求数据
        AgentToolCallRequestData requestData = AgentToolCallRequestData.builder()
                .toolCallId(toolCallId)
                .toolName(toolName)
                .toolArgs(toolArgs)
                .isClientTool(isClientTool)
                .isMcpTool(isMcpTool)
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
        log.info("Agent输出执行完成: sessionCode={}", sessionCode);

        // 非流式调用需要返回
        throw new BusinessException("暂不支持非流式调用");
    }

    @Override
    public void onStreamComplete(Map<String, Object> agentScope, String fullContent, Integer inputTokens, Integer outputTokens, Integer totalTokens, Integer messageCount) {
        log.info("Agent流式输出执行完成: sessionCode={}", sessionCode);

        if (!chunkBuffer.isEmpty()) {
            sendMessage(WebSocketMessageType.AGENT_STREAM, chunkBuffer.toString());
            chunkBuffer.setLength(0);
            lastSendChunkTime = System.currentTimeMillis();
        }

        // 流式调用不需要返回
        WebSocketMessage<?> message = WebSocketMessage.builder()
                .type(WebSocketMessageType.AGENT_COMPLETE.name())
                .sessionCode(sessionCode)
                .requestId(requestId)
                .content(null)
                .data(AgentCompleteResponseData.builder()
                        .maxTokens(Integer.parseInt(agentScope.get("MaxTokens").toString()))
                        .maxMessages(Integer.parseInt(agentScope.get("MaxMessages").toString()))
                        .inputTokens(inputTokens)
                        .outputTokens(outputTokens)
                        .totalTokens(totalTokens)
                        .messageCount(messageCount)
                        .build())
                .timestamp(System.currentTimeMillis())
                .build();

        sendWebSocketMessage(message);
    }

    @Override
    public void onInterrupted() {
        if (sessionContext.completeExecution(requestId)) {
            onPrompt(AgentPromptType.INTERRUPTED, "用户中断", false);
        }
    }

    @Override
    public boolean isInterrupted() {
        return agentExecutionContext.getInterrupted().get();
    }

    @Override
    public void addMemoryMessage(ChatMessage message, Consumer<ChatMessage> successCallback, Consumer<ChatMessage> failCallback) {
        if (chatMemoryChatMemoryStore.addMessage(agentExecutionContext.getConnectSessionId(), message)) {
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
        List<ChatMessage> messages = chatMemoryChatMemoryStore.getMessages(agentExecutionContext.getConnectSessionId());
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
                try {
                    sessionContext.getSendMessageLock().lock();
                    webSocketSession.sendMessage(new TextMessage(json));
                } finally {
                    sessionContext.getSendMessageLock().unlock();
                }
            } else {
                log.warn("WebSocket连接已关闭,无法发送消息: sessionCode={}", sessionCode);
                throw new BusinessException("WebSocket连接已关闭,无法发送消息");
            }
        } catch (Exception e) {
            log.error("发送WebSocket消息失败: sessionCode={}, error={}", sessionCode, e.getMessage(), e);
            throw new BusinessException("发送WebSocket消息失败");
        }
    }
}
