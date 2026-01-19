package org.joker.comfypilot.agent.infrastructure.persistence.po;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.joker.comfypilot.common.infrastructure.persistence.annotation.UniqueKey;
import org.joker.comfypilot.common.infrastructure.persistence.po.BasePO;

import java.util.Map;

/**
 * Agent配置持久化对象
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName(value = "agent_config", autoResultMap = true)
public class AgentConfigPO extends BasePO {

    private static final long serialVersionUID = 1L;

    /**
     * Agent编码（唯一标识）
     */
    @UniqueKey
    private String agentCode;

    /**
     * Agent名称
     */
    private String agentName;

    /**
     * Agent描述
     */
    private String description;

    /**
     * Agent版本号（格式：x.y.z）
     */
    private String version;

    /**
     * Agent Scope配置（JSONB类型，对应langchain4j的agentic-scope）
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> agentScopeConfig;

    /**
     * 配置参数（JSONB类型）
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> config;

    /**
     * Agent状态（ENABLED, DISABLED）
     */
    private String status;
}
