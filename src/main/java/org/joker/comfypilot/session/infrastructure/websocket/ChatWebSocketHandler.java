package org.joker.comfypilot.session.infrastructure.websocket;

import cn.hutool.core.exceptions.ExceptionUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.data.message.*;
import dev.langchain4j.model.chat.response.ChatResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.joker.comfypilot.agent.domain.agent.OrderAgent;
import org.joker.comfypilot.agent.domain.context.AgentExecutionContext;
import org.joker.comfypilot.agent.domain.context.AgentExecutionContextHolder;
import org.joker.comfypilot.agent.infrastructure.memory.ChatMemoryChatMemoryStore;
import org.joker.comfypilot.common.config.JacksonConfig;
import org.joker.comfypilot.common.constant.AuthConstants;
import org.joker.comfypilot.common.domain.message.PersistableChatMessage;
import org.joker.comfypilot.common.exception.BusinessException;
import org.joker.comfypilot.common.util.SpringContextUtil;
import org.joker.comfypilot.common.util.TraceIdUtil;
import org.joker.comfypilot.session.application.dto.AgentCallToolResult;
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
import java.util.concurrent.CompletableFuture;

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

        WebSocketSessionContext context = sessionManager.addSession(wsSessionId, session, userId, sessionCode);

        initHistoryChatMemory(context);

        log.info("WebSocket连接已建立: sessionId={}, userId={}", wsSessionId, userId);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String traceId = TraceIdUtil.generateTraceId();
        TraceIdUtil.setTraceId(traceId);

        try {
            String wsSessionId = session.getId();
            WebSocketSessionContext context = sessionManager.getContext(wsSessionId);

            if (context == null) {
                log.warn("未找到WebSocket会话上下文: wsSessionId={}", wsSessionId);
                return;
            }

            context.updateActiveTime();
            String sessionCode;
            String requestId = null;

            try {
                String payload = message.getPayload();

                // 直接反序列化为 WebSocketMessage，Jackson会根据type字段自动选择正确的data类型
                WebSocketMessage<? extends WebSocketMessageData> wsMessage =
                        objectMapper.readValue(payload, new TypeReference<WebSocketMessage<WebSocketMessageData>>() {
                        });

                // 验证消息类型
                if (wsMessage.getType() == null) {
                    context.sendErrorMessage("消息类型不能为空", requestId);
                    return;
                }

                WebSocketMessageType messageType;
                try {
                    messageType = WebSocketMessageType.valueOf(wsMessage.getType());
                } catch (IllegalArgumentException e) {
                    log.error("未知的消息类型: {}", wsMessage.getType());
                    context.sendErrorMessage("未知的消息类型: " + wsMessage.getType(), requestId);
                    return;
                }

                log.info("收到WebSocket消息: wsSessionId={}, type={}", wsSessionId, messageType);

                sessionCode = wsMessage.getSessionCode();
                requestId = wsMessage.getRequestId();
                if (sessionCode == null) {
                    context.sendErrorMessage("会话编码不能为空", requestId);
                    return;
                }
                if (requestId == null) {
                    context.sendErrorMessage("请求ID不能为空", requestId);
                    return;
                }

                // 根据消息类型处理
                switch (messageType) {
                    case USER_MESSAGE -> handleUserMessage(context, wsMessage);
                    case USER_ORDER -> handleUserOrder(context, wsMessage);
                    case AGENT_TOOL_CALL_RESPONSE -> handleToolCallResponse(context, wsMessage);
                    case INTERRUPT -> handleInterrupt(context, wsMessage);
                    case PING -> handlePing(context, wsMessage);
                    default -> log.warn("未处理的消息类型: {}", messageType);
                }

            } catch (Exception e) {
                log.error("处理WebSocket消息失败: wsSessionId={}, error={}", wsSessionId, e.getMessage(), e);
                context.sendErrorMessage(e.getMessage(), requestId);
            }
        } finally {
            TraceIdUtil.clear();
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
    private void initHistoryChatMemory(WebSocketSessionContext context) throws JsonProcessingException {
        String wsSessionId = context.getWebSocketSession().getId();
        String sessionCode = context.getSessionCode();
        List<ChatMessageDTO> messageHistory = chatSessionService.getMessageHistory(sessionCode);
        if (messageHistory == null || messageHistory.isEmpty()) {
            if (chatMemoryChatMemoryStore.updateMessages(wsSessionId, new ArrayList<>())) {
                throw new BusinessException("当前会话已中断");
            }
            return;
        }

        ObjectMapper mapper = JacksonConfig.getObjectMapper();
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
            historyMessages.add(chatMessage);
        }

        if (chatMemoryChatMemoryStore.updateMessages(wsSessionId, historyMessages)) {
            throw new BusinessException("当前会话已中断");
        }
    }

    /**
     * 处理用户消息
     */
    private void handleUserMessage(WebSocketSessionContext context, WebSocketMessage<?> wsMessage) {
        String content = wsMessage.getContent();

        if (content == null) {
            context.sendErrorMessage("消息内容不能为空", wsMessage.getRequestId());
            return;
        }

        // 异步执行Agent（传递agentCode）
        chatSessionService.sendMessageAsync(wsMessage.getSessionCode(), wsMessage.getRequestId(), wsMessage, context, context.getWebSocketSession());
    }

    /**
     * 处理用户命令
     */
    private void handleUserOrder(WebSocketSessionContext context, WebSocketMessage<?> wsMessage) {
        String content = wsMessage.getContent();
        if (!OrderAgent.ALLOWED_PROMPTS.contains(content)) {
            context.sendErrorMessage("命令格式不合法", wsMessage.getRequestId());
            return;
        }

        // 异步执行Agent（传递agentCode）
        chatSessionService.sendMessageAsync(wsMessage.getSessionCode(), wsMessage.getRequestId(), wsMessage, context, context.getWebSocketSession());
    }

    /**
     * 处理工具调用响应消息
     */
    private void handleToolCallResponse(WebSocketSessionContext context, WebSocketMessage<?> wsMessage) {
        try {
            // Jackson已经自动将data反序列化为AgentToolCallResponseData类型
            Object dataObj = wsMessage.getData();
            if (dataObj == null) {
                context.sendErrorMessage("工具调用响应数据不能为空", wsMessage.getRequestId());
                return;
            }

            // 直接类型转换，无需使用ObjectMapper
            if (!(dataObj instanceof AgentToolCallResponseData responseData)) {
                context.sendErrorMessage("工具调用响应数据类型错误", wsMessage.getRequestId());
                return;
            }

            AgentExecutionContext executionContext = context.getAgentExecutionContext().get();
            if (executionContext == null || !StringUtils.equals(executionContext.getRequestId(), wsMessage.getRequestId())) {
                throw new BusinessException("当前Agent执行上下文不正确");
            }

            TraceIdUtil.setTraceId(executionContext.getTraceId());

            // 中断调用
            if (executionContext.isInterrupted()) {
                toolCallWaitManager.cancelWait(responseData.getToolCallId(), responseData.getToolName());
            }

            if (Boolean.FALSE.equals(responseData.getIsMcpTool()) && Boolean.FALSE.equals(responseData.getIsClientTool())) {
                Tool serverTool = SpringContextUtil.getBean(ToolRegistry.class).getToolByName(responseData.getToolName());
                if (serverTool != null) {
                    try {
                        AgentExecutionContextHolder.set(executionContext);
                        String executeResult = serverTool.executeTool(responseData.getToolCallId(), responseData.getToolName(), responseData.getToolArgs());
                        responseData.setSuccess(true);
                        responseData.setResult(executeResult);
                    } catch (Exception e) {
                        log.error("服务端工具执行失败", e);
                        responseData.setSuccess(false);
                        responseData.setResult(ExceptionUtil.stacktraceToString(e));
                        responseData.setError(e.getMessage());
                    } finally {
                        AgentExecutionContextHolder.clear();
                    }
                } else {
                    responseData.setSuccess(false);
                    responseData.setError("找不到该工具");
                }
            } else if (Boolean.TRUE.equals(responseData.getIsMcpTool())) {
                if (Boolean.TRUE.equals(responseData.getIsAllow())) {
                    // 允许执行mcp工具 todo
                    responseData.setSuccess(true);
                    responseData.setResult("执行成功");
                }
            }

            log.info("收到工具调用响应: wsSessionId={}, sessionCode={}, toolName={}, isAllow={}, success={}",
                    context.getWebSocketSession().getId(), context.getSessionCode(), responseData.getToolName(), responseData.getIsAllow(), responseData.getSuccess());

            AgentCallToolResult callToolResult = AgentCallToolResult.builder()
                    .toolCallId(responseData.getToolCallId())
                    .toolName(responseData.getToolName())
                    .isClientTool(responseData.getIsClientTool())
                    .isMcpTool(responseData.getIsMcpTool())
                    .toolArgs(responseData.getToolArgs())
                    .result(responseData.getResult())
                    .isAllow(responseData.getIsAllow())
                    .success(responseData.getSuccess())
                    .error(responseData.getError())
                    .build();

            // 完成工具调用等待，唤醒等待的Agent线程
            boolean completed = toolCallWaitManager.completeWait(
                    callToolResult.getToolCallId(),
                    callToolResult.getToolName(),
                    callToolResult
            );

            if (!completed) {
                log.warn("未找到对应的工具调用等待: sessionCode={}, requestId={}, toolName={}",
                        wsMessage.getSessionCode(), wsMessage.getRequestId(), responseData.getToolName());
            }

        } catch (Exception e) {
            log.error("处理工具调用响应失败: wsSessionId={}, sessionCode={}, error={}", context.getWebSocketSession().getId(), context.getSessionCode(), e.getMessage(), e);
            context.sendErrorMessage("处理工具调用响应失败: " + e.getMessage(), wsMessage.getRequestId());
        }
    }

    /**
     * 处理中断消息
     */
    private void handleInterrupt(WebSocketSessionContext context, WebSocketMessage<?> wsMessage) {
        String requestId = wsMessage.getRequestId();
        AgentExecutionContext executionContext = context.getAgentExecutionContext().get();
        if (StringUtils.isNotBlank(requestId) && StringUtils.equals(requestId, executionContext.getRequestId())) {
            executionContext.getInterrupted().set(true);
        }
        TraceIdUtil.setTraceId(executionContext.getTraceId());
        CompletableFuture<ChatResponse> lastLLMFuture = executionContext.getLastLLMFuture().get();
        if (lastLLMFuture != null) {
            lastLLMFuture.cancel(true);
        }

        log.info("执行申请中断: wsSessionId={}, sessionCode={}", context.getWebSocketSession().getId(), context.getSessionCode());
    }

    /**
     * 处理心跳消息
     */
    private void handlePing(WebSocketSessionContext context, WebSocketMessage<?> wsMessage) {
        WebSocketMessage<?> response = WebSocketMessage.builder()
                .type(WebSocketMessageType.PONG.name())
                .sessionCode(wsMessage.getSessionCode())
                .requestId(wsMessage.getRequestId())
                .timestamp(System.currentTimeMillis())
                .build();

        context.sendMessage(response);
    }

}
