package org.joker.comfypilot.session.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 发送消息请求
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "发送消息请求")
public class SendMessageRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "会话编码", required = true)
    private String sessionCode;

    @Schema(description = "消息内容", required = true)
    private String content;
}
