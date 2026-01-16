package org.joker.comfypilot.model.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
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

    @Schema(description = "模型标识符", required = true)
    @NotBlank(message = "模型标识符不能为空")
    @Size(max = 100, message = "模型标识符长度不能超过100")
    private String modelIdentifier;

    @Schema(description = "接入方式（remote_api/local）", required = true)
    @NotBlank(message = "接入方式不能为空")
    private String accessType;

    @Schema(description = "模型类型（llm/embedding等）", required = true)
    @NotBlank(message = "模型类型不能为空")
    private String modelType;

    @Schema(description = "提供商ID（远程API时必填）")
    private Long providerId;

    @Schema(description = "模型配置（JSON格式）")
    private String modelConfig;

    @Schema(description = "描述信息")
    private String description;
}
