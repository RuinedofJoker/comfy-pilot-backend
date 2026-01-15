package org.joker.comfypilot.model.infrastructure.persistence.po;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.joker.comfypilot.common.infrastructure.persistence.po.BasePO;

import java.math.BigDecimal;

/**
 * AI模型持久化对象
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("ai_model")
public class AIModelPO extends BasePO {

    private Long providerId;
    private String name;
    private String modelCode;
    private String modelType;
    private String description;
    private Integer maxTokens;
    private Boolean supportStream;
    private Boolean supportTools;
    private BigDecimal inputPrice;
    private BigDecimal outputPrice;
    private String status;
}
