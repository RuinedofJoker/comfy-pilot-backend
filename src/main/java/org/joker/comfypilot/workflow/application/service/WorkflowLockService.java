package org.joker.comfypilot.workflow.application.service;

import org.joker.comfypilot.workflow.domain.model.WorkflowLockInfo;

import java.util.Optional;

/**
 * 工作流锁定服务接口
 * 使用Redis管理工作流的锁定状态
 */
public interface WorkflowLockService {

    /**
     * 锁定工作流
     *
     * @param workflowId 工作流ID
     * @param messageId  消息ID
     * @return 是否锁定成功
     */
    boolean lockWorkflow(Long workflowId, Long messageId);

    /**
     * 解锁工作流
     *
     * @param workflowId 工作流ID
     * @param messageId  消息ID（只有锁定该工作流的消息才能解锁）
     * @return 是否解锁成功
     */
    boolean unlockWorkflow(Long workflowId, Long messageId);

    /**
     * 获取工作流锁定信息
     *
     * @param workflowId 工作流ID
     * @return 锁定信息，如果未锁定则返回空
     */
    Optional<WorkflowLockInfo> getLockInfo(Long workflowId);

    /**
     * 检查工作流是否被锁定
     *
     * @param workflowId 工作流ID
     * @return 是否被锁定
     */
    boolean isLocked(Long workflowId);

    /**
     * 强制解锁工作流（管理员操作）
     *
     * @param workflowId 工作流ID
     */
    void forceUnlock(Long workflowId);
}
