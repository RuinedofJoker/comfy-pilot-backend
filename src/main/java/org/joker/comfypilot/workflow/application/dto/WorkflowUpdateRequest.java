package org.joker.comfypilot.workflow.application.dto;

import lombok.Data;

/**
 * 工作流更新请求DTO
 */
@Data
public class WorkflowUpdateRequest {

    /**
     * 工作流名称
     */
    private String name;

    /**
     * 描述
     */
    private String description;

    /**
     * 工作流JSON数据
     */
    private String workflowData;
}
