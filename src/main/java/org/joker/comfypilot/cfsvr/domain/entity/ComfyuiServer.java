package org.joker.comfypilot.cfsvr.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.joker.comfypilot.cfsvr.domain.enums.HealthStatus;
import org.joker.comfypilot.cfsvr.domain.enums.ServerSourceType;
import org.joker.comfypilot.common.domain.BaseEntity;
import org.joker.comfypilot.common.exception.BusinessException;

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
     * 认证模式（NULL/BASIC_AUTH/OAUTH2等）
     */
    private String authMode;

    /**
     * API密钥
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
     * 注册来源
     */
    private ServerSourceType sourceType;

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

    /**
     * 验证是否允许修改连接配置
     * 只有手动创建的服务才允许修改连接配置
     *
     * @return true-允许修改，false-不允许修改
     */
    public boolean canModifyConnectionConfig() {
        return ServerSourceType.MANUAL.equals(this.sourceType);
    }

    /**
     * 更新基本信息（所有类型都允许）
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
     * 更新连接配置（仅MANUAL类型允许）
     *
     * @param baseUrl        ComfyUI服务地址
     * @param authMode       认证模式
     * @param apiKey         API密钥
     * @param timeoutSeconds 请求超时时间
     * @param maxRetries     最大重试次数
     */
    public void updateConnectionConfig(String baseUrl, String authMode, String apiKey,
                                       Integer timeoutSeconds, Integer maxRetries) {
        if (!canModifyConnectionConfig()) {
            throw new BusinessException("代码注册的服务不允许修改连接配置");
        }
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
     * 启用/禁用服务（仅MANUAL类型允许）
     *
     * @param enabled 是否启用
     */
    public void setEnabled(Boolean enabled) {
        if (!canModifyConnectionConfig()) {
            throw new BusinessException("代码注册的服务不允许修改启用状态");
        }
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
}
