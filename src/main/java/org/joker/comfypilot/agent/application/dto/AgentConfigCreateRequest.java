package org.joker.comfypilot.agent.application.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * Agent配置创建请求
 */
@Data
public class AgentConfigCreateRequest {

    private String name;
    private String type;
    private String description;
    private Long modelId;
    private String systemPrompt;
    private BigDecimal temperature;
    private Integer maxTokens;
    private String toolsConfig;
}
