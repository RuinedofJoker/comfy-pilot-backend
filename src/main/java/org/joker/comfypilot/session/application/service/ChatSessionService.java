package org.joker.comfypilot.session.application.service;

import org.joker.comfypilot.session.application.dto.ChatMessageDTO;
import org.joker.comfypilot.session.application.dto.ChatSessionDTO;
import org.joker.comfypilot.session.domain.context.WebSocketSessionContext;
import org.springframework.web.socket.WebSocketSession;

import java.util.List;

/**
 * 会话服务接口
 */
public interface ChatSessionService {

    /**
     * 创建会话（WebSocket版本）
     *
     * @param userId  用户ID
     * @param agentId Agent ID
     * @param title   会话标题
     * @return 会话编码
     */
    String createSession(Long userId, Long agentId, String title);

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
     * 异步发送消息（WebSocket版本，支持流式输出）
     *
     * @param sessionCode 会话编码
     * @param content     消息内容
     * @param context     WebSocket会话上下文
     * @param session     WebSocket会话
     */
    void sendMessageAsync(String sessionCode, String content, WebSocketSessionContext context, WebSocketSession session);

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
