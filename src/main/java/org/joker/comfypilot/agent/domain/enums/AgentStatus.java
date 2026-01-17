package org.joker.comfypilot.agent.domain.enums;

/**
 * Agent状态枚举
 */
public enum AgentStatus {

    /**
     * 启用状态
     */
    ENABLED("启用"),

    /**
     * 禁用状态
     */
    DISABLED("禁用");

    private final String description;

    AgentStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
