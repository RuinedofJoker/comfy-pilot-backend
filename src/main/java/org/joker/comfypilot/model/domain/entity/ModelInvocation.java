package org.joker.comfypilot.model.domain.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.joker.comfypilot.common.domain.BaseEntity;
import org.joker.comfypilot.model.domain.enums.InvocationStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 模型调用记录领域实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ModelInvocation extends BaseEntity<Long> {

    private Long id;
    private Long modelId;
    private Long agentExecutionId;
    private String requestData;
    private String responseData;
    private InvocationStatus status;
    private String errorMessage;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer durationMs;
    private Integer inputTokens;
    private Integer outputTokens;
    private BigDecimal totalCost;
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
     * 开始调用
     */
    public void start() {
        // TODO: 实现开始调用逻辑
    }

    /**
     * 调用成功
     */
    public void success(String responseData, Integer inputTokens, Integer outputTokens, BigDecimal totalCost) {
        // TODO: 实现调用成功逻辑
    }

    /**
     * 调用失败
     */
    public void fail(String errorMessage) {
        // TODO: 实现调用失败逻辑
    }

    /**
     * 调用超时
     */
    public void timeout() {
        // TODO: 实现调用超时逻辑
    }

    /**
     * 速率限制
     */
    public void rateLimited() {
        // TODO: 实现速率限制逻辑
    }

    /**
     * 计算执行时长
     */
    public void calculateDuration() {
        // TODO: 实现时长计算逻辑
    }
}
