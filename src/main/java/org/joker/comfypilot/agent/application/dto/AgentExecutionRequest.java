package org.joker.comfypilot.agent.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

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
    private String userMessage;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "是否流式执行")
    private Boolean isStreamable;

    @Schema(description = "agent配置（json格式，格式内容是根据agentConfigDefinitions填写的）")
    private Map<String, Object> agentConfig;

    @Schema(description = "工作流内容（一般是json格式，comfyui规定的格式）")
    private String workflowContent;

    @Schema(description = "多模态列表")
    private List<String> multimodalList;
}
