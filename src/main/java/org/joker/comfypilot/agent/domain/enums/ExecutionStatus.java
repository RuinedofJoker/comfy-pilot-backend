package org.joker.comfypilot.agent.domain.enums;

/**
 * Agent执行状态枚举
 */
public enum ExecutionStatus {
    /**
     * 等待中
     */
    PENDING,

    /**
     * 运行中
     */
    RUNNING,

    /**
     * 成功
     */
    SUCCESS,

    /**
     * 失败
     */
    FAILED,

    /**
     * 超时
     */
    TIMEOUT
}
