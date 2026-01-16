package org.joker.comfypilot.workflow.domain.repository;

import org.joker.comfypilot.workflow.domain.entity.WorkflowVersion;

import java.util.List;
import java.util.Optional;

/**
 * 工作流版本仓储接口
 */
public interface WorkflowVersionRepository {

    /**
     * 根据ID查询
     *
     * @param id 版本ID
     * @return 版本实体
     */
    Optional<WorkflowVersion> findById(Long id);

    /**
     * 根据工作流ID查询所有版本
     *
     * @param workflowId 工作流ID
     * @return 版本列表（按版本号降序）
     */
    List<WorkflowVersion> findByWorkflowId(Long workflowId);

    /**
     * 根据工作流ID和版本号查询
     *
     * @param workflowId    工作流ID
     * @param versionNumber 版本号
     * @return 版本实体
     */
    Optional<WorkflowVersion> findByWorkflowIdAndVersionNumber(Long workflowId, Integer versionNumber);

    /**
     * 根据内容哈希查询
     *
     * @param workflowId  工作流ID
     * @param contentHash 内容哈希值
     * @return 版本实体
     */
    Optional<WorkflowVersion> findByWorkflowIdAndContentHash(Long workflowId, String contentHash);

    /**
     * 获取工作流的最大版本号
     *
     * @param workflowId 工作流ID
     * @return 最大版本号（如果没有版本则返回0）
     */
    Integer getMaxVersionNumber(Long workflowId);

    /**
     * 保存版本
     *
     * @param version 版本实体
     * @return 保存后的版本实体
     */
    WorkflowVersion save(WorkflowVersion version);

    /**
     * 根据ID删除
     *
     * @param id 版本ID
     */
    void deleteById(Long id);
}
