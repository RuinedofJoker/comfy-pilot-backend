package org.joker.comfypilot.session.domain.entity;

import lombok.Data;
import org.joker.comfypilot.session.domain.enums.MessageRole;

import java.time.LocalDateTime;

/**
 * 消息领域实体
 */
@Data
public class ChatMessage {

    private Long id;
    private Long sessionId;
    private MessageRole role;
    private String content;
    private LocalDateTime createTime;

    /**
     * 创建用户消息
     */
    public static ChatMessage createUserMessage(Long sessionId, String content) {
        // TODO: 实现创建用户消息逻辑
        return null;
    }

    /**
     * 创建助手消息
     */
    public static ChatMessage createAssistantMessage(Long sessionId, String content) {
        // TODO: 实现创建助手消息逻辑
        return null;
    }

    /**
     * 创建系统消息
     */
    public static ChatMessage createSystemMessage(Long sessionId, String content) {
        // TODO: 实现创建系统消息逻辑
        return null;
    }
}
