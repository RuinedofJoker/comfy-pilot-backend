package org.joker.comfypilot.session.infrastructure.websocket;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.agent.tool.ToolExecutionRequest;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.ToolExecutionResultMessage;
import dev.langchain4j.data.message.UserMessage;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.joker.comfypilot.agent.infrastructure.memory.ChatMemoryChatMemoryStore;
import org.joker.comfypilot.common.constant.AuthConstants;
import org.joker.comfypilot.session.application.dto.ChatMessageDTO;
import org.joker.comfypilot.session.application.dto.WebSocketMessage;
import org.joker.comfypilot.session.application.dto.client2server.AgentToolCallResponseData;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 聊天WebSocket处理器
 */
@Slf4j
@Component
public class ChatWebSocketHandler extends TextWebSocketHandler {

    @Autowired
    private WebSocketSessionManager sessionManager;
    @Autowired
    private ChatMemoryChatMemoryStore chatMemoryChatMemoryStore;
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

        initHistoryChatMemory(wsSessionId, session, userId, sessionCode);

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
        String sessionCode = null;
        String requestId = null;

        try {
            String payload = message.getPayload();

            // 1. 先解析获取type字段
            JsonNode rootNode = objectMapper.readTree(payload);
            JsonNode typeNode = rootNode.get("type");

            if (typeNode == null || typeNode.isNull()) {
                sendErrorMessage(session, "消息类型不能为空", sessionCode, requestId);
                return;
            }

            String messageTypeStr = typeNode.asText();

            // 2. 将type转换为枚举，如果不存在则报错
            WebSocketMessageType messageType;
            try {
                messageType = WebSocketMessageType.valueOf(messageTypeStr);
            } catch (IllegalArgumentException e) {
                log.error("未知的消息类型: {}", messageTypeStr);
                sendErrorMessage(session, "未知的消息类型: " + messageTypeStr, sessionCode, requestId);
                return;
            }

            log.info("收到WebSocket消息: wsSessionId={}, type={}", wsSessionId, messageType);

            // 3. 根据枚举的dataClass反序列化WebSocketMessage
            WebSocketMessage<?> wsMessage = deserializeMessage(payload, messageType);

            sessionCode = wsMessage.getSessionCode();
            requestId = wsMessage.getRequestId();
            if (sessionCode == null) {
                sendErrorMessage(session, "会话编码不能为空", sessionCode, requestId);
                return;
            }
            if (requestId == null) {
                sendErrorMessage(session, "请求ID不能为空", sessionCode, requestId);
                return;
            }

            // 4. 根据消息类型处理
            if (WebSocketMessageType.USER_MESSAGE.equals(messageType)) {
                handleUserMessage(session, context, wsMessage);
            } else if (WebSocketMessageType.USER_ORDER.equals(messageType)) {
                handleUserOrder(session, context, wsMessage);
            } else if (WebSocketMessageType.AGENT_TOOL_CALL_RESPONSE.equals(messageType)) {
                handleToolCallResponse(session, context, wsMessage);
            } else if (WebSocketMessageType.INTERRUPT.equals(messageType)) {
                handleInterrupt(session, context, wsMessage);
            } else if (WebSocketMessageType.PING.equals(messageType)) {
                handlePing(session, context, wsMessage);
            } else {
                log.warn("未处理的消息类型: {}", messageType);
            }

        } catch (Exception e) {
            log.error("处理WebSocket消息失败: wsSessionId={}, error={}", wsSessionId, e.getMessage(), e);
            sendErrorMessage(session, e.getMessage(), sessionCode, requestId);
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
        sessionManager.removeSession(sessionId);
        log.error("WebSocket传输错误: sessionId={}, error={}", sessionId, exception.getMessage(), exception);
    }

    /**
     * 初始化历史聊天记忆
     */
    private void initHistoryChatMemory(String wsSessionId, WebSocketSession webSocketSession, Long userId, String sessionCode) {
        List<ChatMessageDTO> messageHistory = chatSessionService.getMessageHistory(sessionCode);
        if (messageHistory == null || messageHistory.isEmpty()) {
            chatMemoryChatMemoryStore.updateMessages(wsSessionId, new ArrayList<>());
            return;
        }

        List<ChatMessage> historyMessages = new ArrayList<>();
        for (ChatMessageDTO messageHistoryItem : messageHistory) {
            switch (messageHistoryItem.getRole()) {
                case "USER" -> {

                    break;
                }
                case "AGENT_PROMPT" -> {
                    historyMessages.add(UserMessage.from(messageHistoryItem.getContent()));
                    break;
                }
                case "ASSISTANT" -> {
                    if (!historyMessages.isEmpty() && (historyMessages.getLast() instanceof UserMessage)) {
//                        historyMessages.add()
                    }
                }
                case "SUMMARY" -> {
                    if (!historyMessages.isEmpty() && (historyMessages.getLast() instanceof UserMessage)) {
                        historyMessages.add(AiMessage.from("执行已中断"));
                    }/* else if (!historyMessages.isEmpty() && (historyMessages.getLast() instanceof AiMessage) && ((AiMessage) historyMessages.getLast()).hasToolExecutionRequests()) {
                        AiMessage lastAiMessage = (AiMessage) historyMessages.getLast();
                        List<ToolExecutionRequest> toolExecutionRequests = lastAiMessage.toolExecutionRequests();
                        for (int i = 0; i < toolExecutionRequests.size(); i++) {
                            ToolExecutionRequest toolExecutionRequest = toolExecutionRequests.get(i);
                            historyMessages.add(ToolExecutionResultMessage.toolExecutionResultMessage(toolExecutionRequest.id(), toolExecutionRequest.name(), "执行已中断"));
                        }
                    }*/
                    historyMessages.add(AiMessage.from(messageHistoryItem.getContent()));
                }
                case "TOOL_EXECUTION_RESULT" -> {

                }
            }
        }

        chatMemoryChatMemoryStore.updateMessages(wsSessionId, historyMessages);
    }

    /**
     * 根据消息类型反序列化WebSocketMessage
     *
     * @param payload     JSON字符串
     * @param messageType 消息类型枚举
     * @return 反序列化后的WebSocketMessage
     */
    private WebSocketMessage<?> deserializeMessage(String payload, WebSocketMessageType messageType) throws Exception {
        Class<?> dataClass = messageType.getDataClass();

        if (dataClass == null || dataClass == Map.class) {
            // 如果dataClass为null或Map，使用默认反序列化
            return objectMapper.readValue(payload, new TypeReference<WebSocketMessage<Object>>() {
            });
        } else {
            // 根据具体的dataClass类型反序列化
            // 使用JavaType来构建泛型类型
            return objectMapper.readValue(payload,
                    objectMapper.getTypeFactory().constructParametricType(WebSocketMessage.class, dataClass));
        }
    }

    /**
     * 发送消息
     */
    public void sendMessage(WebSocketSession session, WebSocketMessage<?> message) {
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
    private void sendErrorMessage(WebSocketSession session, String error, String sessionCode, String requestId) {
        WebSocketMessage<?> message = WebSocketMessage.builder()
                .type(WebSocketMessageType.ERROR.name())
                .sessionCode(sessionCode)
                .requestId(requestId)
                .error(error)
                .timestamp(System.currentTimeMillis())
                .build();

        sendMessage(session, message);
    }

    /**
     * 处理用户消息
     */
    private void handleUserMessage(WebSocketSession session, WebSocketSessionContext context, WebSocketMessage<?> wsMessage) {
        String content = wsMessage.getContent();

        if (content == null) {
            sendErrorMessage(session, "消息内容不能为空", wsMessage.getSessionCode(), wsMessage.getRequestId());
            return;
        }

        // 检查是否可以执行
        if (!context.canExecute()) {
            context.requestInterrupt();
            return;
        }

        // 异步执行Agent（传递agentCode）
        chatSessionService.sendMessageAsync(wsMessage.getSessionCode(), wsMessage.getRequestId(), wsMessage, context, session);
    }

    /**
     * 处理用户命令
     */
    private void handleUserOrder(WebSocketSession session, WebSocketSessionContext context, WebSocketMessage<?> wsMessage) {
        Set<String> allowOrders = Set.of("/compact");
        String content = wsMessage.getContent();
        if (!allowOrders.contains(content)) {
            sendErrorMessage(session, "命令格式不合法", wsMessage.getSessionCode(), wsMessage.getRequestId());
            return;
        }

        // 检查是否可以执行
        if (!context.canExecute()) {
            context.requestInterrupt();
            return;
        }

        // 异步执行Agent（传递agentCode）
        chatSessionService.sendMessageAsync(wsMessage.getSessionCode(), wsMessage.getRequestId(), wsMessage, context, session);
    }

    /**
     * 处理工具调用响应消息
     */
    private void handleToolCallResponse(WebSocketSession session, WebSocketSessionContext context, WebSocketMessage<?> wsMessage) {
        try {
            // 将data转换为AgentToolCallResponseData
            Object dataObj = wsMessage.getData();
            if (dataObj == null) {
                sendErrorMessage(session, "工具调用响应数据不能为空", wsMessage.getSessionCode(), wsMessage.getRequestId());
                return;
            }

            // 使用ObjectMapper转换data
            AgentToolCallResponseData responseData = objectMapper.convertValue(dataObj, AgentToolCallResponseData.class);

            log.info("收到工具调用响应: sessionId={}, toolName={}, success={}",
                    session.getId(), responseData.getToolName(), responseData.getSuccess());

            // TODO: 将工具调用结果传递给Agent继续执行
            // 这里需要根据实际的Agent执行流程来实现
            // 可能需要在WebSocketSessionContext中添加相应的方法来处理工具调用结果

        } catch (Exception e) {
            log.error("处理工具调用响应失败: sessionId={}, error={}", session.getId(), e.getMessage(), e);
            sendErrorMessage(session, "处理工具调用响应失败: " + e.getMessage(), wsMessage.getSessionCode(), wsMessage.getRequestId());
        }
    }

    /**
     * 处理中断消息
     */
    private void handleInterrupt(WebSocketSession session, WebSocketSessionContext context, WebSocketMessage<?> wsMessage) {
        sessionManager.requestInterrupt(session.getId());
        log.info("执行申请中断: sessionId={}", session.getId());
    }

    /**
     * 处理心跳消息
     */
    private void handlePing(WebSocketSession session, WebSocketSessionContext context, WebSocketMessage<?> wsMessage) {
        WebSocketMessage<?> response = WebSocketMessage.builder()
                .type(WebSocketMessageType.PONG.name())
                .sessionCode(wsMessage.getSessionCode())
                .requestId(wsMessage.getRequestId())
                .timestamp(System.currentTimeMillis())
                .build();

        sendMessage(session, response);
    }

}
