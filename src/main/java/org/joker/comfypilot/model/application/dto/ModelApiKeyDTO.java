package org.joker.comfypilot.model.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.joker.comfypilot.common.application.dto.BaseDTO;

/**
 * 模型API密钥信息
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Schema(description = "模型API密钥信息")
public class ModelApiKeyDTO extends BaseDTO {

    private static final long serialVersionUID = 1L;

    @Schema(description = "提供商ID")
    private Long providerId;

    @Schema(description = "密钥名称")
    private String keyName;

    @Schema(description = "API密钥（脱敏显示）")
    private String apiKey;

    @Schema(description = "是否启用")
    private Boolean isEnabled;
}
