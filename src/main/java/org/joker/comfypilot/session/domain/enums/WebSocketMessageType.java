package org.joker.comfypilot.session.domain.enums;

import lombok.Getter;
import org.joker.comfypilot.session.application.dto.client2server.AgentToolCallResponseData;
import org.joker.comfypilot.session.application.dto.client2server.UserMessageRequestData;
import org.joker.comfypilot.session.application.dto.server2client.AgentCompleteResponseData;
import org.joker.comfypilot.session.application.dto.server2client.AgentToolCallRequestData;

import java.util.Map;

/**
 * WebSocket消息类型枚举
 */
@Getter
public enum WebSocketMessageType {

    /**
     * 客户端 -> 服务端：发送用户消息
     */
    USER_MESSAGE("用户消息", UserMessageRequestData.class),

    /**
     * 客户端 -> 服务端：Agent调用Tool响应
     */
    AGENT_TOOL_CALL_RESPONSE("Agent调用工具响应", AgentToolCallResponseData.class),

    /**
     * 客户端 -> 服务端：中断执行
     */
    INTERRUPT("中断执行", Map.class),

    /**
     * 客户端 -> 服务端：心跳
     */
    PING("心跳", Map.class),

    /**
     * 服务端 -> 客户端：会话已创建
     */
    SESSION_CREATED("会话已创建", Map.class),

    /**
     * 服务端 -> 客户端：Agent开始思考
     */
    AGENT_THINKING("Agent思考中", Map.class),

    /**
     * 服务端 -> 客户端：Agent流式输出（部分内容）
     */
    AGENT_STREAM("Agent流式输出", Map.class),

    /**
     * 服务端 -> 客户端：Agent完成输出
     */
    AGENT_COMPLETE("Agent完成", AgentCompleteResponseData.class),

    /**
     * 服务端 -> 客户端：Agent请求调用Tool
     */
    AGENT_TOOL_CALL_REQUEST("Agent调用工具请求", AgentToolCallRequestData.class),

    /**
     * 服务端 -> 客户端：执行被中断
     */
    EXECUTION_INTERRUPTED("执行中断", Map.class),

    /**
     * 服务端 -> 客户端：执行错误
     */
    ERROR("错误", Map.class),

    /**
     * 服务端 -> 客户端：心跳响应
     */
    PONG("心跳响应", Map.class);

    private final String description;

    private final Class<?> dataClass;

    WebSocketMessageType(String description, Class<?> dataClass) {
        this.description = description;
        this.dataClass = dataClass;
    }

}
