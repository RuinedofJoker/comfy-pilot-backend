package org.joker.comfypilot.agent.domain.enums;

/**
 * Agent配置项类型
 */
public enum AgentConfigType {

    STRING("字符串"),
    NUMBER("数字"),
    BOOLEAN("布尔"),
    MODEL("模型"),
    ;

    private final String description;

    AgentConfigType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

}
