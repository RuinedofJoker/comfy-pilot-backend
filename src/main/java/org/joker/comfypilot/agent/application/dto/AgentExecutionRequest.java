package org.joker.comfypilot.agent.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Agent执行请求DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Agent执行请求")
public class AgentExecutionRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "会话ID")
    private Long sessionId;

    @Schema(description = "用户输入内容")
    private String input;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "是否流式执行")
    private Boolean isStreamable;
}
