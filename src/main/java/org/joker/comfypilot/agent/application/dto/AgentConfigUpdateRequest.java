package org.joker.comfypilot.agent.application.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * Agent配置更新请求
 */
@Data
public class AgentConfigUpdateRequest {

    private String name;
    private String description;
    private String systemPrompt;
    private BigDecimal temperature;
    private Integer maxTokens;
    private String toolsConfig;
}
