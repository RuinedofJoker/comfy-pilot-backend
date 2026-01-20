package org.joker.comfypilot.workflow.interfaces.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.joker.comfypilot.common.interfaces.response.Result;
import org.joker.comfypilot.workflow.application.dto.WorkflowVersionDTO;
import org.joker.comfypilot.workflow.application.service.WorkflowVersionService;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 工作流版本控制器
 */
@Tag(name = "工作流版本管理", description = "工作流版本创建、查询相关接口")
@RestController
@RequestMapping("/api/v1/workflows/{workflowId}/versions")
public class WorkflowVersionController {

    @Autowired
    private WorkflowVersionService versionService;

    /**
     * 创建工作流版本
     *//*
    @Operation(summary = "创建工作流版本", description = "创建新的工作流版本（Agent对话时调用）")
    @PostMapping
    public Result<WorkflowVersionDTO> createVersion(
            @Parameter(description = "工作流ID", required = true) @PathVariable Long workflowId,
            @Validated @RequestBody CreateVersionRequest request) {
        Long userId = UserContextHolder.getCurrentUserId();
        WorkflowVersionDTO dto = versionService.createVersion(workflowId, request, userId);
        return Result.success(dto);
    }

    *//**
     * 查询工作流版本列表
     *//*
    @Operation(summary = "查询版本列表", description = "查询指定工作流的所有版本（按版本号降序）")
    @GetMapping
    public Result<List<WorkflowVersionDTO>> listVersions(
            @Parameter(description = "工作流ID", required = true) @PathVariable Long workflowId) {
        List<WorkflowVersionDTO> list = versionService.listVersions(workflowId);
        return Result.success(list);
    }
*/
    /**
     * 查询版本详情
     */
    @Operation(summary = "查询版本详情", description = "查询指定版本的详细信息")
    @GetMapping("/{versionCode}")
    public Result<WorkflowVersionDTO> getVersionById(
            @Parameter(description = "工作流ID", required = true) @PathVariable Long workflowId,
            @Parameter(description = "版本ID", required = true) @PathVariable String versionCode) {
        WorkflowVersionDTO dto = versionService.getVersionByVersionCode(workflowId, versionCode);
        return Result.success(dto);
    }
}
