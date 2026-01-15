package org.joker.comfypilot.comfyui.domain.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.joker.comfypilot.common.domain.BaseEntity;
import org.joker.comfypilot.comfyui.domain.enums.ServiceStatus;

import java.time.LocalDateTime;

/**
 * ComfyUI服务领域实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ComfyuiService extends BaseEntity<Long> {

    private Long id;
    private String name;
    private String url;
    private String description;
    private ServiceStatus status;
    private LocalDateTime lastCheckTime;
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
     * 检查服务状态
     */
    public void checkStatus() {
        // TODO: 实现服务状态检查逻辑
    }

    /**
     * 标记为在线
     */
    public void markOnline() {
        // TODO: 实现标记在线逻辑
    }

    /**
     * 标记为离线
     */
    public void markOffline() {
        // TODO: 实现标记离线逻辑
    }

    /**
     * 标记为错误
     */
    public void markError() {
        // TODO: 实现标记错误逻辑
    }

    /**
     * 判断是否在线
     */
    public boolean isOnline() {
        return ServiceStatus.ONLINE.equals(this.status);
    }
}
