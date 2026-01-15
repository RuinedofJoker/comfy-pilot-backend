package org.joker.comfypilot.user.domain.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.joker.comfypilot.common.domain.BaseEntity;
import org.joker.comfypilot.user.domain.enums.UserRole;
import org.joker.comfypilot.user.domain.enums.UserStatus;

import java.time.LocalDateTime;

/**
 * 用户领域实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class User extends BaseEntity<Long> {

    private Long id;
    private String username;
    private String password;
    private String displayName;
    private String email;
    private String avatarUrl;
    private UserRole role;
    private UserStatus status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public LocalDateTime getCreateTime() {
        return createTime;
    }

    @Override
    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    /**
     * 验证密码
     */
    public boolean validatePassword(String rawPassword) {
        // TODO: 实现密码验证逻辑
        return false;
    }

    /**
     * 修改密码
     */
    public void changePassword(String oldPassword, String newPassword) {
        // TODO: 实现密码修改逻辑
    }

    /**
     * 更新个人信息
     */
    public void updateProfile(String displayName, String email, String avatarUrl) {
        // TODO: 实现个人信息更新逻辑
    }

    /**
     * 禁用用户
     */
    public void disable() {
        // TODO: 实现用户禁用逻辑
    }

    /**
     * 激活用户
     */
    public void activate() {
        // TODO: 实现用户激活逻辑
    }

    /**
     * 判断是否为管理员
     */
    public boolean isAdmin() {
        return UserRole.ADMIN.equals(this.role);
    }

    /**
     * 判断是否已激活
     */
    public boolean isActive() {
        return UserStatus.ACTIVE.equals(this.status);
    }
}
