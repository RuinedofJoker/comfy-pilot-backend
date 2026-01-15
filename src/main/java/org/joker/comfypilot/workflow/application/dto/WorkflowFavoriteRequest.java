package org.joker.comfypilot.workflow.application.dto;

import lombok.Data;

/**
 * 工作流收藏请求DTO
 */
@Data
public class WorkflowFavoriteRequest {

    /**
     * 是否收藏
     */
    private Boolean favorite;
}
