package org.joker.comfypilot.auth.infrastructure.context;

import org.joker.comfypilot.auth.infrastructure.redis.model.UserSessionRedis;

/**
 * 用户上下文ThreadLocal
 * 用于在请求处理过程中传递用户信息
 */
public class UserContextHolder {

    private static final ThreadLocal<UserSessionRedis> CONTEXT = new ThreadLocal<>();

    /**
     * 设置当前用户会话
     *
     * @param session 用户会话
     */
    public static void setUserSession(UserSessionRedis session) {
        CONTEXT.set(session);
    }

    /**
     * 获取当前用户会话
     *
     * @return 用户会话
     */
    public static UserSessionRedis getUserSession() {
        return CONTEXT.get();
    }

    /**
     * 获取当前用户ID
     *
     * @return 用户ID
     */
    public static Long getCurrentUserId() {
        UserSessionRedis session = CONTEXT.get();
        return session != null ? session.getUserId() : null;
    }

    /**
     * 清除当前用户上下文
     */
    public static void clear() {
        CONTEXT.remove();
    }
}
