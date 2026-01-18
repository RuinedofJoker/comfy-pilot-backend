package org.joker.comfypilot.cfsvr.infrastructure.persistence.po;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.joker.comfypilot.common.infrastructure.persistence.po.BasePO;

import java.time.LocalDateTime;

/**
 * ComfyUI服务持久化对象
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName("comfyui_server")
public class ComfyuiServerPO extends BasePO {

    private static final long serialVersionUID = 1L;

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
    private String healthStatus;

    /**
     * 是否启用高级功能
     */
    private Boolean advancedFeaturesEnabled;

    /**
     * 高级功能配置（JSON格式存储）
     */
    private String advancedFeatures;
}
