package org.joker.comfypilot.session.domain.enums;

import lombok.Getter;

/**
 * 消息状态枚举
 */
@Getter
public enum MessageStatus {

    /**
     * 活跃状态 - 会话正在进行中
     */
    ACTIVE("活跃"),

    /**
     * 已归档 - 会话已归档
     */
    ARCHIVED("已归档");

    private final String description;

    MessageStatus(String description) {
        this.description = description;
    }

}
