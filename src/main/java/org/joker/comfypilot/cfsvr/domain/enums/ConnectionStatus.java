package org.joker.comfypilot.cfsvr.domain.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * 连接状态枚举
 */
@Getter
public enum ConnectionStatus {

    /**
     * 已连接
     */
    CONNECTED("CONNECTED", "已连接"),

    /**
     * 已断开
     */
    DISCONNECTED("DISCONNECTED", "已断开"),

    /**
     * 未知状态
     */
    UNKNOWN("UNKNOWN", "未知");

    /**
     * 状态码(存储到数据库的值)
     */
    @EnumValue
    @JsonValue
    private final String code;

    /**
     * 状态描述
     */
    private final String description;

    ConnectionStatus(String code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * 根据状态码获取枚举
     *
     * @param code 状态码
     * @return 连接状态枚举
     */
    public static ConnectionStatus fromCode(String code) {
        for (ConnectionStatus status : ConnectionStatus.values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Invalid connection status code: " + code);
    }
}
