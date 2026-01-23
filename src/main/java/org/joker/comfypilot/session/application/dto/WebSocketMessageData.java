package org.joker.comfypilot.session.application.dto;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.joker.comfypilot.session.application.dto.client2server.AgentToolCallResponseData;
import org.joker.comfypilot.session.application.dto.client2server.UserMessageRequestData;
import org.joker.comfypilot.session.application.dto.server2client.AgentCompleteResponseData;
import org.joker.comfypilot.session.application.dto.server2client.AgentToolCallRequestData;

import java.io.Serializable;

/**
 * WebSocket消息数据基础接口
 * 所有WebSocket消息的data字段都应实现此接口
 *
 * <p>使用Jackson的多态反序列化机制，根据外层WebSocketMessage的type字段自动反序列化为对应的具体类型。
 * 参考 {@link org.joker.comfypilot.common.domain.message.PersistableChatMessage} 的设计。
 *
 * <p>消息类型分为两大类：
 * <ul>
 *   <li>{@link ClientToServerMessage} - 客户端发送到服务端的消息</li>
 *   <li>{@link ServerToClientMessage} - 服务端发送到客户端的消息</li>
 * </ul>
 */
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXTERNAL_PROPERTY,
        property = "type"
)
@JsonSubTypes({
        // 客户端 -> 服务端
        @JsonSubTypes.Type(value = UserMessageRequestData.class, name = "USER_MESSAGE"),
        @JsonSubTypes.Type(value = UserMessageRequestData.class, name = "USER_ORDER"),
        @JsonSubTypes.Type(value = AgentToolCallResponseData.class, name = "AGENT_TOOL_CALL_RESPONSE"),
        @JsonSubTypes.Type(value = VoidWebSocketMessageData.class, name = "INTERRUPT"),
        @JsonSubTypes.Type(value = VoidWebSocketMessageData.class, name = "PING"),

        // 服务端 -> 客户端
        @JsonSubTypes.Type(value = VoidWebSocketMessageData.class, name = "AGENT_THINKING"),
        @JsonSubTypes.Type(value = VoidWebSocketMessageData.class, name = "SUMMERY"),
        @JsonSubTypes.Type(value = VoidWebSocketMessageData.class, name = "SUMMERY_COMPLETE"),
        @JsonSubTypes.Type(value = VoidWebSocketMessageData.class, name = "AGENT_STREAM"),
        @JsonSubTypes.Type(value = AgentCompleteResponseData.class, name = "AGENT_COMPLETE"),
        @JsonSubTypes.Type(value = AgentToolCallRequestData.class, name = "AGENT_TOOL_CALL_REQUEST"),
        @JsonSubTypes.Type(value = VoidWebSocketMessageData.class, name = "EXECUTION_INTERRUPTED"),
        @JsonSubTypes.Type(value = VoidWebSocketMessageData.class, name = "ERROR"),
        @JsonSubTypes.Type(value = VoidWebSocketMessageData.class, name = "PONG")
})
public interface WebSocketMessageData extends Serializable {
}
