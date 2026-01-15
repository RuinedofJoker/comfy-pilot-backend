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
     */
    ChatSession save(ChatSession session);

    /**
     * 根据ID查询会话
     */
    Optional<ChatSession> findById(Long id);

    /**
     * 根据用户ID查询会话列表
     */
    List<ChatSession> findByUserId(Long userId);

    /**
     * 根据工作流ID查询会话列表
     */
    List<ChatSession> findByWorkflowId(Long workflowId);

    /**
     * 删除会话
     */
    void deleteById(Long id);
}
