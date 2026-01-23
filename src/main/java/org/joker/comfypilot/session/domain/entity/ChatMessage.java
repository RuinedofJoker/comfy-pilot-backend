package org.joker.comfypilot.session.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.joker.comfypilot.common.domain.BaseEntity;
import org.joker.comfypilot.common.enums.MessageRole;
import org.joker.comfypilot.session.domain.enums.MessageStatus;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 消息领域实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessage extends BaseEntity<Long> {

    private static final long serialVersionUID = 1L;

    /**
     * 消息ID
     */
    private Long id;

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
    private MessageRole role;

    /**
     * 消息状态
     */
    private MessageStatus status;

    /**
     * 消息内容
     */
    private String content;

    /**
     * 消息内容数据
     */
    private String chatContent;

    /**
     * 元数据（JSON格式）
     */
    private Map<String, Object> metadata;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

}
