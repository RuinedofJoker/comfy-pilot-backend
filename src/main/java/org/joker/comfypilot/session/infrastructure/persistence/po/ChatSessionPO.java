package org.joker.comfypilot.session.infrastructure.persistence.po;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.joker.comfypilot.common.infrastructure.persistence.annotation.UniqueKey;
import org.joker.comfypilot.common.infrastructure.persistence.po.BasePO;

/**
 * 会话持久化对象
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName("chat_session")
public class ChatSessionPO extends BasePO {

    private static final long serialVersionUID = 1L;

    /**
     * 会话编码（唯一标识）
     */
    @UniqueKey
    private String sessionCode;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * ComfyUI服务ID
     */
    private Long comfyuiServerId;

    /**
     * 会话使用的agent的agentCode
     */
    private String agentCode;

    /**
     * 会话使用的agent的运行时配置（json格式）
     */
    private String agentConfig;

    /**
     * 会话标题
     */
    private String title;

    /**
     * 会话状态（ACTIVE, CLOSED）
     */
    private String status;
}
