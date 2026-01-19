package org.joker.comfypilot.user.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.joker.comfypilot.common.domain.BaseEntity;
import org.joker.comfypilot.user.domain.enums.UserStatus;

import java.time.LocalDateTime;

/**
 * 用户领域实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User extends BaseEntity<Long> {

    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     */
    private Long id;

    /**
     * 用户编码（唯一，在创建用户时随机生成）
     */
    private String userCode;

    /**
     * 邮箱地址（唯一）
     */
    private String email;

    /**
     * 用户名
     */
    private String username;

    /**
     * 密码哈希值（BCrypt）
     */
    private String passwordHash;

    /**
     * 头像URL
     */
    private String avatarUrl;

    /**
     * 用户状态
     */
    private UserStatus status;

    /**
     * 最后登录时间
     */
    private LocalDateTime lastLoginTime;

    /**
     * 最后登录IP
     */
    private String lastLoginIp;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 创建人ID
     */
    private Long createBy;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 更新人ID
     */
    private Long updateBy;

    /**
     * 更新用户名
     *
     * @param username 新用户名
     */
    public void updateUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("用户名不能为空");
        }
        if (username.length() > 50) {
            throw new IllegalArgumentException("用户名长度不能超过50个字符");
        }
        this.username = username.trim();
    }

    /**
     * 更新头像URL
     *
     * @param avatarUrl 新头像URL
     */
    public void updateAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    /**
     * 更新密码
     *
     * @param passwordHash 新密码哈希值
     */
    public void updatePassword(String passwordHash) {
        if (passwordHash == null || passwordHash.trim().isEmpty()) {
            throw new IllegalArgumentException("密码哈希值不能为空");
        }
        this.passwordHash = passwordHash;
    }

    /**
     * 更新最后登录信息
     *
     * @param loginTime 登录时间
     * @param loginIp   登录IP
     */
    public void updateLastLogin(LocalDateTime loginTime, String loginIp) {
        this.lastLoginTime = loginTime;
        this.lastLoginIp = loginIp;
    }

    /**
     * 检查用户是否可以登录
     *
     * @return true-可以登录，false-不可以登录
     */
    public boolean canLogin() {
        return UserStatus.ACTIVE.equals(this.status);
    }

    /**
     * 锁定用户
     */
    public void lock() {
        this.status = UserStatus.LOCKED;
    }

    /**
     * 激活用户
     */
    public void activate() {
        this.status = UserStatus.ACTIVE;
    }
}
