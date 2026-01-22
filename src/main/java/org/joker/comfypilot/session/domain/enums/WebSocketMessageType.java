package org.joker.comfypilot.session.domain.enums;

/**
 * WebSocket消息类型枚举
 */
public enum WebSocketMessageType {

    /**
     * 客户端 -> 服务端：发送用户消息
     */
    USER_MESSAGE("用户消息"),

    /**
     * 客户端 -> 服务端：用户响应（回答Agent是/否）
     */
    USER_RESPONSE("用户响应"),

    /**
     * 客户端 -> 服务端：中断执行
     */
    INTERRUPT("中断执行"),

    /**
     * 客户端 -> 服务端：心跳
     */
    PING("心跳"),

    /**
     * 服务端 -> 客户端：会话已创建
     */
    SESSION_CREATED("会话已创建"),

    /**
     * 服务端 -> 客户端：Agent开始思考
     */
    AGENT_THINKING("Agent思考中"),

    /**
     * 服务端 -> 客户端：Agent流式输出（部分内容）
     */
    AGENT_STREAM("Agent流式输出"),

    /**
     * 服务端 -> 客户端：Agent完成输出
     */
    AGENT_COMPLETE("Agent完成"),

    /**
     * 服务端 -> 客户端：Agent请求用户输入
     */
    AGENT_REQUEST_INPUT("Agent请求输入"),

    /**
     * 服务端 -> 客户端：Agent执行Tool
     */
    AGENT_TOOL_CALL("Agent调用工具"),

    /**
     * 服务端 -> 客户端：执行被中断
     */
    EXECUTION_INTERRUPTED("执行中断"),

    /**
     * 服务端 -> 客户端：执行错误
     */
    ERROR("错误"),

    /**
     * 服务端 -> 客户端：心跳响应
     */
    PONG("心跳响应");

    private final String description;

    WebSocketMessageType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
