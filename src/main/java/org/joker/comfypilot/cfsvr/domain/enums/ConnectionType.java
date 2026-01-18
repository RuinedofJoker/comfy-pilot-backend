package org.joker.comfypilot.cfsvr.domain.enums;

import lombok.Getter;

/**
 * 服务器连接方式枚举
 */
@Getter
public enum ConnectionType {

    /**
     * SSH连接方式
     */
    SSH("SSH", "SSH远程连接"),

    /**
     * 预留：Docker连接方式
     */
    DOCKER("DOCKER", "Docker容器连接"),

    /**
     * 预留：Kubernetes连接方式
     */
    KUBERNETES("KUBERNETES", "Kubernetes集群连接");

    /**
     * 连接方式代码
     */
    private final String code;

    /**
     * 连接方式描述
     */
    private final String description;

    ConnectionType(String code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * 根据代码获取枚举
     *
     * @param code 连接方式代码
     * @return 连接方式枚举
     */
    public static ConnectionType fromCode(String code) {
        if (code == null) {
            return null;
        }
        for (ConnectionType type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("未知的连接方式: " + code);
    }
}
