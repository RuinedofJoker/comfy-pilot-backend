package org.joker.comfypilot.model.application.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * AI模型创建请求
 */
@Data
public class AIModelCreateRequest {

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
}
