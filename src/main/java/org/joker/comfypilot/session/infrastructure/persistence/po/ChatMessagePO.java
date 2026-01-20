package org.joker.comfypilot.session.infrastructure.persistence.po;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.joker.comfypilot.common.infrastructure.persistence.po.BasePO;
import org.joker.comfypilot.common.infrastructure.persistence.typehandler.PostgresJsonbTypeHandler;

import java.util.Map;

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
     * 消息角色（USER, ASSISTANT, SYSTEM）
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
     * 元数据（JSONB类型）
     */
    @TableField(typeHandler = PostgresJsonbTypeHandler.class)
    private Map<String, Object> metadata;
}
