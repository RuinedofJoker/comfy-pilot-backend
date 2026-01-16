package org.joker.comfypilot.permission.domain.repository;

import org.joker.comfypilot.permission.domain.entity.Permission;

import java.util.List;
import java.util.Optional;

/**
 * 权限仓储接口
 */
public interface PermissionRepository {

    /**
     * 根据ID查询权限
     */
    Optional<Permission> findById(Long id);

    /**
     * 根据权限编码查询
     */
    Optional<Permission> findByPermissionCode(String permissionCode);

    /**
     * 批量查询权限
     */
    List<Permission> findByIds(List<Long> ids);

    /**
     * 保存权限
     */
    Permission save(Permission permission);
}
