package org.joker.comfypilot.agent.domain.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.joker.comfypilot.agent.domain.enums.ExecutionStatus;
import org.joker.comfypilot.agent.domain.enums.ExecutionType;
import org.joker.comfypilot.common.domain.BaseEntity;

import java.time.LocalDateTime;

/**
 * Agent执行记录领域实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class AgentExecution extends BaseEntity<Long> {

    private Long id;
    private Long agentId;
    private Long sessionId;
    private Long messageId;
    private ExecutionType executionType;
    private String inputData;
    private String outputData;
    private String toolsCalled;
    private ExecutionStatus status;
    private String errorMessage;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer durationMs;
    private String tokenUsage;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public LocalDateTime getCreateTime() {
        return createTime;
    }

    @Override
    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    /**
     * 开始执行
     */
    public void start() {
        // TODO: 实现开始执行逻辑
    }

    /**
     * 执行成功
     */
    public void success(String outputData, String toolsCalled, String tokenUsage) {
        // TODO: 实现执行成功逻辑
    }

    /**
     * 执行失败
     */
    public void fail(String errorMessage) {
        // TODO: 实现执行失败逻辑
    }

    /**
     * 执行超时
     */
    public void timeout() {
        // TODO: 实现执行超时逻辑
    }

    /**
     * 计算执行时长
     */
    public void calculateDuration() {
        // TODO: 实现时长计算逻辑
    }
}
