package org.joker.comfypilot.agent.infrastructure.persistence.po;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.joker.comfypilot.common.infrastructure.persistence.po.BasePO;

/**
 * 用户Agent配置持久化对象
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName(value = "user_agent_config")
public class UserAgentConfigPO extends BasePO {

    private static final long serialVersionUID = 1L;

    /**
     * 所属用户ID
     */
    private Long userId;

    /**
     * Agent编码
     */
    private String agentCode;

    /**
     * Agent的运行时配置（json格式）
     */
    private String agentConfig;

}
