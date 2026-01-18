package org.joker.comfypilot.permission.application.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.joker.comfypilot.auth.infrastructure.context.UserContextHolder;
import org.joker.comfypilot.common.exception.BusinessException;
import org.joker.comfypilot.permission.application.dto.RoleDTO;
import org.joker.comfypilot.permission.application.service.PermissionService;
import org.joker.comfypilot.permission.domain.entity.Permission;
import org.joker.comfypilot.permission.domain.entity.Role;
import org.joker.comfypilot.permission.domain.entity.UserRole;
import org.joker.comfypilot.permission.domain.repository.RolePermissionRepository;
import org.joker.comfypilot.permission.domain.repository.RoleRepository;
import org.joker.comfypilot.permission.domain.repository.UserRoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 权限服务实现
 */
@Slf4j
@Service
public class PermissionServiceImpl implements PermissionService {

    @Autowired
    private UserRoleRepository userRoleRepository;
    @Autowired
    private RolePermissionRepository rolePermissionRepository;
    @Autowired
    private RoleRepository roleRepository;

    @Override
    public List<RoleDTO> getCurrentUserRoles() {
        Long userId = UserContextHolder.getCurrentUserId();
        return getUserRoles(userId);
    }

    @Override
    public List<String> getCurrentUserPermissions() {
        Long userId = UserContextHolder.getCurrentUserId();
        return getUserPermissions(userId);
    }

    @Override
    public List<RoleDTO> getUserRoles(Long userId) {
        List<Role> roles = userRoleRepository.findRolesByUserId(userId);
        return roles.stream()
                .map(role -> RoleDTO.builder()
                        .roleCode(role.getRoleCode())
                        .roleName(role.getRoleName())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public List<String> getUserPermissions(Long userId) {
        // 1. 查询用户的所有角色
        List<Role> roles = userRoleRepository.findRolesByUserId(userId);
        if (roles.isEmpty()) {
            return List.of();
        }

        // 2. 获取角色ID列表
        List<Long> roleIds = roles.stream()
                .map(Role::getId)
                .collect(Collectors.toList());

        // 3. 查询这些角色的所有权限
        List<Permission> permissions = rolePermissionRepository.findPermissionsByRoleIds(roleIds);

        // 4. 返回权限编码列表
        return permissions.stream()
                .map(Permission::getPermissionCode)
                .distinct()
                .collect(Collectors.toList());
    }

    @Override
    public void assignRoleToUser(Long userId, String roleCode) {
        // 1. 查询角色是否存在
        Role role = roleRepository.findByRoleCode(roleCode)
                .orElseThrow(() -> new BusinessException("角色不存在: " + roleCode));

        // 2. 检查是否已经分配
        List<Role> existingRoles = userRoleRepository.findRolesByUserId(userId);
        boolean alreadyAssigned = existingRoles.stream()
                .anyMatch(r -> r.getId().equals(role.getId()));

        if (alreadyAssigned) {
            log.info("用户已拥有该角色, userId={}, roleCode={}", userId, roleCode);
            return;
        }

        // 3. 创建用户角色关联
        UserRole userRole = UserRole.builder()
                .userId(userId)
                .roleId(role.getId())
                .createTime(LocalDateTime.now())
                .build();

        userRoleRepository.save(userRole);
        log.info("为用户分配角色成功, userId={}, roleCode={}", userId, roleCode);
    }
}
