package org.joker.comfypilot.common.exception;

/**
 * 资源未找到异常
 */
public class ResourceNotFoundException extends BusinessException {

    private static final long serialVersionUID = 1L;

    public ResourceNotFoundException(String message) {
        super(404, message);
    }

    public ResourceNotFoundException(String resourceName, Object resourceId) {
        super(404, String.format("%s not found: %s", resourceName, resourceId));
    }
}
