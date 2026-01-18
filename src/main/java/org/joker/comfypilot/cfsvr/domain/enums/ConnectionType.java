package org.joker.comfypilot.cfsvr.domain.enums;

import lombok.Getter;

/**
 * 服务器连接方式枚举
 */
@Getter
public enum ConnectionType {

    /**
     * 本地连接方式
     * ComfyUI 与平台部署在同一机器上，通过本地文件系统直接访问
     */
    LOCAL("LOCAL", "本地直接访问"),

    /**
     * SSH 远程连接方式
     * 通过 SSH 协议连接到远程服务器
     */
    SSH("SSH", "SSH远程连接"),

    ;

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
