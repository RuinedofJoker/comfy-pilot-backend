package org.joker.comfypilot.user.infrastructure.persistence.po;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.joker.comfypilot.common.infrastructure.persistence.po.BasePO;
import org.joker.comfypilot.user.domain.enums.UserStatus;

import java.time.LocalDateTime;

/**
 * 用户持久化对象
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName("\"user\"")
public class UserPO extends BasePO {

    private static final long serialVersionUID = 1L;

    /**
     * 用户编码（唯一）
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
}
