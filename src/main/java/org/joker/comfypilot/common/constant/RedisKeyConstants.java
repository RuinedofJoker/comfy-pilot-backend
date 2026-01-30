package org.joker.comfypilot.common.constant;

/**
 * Redis Key 常量
 */
public class RedisKeyConstants {

    private RedisKeyConstants() {
    }

    /**
     * 会话 Token 消耗统计 Key 前缀
     * 格式: session:token:usage:{sessionCode}
     */
    public static final String SESSION_TOKEN_USAGE_PREFIX = "session:token:usage:";

    /**
     * 获取会话 Token 消耗统计 Key
     *
     * @param sessionCode 会话编码
     * @return Redis Key
     */
    public static String getSessionTokenUsageKey(String sessionCode) {
        return SESSION_TOKEN_USAGE_PREFIX + sessionCode;
    }
}
