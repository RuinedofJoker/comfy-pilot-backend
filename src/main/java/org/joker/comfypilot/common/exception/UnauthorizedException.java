package org.joker.comfypilot.common.exception;

/**
 * 未授权异常
 */
public class UnauthorizedException extends BusinessException {

    private static final long serialVersionUID = 1L;

    public UnauthorizedException(String message) {
        super(401, message);
    }

    public UnauthorizedException() {
        super(401, "未授权访问");
    }
}
