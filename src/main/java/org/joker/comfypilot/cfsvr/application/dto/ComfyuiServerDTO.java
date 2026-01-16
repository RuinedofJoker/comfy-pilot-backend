package org.joker.comfypilot.cfsvr.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.joker.comfypilot.common.application.dto.BaseDTO;

import java.time.LocalDateTime;

/**
 * ComfyUI服务信息DTO
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Schema(description = "ComfyUI服务信息")
public class ComfyuiServerDTO extends BaseDTO {

    private static final long serialVersionUID = 1L;

    @Schema(description = "服务唯一标识符")
    private String serverKey;

    @Schema(description = "服务名称")
    private String serverName;

    @Schema(description = "服务描述")
    private String description;

    @Schema(description = "ComfyUI服务地址")
    private String baseUrl;

    @Schema(description = "认证模式")
    private String authMode;

    @Schema(description = "API密钥")
    private String apiKey;

    @Schema(description = "请求超时时间（秒）")
    private Integer timeoutSeconds;

    @Schema(description = "最大重试次数")
    private Integer maxRetries;

    @Schema(description = "注册来源：MANUAL/CODE_BASED")
    private String sourceType;

    @Schema(description = "是否启用")
    private Boolean isEnabled;

    @Schema(description = "最后健康检查时间")
    private LocalDateTime lastHealthCheckTime;

    @Schema(description = "健康状态：HEALTHY/UNHEALTHY/UNKNOWN")
    private String healthStatus;
}
