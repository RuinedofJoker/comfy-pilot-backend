package org.joker.comfypilot.agent.domain.enums;

import lombok.Getter;

/**
 * Agent配置项类型
 */
@Getter
public enum AgentConfigType {

    STRING("字符串"),
    TEXT("文本"),
    INT("整数"),
    FLOAT("浮点数"),
    BOOLEAN("布尔"),
    MODEL("模型"),
    ;

    private final String description;

    AgentConfigType(String description) {
        this.description = description;
    }

}
