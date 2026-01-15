package org.joker.comfypilot.session.domain.enums;

import lombok.Getter;

/**
 * 会话状态枚举
 */
@Getter
public enum SessionStatus {

    /**
     * 进行中
     */
    ACTIVE("ACTIVE", "进行中"),

    /**
     * 已完成
     */
    COMPLETED("COMPLETED", "已完成"),

    /**
     * 已归档
     */
    ARCHIVED("ARCHIVED", "已归档");

    private final String code;
    private final String description;

    SessionStatus(String code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * 根据code获取枚举
     */
    public static SessionStatus fromCode(String code) {
        for (SessionStatus status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Invalid SessionStatus code: " + code);
    }
}
