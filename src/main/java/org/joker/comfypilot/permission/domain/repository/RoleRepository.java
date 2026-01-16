package org.joker.comfypilot.permission.domain.repository;

import org.joker.comfypilot.permission.domain.entity.Role;

import java.util.Optional;

/**
 * 角色仓储接口
 */
public interface RoleRepository {

    /**
     * 根据ID查询角色
     */
    Optional<Role> findById(Long id);

    /**
     * 根据角色编码查询
     */
    Optional<Role> findByRoleCode(String roleCode);

    /**
     * 检查角色编码是否存在
     */
    boolean existsByRoleCode(String roleCode);

    /**
     * 保存角色
     */
    Role save(Role role);

    /**
     * 删除角色（逻辑删除）
     */
    void deleteById(Long id);
}
