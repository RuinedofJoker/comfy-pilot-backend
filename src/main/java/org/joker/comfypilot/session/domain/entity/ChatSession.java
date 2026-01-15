package org.joker.comfypilot.session.domain.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.joker.comfypilot.common.domain.BaseEntity;
import org.joker.comfypilot.session.domain.enums.SessionStatus;

import java.time.LocalDateTime;

/**
 * 会话领域实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ChatSession extends BaseEntity<Long> {

    private Long id;
    private Long userId;
    private Long workflowId;
    private String title;
    private SessionStatus status;
    private String lastMessageSummary;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public LocalDateTime getCreateTime() {
        return createTime;
    }

    @Override
    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    /**
     * 完成会话
     */
    public void complete() {
        // TODO: 实现完成会话逻辑
    }

    /**
     * 归档会话
     */
    public void archive() {
        // TODO: 实现归档会话逻辑
    }

    /**
     * 更新最后消息摘要
     */
    public void updateLastMessageSummary(String summary) {
        // TODO: 实现更新最后消息摘要逻辑
    }
}
