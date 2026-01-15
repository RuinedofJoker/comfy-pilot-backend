package org.joker.comfypilot.workflow.application.dto;

import lombok.Data;

/**
 * 工作流创建请求DTO
 */
@Data
public class WorkflowCreateRequest {

    /**
     * 服务ID
     */
    private Long serviceId;

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
