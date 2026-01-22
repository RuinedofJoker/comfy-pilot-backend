package org.joker.comfypilot.session.infrastructure.persistence.po;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.joker.comfypilot.common.infrastructure.persistence.po.BasePO;

/**
 * 消息持久化对象
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName(value = "chat_message", autoResultMap = true)
public class ChatMessagePO extends BasePO {

    private static final long serialVersionUID = 1L;

    /**
     * 会话ID
     */
    private Long sessionId;

    /**
     * 会话编码
     */
    private String sessionCode;

    /**
     * 请求ID（类型应该是时间戳）
     */
    private String requestId;

    /**
     * 消息角色
     */
    private String role;

    /**
     * 消息状态（ACTIVE, ARCHIVED）
     */
    private String status;

    /**
     * 消息内容
     */
    private String content;

    /**
     * 消息内容数据
     */
    private String contentData;

    /**
     * 元数据（JSON类型）
     */
    private String metadata;
}
