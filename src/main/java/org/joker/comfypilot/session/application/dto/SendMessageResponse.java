package org.joker.comfypilot.session.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 发送消息响应
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "发送消息响应")
public class SendMessageResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "用户消息")
    private ChatMessageDTO userMessage;

    @Schema(description = "助手消息")
    private ChatMessageDTO assistantMessage;
}
