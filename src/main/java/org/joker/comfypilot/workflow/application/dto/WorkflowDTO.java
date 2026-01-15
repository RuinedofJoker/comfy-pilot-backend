package org.joker.comfypilot.workflow.application.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.joker.comfypilot.common.application.dto.BaseDTO;

import java.time.LocalDateTime;

/**
 * 工作流DTO
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class WorkflowDTO extends BaseDTO {

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 服务ID
     */
    private Long serviceId;

    /**
     * 服务名称
     */
    private String serviceName;

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

    /**
     * 是否收藏
     */
    private Boolean isFavorite;

    /**
     * 会话数量
     */
    private Integer sessionCount;

    /**
     * 版本数量
     */
    private Integer versionCount;

    /**
     * 最后使用时间
     */
    private LocalDateTime lastUsedAt;
}
