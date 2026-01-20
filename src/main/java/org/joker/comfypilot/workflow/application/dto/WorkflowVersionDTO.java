package org.joker.comfypilot.workflow.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.joker.comfypilot.common.application.dto.BaseDTO;

/**
 * 工作流版本信息DTO
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Schema(description = "工作流版本信息")
public class WorkflowVersionDTO extends BaseDTO {

    private static final long serialVersionUID = 1L;

    @Schema(description = "所属工作流ID")
    private Long workflowId;

    @Schema(description = "版本号")
    private String versionCode;

    @Schema(description = "来源版本号")
    private String fromVersionCode;

    @Schema(description = "版本内容（JSON格式）")
    private String content;

    @Schema(description = "内容的SHA-256哈希值")
    private String contentHash;

    @Schema(description = "变更摘要")
    private String changeSummary;

    @Schema(description = "关联的会话ID")
    private Long sessionId;
}
