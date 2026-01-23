package org.joker.comfypilot.session.application.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.joker.comfypilot.agent.application.dto.AgentConfigDTO;
import org.joker.comfypilot.agent.application.dto.AgentExecutionRequest;
import org.joker.comfypilot.agent.application.executor.AgentExecutor;
import org.joker.comfypilot.agent.application.service.AgentConfigService;
import org.joker.comfypilot.agent.domain.context.AgentExecutionContext;
import org.joker.comfypilot.cfsvr.application.service.ComfyuiServerService;
import org.joker.comfypilot.common.exception.BusinessException;
import org.joker.comfypilot.session.application.converter.ChatSessionDTOConverter;
import org.joker.comfypilot.session.application.dto.*;
import org.joker.comfypilot.session.application.dto.client2server.UserMessageRequestData;
import org.joker.comfypilot.session.application.service.ChatSessionService;
import org.joker.comfypilot.session.domain.context.WebSocketSessionContext;
import org.joker.comfypilot.session.domain.entity.ChatMessage;
import org.joker.comfypilot.session.domain.entity.ChatSession;
import org.joker.comfypilot.common.enums.MessageRole;
import org.joker.comfypilot.session.domain.enums.MessageStatus;
import org.joker.comfypilot.session.domain.enums.SessionStatus;
import org.joker.comfypilot.session.domain.repository.ChatMessageRepository;
import org.joker.comfypilot.session.domain.repository.ChatSessionRepository;
import org.joker.comfypilot.session.infrastructure.websocket.WebSocketAgentCallback;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.socket.WebSocketSession;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 会话服务实现 - 支持WebSocket流式输出
 */
@Slf4j
@Service
public class ChatSessionServiceImpl implements ChatSessionService {

    @Autowired
    private ChatSessionRepository chatSessionRepository;
    @Autowired
    private ChatMessageRepository chatMessageRepository;
    @Autowired
    private ChatSessionDTOConverter dtoConverter;
    @Autowired
    private AgentConfigService agentConfigService;
    @Autowired
    private ComfyuiServerService comfyuiServerService;
    @Lazy
    @Autowired
    private AgentExecutor agentExecutor;
    @Autowired
    private ObjectMapper objectMapper;

    @Override
    @Transactional
    public String createSession(Long userId, CreateSessionRequest request) {
        log.info("创建会话: userId={}, comfyuiServerId={}", userId, request.getComfyuiServerId());

        comfyuiServerService.getById(request.getComfyuiServerId());

        // 生成会话编码
        String sessionCode = "session_" + UUID.randomUUID().toString().replace("-", "");

        AgentConfigDTO agent = agentConfigService.getAgentByCode(request.getAgentCode());

        // 创建会话实体
        ChatSession chatSession = ChatSession.builder()
                .sessionCode(sessionCode)
                .userId(userId)
                .comfyuiServerId(request.getComfyuiServerId())
                .agentCode(request.getAgentCode())
                .agentConfig(getAgentConfig(agent, request.getAgentConfig()))
                .title(StringUtils.isNotBlank(request.getTitle()) ? request.getTitle() : "新会话")
                .status(SessionStatus.ACTIVE)
                .build();

        // 保存会话
        chatSessionRepository.save(chatSession);

        log.info("会话创建成功: sessionCode={}", sessionCode);
        return sessionCode;
    }

    @Override
    @Transactional
    public String updateSession(Long userId, String sessionCode, UpdateSessionRequest request) {
        log.info("编辑会话: userId={}, sessionCode={}", userId, sessionCode);

        ChatSessionDTO chatSessionDTO = getSessionByCode(sessionCode);

        if (!chatSessionDTO.getUserId().equals(userId)) {
            throw new BusinessException("当前会话不属于该用户，无法编辑");
        }

        AgentConfigDTO agent = agentConfigService.getAgentByCode(request.getAgentCode());

        // 创建会话实体
        ChatSession chatSession = ChatSession.builder()
                .id(chatSessionDTO.getId())
                .agentCode(request.getAgentCode())
                .agentConfig(getAgentConfig(agent, request.getAgentConfig()))
                .title(StringUtils.isNotBlank(request.getTitle()) ? request.getTitle() : chatSessionDTO.getTitle())
                .build();

        // 保存会话
        chatSessionRepository.updateById(chatSession);

        log.info("会话更新成功: sessionCode={}", sessionCode);
        return sessionCode;
    }

    @Override
    public ChatSessionDTO getSessionById(Long sessionId) {
        log.info("查询会话: sessionId={}", sessionId);

        ChatSession chatSession = chatSessionRepository.findById(sessionId)
                .orElseThrow(() -> new BusinessException("会话不存在: " + sessionId));

        return dtoConverter.toDTO(chatSession);
    }

    @Override
    public ChatSessionDTO getSessionByCode(String sessionCode) {
        log.info("查询会话: sessionCode={}", sessionCode);

        ChatSession chatSession = chatSessionRepository.findBySessionCode(sessionCode)
                .orElseThrow(() -> new BusinessException("会话不存在: " + sessionCode));

        return dtoConverter.toDTO(chatSession);
    }

    @Override
    public List<ChatSessionDTO> getSessionsByUserId(Long userId) {
        log.info("查询用户会话列表: userId={}", userId);

        List<ChatSession> sessions = chatSessionRepository.findByUserId(userId);
        return sessions.stream()
                .map(dtoConverter::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<ChatSessionDTO> getActiveSessionsByUserIdAndComfyuiServerId(Long userId, Long comfyuiServerId) {
        log.info("查询用户活跃会话列表: userId={}", userId);

        List<ChatSession> sessions = chatSessionRepository.findActiveByUserIdAndComfyuiServerId(userId, comfyuiServerId);
        return sessions.stream()
                .map(dtoConverter::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<ChatMessageDTO> getMessageHistory(String sessionCode) {
        log.info("查询会话消息历史: sessionCode={}", sessionCode);

        // 查询会话
        ChatSession chatSession = chatSessionRepository.findBySessionCode(sessionCode)
                .orElseThrow(() -> new BusinessException("会话不存在: " + sessionCode));

        // 查询消息列表
        List<ChatMessage> messages = chatMessageRepository.findBySessionId(chatSession.getId());
        return messages.stream()
                .map(dtoConverter::toMessageDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void archiveSession(String sessionCode) {
        log.info("归档会话: sessionCode={}", sessionCode);

        ChatSession chatSession = chatSessionRepository.findBySessionCode(sessionCode)
                .orElseThrow(() -> new BusinessException("会话不存在: " + sessionCode));

        // 调用领域行为归档会话
        chatSession.archive();
        chatSessionRepository.updateById(chatSession);

        log.info("会话已归档: sessionCode={}", sessionCode);
    }

    @Override
    @Async
    public void sendMessageAsync(String sessionCode, String requestId, WebSocketMessage<?> wsMessage,
                                 WebSocketSessionContext wsContext,
                                 WebSocketSession webSocketSession) {
        log.info("异步发送消息: sessionCode={}, type={}, content={}", sessionCode, wsMessage.getType(), wsMessage.getContent());

        try {
            WebSocketMessage<UserMessageRequestData> userRequestWsMessage = (WebSocketMessage<UserMessageRequestData>) wsMessage;
            UserMessageRequestData userRequestWsMessageData = userRequestWsMessage.getData();

            String content = userRequestWsMessage.getContent();

            // 标记开始执行
            if (wsContext.canExecute()) {
                wsContext.startExecution();
            } else {
                if (wsContext.isExecuting()) {
                    throw new BusinessException("会话未结束，请稍后再试");
                }
                if (wsContext.isInterrupted()) {
                    throw new BusinessException("会话正在中断中，请稍后再试");
                }
            }

            // 查询会话
            ChatSession chatSession = chatSessionRepository.findBySessionCode(sessionCode)
                    .orElseThrow(() -> new BusinessException("会话不存在: " + sessionCode));

            // 验证会话状态
            if (!chatSession.isActive()) {
                throw new BusinessException("会话已关闭,无法发送消息");
            }

            // 保存用户消息
            ChatMessage userMessage = ChatMessage.builder()
                    .sessionId(chatSession.getId())
                    .sessionCode(sessionCode)
                    .requestId(requestId)
                    .role(content.startsWith("/") ? MessageRole.USER_ORDER : MessageRole.USER)
                    .status(MessageStatus.ACTIVE)
                    .metadata(new HashMap<>())
                    .content(content)
                    .build();
            chatMessageRepository.save(userMessage);

            // 构建Agent执行请求
            AgentExecutionRequest agentRequest = AgentExecutionRequest.builder()
                    .sessionId(chatSession.getId())
                    .userId(wsContext.getUserId())
                    .requestId(requestId)
                    .workflowContent(userRequestWsMessageData.getWorkflowContent())
                    .toolSchemas(userRequestWsMessageData.getToolSchemas())
                    .userMessage(content)
                    .agentConfig(chatSession.getAgentConfig())
                    .isStreamable(true)
                    .build();

            // 获取Agent执行上下文（直接使用传入的agentCode）
            AgentExecutionContext executionContext = agentExecutor.getExecutionContext(chatSession.getAgentCode(), agentRequest);

            // 创建流式回调
            WebSocketAgentCallback agentCallback = new WebSocketAgentCallback(
                    webSocketSession, wsContext, executionContext, sessionCode, requestId, objectMapper
            );

            // 设置agent回调
            executionContext.setAgentCallback(agentCallback);

            // 8. 执行Agent
            agentExecutor.execute(executionContext);

            log.info("消息发送成功: sessionCode={}, requestId={}", sessionCode, requestId);
        } catch (Exception e) {
            log.error("消息发送失败: sessionCode={}, requestId={}, error={}", sessionCode, requestId, e.getMessage(), e);
            wsContext.completeExecution();
            throw new BusinessException("消息发送失败: " + e.getMessage(), e);
        }
    }

    private Map<String, Object> getAgentConfig(AgentConfigDTO agent, String agentConfigJson) {
        // 调用 AgentConfigService 的校验+转换方法
        return agentConfigService.validateAndParseAgentConfig(agent, agentConfigJson);
    }
}
