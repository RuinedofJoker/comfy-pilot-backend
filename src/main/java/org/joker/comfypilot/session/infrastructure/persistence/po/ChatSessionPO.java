package org.joker.comfypilot.session.infrastructure.persistence.po;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.joker.comfypilot.common.infrastructure.persistence.po.BasePO;

/**
 * 会话持久化对象
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("chat_session")
public class ChatSessionPO extends BasePO {

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 工作流ID
     */
    private Long workflowId;

    /**
     * 会话标题
     */
    private String title;

    /**
     * 状态
     */
    private String status;

    /**
     * 最后消息摘要
     */
    private String lastMessageSummary;
}
