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
 * 创建API密钥请求
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "创建API密钥请求")
public class CreateApiKeyRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "提供商ID", required = true)
    @NotNull(message = "提供商ID不能为空")
    private Long providerId;

    @Schema(description = "密钥名称", required = true)
    @NotBlank(message = "密钥名称不能为空")
    @Size(max = 100, message = "密钥名称长度不能超过100")
    private String keyName;

    @Schema(description = "API密钥", required = true)
    @NotBlank(message = "API密钥不能为空")
    private String apiKey;
}
