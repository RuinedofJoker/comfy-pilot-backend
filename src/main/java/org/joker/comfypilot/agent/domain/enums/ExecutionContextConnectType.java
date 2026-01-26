package org.joker.comfypilot.agent.domain.enums;

import lombok.Getter;

/**
 * Agent执行上下文连接类型
 */
@Getter
public enum ExecutionContextConnectType {

    WEBSOCKET("WebSocket")
    ;

    private final String description;

    ExecutionContextConnectType(String description) {
        this.description = description;
    }

}
