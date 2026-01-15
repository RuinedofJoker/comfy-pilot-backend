package org.joker.comfypilot.session.application.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 消息DTO
 */
@Data
public class ChatMessageDTO {

    /**
     * 消息ID
     */
    private Long id;

    /**
     * 会话ID
     */
    private Long sessionId;

    /**
     * 角色
     */
    private String role;

    /**
     * 消息内容
     */
    private String content;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
