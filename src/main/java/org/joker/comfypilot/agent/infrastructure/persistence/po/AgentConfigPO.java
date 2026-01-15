package org.joker.comfypilot.agent.infrastructure.persistence.po;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.joker.comfypilot.common.infrastructure.persistence.po.BasePO;

import java.math.BigDecimal;

/**
 * Agent配置持久化对象
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("agent_config")
public class AgentConfigPO extends BasePO {

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
