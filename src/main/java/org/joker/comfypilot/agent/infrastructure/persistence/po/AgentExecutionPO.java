package org.joker.comfypilot.agent.infrastructure.persistence.po;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.joker.comfypilot.common.infrastructure.persistence.po.BasePO;

import java.time.LocalDateTime;

/**
 * Agent执行记录持久化对象
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("agent_execution")
public class AgentExecutionPO extends BasePO {

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
