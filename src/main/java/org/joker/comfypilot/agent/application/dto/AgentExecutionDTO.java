package org.joker.comfypilot.agent.application.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.joker.comfypilot.common.application.dto.BaseDTO;

import java.time.LocalDateTime;

/**
 * Agent执行记录DTO
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class AgentExecutionDTO extends BaseDTO {

    private Long id;
    private Long agentId;
    private Long sessionId;
    private Long messageId;
    private String executionType;
    private String inputData;
    private String outputData;
    private String toolsCalled;
    private String status;
    private String errorMessage;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer durationMs;
    private String tokenUsage;
}
