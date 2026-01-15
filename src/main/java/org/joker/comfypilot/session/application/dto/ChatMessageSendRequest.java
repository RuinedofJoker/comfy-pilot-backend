package org.joker.comfypilot.session.application.dto;

import lombok.Data;

/**
 * 消息发送请求DTO
 */
@Data
public class ChatMessageSendRequest {

    /**
     * 消息内容
     */
    private String content;
}
