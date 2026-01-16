package org.joker.comfypilot.workflow.application.service;

import org.joker.comfypilot.workflow.application.dto.CreateVersionRequest;
import org.joker.comfypilot.workflow.application.dto.WorkflowVersionDTO;

import java.util.List;

/**
 * 工作流版本应用服务接口
 */
public interface WorkflowVersionService {

    /**
     * 创建工作流版本
     *
     * @param workflowId 工作流ID
     * @param request    创建请求
     * @param userId     创建人ID
     * @return 版本DTO
     */
    WorkflowVersionDTO createVersion(Long workflowId, CreateVersionRequest request, Long userId);

    /**
     * 查询工作流的版本列表
     *
     * @param workflowId 工作流ID
     * @return 版本列表
     */
    List<WorkflowVersionDTO> listVersions(Long workflowId);

    /**
     * 查询版本详情
     *
     * @param workflowId 工作流ID
     * @param versionId  版本ID
     * @return 版本DTO
     */
    WorkflowVersionDTO getVersionById(Long workflowId, Long versionId);
}
