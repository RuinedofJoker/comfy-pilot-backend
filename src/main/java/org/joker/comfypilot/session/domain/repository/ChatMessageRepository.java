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
     *
     * @param chatMessage 消息实体
     * @return 保存后的消息实体
     */
    ChatMessage save(ChatMessage chatMessage);

    /**
     * 根据ID查询消息
     *
     * @param id 消息ID
     * @return 消息实体
     */
    Optional<ChatMessage> findById(Long id);

    /**
     * 根据会话ID查询消息列表
     *
     * @param sessionId 会话ID
     * @return 消息列表（按创建时间升序）
     */
    List<ChatMessage> findBySessionId(Long sessionId);

    /**
     * 根据会话ID查询消息列表（分页）
     *
     * @param sessionId 会话ID
     * @param offset    偏移量
     * @param limit     限制数量
     * @return 消息列表
     */
    List<ChatMessage> findBySessionIdWithPagination(Long sessionId, int offset, int limit);

    /**
     * 统计会话的消息数量
     *
     * @param sessionId 会话ID
     * @return 消息数量
     */
    long countBySessionId(Long sessionId);

    /**
     * 删除消息（逻辑删除）
     *
     * @param id 消息ID
     */
    void deleteById(Long id);
}
