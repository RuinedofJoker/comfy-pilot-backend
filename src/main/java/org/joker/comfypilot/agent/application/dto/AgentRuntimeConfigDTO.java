package org.joker.comfypilot.agent.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.joker.comfypilot.agent.domain.service.AgentConfigDefinition;
import org.joker.comfypilot.common.application.dto.BaseDTO;

import java.util.List;

/**
 * Agent运行时配置信息DTO
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Schema(description = "Agent运行时配置信息")
public class AgentRuntimeConfigDTO extends BaseDTO {

    private static final long serialVersionUID = 1L;

    @Schema(description = "Agent编码")
    private String agentCode;

    @Schema(description = "Agent名称")
    private String agentName;

    @Schema(description = "Agent描述")
    private String description;

    @Schema(description = "Agent版本号")
    private String version;

    @Schema(description = "Agent运行时配置定义")
    private List<AgentConfigDefinition> agentConfigDefinitions;
}
