package org.joker.comfypilot.agent.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 用户Agent配置DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "用户Agent配置DTO")
public class UserAgentConfigDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "AgentCode")
    private String agentCode;

    @Schema(description = "Agent的运行时配置（json格式）")
    private String agentConfig;
}
