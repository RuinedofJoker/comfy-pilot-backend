package org.joker.comfypilot.permission.domain.repository;

import org.joker.comfypilot.permission.domain.entity.Role;
import org.joker.comfypilot.permission.domain.entity.UserRole;

import java.util.List;

/**
 * 用户角色仓储接口
 */
public interface UserRoleRepository {

    /**
     * 查询用户的所有角色关联
     */
    List<UserRole> findByUserId(Long userId);

    /**
     * 查询用户的所有角色实体
     */
    List<Role> findRolesByUserId(Long userId);

    /**
     * 保存用户角色关联
     */
    UserRole save(UserRole userRole);

    /**
     * 删除用户角色关联
     */
    void deleteByUserIdAndRoleId(Long userId, Long roleId);
}
