package org.joker.comfypilot.session.application.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.joker.comfypilot.common.application.dto.BaseDTO;

/**
 * 会话DTO
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ChatSessionDTO extends BaseDTO {

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 工作流ID
     */
    private Long workflowId;

    /**
     * 会话标题
     */
    private String title;

    /**
     * 状态
     */
    private String status;

    /**
     * 最后消息摘要
     */
    private String lastMessageSummary;
}
