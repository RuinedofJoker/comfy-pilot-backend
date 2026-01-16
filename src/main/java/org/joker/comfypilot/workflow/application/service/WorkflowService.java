package org.joker.comfypilot.workflow.application.service;

import org.joker.comfypilot.workflow.application.dto.*;
import org.joker.comfypilot.workflow.domain.entity.Workflow;

import java.util.List;

/**
 * 工作流应用服务接口
 */
public interface WorkflowService {

    /**
     * 创建工作流
     *
     * @param request 创建请求
     * @param userId  创建人ID
     * @return 工作流DTO
     */
    WorkflowDTO createWorkflow(CreateWorkflowRequest request, Long userId);

    /**
     * 根据ID查询工作流
     *
     * @param id 工作流ID
     * @return 工作流DTO
     */
    WorkflowDTO getById(Long id);

    /**
     * 查询工作流列表
     *
     * @param comfyuiServerId ComfyUI服务ID（可选）
     * @param isLocked        是否锁定（可选）
     * @param createBy        创建人ID（可选）
     * @return 工作流列表
     */
    List<WorkflowDTO> listWorkflows(Long comfyuiServerId, Boolean isLocked, Long createBy);

    /**
     * 更新工作流信息
     *
     * @param id      工作流ID
     * @param request 更新请求
     * @param userId  更新人ID
     * @return 工作流DTO
     */
    WorkflowDTO updateWorkflow(Long id, UpdateWorkflowRequest request, Long userId);

    /**
     * 删除工作流
     *
     * @param id     工作流ID
     * @param userId 删除人ID
     */
    void deleteWorkflow(Long id, Long userId);

    /**
     * 保存工作流内容
     *
     * @param id      工作流ID
     * @param request 保存内容请求
     * @param userId  操作人ID
     * @return 工作流DTO
     */
    WorkflowDTO saveContent(Long id, SaveWorkflowContentRequest request, Long userId);

    /**
     * 获取工作流内容
     *
     * @param id 工作流ID
     * @return 工作流内容（JSON字符串）
     */
    String getContent(Long id);

    /**
     * 锁定工作流
     *
     * @param id     工作流ID
     * @param userId 锁定人ID
     * @return 工作流DTO
     */
    WorkflowDTO lockWorkflow(Long id, Long userId);

    /**
     * 解锁工作流
     *
     * @param id     工作流ID
     * @param userId 解锁人ID
     * @return 工作流DTO
     */
    WorkflowDTO unlockWorkflow(Long id, Long userId);
}
