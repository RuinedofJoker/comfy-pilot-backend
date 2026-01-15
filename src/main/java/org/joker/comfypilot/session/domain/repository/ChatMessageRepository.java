package org.joker.comfypilot.session.domain.repository;

import org.joker.comfypilot.session.domain.entity.ChatMessage;

import java.util.List;
import java.util.Optional;

/**
 * 消息仓储接口
 */
public interface ChatMessageRepository {

    /**
     * 保存消息
     */
    ChatMessage save(ChatMessage message);

    /**
     * 根据ID查询消息
     */
    Optional<ChatMessage> findById(Long id);

    /**
     * 根据会话ID查询消息列表
     */
    List<ChatMessage> findBySessionId(Long sessionId);
}
