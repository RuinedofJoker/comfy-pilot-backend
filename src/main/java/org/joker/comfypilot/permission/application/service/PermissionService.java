package org.joker.comfypilot.permission.application.service;

import org.joker.comfypilot.permission.application.dto.RoleDTO;

import java.util.List;

/**
 * 权限服务接口
 */
public interface PermissionService {

    /**
     * 获取当前用户的角色列表
     */
    List<RoleDTO> getCurrentUserRoles();

    /**
     * 获取当前用户的权限列表
     */
    List<String> getCurrentUserPermissions();

    /**
     * 获取指定用户的角色列表
     */
    List<RoleDTO> getUserRoles(Long userId);

    /**
     * 获取指定用户的权限列表
     */
    List<String> getUserPermissions(Long userId);

    /**
     * 为用户分配角色
     */
    void assignRoleToUser(Long userId, String roleCode);
}
