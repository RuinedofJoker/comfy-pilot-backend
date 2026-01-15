package org.joker.comfypilot.model.application.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * AI模型更新请求
 */
@Data
public class AIModelUpdateRequest {

    private String name;
    private String description;
    private Integer maxTokens;
    private Boolean supportStream;
    private Boolean supportTools;
    private BigDecimal inputPrice;
    private BigDecimal outputPrice;
}
