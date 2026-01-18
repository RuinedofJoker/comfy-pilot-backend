package org.joker.comfypilot.permission.config;

import lombok.extern.slf4j.Slf4j;
import org.joker.comfypilot.permission.domain.entity.Permission;
import org.joker.comfypilot.permission.domain.entity.Role;
import org.joker.comfypilot.permission.domain.entity.RolePermission;
import org.joker.comfypilot.permission.domain.repository.PermissionRepository;
import org.joker.comfypilot.permission.domain.repository.RolePermissionRepository;
import org.joker.comfypilot.permission.domain.repository.RoleRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 权限模块初始化器
 * 应用启动时自动创建系统内置角色和基础权限
 */
@Slf4j
@Component
public class PermissionInitializer implements CommandLineRunner {

    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private PermissionRepository permissionRepository;
    @Autowired
    private RolePermissionRepository rolePermissionRepository;

    @Override
    public void run(String... args) {
        log.info("开始初始化权限模块...");

        try {
            // 1. 初始化系统角色
            initSystemRoles();

            // 2. 初始化基础权限
            initBasePermissions();

            // 3. 为角色分配权限
            assignPermissionsToRoles();

            log.info("权限模块初始化完成");
        } catch (Exception e) {
            log.error("权限模块初始化失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 初始化系统角色
     */
    private void initSystemRoles() {
        log.info("初始化系统角色...");

        // 创建 ADMIN 角色
        if (!roleRepository.existsByRoleCode("ADMIN")) {
            Role adminRole = Role.builder()
                    .roleCode("ADMIN")
                    .roleName("管理员")
                    .description("系统管理员，拥有所有权限")
                    .isSystem(true)
                    .createTime(LocalDateTime.now())
                    .isDeleted(false)
                    .build();
            roleRepository.save(adminRole);
            log.info("创建系统角色: ADMIN");
        }

        // 创建 USER 角色
        if (!roleRepository.existsByRoleCode("USER")) {
            Role userRole = Role.builder()
                    .roleCode("USER")
                    .roleName("普通用户")
                    .description("普通用户，拥有基础权限")
                    .isSystem(true)
                    .createTime(LocalDateTime.now())
                    .isDeleted(false)
                    .build();
            roleRepository.save(userRole);
            log.info("创建系统角色: USER");
        }

        log.info("系统角色初始化完成");
    }

    /**
     * 初始化基础权限
     */
    private void initBasePermissions() {
        log.info("初始化基础权限...");

        List<PermissionDefinition> permissions = new ArrayList<>();

        // Workflow 相关权限
        permissions.add(new PermissionDefinition("workflow:create", "创建工作流", "workflow"));
        permissions.add(new PermissionDefinition("workflow:read", "查看工作流", "workflow"));
        permissions.add(new PermissionDefinition("workflow:update", "更新工作流", "workflow"));
        permissions.add(new PermissionDefinition("workflow:delete", "删除工作流", "workflow"));
        permissions.add(new PermissionDefinition("workflow:execute", "执行工作流", "workflow"));

        // User 相关权限
        permissions.add(new PermissionDefinition("user:read", "查看用户信息", "user"));
        permissions.add(new PermissionDefinition("user:update", "更新用户信息", "user"));

        // 保存权限
        for (PermissionDefinition def : permissions) {
            if (!permissionRepository.findByPermissionCode(def.code).isPresent()) {
                Permission permission = Permission.builder()
                        .permissionCode(def.code)
                        .permissionName(def.name)
                        .resourceType(def.resourceType)
                        .description(def.name)
                        .createTime(LocalDateTime.now())
                        .isDeleted(false)
                        .build();
                permissionRepository.save(permission);
                log.info("创建权限: {}", def.code);
            }
        }

        log.info("基础权限初始化完成");
    }

    /**
     * 为角色分配权限
     */
    private void assignPermissionsToRoles() {
        log.info("为角色分配权限...");

        // 获取角色
        Role adminRole = roleRepository.findByRoleCode("ADMIN").orElse(null);
        Role userRole = roleRepository.findByRoleCode("USER").orElse(null);

        if (adminRole == null || userRole == null) {
            log.warn("角色不存在，跳过权限分配");
            return;
        }

        // ADMIN 拥有所有权限
        assignAllPermissionsToRole(adminRole);

        // USER 只拥有基础权限
        assignBasicPermissionsToRole(userRole);

        log.info("角色权限分配完成");
    }

    /**
     * 为 ADMIN 角色分配所有权限
     */
    private void assignAllPermissionsToRole(Role role) {
        List<String> allPermissions = List.of(
                "workflow:create", "workflow:read", "workflow:update", "workflow:delete", "workflow:execute",
                "user:read", "user:update"
        );

        for (String permCode : allPermissions) {
            Permission permission = permissionRepository.findByPermissionCode(permCode).orElse(null);
            if (permission != null) {
                assignPermissionToRole(role.getId(), permission.getId());
            }
        }
    }

    /**
     * 为 USER 角色分配基础权限
     */
    private void assignBasicPermissionsToRole(Role role) {
        List<String> basicPermissions = List.of(
                "workflow:read", "workflow:execute",
                "user:read", "user:update"
        );

        for (String permCode : basicPermissions) {
            Permission permission = permissionRepository.findByPermissionCode(permCode).orElse(null);
            if (permission != null) {
                assignPermissionToRole(role.getId(), permission.getId());
            }
        }
    }

    /**
     * 分配权限给角色（避免重复）
     */
    private void assignPermissionToRole(Long roleId, Long permissionId) {
        List<RolePermission> existing = rolePermissionRepository.findByRoleId(roleId);
        boolean alreadyAssigned = existing.stream()
                .anyMatch(rp -> rp.getPermissionId().equals(permissionId));

        if (!alreadyAssigned) {
            RolePermission rolePermission = RolePermission.builder()
                    .roleId(roleId)
                    .permissionId(permissionId)
                    .createTime(LocalDateTime.now())
                    .build();
            rolePermissionRepository.save(rolePermission);
        }
    }

    /**
     * 权限定义内部类
     */
    private static class PermissionDefinition {
        String code;
        String name;
        String resourceType;

        PermissionDefinition(String code, String name, String resourceType) {
            this.code = code;
            this.name = name;
            this.resourceType = resourceType;
        }
    }
}
