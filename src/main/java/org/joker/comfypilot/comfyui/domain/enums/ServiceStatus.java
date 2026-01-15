package org.joker.comfypilot.comfyui.domain.enums;

import lombok.Getter;

/**
 * ComfyUI服务状态枚举
 */
@Getter
public enum ServiceStatus {

    /**
     * 在线
     */
    ONLINE("ONLINE", "在线"),

    /**
     * 离线
     */
    OFFLINE("OFFLINE", "离线"),

    /**
     * 错误
     */
    ERROR("ERROR", "错误");

    private final String code;
    private final String description;

    ServiceStatus(String code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * 根据code获取枚举
     */
    public static ServiceStatus fromCode(String code) {
        for (ServiceStatus status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Invalid ServiceStatus code: " + code);
    }
}
