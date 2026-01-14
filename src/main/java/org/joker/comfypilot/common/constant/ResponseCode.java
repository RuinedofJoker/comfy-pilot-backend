package org.joker.comfypilot.common.constant;

/**
 * 响应码常量
 */
public class ResponseCode {

    private ResponseCode() {
    }

    /**
     * 成功
     */
    public static final int SUCCESS = 200;

    /**
     * 参数错误
     */
    public static final int BAD_REQUEST = 400;

    /**
     * 未授权
     */
    public static final int UNAUTHORIZED = 401;

    /**
     * 禁止访问
     */
    public static final int FORBIDDEN = 403;

    /**
     * 资源未找到
     */
    public static final int NOT_FOUND = 404;

    /**
     * 服务器内部错误
     */
    public static final int INTERNAL_SERVER_ERROR = 500;

    /**
     * 服务不可用
     */
    public static final int SERVICE_UNAVAILABLE = 503;
}
