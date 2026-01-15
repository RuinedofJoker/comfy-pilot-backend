package org.joker.comfypilot.workflow.domain.repository;

import org.joker.comfypilot.workflow.domain.entity.WorkflowVersion;

import java.util.List;
import java.util.Optional;

/**
 * 工作流版本仓储接口
 */
public interface WorkflowVersionRepository {

    /**
     * 保存版本
     */
    WorkflowVersion save(WorkflowVersion version);

    /**
     * 根据ID查询版本
     */
    Optional<WorkflowVersion> findById(Long id);

    /**
     * 根据工作流ID查询版本列表
     */
    List<WorkflowVersion> findByWorkflowId(Long workflowId);

    /**
     * 根据会话ID查询版本列表
     */
    List<WorkflowVersion> findBySessionId(Long sessionId);
}
