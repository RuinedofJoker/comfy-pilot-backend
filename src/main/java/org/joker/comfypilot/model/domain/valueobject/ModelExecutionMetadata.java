package org.joker.comfypilot.model.domain.valueobject;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 模型执行元数据值对象
 * 记录模型执行的元信息
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModelExecutionMetadata implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 使用的模型标识符
     */
    private String modelIdentifier;

    /**
     * 使用的模型名称
     */
    private String modelName;

    /**
     * 执行开始时间
     */
    private LocalDateTime startTime;

    /**
     * 执行结束时间
     */
    private LocalDateTime endTime;

    /**
     * 执行耗时（毫秒）
     */
    private Long durationMs;

    /**
     * 输入token数量
     */
    private Integer inputTokens;

    /**
     * 输出token数量
     */
    private Integer outputTokens;

    /**
     * 总token数量
     */
    private Integer totalTokens;

    /**
     * 本次调用成本（美元）
     */
    private Double cost;

    /**
     * 是否成功
     */
    private Boolean success;

    /**
     * 错误信息（如果失败）
     */
    private String errorMessage;
}
