package org.joker.comfypilot.session.application.service;

import org.joker.comfypilot.session.application.dto.ChatMessageDTO;
import org.joker.comfypilot.session.application.dto.ChatSessionDTO;
import org.joker.comfypilot.session.application.dto.CreateSessionRequest;
import org.joker.comfypilot.session.application.dto.SendMessageRequest;

import java.util.List;

/**
 * 会话服务接口
 */
public interface ChatSessionService {

    /**
     * 创建会话
     *
     * @param userId  用户ID
     * @param request 创建会话请求
     * @return 会话DTO
     */
    ChatSessionDTO createSession(Long userId, CreateSessionRequest request);

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
     * 发送消息
     *
     * @param userId  用户ID
     * @param request 发送消息请求
     * @return 助手回复消息
     */
    ChatMessageDTO sendMessage(Long userId, SendMessageRequest request);

    /**
     * 查询会话消息历史
     *
     * @param sessionCode 会话编码
     * @return 消息列表
     */
    List<ChatMessageDTO> getMessageHistory(String sessionCode);

    /**
     * 关闭会话
     *
     * @param sessionCode 会话编码
     */
    void closeSession(String sessionCode);
}
