package org.joker.comfypilot.cfsvr.interfaces.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.joker.comfypilot.cfsvr.application.dto.ComfyuiServerDTO;
import org.joker.comfypilot.cfsvr.application.dto.ComfyuiServerPublicDTO;
import org.joker.comfypilot.cfsvr.application.dto.CreateServerRequest;
import org.joker.comfypilot.cfsvr.application.dto.UpdateServerRequest;
import org.joker.comfypilot.cfsvr.application.service.ComfyuiServerService;
import org.joker.comfypilot.common.interfaces.response.Result;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * ComfyUI服务控制器
 */
@Tag(name = "ComfyUI服务管理", description = "ComfyUI服务配置、查询、管理相关接口")
@RestController
@RequestMapping("/api/v1/comfyui-servers")
public class ComfyuiServerController {

    @Autowired
    private ComfyuiServerService comfyuiServerService;

    /**
     * 创建ComfyUI服务（手动）
     */
    @Operation(summary = "创建ComfyUI服务", description = "管理员手动创建ComfyUI服务")
    @PostMapping
    public Result<ComfyuiServerDTO> createServer(@Validated @RequestBody CreateServerRequest request) {
        ComfyuiServerDTO dto = comfyuiServerService.createManually(request);
        return Result.success(dto);
    }

    /**
     * 查询服务列表（后台管理使用）
     */
    @Operation(summary = "查询服务列表", description = "查询所有ComfyUI服务列表（后台管理使用）")
    @GetMapping
    public Result<List<ComfyuiServerDTO>> listServers() {
        List<ComfyuiServerDTO> list = comfyuiServerService.listServers();
        return Result.success(list);
    }

    /**
     * 查询启用的服务列表（前台使用）
     */
    @Operation(summary = "查询启用的服务列表", description = "查询启用的ComfyUI服务列表（前台使用，不包含敏感配置）")
    @GetMapping("/enabled")
    public Result<List<ComfyuiServerPublicDTO>> listEnabledServers() {
        List<ComfyuiServerPublicDTO> list = comfyuiServerService.listEnabledServers();
        return Result.success(list);
    }

    /**
     * 根据ID查询服务详情
     */
    @Operation(summary = "查询服务详情", description = "根据服务ID查询详细信息")
    @GetMapping("/{id}")
    public Result<ComfyuiServerDTO> getServerById(
            @Parameter(description = "服务ID", required = true) @PathVariable Long id) {
        ComfyuiServerDTO dto = comfyuiServerService.getById(id);
        return Result.success(dto);
    }

    /**
     * 根据serverKey查询服务
     */
    @Operation(summary = "根据标识符查询", description = "根据服务唯一标识符查询服务信息")
    @GetMapping("/key/{serverKey}")
    public Result<ComfyuiServerDTO> getServerByKey(
            @Parameter(description = "服务唯一标识符", required = true) @PathVariable String serverKey) {
        ComfyuiServerDTO dto = comfyuiServerService.getByServerKey(serverKey);
        return Result.success(dto);
    }

    /**
     * 更新服务信息
     */
    @Operation(summary = "更新服务信息", description = "更新ComfyUI服务信息")
    @PutMapping("/{id}")
    public Result<ComfyuiServerDTO> updateServer(
            @Parameter(description = "服务ID", required = true) @PathVariable Long id,
            @Validated @RequestBody UpdateServerRequest request) {
        ComfyuiServerDTO dto = comfyuiServerService.updateServer(id, request);
        return Result.success(dto);
    }

    /**
     * 删除服务
     */
    @Operation(summary = "删除服务", description = "删除ComfyUI服务")
    @DeleteMapping("/{id}")
    public Result<Void> deleteServer(
            @Parameter(description = "服务ID", required = true) @PathVariable Long id) {
        comfyuiServerService.deleteServer(id);
        return Result.success();
    }
}
