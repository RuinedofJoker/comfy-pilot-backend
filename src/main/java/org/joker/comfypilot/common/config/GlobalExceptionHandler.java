package org.joker.comfypilot.common.config;

import org.joker.comfypilot.common.exception.BusinessException;
import org.joker.comfypilot.common.exception.ResourceNotFoundException;
import org.joker.comfypilot.common.exception.UnauthorizedException;
import org.joker.comfypilot.common.exception.ValidationException;
import org.joker.comfypilot.common.interfaces.response.Result;
import org.joker.comfypilot.common.util.TraceIdUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

/**
 * 全局异常处理器
 * 统一处理所有异常并返回标准响应格式
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * 处理业务异常
     */
    @ExceptionHandler(BusinessException.class)
    public Result<Void> handleBusinessException(BusinessException e) {
        log.warn("Business exception: {}", e.getMessage());
        Result<Void> result = Result.error(e.getCode(), e.getMessage());
        result.setTraceId(TraceIdUtil.getTraceId());
        return result;
    }

    /**
     * 处理参数验证异常
     */
    @ExceptionHandler(ValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleValidationException(ValidationException e) {
        log.warn("Validation exception: {}", e.getMessage());
        Result<Void> result = Result.error(e.getCode(), e.getMessage());
        result.setTraceId(TraceIdUtil.getTraceId());
        return result;
    }

    /**
     * 处理未授权异常
     */
    @ExceptionHandler(UnauthorizedException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public Result<Void> handleUnauthorizedException(UnauthorizedException e) {
        log.warn("Unauthorized exception: {}", e.getMessage());
        Result<Void> result = Result.error(e.getCode(), e.getMessage());
        result.setTraceId(TraceIdUtil.getTraceId());
        return result;
    }

    /**
     * 处理资源未找到异常
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Result<Void> handleResourceNotFoundException(ResourceNotFoundException e) {
        log.warn("Resource not found: {}", e.getMessage());
        Result<Void> result = Result.error(e.getCode(), e.getMessage());
        result.setTraceId(TraceIdUtil.getTraceId());
        return result;
    }

    /**
     * 处理参数绑定异常
     */
    @ExceptionHandler(BindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleBindException(BindException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        log.warn("Bind exception: {}", message);
        Result<Void> result = Result.error(400, message);
        result.setTraceId(TraceIdUtil.getTraceId());
        return result;
    }

    /**
     * 处理方法参数验证异常
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));
        log.warn("Method argument not valid: {}", message);
        Result<Void> result = Result.error(400, message);
        result.setTraceId(TraceIdUtil.getTraceId());
        return result;
    }

    /**
     * 处理其他未知异常
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<Void> handleException(Exception e) {
        log.error("Unexpected exception", e);
        Result<Void> result = Result.error(500, "系统内部错误");
        result.setTraceId(TraceIdUtil.getTraceId());
        return result;
    }
}
