package org.joker.comfypilot.session.application.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.joker.comfypilot.agent.application.dto.AgentExecutionRequest;
import org.joker.comfypilot.agent.application.dto.AgentExecutionResponse;
import org.joker.comfypilot.agent.application.executor.AgentExecutor;
import org.joker.comfypilot.agent.domain.context.AgentExecutionContext;
import org.joker.comfypilot.session.application.converter.ChatSessionDTOConverter;
import org.joker.comfypilot.session.application.dto.ChatMessageDTO;
import org.joker.comfypilot.session.application.dto.ChatSessionDTO;
import org.joker.comfypilot.session.application.service.ChatSessionService;
import org.joker.comfypilot.session.domain.context.WebSocketSessionContext;
import org.joker.comfypilot.session.domain.entity.ChatMessage;
import org.joker.comfypilot.session.domain.entity.ChatSession;
import org.joker.comfypilot.session.domain.enums.MessageRole;
import org.joker.comfypilot.session.domain.enums.SessionStatus;
import org.joker.comfypilot.session.domain.repository.ChatMessageRepository;
import org.joker.comfypilot.session.domain.repository.ChatSessionRepository;
import org.joker.comfypilot.session.infrastructure.websocket.WebSocketStreamCallback;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.socket.WebSocketSession;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 会话服务实现 - 支持WebSocket流式输出
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChatSessionServiceImpl implements ChatSessionService {

    private final ChatSessionRepository chatSessionRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final ChatSessionDTOConverter dtoConverter;
    private final AgentExecutor agentExecutor;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public String createSession(Long userId, String title) {
        log.info("创建会话: userId={}", userId);

        // 生成会话编码
        String sessionCode = "session_" + UUID.randomUUID().toString().replace("-", "");

        // 创建会话实体
        ChatSession chatSession = ChatSession.builder()
                .sessionCode(sessionCode)
                .userId(userId)
                .agentId(null)  // 不再在会话级别指定Agent
                .title(title != null ? title : "新会话")
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
    public ChatSessionDTO getSessionByCode(String sessionCode) {
        log.info("查询会话: sessionCode={}", sessionCode);

        ChatSession chatSession = chatSessionRepository.findBySessionCode(sessionCode)
                .orElseThrow(() -> new RuntimeException("会话不存在: " + sessionCode));

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
    public List<ChatMessageDTO> getMessageHistory(String sessionCode) {
        log.info("查询会话消息历史: sessionCode={}", sessionCode);

        // 查询会话
        ChatSession chatSession = chatSessionRepository.findBySessionCode(sessionCode)
                .orElseThrow(() -> new RuntimeException("会话不存在: " + sessionCode));

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
                .orElseThrow(() -> new RuntimeException("会话不存在: " + sessionCode));

        // 调用领域行为归档会话
        chatSession.archive();
        chatSessionRepository.update(chatSession);

        log.info("会话已归档: sessionCode={}", sessionCode);
    }

    @Override
    @Async
    public void sendMessageAsync(String sessionCode, String content, String agentCode,
                                  WebSocketSessionContext wsContext,
                                  WebSocketSession webSocketSession) {
        log.info("异步发送消息: sessionCode={}, agentCode={}, content={}", sessionCode, agentCode, content);

        try {
            // 标记开始执行
            wsContext.startExecution();

            // 1. 查询会话
            ChatSession chatSession = chatSessionRepository.findBySessionCode(sessionCode)
                    .orElseThrow(() -> new RuntimeException("会话不存在: " + sessionCode));

            // 2. 验证会话状态
            if (!chatSession.isActive()) {
                throw new RuntimeException("会话已关闭,无法发送消息");
            }

            // 3. 保存用户消息
            ChatMessage userMessage = ChatMessage.builder()
                    .sessionId(chatSession.getId())
                    .role(MessageRole.USER)
                    .content(content)
                    .createTime(LocalDateTime.now())
                    .updateTime(LocalDateTime.now())
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
                    .isStreamable(true)
                    .build();

            // 6. 获取Agent执行上下文（直接使用传入的agentCode）
            AgentExecutionContext executionContext = agentExecutor.getExecutionContext(agentCode, agentRequest);

            // 设置流式回调
            executionContext.setStreamCallback(streamCallback);

            // 7. 执行Agent
            AgentExecutionResponse response = agentExecutor.execute(executionContext);

            // 8. 保存助手消息
            /*ChatMessage assistantMessage = ChatMessage.builder()
                    .sessionId(chatSession.getId())
                    .role(MessageRole.ASSISTANT)
                    .content(response.getOutput())
                    .createTime(LocalDateTime.now())
                    .updateTime(LocalDateTime.now())
                    .build();
            chatMessageRepository.save(assistantMessage);*/

            log.info("消息发送成功: sessionCode={}", sessionCode);

        } catch (Exception e) {
            log.error("消息发送失败: sessionCode={}, error={}", sessionCode, e.getMessage(), e);
            wsContext.completeExecution();
            throw new RuntimeException("消息发送失败: " + e.getMessage(), e);
        }
    }
}
