package org.joker.comfypilot.session.application.dto;

import lombok.Data;

/**
 * 会话创建请求DTO
 */
@Data
public class ChatSessionCreateRequest {

    /**
     * 工作流ID
     */
    private Long workflowId;

    /**
     * 会话标题
     */
    private String title;
}
