package org.joker.comfypilot.resource.domain.enums;

import lombok.Getter;

/**
 * 文件来源类型枚举
 */
@Getter
public enum FileSourceType {

    /**
     * 服务器本地文件
     */
    SERVER_LOCAL("服务器本地文件");

    private final String description;

    FileSourceType(String description) {
        this.description = description;
    }
}
