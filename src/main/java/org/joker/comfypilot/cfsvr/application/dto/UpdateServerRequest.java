package org.joker.comfypilot.cfsvr.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 更新ComfyUI服务请求
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "更新ComfyUI服务请求")
public class UpdateServerRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "服务名称")
    @Size(max = 100, message = "服务名称长度不能超过100")
    private String serverName;

    @Schema(description = "服务描述")
    @Size(max = 500, message = "服务描述长度不能超过500")
    private String description;

    @Schema(description = "ComfyUI服务地址")
    @Size(max = 255, message = "服务地址长度不能超过255")
    private String baseUrl;

    @Schema(description = "认证模式")
    @Size(max = 20, message = "认证模式长度不能超过20")
    private String authMode;

    @Schema(description = "API密钥")
    @Size(max = 255, message = "API密钥长度不能超过255")
    private String apiKey;

    @Schema(description = "请求超时时间（秒）")
    private Integer timeoutSeconds;

    @Schema(description = "最大重试次数")
    private Integer maxRetries;

    @Schema(description = "是否启用")
    private Boolean isEnabled;

    /**
     * 判断是否有连接配置变更
     */
    public boolean hasConnectionConfigChanges() {
        return baseUrl != null || authMode != null || apiKey != null
                || timeoutSeconds != null || maxRetries != null;
    }
}
