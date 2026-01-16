package org.joker.comfypilot.model.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 更新提供商请求
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "更新提供商请求")
public class UpdateProviderRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "提供商名称")
    @Size(max = 100, message = "提供商名称长度不能超过100")
    private String providerName;

    @Schema(description = "API基础URL")
    @Size(max = 500, message = "API基础URL长度不能超过500")
    private String apiBaseUrl;

    @Schema(description = "描述信息")
    private String description;
}
