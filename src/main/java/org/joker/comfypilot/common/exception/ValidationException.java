package org.joker.comfypilot.common.exception;

/**
 * 参数验证异常
 */
public class ValidationException extends BusinessException {

    private static final long serialVersionUID = 1L;

    public ValidationException(String message) {
        super(400, message);
    }
}
