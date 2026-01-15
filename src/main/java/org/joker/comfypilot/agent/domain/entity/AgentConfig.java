package org.joker.comfypilot.agent.domain.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.joker.comfypilot.agent.domain.enums.AgentStatus;
import org.joker.comfypilot.agent.domain.enums.AgentType;
import org.joker.comfypilot.common.domain.BaseEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Agent配置领域实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class AgentConfig extends BaseEntity<Long> {

    private Long id;
    private String name;
    private AgentType type;
    private String description;
    private Long modelId;
    private String systemPrompt;
    private BigDecimal temperature;
    private Integer maxTokens;
    private String toolsConfig;
    private AgentStatus status;
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
     * 激活Agent
     */
    public void activate() {
        // TODO: 实现激活逻辑
    }

    /**
     * 停用Agent
     */
    public void deactivate() {
        // TODO: 实现停用逻辑
    }

    /**
     * 设置维护模式
     */
    public void setMaintenance() {
        // TODO: 实现维护模式逻辑
    }

    /**
     * 更新配置
     */
    public void updateConfig(String systemPrompt, BigDecimal temperature, Integer maxTokens, String toolsConfig) {
        // TODO: 实现配置更新逻辑
    }

    /**
     * 判断是否激活
     */
    public boolean isActive() {
        return AgentStatus.ACTIVE.equals(this.status);
    }
}
