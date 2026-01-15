package org.joker.comfypilot.model.domain.enums;

/**
 * 模型调用状态枚举
 */
public enum InvocationStatus {
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
    TIMEOUT,

    /**
     * 速率限制
     */
    RATE_LIMITED
}
