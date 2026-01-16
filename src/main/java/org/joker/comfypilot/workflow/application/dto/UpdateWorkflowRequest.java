package org.joker.comfypilot.workflow.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 更新工作流请求DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "更新工作流请求")
public class UpdateWorkflowRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "工作流名称", example = "更新后的工作流名称")
    @Size(min = 2, max = 100, message = "工作流名称长度必须在2-100个字符之间")
    private String workflowName;

    @Schema(description = "工作流描述", example = "更新后的描述")
    @Size(max = 500, message = "工作流描述长度不能超过500个字符")
    private String description;

    @Schema(description = "工作流缩略图URL", example = "https://example.com/thumbnail.png")
    @Size(max = 500, message = "缩略图URL长度不能超过500个字符")
    private String thumbnailUrl;
}
