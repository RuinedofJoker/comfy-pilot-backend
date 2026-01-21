package org.joker.comfypilot.session.domain.repository;

import org.joker.comfypilot.session.domain.entity.ChatSession;

import java.util.List;
import java.util.Optional;

/**
 * 会话仓储接口
 */
public interface ChatSessionRepository {

    /**
     * 保存会话
     *
     * @param chatSession 会话实体
     * @return 保存后的会话实体
     */
    ChatSession save(ChatSession chatSession);

    /**
     * 更新会话
     *
     * @param id          会话ID
     * @param chatSession 会话实体
     * @return 更新后的会话实体
     */
    ChatSession updateById(Long id, ChatSession chatSession);

    /**
     * 根据ID查询会话
     *
     * @param id 会话ID
     * @return 会话实体
     */
    Optional<ChatSession> findById(Long id);

    /**
     * 根据会话编码查询会话
     *
     * @param sessionCode 会话编码
     * @return 会话实体
     */
    Optional<ChatSession> findBySessionCode(String sessionCode);

    /**
     * 根据用户ID查询会话列表
     *
     * @param userId 用户ID
     * @return 会话列表
     */
    List<ChatSession> findByUserId(Long userId);

    /**
     * 根据用户ID查询活跃会话列表
     *
     * @param userId          用户ID
     * @param comfyuiServerId ComfyUI服务ID
     * @return 会话列表
     */
    List<ChatSession> findActiveByUserIdAndComfyuiServerId(Long userId, Long comfyuiServerId);

    /**
     * 更新会话
     *
     * @param chatSession 会话实体
     * @return 更新后的会话实体
     */
    ChatSession updateById(ChatSession chatSession);

    /**
     * 删除会话（逻辑删除）
     *
     * @param id 会话ID
     */
    void deleteById(Long id);
}
