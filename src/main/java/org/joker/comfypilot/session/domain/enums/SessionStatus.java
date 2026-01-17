package org.joker.comfypilot.session.domain.enums;

/**
 * 会话状态枚举
 */
public enum SessionStatus {

    /**
     * 活跃状态 - 会话正在进行中
     */
    ACTIVE("活跃"),

    /**
     * 已归档 - 会话已归档
     */
    ARCHIVED("已归档");

    private final String description;

    SessionStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
