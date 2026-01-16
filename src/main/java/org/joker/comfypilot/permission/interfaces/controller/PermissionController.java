package org.joker.comfypilot.permission.interfaces.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.joker.comfypilot.common.interfaces.response.Result;
import org.joker.comfypilot.permission.application.dto.RoleDTO;
import org.joker.comfypilot.permission.application.service.PermissionService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 权限控制器
 */
@Tag(name = "权限管理", description = "权限相关接口")
@RestController
@RequestMapping("/api/v1/permissions")
@RequiredArgsConstructor
public class PermissionController {

    private final PermissionService permissionService;

    @Operation(summary = "获取当前用户角色")
    @GetMapping("/my-roles")
    public Result<List<RoleDTO>> getCurrentUserRoles() {
        List<RoleDTO> roles = permissionService.getCurrentUserRoles();
        return Result.success(roles);
    }

    @Operation(summary = "获取当前用户权限")
    @GetMapping("/my-permissions")
    public Result<List<String>> getCurrentUserPermissions() {
        List<String> permissions = permissionService.getCurrentUserPermissions();
        return Result.success(permissions);
    }
}
