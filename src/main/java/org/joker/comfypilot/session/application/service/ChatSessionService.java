package org.joker.comfypilot.session.application.service;

import org.joker.comfypilot.session.application.dto.ChatMessageDTO;
import org.joker.comfypilot.session.application.dto.ChatSessionDTO;
import org.joker.comfypilot.session.application.dto.CreateSessionRequest;
import org.joker.comfypilot.session.domain.context.WebSocketSessionContext;
import org.springframework.web.socket.WebSocketSession;

import java.util.List;
import java.util.Map;

/**
 * 会话服务接口
 */
public interface ChatSessionService {

    /**
     * 创建会话（WebSocket版本）
     *
     * @param userId 用户ID
     * @param request  创建会话DTO
     * @return 会话编码
     */
    String createSession(Long userId, CreateSessionRequest request);

    /**
     * 根据会话id查询会话
     *
     * @param sessionId 会话id
     * @return 会话DTO
     */
    ChatSessionDTO getSessionById(Long sessionId);

    /**
     * 根据会话编码查询会话
     *
     * @param sessionCode 会话编码
     * @return 会话DTO
     */
    ChatSessionDTO getSessionByCode(String sessionCode);

    /**
     * 根据用户ID查询会话列表
     *
     * @param userId 用户ID
     * @return 会话列表
     */
    List<ChatSessionDTO> getSessionsByUserId(Long userId);

    /**
     * 根据用户ID查询活跃会话列表
     *
     * @param userId 用户ID
     * @return 会话列表
     */
    List<ChatSessionDTO> getActiveSessionsByUserId(Long userId);

    /**
     * 异步发送消息（WebSocket版本，支持流式输出）
     *
     * @param sessionCode 会话编码
     * @param content     消息内容
     * @param data        额外数据
     * @param context     WebSocket会话上下文
     * @param session     WebSocket会话
     */
    void sendMessageAsync(String sessionCode, String content, Map<String, Object> data, WebSocketSessionContext context, WebSocketSession session);

    /**
     * 查询会话消息历史
     *
     * @param sessionCode 会话编码
     * @return 消息列表
     */
    List<ChatMessageDTO> getMessageHistory(String sessionCode);

    /**
     * 归档会话
     *
     * @param sessionCode 会话编码
     */
    void archiveSession(String sessionCode);
}
