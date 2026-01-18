package org.joker.comfypilot.cfsvr.domain.enums;

import lombok.Getter;

/**
 * ComfyUI 服务认证模式
 */
@Getter
public enum AuthMode {

    /**
     * 无认证
     */
    NULL("null", "无认证"),

    /**
     * Basic Auth 认证（通过 Nginx 反向代理实现）
     */
    BASIC_AUTH("basic_auth", "Basic Auth 认证");

    /**
     * 认证模式代码
     */
    private final String code;

    /**
     * 认证模式名称
     */
    private final String name;

    AuthMode(String code, String name) {
        this.code = code;
        this.name = name;
    }

    /**
     * 根据代码获取枚举
     *
     * @param code 认证模式代码
     * @return 认证模式枚举
     */
    public static AuthMode fromCode(String code) {
        if (code == null || code.trim().isEmpty()) {
            return NULL;
        }
        for (AuthMode mode : values()) {
            if (mode.code.equals(code)) {
                return mode;
            }
        }
        return NULL;
    }
}
