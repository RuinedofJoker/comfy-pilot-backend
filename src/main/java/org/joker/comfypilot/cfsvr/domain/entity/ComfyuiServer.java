package org.joker.comfypilot.cfsvr.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.joker.comfypilot.cfsvr.domain.enums.AuthMode;
import org.joker.comfypilot.cfsvr.domain.enums.HealthStatus;
import org.joker.comfypilot.common.domain.BaseEntity;

import java.time.LocalDateTime;

/**
 * ComfyUI服务领域实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ComfyuiServer extends BaseEntity<Long> {

    private static final long serialVersionUID = 1L;

    /**
     * 服务ID
     */
    private Long id;

    /**
     * 服务唯一标识符
     */
    private String serverKey;

    /**
     * 服务名称
     */
    private String serverName;

    /**
     * 服务描述
     */
    private String description;

    /**
     * ComfyUI服务地址
     */
    private String baseUrl;

    /**
     * 认证模式
     */
    private AuthMode authMode;

    /**
     * API密钥（用于 Basic Auth 等认证方式）
     */
    private String apiKey;

    /**
     * 请求超时时间（秒）
     */
    private Integer timeoutSeconds;

    /**
     * 最大重试次数
     */
    private Integer maxRetries;

    /**
     * 是否启用
     */
    private Boolean isEnabled;

    /**
     * 最后健康检查时间
     */
    private LocalDateTime lastHealthCheckTime;

    /**
     * 健康状态
     */
    private HealthStatus healthStatus;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    // ==================== 业务方法 ====================

    /**
     * 更新基本信息
     *
     * @param serverName  服务名称
     * @param description 服务描述
     */
    public void updateBasicInfo(String serverName, String description) {
        if (serverName != null && !serverName.trim().isEmpty()) {
            this.serverName = serverName;
        }
        if (description != null) {
            this.description = description;
        }
    }

    /**
     * 更新连接配置
     *
     * @param baseUrl        ComfyUI服务地址
     * @param authMode       认证模式
     * @param apiKey         API密钥
     * @param timeoutSeconds 请求超时时间
     * @param maxRetries     最大重试次数
     */
    public void updateConnectionConfig(String baseUrl, AuthMode authMode, String apiKey,
                                       Integer timeoutSeconds, Integer maxRetries) {
        if (baseUrl != null && !baseUrl.trim().isEmpty()) {
            this.baseUrl = baseUrl;
        }
        this.authMode = authMode;
        this.apiKey = apiKey;
        if (timeoutSeconds != null && timeoutSeconds > 0) {
            this.timeoutSeconds = timeoutSeconds;
        }
        if (maxRetries != null && maxRetries >= 0) {
            this.maxRetries = maxRetries;
        }
    }

    /**
     * 启用/禁用服务
     *
     * @param enabled 是否启用
     */
    public void setEnabled(Boolean enabled) {
        this.isEnabled = enabled;
    }

    /**
     * 更新健康状态
     *
     * @param status 健康状态
     */
    public void updateHealthStatus(HealthStatus status) {
        this.healthStatus = status;
        this.lastHealthCheckTime = LocalDateTime.now();
    }

    /**
     * 判断服务是否健康
     *
     * @return true-健康，false-不健康
     */
    public boolean isHealthy() {
        return HealthStatus.HEALTHY.equals(this.healthStatus);
    }
}
