package org.joker.comfypilot.workflow.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 工作流锁定信息
 * 存储在Redis中的锁定信息
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkflowLockInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 工作流ID
     */
    private Long workflowId;

    /**
     * 锁定消息ID
     */
    private Long messageId;

    /**
     * 锁定时间
     */
    private LocalDateTime lockedAt;
}
