package org.joker.comfypilot.model.application.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.joker.comfypilot.common.application.dto.BaseDTO;

import java.math.BigDecimal;

/**
 * AI模型DTO
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class AIModelDTO extends BaseDTO {

    private Long id;
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
