package org.joker.comfypilot.agent.application.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.joker.comfypilot.common.application.dto.BaseDTO;

import java.math.BigDecimal;

/**
 * Agent配置DTO
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class AgentConfigDTO extends BaseDTO {

    private Long id;
    private String name;
    private String type;
    private String description;
    private Long modelId;
    private String systemPrompt;
    private BigDecimal temperature;
    private Integer maxTokens;
    private String toolsConfig;
    private String status;
}
