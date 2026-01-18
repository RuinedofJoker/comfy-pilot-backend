package org.joker.comfypilot.cfsvr.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 创建ComfyUI服务请求
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "创建ComfyUI服务请求")
public class CreateServerRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "服务唯一标识符（可选，不填则自动生成UUID）")
    @Size(max = 100, message = "服务标识符长度不能超过100")
    private String serverKey;

    @Schema(description = "服务名称", required = true)
    @NotBlank(message = "服务名称不能为空")
    @Size(max = 100, message = "服务名称长度不能超过100")
    private String serverName;

    @Schema(description = "服务描述")
    @Size(max = 500, message = "服务描述长度不能超过500")
    private String description;

    @Schema(description = "ComfyUI服务地址", required = true)
    @NotBlank(message = "服务地址不能为空")
    @Size(max = 255, message = "服务地址长度不能超过255")
    private String baseUrl;

    @Schema(description = "认证模式")
    @Size(max = 20, message = "认证模式长度不能超过20")
    private String authMode;

    @Schema(description = "API密钥")
    @Size(max = 255, message = "API密钥长度不能超过255")
    private String apiKey;

    @Schema(description = "请求超时时间（秒）", example = "30")
    private Integer timeoutSeconds;

    @Schema(description = "最大重试次数", example = "3")
    private Integer maxRetries;

    @Schema(description = "是否启用高级功能")
    private Boolean advancedFeaturesEnabled;

    @Schema(description = "高级功能配置")
    private ComfyuiServerAdvancedFeaturesDTO advancedFeatures;
}
