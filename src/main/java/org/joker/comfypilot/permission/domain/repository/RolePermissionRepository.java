package org.joker.comfypilot.permission.domain.repository;

import org.joker.comfypilot.permission.domain.entity.Permission;
import org.joker.comfypilot.permission.domain.entity.RolePermission;

import java.util.List;

/**
 * 角色权限仓储接口
 */
public interface RolePermissionRepository {

    /**
     * 查询角色的所有权限关联
     */
    List<RolePermission> findByRoleId(Long roleId);

    /**
     * 查询角色的所有权限实体
     */
    List<Permission> findPermissionsByRoleId(Long roleId);

    /**
     * 批量查询多个角色的权限
     */
    List<Permission> findPermissionsByRoleIds(List<Long> roleIds);

    /**
     * 保存角色权限关联
     */
    RolePermission save(RolePermission rolePermission);
}
