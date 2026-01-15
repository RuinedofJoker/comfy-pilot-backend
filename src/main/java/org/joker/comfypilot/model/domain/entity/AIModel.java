package org.joker.comfypilot.model.domain.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.joker.comfypilot.common.domain.BaseEntity;
import org.joker.comfypilot.model.domain.enums.ModelStatus;
import org.joker.comfypilot.model.domain.enums.ModelType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * AI模型领域实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class AIModel extends BaseEntity<Long> {

    private Long id;
    private Long providerId;
    private String name;
    private String modelCode;
    private ModelType modelType;
    private String description;
    private Integer maxTokens;
    private Boolean supportStream;
    private Boolean supportTools;
    private BigDecimal inputPrice;
    private BigDecimal outputPrice;
    private ModelStatus status;
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
     * 激活模型
     */
    public void activate() {
        // TODO: 实现激活逻辑
    }

    /**
     * 停用模型
     */
    public void deactivate() {
        // TODO: 实现停用逻辑
    }

    /**
     * 标记为已弃用
     */
    public void deprecate() {
        // TODO: 实现弃用逻辑
    }

    /**
     * 计算调用费用
     */
    public BigDecimal calculateCost(Integer inputTokens, Integer outputTokens) {
        // TODO: 实现费用计算逻辑
        return BigDecimal.ZERO;
    }

    /**
     * 判断是否支持流式输出
     */
    public boolean isStreamSupported() {
        return Boolean.TRUE.equals(this.supportStream);
    }

    /**
     * 判断是否支持工具调用
     */
    public boolean isToolsSupported() {
        return Boolean.TRUE.equals(this.supportTools);
    }
}
