package org.joker.comfypilot.agent.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Agent执行响应DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Agent执行响应")
public class AgentExecutionResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "执行日志ID")
    private Long logId;

    @Schema(description = "Agent输出内容")
    private String output;

    @Schema(description = "执行状态")
    private String status;

    @Schema(description = "错误信息")
    private String errorMessage;

    @Schema(description = "执行耗时（毫秒）")
    private Long executionTimeMs;

    @Schema(description = "执行开始时间戳（毫秒）")
    private Long executionStartMs;
}
