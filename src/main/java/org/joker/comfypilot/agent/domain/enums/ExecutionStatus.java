package org.joker.comfypilot.agent.domain.enums;

/**
 * 执行状态枚举
 */
public enum ExecutionStatus {

    /**
     * 执行成功
     */
    SUCCESS("成功"),

    /**
     * 执行失败
     */
    FAILED("失败"),

    /**
     * 执行中
     */
    RUNNING("执行中");

    private final String description;

    ExecutionStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
