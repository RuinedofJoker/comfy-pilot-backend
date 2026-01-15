package org.joker.comfypilot.workflow.domain.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.joker.comfypilot.common.domain.BaseEntity;

import java.time.LocalDateTime;

/**
 * 工作流领域实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class Workflow extends BaseEntity<Long> {

    private Long id;
    private Long userId;
    private Long serviceId;
    private String name;
    private String description;
    private String workflowData;
    private Boolean isFavorite;
    private LocalDateTime lastUsedAt;
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
     * 收藏工作流
     */
    public void favorite() {
        // TODO: 实现收藏逻辑
    }

    /**
     * 取消收藏
     */
    public void unfavorite() {
        // TODO: 实现取消收藏逻辑
    }

    /**
     * 更新最后使用时间
     */
    public void updateLastUsedTime() {
        // TODO: 实现更新最后使用时间逻辑
    }

    /**
     * 更新工作流数据
     */
    public void updateWorkflowData(String workflowData) {
        // TODO: 实现更新工作流数据逻辑
    }
}
