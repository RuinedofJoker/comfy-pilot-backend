package org.joker.comfypilot.agent.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.joker.comfypilot.agent.domain.enums.AgentStatus;
import org.joker.comfypilot.agent.domain.service.AgentConfigDefinition;
import org.joker.comfypilot.common.domain.BaseEntity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Agent配置领域实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgentConfig extends BaseEntity<Long> {

    private static final long serialVersionUID = 1L;

    /**
     * Agent ID
     */
    private Long id;

    /**
     * Agent编码（唯一标识）
     */
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
     * Agent Scope配置（JSON格式，对应langchain4j的AgentScope）
     */
    private Map<String, Object> agentScopeConfig;

    /**
     * agent运行时配置（JSON格式）
     */
    private Map<String, Object> config;

    /**
     * agent运行时配置定义（JSON格式）
     */
    private List<AgentConfigDefinition> agentConfigDefinitions;

    /**
     * Agent状态
     */
    private AgentStatus status;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 领域行为：启用Agent
     */
    public void enable() {
        this.status = AgentStatus.ENABLED;
        this.updateTime = LocalDateTime.now();
    }

    /**
     * 领域行为：禁用Agent
     */
    public void disable() {
        this.status = AgentStatus.DISABLED;
        this.updateTime = LocalDateTime.now();
    }

    /**
     * 领域行为：检查Agent是否启用
     */
    public boolean isEnabled() {
        return AgentStatus.ENABLED.equals(this.status);
    }
}
