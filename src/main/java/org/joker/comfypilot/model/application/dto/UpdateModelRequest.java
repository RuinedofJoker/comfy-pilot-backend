package org.joker.comfypilot.model.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 更新模型请求
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "更新模型请求")
public class UpdateModelRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "模型名称")
    @Size(max = 100, message = "模型名称长度不能超过100")
    private String modelName;

    @Schema(description = "模型配置（JSON格式）")
    private String modelConfig;

    @Schema(description = "描述信息")
    private String description;
}
