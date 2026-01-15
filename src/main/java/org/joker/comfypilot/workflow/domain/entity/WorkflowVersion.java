package org.joker.comfypilot.workflow.domain.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 工作流版本领域实体
 */
@Data
public class WorkflowVersion {

    private Long id;
    private Long workflowId;
    private Long sessionId;
    private Integer versionNumber;
    private String workflowData;
    private String changeSummary;
    private LocalDateTime createTime;
    private Long createBy;

    /**
     * 创建新版本
     */
    public static WorkflowVersion create(Long workflowId, Long sessionId, Integer versionNumber,
                                         String workflowData, String changeSummary) {
        // TODO: 实现创建新版本逻辑
        return null;
    }
}
