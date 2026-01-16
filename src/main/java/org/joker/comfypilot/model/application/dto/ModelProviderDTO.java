package org.joker.comfypilot.model.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.joker.comfypilot.common.application.dto.BaseDTO;

/**
 * 模型提供商信息
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Schema(description = "模型提供商信息")
public class ModelProviderDTO extends BaseDTO {

    private static final long serialVersionUID = 1L;

    @Schema(description = "提供商名称")
    private String providerName;

    @Schema(description = "提供商类型")
    private String providerType;

    @Schema(description = "API基础URL")
    private String apiBaseUrl;

    @Schema(description = "描述信息")
    private String description;

    @Schema(description = "是否启用")
    private Boolean isEnabled;
}
