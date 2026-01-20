package org.joker.comfypilot.workflow.interfaces.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.joker.comfypilot.auth.infrastructure.context.UserContextHolder;
import org.joker.comfypilot.common.interfaces.response.Result;
import org.joker.comfypilot.workflow.application.dto.*;
import org.joker.comfypilot.workflow.application.service.WorkflowService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * 工作流控制器
 */
@Tag(name = "工作流管理", description = "工作流创建、查询、编辑、锁定相关接口")
@RestController
@RequestMapping("/api/v1/workflows")
public class WorkflowController {

    @Autowired
    private WorkflowService workflowService;

    /**
     * 创建工作流
     */
    @Operation(summary = "创建工作流", description = "创建新的工作流")
    @PostMapping
    public Result<WorkflowDTO> createWorkflow(@Validated @RequestBody CreateWorkflowRequest request) {
        Long userId = UserContextHolder.getCurrentUserId();
        WorkflowDTO dto = workflowService.createWorkflow(request, userId);
        return Result.success(dto);
    }

    /**
     * 查询工作流列表
     */
    @Operation(summary = "查询当前用户的工作流列表", description = "查询工作流列表，支持按ComfyUI服务过滤")
    @GetMapping
    public Result<List<WorkflowDTO>> listWorkflows(
            @Parameter(description = "ComfyUI服务ID") @RequestParam(required = false) Long comfyuiServerId
    ) {
        Long currentUserId = UserContextHolder.getCurrentUserId();
        List<WorkflowDTO> list = workflowService.listWorkflows(comfyuiServerId, null, currentUserId);
        return Result.success(list);
    }

    /**
     * 查询工作流详情
     */
    @Operation(summary = "查询工作流详情", description = "根据工作流ID查询详细信息")
    @GetMapping("/{id}")
    public Result<WorkflowDTO> getWorkflowById(
            @Parameter(description = "工作流ID", required = true) @PathVariable Long id) {
        WorkflowDTO dto = workflowService.getById(id);
        return Result.success(dto);
    }

    /**
     * 更新工作流信息
     */
    @Operation(summary = "更新工作流信息", description = "更新工作流的基本信息（名称、描述、缩略图）")
    @PutMapping("/{id}")
    public Result<WorkflowDTO> updateWorkflow(
            @Parameter(description = "工作流ID", required = true) @PathVariable Long id,
            @Validated @RequestBody UpdateWorkflowRequest request) {
        WorkflowDTO dto = workflowService.updateWorkflow(id, request);
        return Result.success(dto);
    }

    /**
     * 删除工作流
     */
    @Operation(summary = "删除工作流", description = "删除指定的工作流")
    @DeleteMapping("/{id}")
    public Result<Void> deleteWorkflow(
            @Parameter(description = "工作流ID", required = true) @PathVariable Long id,
            @Parameter(description = "消息ID", required = true) @RequestParam Long messageId) {
        workflowService.deleteWorkflow(id, messageId);
        return Result.success();
    }

    /**
     * 保存工作流内容
     */
    @Operation(summary = "保存工作流内容", description = "保存工作流的激活内容（用户手动保存）")
    @PostMapping("/{id}/content")
    public Result<WorkflowDTO> saveContent(
            @Parameter(description = "工作流ID", required = true) @PathVariable Long id,
            @Parameter(description = "消息ID", required = true) @RequestParam Long messageId,
            @Validated @RequestBody SaveWorkflowContentRequest request) {
        WorkflowDTO dto = workflowService.saveContent(id, request, messageId);
        return Result.success(dto);
    }

    /**
     * 获取工作流内容
     */
    @Operation(summary = "获取工作流内容", description = "获取工作流的激活内容（JSON格式）")
    @GetMapping("/{id}/content")
    public Result<String> getContent(
            @Parameter(description = "工作流ID", required = true) @PathVariable Long id) {
        String content = workflowService.getContent(id);
        return Result.success(content);
    }

}
