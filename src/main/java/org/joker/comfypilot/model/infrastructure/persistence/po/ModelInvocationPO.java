package org.joker.comfypilot.model.infrastructure.persistence.po;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.joker.comfypilot.common.infrastructure.persistence.po.BasePO;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 模型调用记录持久化对象
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("model_invocation")
public class ModelInvocationPO extends BasePO {

    private Long modelId;
    private Long agentExecutionId;
    private String requestData;
    private String responseData;
    private String status;
    private String errorMessage;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer durationMs;
    private Integer inputTokens;
    private Integer outputTokens;
    private BigDecimal totalCost;
}
