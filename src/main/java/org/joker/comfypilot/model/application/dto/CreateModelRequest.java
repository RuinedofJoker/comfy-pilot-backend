package org.joker.comfypilot.model.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 创建模型请求
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "创建模型请求")
public class CreateModelRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "模型名称", required = true)
    @NotBlank(message = "模型名称不能为空")
    @Size(max = 100, message = "模型名称长度不能超过100")
    private String modelName;

    @Schema(description = "模型显示名称", required = true)
    @NotBlank(message = "模型显示名称不能为空")
    @Size(max = 100, message = "模型显示名称长度不能超过100")
    private String modelDisplayName;

    @Schema(description = "模型标识符（可选，不填则自动生成）")
    @Size(max = 100, message = "模型标识符长度不能超过100")
    private String modelIdentifier;

    @Schema(description = "模型调用方式", required = true)
    @NotBlank(message = "模型调用方式不能为空")
    private String modelCallingType;

    @Schema(description = "API基础URL")
    @Size(max = 500, message = "API基础URL长度不能超过500")
    private String apiBaseUrl;

    @Schema(description = "API Key")
    private String apiKey;

    @Schema(description = "提供商ID")
    private Long providerId;

    @Schema(description = "提供协议类型")
    private String providerType;

    @Schema(description = "模型配置（JSON格式字符串）")
    private String modelConfig;

    @Schema(description = "描述信息")
    private String description;

    @Schema(description = "是否启用", required = true)
    @NotNull(message = "是否启用不能为空")
    private Boolean isEnabled;
}
