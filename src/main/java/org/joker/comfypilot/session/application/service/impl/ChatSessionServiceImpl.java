package org.joker.comfypilot.session.application.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.joker.comfypilot.agent.application.dto.AgentConfigDTO;
import org.joker.comfypilot.agent.application.dto.AgentExecutionRequest;
import org.joker.comfypilot.agent.application.executor.AgentExecutor;
import org.joker.comfypilot.agent.application.service.AgentConfigService;
import org.joker.comfypilot.agent.domain.context.AgentExecutionContext;
import org.joker.comfypilot.common.exception.BusinessException;
import org.joker.comfypilot.session.application.converter.ChatSessionDTOConverter;
import org.joker.comfypilot.session.application.dto.ChatMessageDTO;
import org.joker.comfypilot.session.application.dto.ChatSessionDTO;
import org.joker.comfypilot.session.application.dto.CreateSessionRequest;
import org.joker.comfypilot.session.application.service.ChatSessionService;
import org.joker.comfypilot.session.domain.context.WebSocketSessionContext;
import org.joker.comfypilot.session.domain.entity.ChatMessage;
import org.joker.comfypilot.session.domain.entity.ChatSession;
import org.joker.comfypilot.session.domain.enums.MessageRole;
import org.joker.comfypilot.session.domain.enums.MessageStatus;
import org.joker.comfypilot.session.domain.enums.SessionStatus;
import org.joker.comfypilot.session.domain.repository.ChatMessageRepository;
import org.joker.comfypilot.session.domain.repository.ChatSessionRepository;
import org.joker.comfypilot.session.infrastructure.websocket.WebSocketStreamCallback;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.socket.WebSocketSession;

import java.time.LocalDateTime;
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
    @Lazy
    @Autowired
    private AgentExecutor agentExecutor;
    @Autowired
    private ObjectMapper objectMapper;

    @Override
    @Transactional
    public String createSession(Long userId, CreateSessionRequest request) {
        log.info("创建会话: userId={}", userId);

        // 生成会话编码
        String sessionCode = "session_" + UUID.randomUUID().toString().replace("-", "");

        AgentConfigDTO agent = agentConfigService.getAgentByCode(request.getAgentCode());

        // 创建会话实体
        ChatSession chatSession = ChatSession.builder()
                .sessionCode(sessionCode)
                .userId(userId)
                .agentCode(request.getAgentCode())
                .agentConfig(getAgentConfig(agent, request.getAgentConfig()))
                .title(request.getTitle() != null ? request.getTitle() : "新会话")
                .status(SessionStatus.ACTIVE)
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build();

        // 保存会话
        chatSessionRepository.save(chatSession);

        log.info("会话创建成功: sessionCode={}", sessionCode);
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
    public List<ChatSessionDTO> getActiveSessionsByUserId(Long userId) {
        log.info("查询用户活跃会话列表: userId={}", userId);

        List<ChatSession> sessions = chatSessionRepository.findActiveByUserId(userId);
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
        chatSessionRepository.update(chatSession);

        log.info("会话已归档: sessionCode={}", sessionCode);
    }

    @Override
    @Async
    public void sendMessageAsync(String sessionCode, String content, Map<String, Object> data,
                                  WebSocketSessionContext wsContext,
                                  WebSocketSession webSocketSession) {
        log.info("异步发送消息: sessionCode={}, content={}", sessionCode, content);

        try {
            // 标记开始执行
            wsContext.startExecution();

            // 1. 查询会话
            ChatSession chatSession = chatSessionRepository.findBySessionCode(sessionCode)
                    .orElseThrow(() -> new BusinessException("会话不存在: " + sessionCode));

            // 2. 验证会话状态
            if (!chatSession.isActive()) {
                throw new BusinessException("会话已关闭,无法发送消息");
            }
            if (data == null) {
                throw new BusinessException("用户消息未提供额外数据");
            }

            String agentConfig;
            if (data.get("agentConfig") instanceof String) {
                agentConfig = (String) data.get("agentConfig");
            } else if (data.get("agentConfig") instanceof Map<?, ?>) {
                agentConfig = objectMapper.writeValueAsString(data.get("agentConfig"));
            } else if (data.get("agentConfig") == null) {
                agentConfig = "{}";
            } else {
                throw new BusinessException("不能识别的agentConfig类型" +  data.get("agentConfig"));
            }


            // 3. 保存用户消息
            ChatMessage userMessage = ChatMessage.builder()
                    .sessionId(chatSession.getId())
                    .role(MessageRole.USER)
                    .status(MessageStatus.ACTIVE)
                    .content(content)
                    .build();
            chatMessageRepository.save(userMessage);

            // 4. 创建流式回调
            WebSocketStreamCallback streamCallback = new WebSocketStreamCallback(
                    webSocketSession, wsContext, sessionCode, objectMapper);

            // 5. 构建Agent执行请求
            AgentExecutionRequest agentRequest = AgentExecutionRequest.builder()
                    .sessionId(chatSession.getId())
                    .userId(wsContext.getUserId())
                    .input(content)
                    .agentConfig(agentConfig)
                    .isStreamable(true)
                    .build();

            // 6. 获取Agent执行上下文（直接使用传入的agentCode）
            AgentExecutionContext executionContext = agentExecutor.getExecutionContext(chatSession.getAgentCode(), agentRequest);

            // 设置流式回调
            executionContext.setStreamCallback(streamCallback);

            // 7. 执行Agent
            agentExecutor.execute(executionContext);

            log.info("消息发送成功: sessionCode={}", sessionCode);
        } catch (Exception e) {
            log.error("消息发送失败: sessionCode={}, error={}", sessionCode, e.getMessage(), e);
            wsContext.completeExecution();
            throw new BusinessException("消息发送失败: " + e.getMessage(), e);
        }
    }

    private Map<String, Object> getAgentConfig(AgentConfigDTO agent, String agentConfigJson) {
        // TODO 根据AgentConfigDefinition动态校验与解析agentConfigJson(这里需要在AgentConfigService里开放接口，这里直接调用接口)

        if (StringUtils.isBlank(agentConfigJson)) {
            return new HashMap<>();
        }


        return new HashMap<>();
    }
}
