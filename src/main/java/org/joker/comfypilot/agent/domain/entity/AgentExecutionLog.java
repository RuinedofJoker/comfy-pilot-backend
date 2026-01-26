package org.joker.comfypilot.agent.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.joker.comfypilot.agent.domain.enums.ExecutionStatus;
import org.joker.comfypilot.common.domain.BaseEntity;

import java.time.LocalDateTime;

/**
 * Agent执行日志领域实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgentExecutionLog extends BaseEntity<Long> {

    private static final long serialVersionUID = 1L;

    /**
     * 日志ID
     */
    private Long id;

    /**
     * Agent ID
     */
    private Long agentId;

    /**
     * 会话ID
     */
    private Long sessionId;

    /**
     * 输入内容
     */
    private String input;

    /**
     * 输出内容
     */
    private String output;

    /**
     * 执行状态
     */
    private ExecutionStatus status;

    /**
     * 错误信息
     */
    private String errorMessage;

    /**
     * 执行耗时（毫秒）
     */
    private Long executionTimeMs;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 领域行为：标记执行成功
     */
    public void markSuccess(String output, Long executionTimeMs) {
        this.status = ExecutionStatus.SUCCESS;
        this.output = output;
        this.executionTimeMs = executionTimeMs;
        this.updateTime = LocalDateTime.now();
    }

    /**
     * 领域行为：标记执行中断
     */
    public void markInterrupted(Long executionTimeMs) {
        this.status = ExecutionStatus.INTERRUPTED;
        this.executionTimeMs = executionTimeMs;
        this.updateTime = LocalDateTime.now();
    }

    /**
     * 领域行为：标记执行失败
     */
    public void markFailed(String errorMessage, Long executionTimeMs) {
        this.status = ExecutionStatus.FAILED;
        this.errorMessage = errorMessage;
        this.executionTimeMs = executionTimeMs;
        this.updateTime = LocalDateTime.now();
    }
}
