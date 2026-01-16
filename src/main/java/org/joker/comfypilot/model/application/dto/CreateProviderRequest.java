package org.joker.comfypilot.model.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 创建提供商请求
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "创建提供商请求")
public class CreateProviderRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "提供商名称", required = true)
    @NotBlank(message = "提供商名称不能为空")
    @Size(max = 100, message = "提供商名称长度不能超过100")
    private String providerName;

    @Schema(description = "提供商类型", required = true)
    @NotBlank(message = "提供商类型不能为空")
    private String providerType;

    @Schema(description = "API基础URL")
    @Size(max = 500, message = "API基础URL长度不能超过500")
    private String apiBaseUrl;

    @Schema(description = "描述信息")
    private String description;
}
