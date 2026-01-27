package org.joker.comfypilot.model.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.joker.comfypilot.common.application.dto.BaseDTO;

/**
 * AI模型信息
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Schema(description = "AI模型信息")
public class AiModelDTO extends BaseDTO {

    private static final long serialVersionUID = 1L;

    @Schema(description = "模型名称")
    private String modelName;

    @Schema(description = "模型标识符")
    private String modelIdentifier;

    @Schema(description = "模型显示名称")
    private String modelDisplayName;

    @Schema(description = "接入方式（remote_api/local）")
    private String accessType;

    @Schema(description = "模型类型（llm/embedding等）")
    private String modelType;

    @Schema(description = "模型调用方式")
    private String modelCallingType;

    @Schema(description = "API基础URL")
    private String apiBaseUrl;

    @Schema(description = "API Key")
    private String apiKey;

    @Schema(description = "提供商ID")
    private Long providerId;

    @Schema(description = "提供协议类型")
    private String providerType;

    @Schema(description = "模型配置（json格式）")
    private String modelConfig;

    @Schema(description = "描述信息")
    private String description;

    @Schema(description = "是否启用")
    private Boolean isEnabled;
}
