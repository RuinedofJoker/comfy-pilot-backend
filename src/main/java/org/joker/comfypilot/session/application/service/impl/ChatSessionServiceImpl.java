package org.joker.comfypilot.session.application.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.joker.comfypilot.session.application.converter.ChatSessionDTOConverter;
import org.joker.comfypilot.session.application.dto.ChatMessageDTO;
import org.joker.comfypilot.session.application.dto.ChatSessionDTO;
import org.joker.comfypilot.session.application.dto.CreateSessionRequest;
import org.joker.comfypilot.session.application.dto.SendMessageRequest;
import org.joker.comfypilot.session.application.service.ChatSessionService;
import org.joker.comfypilot.session.domain.entity.ChatMessage;
import org.joker.comfypilot.session.domain.entity.ChatSession;
import org.joker.comfypilot.session.domain.enums.MessageRole;
import org.joker.comfypilot.session.domain.enums.SessionStatus;
import org.joker.comfypilot.session.domain.repository.ChatMessageRepository;
import org.joker.comfypilot.session.domain.repository.ChatSessionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 会话服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChatSessionServiceImpl implements ChatSessionService {

    private final ChatSessionRepository chatSessionRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final ChatSessionDTOConverter dtoConverter;
    // TODO: 等Agent模块实现后注入 AgentExecutor

    @Override
    @Transactional
    public ChatSessionDTO createSession(Long userId, CreateSessionRequest request) {
        log.info("创建会话: userId={}, agentId={}", userId, request.getAgentId());

        // 生成会话编码
        String sessionCode = "session_" + UUID.randomUUID().toString().replace("-", "");

        // 创建会话实体
        ChatSession chatSession = ChatSession.builder()
                .sessionCode(sessionCode)
                .userId(userId)
                .agentId(request.getAgentId())
                .title(request.getTitle() != null ? request.getTitle() : "新会话")
                .status(SessionStatus.ACTIVE)
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build();

        // 保存会话
        ChatSession savedSession = chatSessionRepository.save(chatSession);

        log.info("会话创建成功: sessionCode={}", sessionCode);
        return dtoConverter.toDTO(savedSession);
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
    public void closeSession(String sessionCode) {
        log.info("关闭会话: sessionCode={}", sessionCode);

        ChatSession chatSession = chatSessionRepository.findBySessionCode(sessionCode)
                .orElseThrow(() -> new RuntimeException("会话不存在: " + sessionCode));

        // 调用领域行为关闭会话
        chatSession.close();
        chatSessionRepository.update(chatSession);

        log.info("会话已关闭: sessionCode={}", sessionCode);
    }

    @Override
    @Transactional
    public ChatMessageDTO sendMessage(Long userId, SendMessageRequest request) {
        log.info("发送消息: userId={}, sessionCode={}", userId, request.getSessionCode());

        // 1. 查询会话
        ChatSession chatSession = chatSessionRepository.findBySessionCode(request.getSessionCode())
                .orElseThrow(() -> new RuntimeException("会话不存在: " + request.getSessionCode()));

        // 2. 验证会话状态
        if (!chatSession.isActive()) {
            throw new RuntimeException("会话已关闭,无法发送消息");
        }

        // 3. 保存用户消息
        ChatMessage userMessage = ChatMessage.builder()
                .sessionId(chatSession.getId())
                .role(MessageRole.USER)
                .content(request.getContent())
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build();
        chatMessageRepository.save(userMessage);

        // 4. TODO: 调用Agent执行(等Agent模块实现后补充)
        // AgentExecutionRequest agentRequest = AgentExecutionRequest.builder()
        //         .agentId(chatSession.getAgentId())
        //         .sessionId(chatSession.getId())
        //         .input(request.getContent())
        //         .build();
        // AgentExecutionResponse agentResponse = agentExecutor.execute(agentRequest);

        // 5. 临时返回模拟的助手消息(等Agent模块实现后删除)
        String assistantContent = "这是一条模拟的助手回复。Agent模块实现后,这里将返回真实的AI回复。";

        // 6. 保存助手消息
        ChatMessage assistantMessage = ChatMessage.builder()
                .sessionId(chatSession.getId())
                .role(MessageRole.ASSISTANT)
                .content(assistantContent)
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build();
        ChatMessage savedAssistantMessage = chatMessageRepository.save(assistantMessage);

        log.info("消息发送成功: sessionCode={}", request.getSessionCode());
        return dtoConverter.toMessageDTO(savedAssistantMessage);
    }
}
