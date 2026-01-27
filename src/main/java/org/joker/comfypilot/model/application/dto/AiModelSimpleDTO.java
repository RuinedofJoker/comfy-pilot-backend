package org.joker.comfypilot.model.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * AI模型简化信息（用于前台查询）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "AI模型简化信息")
public class AiModelSimpleDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "模型ID")
    private Long id;

    @Schema(description = "模型名称")
    private String modelName;

    @Schema(description = "模型显示名称")
    private String modelDisplayName;

    @Schema(description = "模型标识符")
    private String modelIdentifier;

    @Schema(description = "模型类型（llm/embedding等）")
    private String modelType;

    @Schema(description = "模型调用方式")
    private String modelCallingType;

    @Schema(description = "描述信息")
    private String description;
}
