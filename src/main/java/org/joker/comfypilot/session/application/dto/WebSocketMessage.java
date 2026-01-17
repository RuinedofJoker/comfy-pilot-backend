package org.joker.comfypilot.session.application.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Map;

/**
 * WebSocket消息DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class WebSocketMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 消息类型
     */
    private String type;

    /**
     * 会话编码
     */
    private String sessionCode;

    /**
     * 消息内容
     */
    private String content;

    /**
     * 附加数据
     */
    private Map<String, Object> data;

    /**
     * 错误信息
     */
    private String error;

    /**
     * 时间戳
     */
    private Long timestamp;
}
