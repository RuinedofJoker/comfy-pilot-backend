package org.joker.comfypilot.workflow.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.joker.comfypilot.common.application.dto.BaseDTO;

import java.time.LocalDateTime;

/**
 * 工作流信息DTO
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Schema(description = "工作流信息")
public class WorkflowDTO extends BaseDTO {

    private static final long serialVersionUID = 1L;

    @Schema(description = "工作流名称")
    private String workflowName;

    @Schema(description = "工作流描述")
    private String description;

    @Schema(description = "所属ComfyUI服务ID")
    private Long comfyuiServerId;

    @Schema(description = "所属ComfyUI服务唯一标识符")
    private String comfyuiServerKey;

    @Schema(description = "当前激活版本的内容（JSON格式）")
    private String activeContent;

    @Schema(description = "激活内容的SHA-256哈希值")
    private String activeContentHash;

    @Schema(description = "工作流缩略图URL")
    private String thumbnailUrl;

    @Schema(description = "是否锁定")
    private Boolean isLocked;

    @Schema(description = "锁定人ID")
    private Long lockedBy;

    @Schema(description = "锁定时间")
    private LocalDateTime lockedAt;
}
