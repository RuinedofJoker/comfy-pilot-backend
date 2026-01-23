package org.joker.comfypilot.session.application.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * WebSocket消息DTO
 * @param <T> 附加数据类型
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class WebSocketMessage<T extends WebSocketMessageData> implements Serializable {

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
     * 请求ID，时间戳
     */
    private String requestId;

    /**
     * 消息内容
     */
    private String content;

    /**
     * 附加数据（泛型）
     * 根据消息类型使用不同的数据类型：
     * - 服务端->客户端：使用 server2client 包下的数据类型
     * - 客户端->服务端：使用 client2server 包下的数据类型
     */
    private T data;

    /**
     * 错误信息
     */
    private String error;

    /**
     * 时间戳
     */
    private Long timestamp;
}
