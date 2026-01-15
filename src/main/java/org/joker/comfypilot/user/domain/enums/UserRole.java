package org.joker.comfypilot.user.domain.enums;

import lombok.Getter;

/**
 * 用户角色枚举
 */
@Getter
public enum UserRole {

    /**
     * 管理员
     */
    ADMIN("ADMIN", "管理员"),

    /**
     * 普通用户
     */
    USER("USER", "普通用户");

    private final String code;
    private final String description;

    UserRole(String code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * 根据code获取枚举
     */
    public static UserRole fromCode(String code) {
        for (UserRole role : values()) {
            if (role.getCode().equals(code)) {
                return role;
            }
        }
        throw new IllegalArgumentException("Invalid UserRole code: " + code);
    }
}
