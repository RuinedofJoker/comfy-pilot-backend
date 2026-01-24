package org.joker.comfypilot.session.infrastructure.websocket;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.data.message.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.joker.comfypilot.agent.infrastructure.memory.ChatMemoryChatMemoryStore;
import org.joker.comfypilot.common.constant.AuthConstants;
import org.joker.comfypilot.common.domain.message.PersistableChatMessage;
import org.joker.comfypilot.common.enums.MessageRole;
import org.joker.comfypilot.common.util.SpringContextUtil;
import org.joker.comfypilot.session.application.dto.ChatMessageDTO;
import org.joker.comfypilot.session.application.dto.WebSocketMessage;
import org.joker.comfypilot.session.application.dto.WebSocketMessageData;
import org.joker.comfypilot.session.application.dto.client2server.AgentToolCallResponseData;
import org.joker.comfypilot.session.application.dto.server2client.AgentPromptData;
import org.joker.comfypilot.session.application.service.ChatSessionService;
import org.joker.comfypilot.session.domain.context.WebSocketSessionContext;
import org.joker.comfypilot.session.domain.enums.AgentPromptType;
import org.joker.comfypilot.session.domain.enums.WebSocketMessageType;
import org.joker.comfypilot.agent.domain.toolcall.ToolCallWaitManager;
import org.joker.comfypilot.tool.domain.service.Tool;
import org.joker.comfypilot.tool.domain.service.ToolRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.ArrayList;
import java.util.List;
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
    @Autowired
    private ToolCallWaitManager toolCallWaitManager;

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

            // 直接反序列化为 WebSocketMessage，Jackson会根据type字段自动选择正确的data类型
            WebSocketMessage<? extends WebSocketMessageData> wsMessage =
                objectMapper.readValue(payload, new TypeReference<WebSocketMessage<WebSocketMessageData>>() {});

            // 验证消息类型
            if (wsMessage.getType() == null) {
                sendErrorMessage(session, "消息类型不能为空", sessionCode, requestId);
                return;
            }

            WebSocketMessageType messageType;
            try {
                messageType = WebSocketMessageType.valueOf(wsMessage.getType());
            } catch (IllegalArgumentException e) {
                log.error("未知的消息类型: {}", wsMessage.getType());
                sendErrorMessage(session, "未知的消息类型: " + wsMessage.getType(), sessionCode, requestId);
                return;
            }

            log.info("收到WebSocket消息: wsSessionId={}, type={}", wsSessionId, messageType);

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

            // 根据消息类型处理
            switch (messageType) {
                case USER_MESSAGE -> handleUserMessage(session, context, wsMessage);
                case USER_ORDER -> handleUserOrder(session, context, wsMessage);
                case AGENT_TOOL_CALL_RESPONSE -> handleToolCallResponse(session, context, wsMessage);
                case INTERRUPT -> handleInterrupt(session, context, wsMessage);
                case PING -> handlePing(session, context, wsMessage);
                default -> log.warn("未处理的消息类型: {}", messageType);
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
    private void initHistoryChatMemory(String wsSessionId, WebSocketSession webSocketSession, Long userId, String sessionCode) throws JsonProcessingException {
        List<ChatMessageDTO> messageHistory = chatSessionService.getMessageHistory(sessionCode);
        if (messageHistory == null || messageHistory.isEmpty()) {
            chatMemoryChatMemoryStore.updateMessages(wsSessionId, new ArrayList<>());
            return;
        }

        ObjectMapper mapper = new ObjectMapper();
        List<ChatMessage> historyMessages = new ArrayList<>();
        for (ChatMessageDTO messageHistoryItem : messageHistory) {
            PersistableChatMessage persistableChatMessage = null;
            ChatMessage chatMessage = null;
            if (StringUtils.isNotBlank(messageHistoryItem.getChatContent())) {
                persistableChatMessage = mapper.readValue(messageHistoryItem.getChatContent(), PersistableChatMessage.class);
            }
            if (persistableChatMessage != null) {
                chatMessage = PersistableChatMessage.toLangChain4j(persistableChatMessage);
            }

            Set<String> canBeHistoryRoles = Set.of(
                    MessageRole.USER.name(),
                    MessageRole.AGENT_PROMPT.name(),
                    MessageRole.ASSISTANT.name(),
                    MessageRole.SUMMARY.name(),
                    MessageRole.TOOL_EXECUTION_RESULT.name(),
                    MessageRole.AGENT_PLAN.name()
            );
            if (canBeHistoryRoles.contains(messageHistoryItem.getRole())) {
                if (chatMessage != null) {
                    historyMessages.add(chatMessage);
                } else {
                    log.warn("消息有存入历史的角色{}但没有内容,id={}", messageHistoryItem.getRole(), messageHistoryItem.getId());
                }
            }
        }

        chatMemoryChatMemoryStore.updateMessages(wsSessionId, historyMessages);
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
                .type(WebSocketMessageType.AGENT_PROMPT.name())
                .sessionCode(sessionCode)
                .requestId(requestId)
                .data(AgentPromptData.builder().promptType(AgentPromptType.ERROR).message(error).build())
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
            // Jackson已经自动将data反序列化为AgentToolCallResponseData类型
            Object dataObj = wsMessage.getData();
            if (dataObj == null) {
                sendErrorMessage(session, "工具调用响应数据不能为空", wsMessage.getSessionCode(), wsMessage.getRequestId());
                return;
            }

            // 直接类型转换，无需使用ObjectMapper
            if (!(dataObj instanceof AgentToolCallResponseData)) {
                sendErrorMessage(session, "工具调用响应数据类型错误", wsMessage.getSessionCode(), wsMessage.getRequestId());
                return;
            }

            AgentToolCallResponseData responseData = (AgentToolCallResponseData) dataObj;
            if (Boolean.TRUE.equals(responseData.getIsAllow()) && Boolean.FALSE.equals(responseData.getIsClientTool())) {
                Tool serverTool = SpringContextUtil.getBean(ToolRegistry.class).getToolByName(responseData.getToolName());
                if (serverTool != null) {
                    try {
                        String executeResult = serverTool.executeTool(responseData.getToolCallId(), responseData.getToolName(), responseData.getToolArgs());
                        responseData.setSuccess(true);
                        responseData.setResult(executeResult);
                    } catch (Exception e) {
                        log.error("服务端工具执行失败", e);
                        responseData.setSuccess(false);
                        responseData.setError(e.getMessage());
                    }
                } else {
                    responseData.setSuccess(false);
                    responseData.setError("找不到该工具");
                }
            }

            log.info("收到工具调用响应: sessionId={}, toolName={}, isAllow={}, success={}",
                    session.getId(), responseData.getToolName(), responseData.getIsAllow(), responseData.getSuccess());

            // 完成工具调用等待，唤醒等待的Agent线程
            boolean completed = toolCallWaitManager.completeWait(
                    wsMessage.getSessionCode(),
                    wsMessage.getRequestId(),
                    responseData.getToolName(),
                    responseData
            );

            if (!completed) {
                log.warn("未找到对应的工具调用等待: sessionCode={}, requestId={}, toolName={}",
                        wsMessage.getSessionCode(), wsMessage.getRequestId(), responseData.getToolName());
            }

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
