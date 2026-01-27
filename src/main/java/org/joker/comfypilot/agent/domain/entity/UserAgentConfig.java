package org.joker.comfypilot.agent.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.joker.comfypilot.common.domain.BaseEntity;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 用户Agent配置领域实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserAgentConfig extends BaseEntity<Long> {

    /**
     * Agent ID
     */
    private Long id;

    /**
     * 所属用户ID
     */
    private Long userId;

    /**
     * AgentCode
     */
    private String agentCode;

    /**
     * Agent的运行时配置（json格式）
     */
    private Map<String, Object> agentConfig;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

}
