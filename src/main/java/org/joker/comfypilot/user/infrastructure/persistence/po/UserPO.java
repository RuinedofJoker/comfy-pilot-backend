package org.joker.comfypilot.user.infrastructure.persistence.po;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.joker.comfypilot.common.infrastructure.persistence.po.BasePO;

/**
 * 用户持久化对象
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("user")
public class UserPO extends BasePO {

    /**
     * 用户名
     */
    private String username;

    /**
     * 密码(加密)
     */
    private String password;

    /**
     * 显示名称
     */
    private String displayName;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 头像URL
     */
    private String avatarUrl;

    /**
     * 角色
     */
    private String role;

    /**
     * 状态
     */
    private String status;
}
