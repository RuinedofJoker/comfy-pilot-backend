package org.joker.comfypilot.user.domain.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * 用户状态枚举
 */
@Getter
public enum UserStatus {

    /**
     * 活跃状态，可正常使用
     */
    ACTIVE("ACTIVE", "活跃"),

    /**
     * 未激活状态，注册后未激活邮箱
     */
    INACTIVE("INACTIVE", "未激活"),

    /**
     * 锁定状态，因违规或安全原因被锁定
     */
    LOCKED("LOCKED", "锁定"),

    /**
     * 已删除状态，用户主动注销账户
     */
    DELETED("DELETED", "已删除");

    /**
     * 状态码（存储到数据库的值）
     */
    @EnumValue
    @JsonValue
    private final String code;

    /**
     * 状态描述
     */
    private final String description;

    UserStatus(String code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * 根据状态码获取枚举
     *
     * @param code 状态码
     * @return 用户状态枚举
     */
    public static UserStatus fromCode(String code) {
        for (UserStatus status : UserStatus.values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Invalid user status code: " + code);
    }
}
