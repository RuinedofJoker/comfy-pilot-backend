package org.joker.comfypilot.workflow.application.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 工作流版本DTO
 */
@Data
public class WorkflowVersionDTO {

    /**
     * 版本ID
     */
    private Long id;

    /**
     * 工作流ID
     */
    private Long workflowId;

    /**
     * 会话ID
     */
    private Long sessionId;

    /**
     * 版本号
     */
    private Integer versionNumber;

    /**
     * 工作流快照
     */
    private String workflowData;

    /**
     * 变更摘要
     */
    private String changeSummary;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 创建人ID
     */
    private Long createBy;
}
