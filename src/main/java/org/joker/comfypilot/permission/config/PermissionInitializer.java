package org.joker.comfypilot.permission.config;

import lombok.extern.slf4j.Slf4j;
import org.joker.comfypilot.permission.domain.entity.Permission;
import org.joker.comfypilot.permission.domain.entity.Role;
import org.joker.comfypilot.permission.domain.entity.RolePermission;
import org.joker.comfypilot.permission.domain.entity.UserRole;
import org.joker.comfypilot.permission.domain.repository.PermissionRepository;
import org.joker.comfypilot.permission.domain.repository.RolePermissionRepository;
import org.joker.comfypilot.permission.domain.repository.RoleRepository;
import org.joker.comfypilot.permission.domain.repository.UserRoleRepository;
import org.joker.comfypilot.user.config.DefaultAdminProperties;
import org.joker.comfypilot.user.domain.entity.User;
import org.joker.comfypilot.user.domain.enums.UserStatus;
import org.joker.comfypilot.user.domain.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

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
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserRoleRepository userRoleRepository;
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;
    @Autowired
    private DefaultAdminProperties defaultAdminProperties;

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

            // 4. 初始化默认管理员账户
            initDefaultAdminAccount();

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
                    .id(1L)
                    .roleCode("ADMIN")
                    .roleName("管理员")
                    .description("系统管理员，拥有所有权限")
                    .isSystem(true)
                    .createTime(LocalDateTime.now())
                    .build();
            roleRepository.save(adminRole);
            log.info("创建系统角色: ADMIN");
        }

        // 创建 USER 角色
        if (!roleRepository.existsByRoleCode("USER")) {
            Role userRole = Role.builder()
                    .id(2L)
                    .roleCode("USER")
                    .roleName("普通用户")
                    .description("普通用户，拥有基础权限")
                    .isSystem(true)
                    .createTime(LocalDateTime.now())
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
    }

    /**
     * 为 USER 角色分配基础权限
     */
    private void assignBasicPermissionsToRole(Role role) {
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
     * 初始化默认管理员账户
     */
    private void initDefaultAdminAccount() {
        log.info("开始初始化默认管理员账户...");

        if (defaultAdminProperties.getEnabled()) {
            // 配置启用，创建或更新默认管理员账户
            createOrUpdateDefaultAdmin();
        } else {
            // 配置未启用，删除默认管理员账户
            deleteDefaultAdmin();
        }

        log.info("默认管理员账户初始化完成");
    }

    /**
     * 创建或更新默认管理员账户
     */
    private void createOrUpdateDefaultAdmin() {
        String username = defaultAdminProperties.getUsername();
        String password = defaultAdminProperties.getPassword();

        log.info("配置已启用，创建/更新默认管理员账户: {}", username);

        // 检查 ID=1 的用户是否存在
        User existingUser = userRepository.findById(1L).orElse(null);

        if (existingUser == null) {
            // 用户不存在，创建新用户
            createDefaultAdmin(username, password);
        } else {
            // 用户已存在，更新用户信息
            updateDefaultAdmin(existingUser, username, password);
        }

        // 确保用户角色关联存在
        ensureAdminRole();
    }

    /**
     * 创建默认管理员用户
     */
    private void createDefaultAdmin(String username, String password) {
        String passwordHash = passwordEncoder.encode(password);

        User adminUser = User.builder()
                .id(1L)
                .userCode(username)
                .username(username)
                .email(username)
                .passwordHash(passwordHash)
                .status(UserStatus.ACTIVE)
                .createTime(LocalDateTime.now())
                .build();

        userRepository.save(adminUser);
        log.info("创建默认管理员账户成功: id=1, username={}", username);
    }

    /**
     * 更新默认管理员用户
     */
    private void updateDefaultAdmin(User existingUser, String username, String password) {
        String passwordHash = passwordEncoder.encode(password);

        existingUser.setUserCode(username);
        existingUser.setUsername(username);
        existingUser.setEmail(username);
        existingUser.setPasswordHash(passwordHash);
        existingUser.setStatus(UserStatus.ACTIVE);
        existingUser.setUpdateTime(LocalDateTime.now());

        userRepository.save(existingUser);
        log.info("更新默认管理员账户成功: id=1, username={}", username);
    }

    /**
     * 确保管理员角色关联存在
     */
    private void ensureAdminRole() {
        List<UserRole> existingRoles = userRoleRepository.findByUserId(1L);
        boolean hasAdminRole = existingRoles.stream()
                .anyMatch(ur -> ur.getRoleId().equals(1L));

        if (!hasAdminRole) {
            UserRole userRole = UserRole.builder()
                    .id(1L)
                    .userId(1L)
                    .roleId(1L)
                    .createTime(LocalDateTime.now())
                    .build();
            userRoleRepository.save(userRole);
            log.info("创建默认管理员角色关联: user_id=1, role_id=1");
        }
    }

    /**
     * 删除默认管理员账户
     */
    private void deleteDefaultAdmin() {
        log.info("配置未启用，删除默认管理员账户");

        // 检查 ID=1 的用户是否存在
        User existingUser = userRepository.findById(1L).orElse(null);

        if (existingUser != null) {
            // 删除用户角色关联
            userRoleRepository.deleteByUserIdAndRoleId(1L, 1L);
            log.info("删除默认管理员角色关联: user_id=1, role_id=1");

            // 删除用户
            userRepository.deleteById(1L);
            log.info("删除默认管理员账户: id=1");
        } else {
            log.info("默认管理员账户不存在，无需删除");
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
