package org.joker.comfypilot.common.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.joker.comfypilot.common.util.TraceIdUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * TraceId 拦截器
 * 在请求进入时生成或获取 TraceId，请求结束时清除
 */
@Component
public class TraceIdInterceptor implements HandlerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(TraceIdInterceptor.class);

    /**
     * HTTP 请求头中的 TraceId 键名
     */
    private static final String TRACE_ID_HEADER = "X-Trace-Id";

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request,
                             @NonNull HttpServletResponse response,
                             @NonNull Object handler) {
        // 从请求头获取 TraceId，如果没有则生成新的
        String traceId = request.getHeader(TRACE_ID_HEADER);
        if (!StringUtils.hasText(traceId)) {
            traceId = TraceIdUtil.generateTraceId();
        }

        // 设置到 MDC
        TraceIdUtil.setTraceId(traceId);

        // 将 TraceId 添加到响应头，方便客户端追踪
        response.setHeader(TRACE_ID_HEADER, traceId);

        log.debug("Request started with traceId: {}", traceId);
        return true;
    }

    @Override
    public void afterCompletion(@NonNull HttpServletRequest request,
                                @NonNull HttpServletResponse response,
                                @NonNull Object handler,
                                Exception ex) {
        // 请求结束后清除 MDC，避免内存泄漏
        TraceIdUtil.clear();
    }
}
