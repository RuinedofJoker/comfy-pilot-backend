package org.joker.comfypilot.common.constant;

/**
 * 认证相关常量
 */
public class AuthConstants {

    private AuthConstants() {
    }

    /**
     * Authorization 请求头名称
     */
    public static final String AUTHORIZATION_HEADER = "Authorization";

    /**
     * Bearer Token 前缀
     */
    public static final String BEARER_PREFIX = "Bearer ";

    /**
     * WebSocket Session 属性：用户ID
     */
    public static final String USER_ID_ATTRIBUTE = "userId";
}
