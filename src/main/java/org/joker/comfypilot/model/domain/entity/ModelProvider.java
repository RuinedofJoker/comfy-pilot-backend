package org.joker.comfypilot.model.domain.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.joker.comfypilot.common.domain.BaseEntity;
import org.joker.comfypilot.model.domain.enums.ProviderCode;
import org.joker.comfypilot.model.domain.enums.ProviderStatus;

import java.time.LocalDateTime;

/**
 * 模型提供商领域实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ModelProvider extends BaseEntity<Long> {

    private Long id;
    private String name;
    private ProviderCode code;
    private String baseUrl;
    private String apiKey;
    private ProviderStatus status;
    private Integer rateLimit;
    private Integer timeoutMs;
    private Integer retryTimes;
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
     * 激活提供商
     */
    public void activate() {
        // TODO: 实现激活逻辑
    }

    /**
     * 停用提供商
     */
    public void deactivate() {
        // TODO: 实现停用逻辑
    }

    /**
     * 标记为错误状态
     */
    public void markError() {
        // TODO: 实现错误标记逻辑
    }

    /**
     * 更新配置
     */
    public void updateConfig(String baseUrl, String apiKey, Integer rateLimit, Integer timeoutMs, Integer retryTimes) {
        // TODO: 实现配置更新逻辑
    }

    /**
     * 测试连接
     */
    public boolean testConnection() {
        // TODO: 实现连接测试逻辑
        return false;
    }
}
