package org.joker.comfypilot.common.util;

import org.slf4j.MDC;

import java.util.UUID;

/**
 * TraceId 工具类
 * 用于生成和管理请求追踪ID
 */
public class TraceIdUtil {

    /**
     * TraceId 在 MDC 中的键名
     */
    public static final String TRACE_ID = "traceId";

    /**
     * 生成新的 TraceId
     *
     * @return TraceId
     */
    public static String generateTraceId() {
        return UUID.randomUUID().toString();
    }

    /**
     * 设置 TraceId 到 MDC
     *
     * @param traceId TraceId
     */
    public static void setTraceId(String traceId) {
        MDC.put(TRACE_ID, traceId);
    }

    /**
     * 获取当前 TraceId
     *
     * @return TraceId
     */
    public static String getTraceId() {
        return MDC.get(TRACE_ID);
    }

    /**
     * 清除 TraceId
     */
    public static void removeTraceId() {
        MDC.remove(TRACE_ID);
    }

    /**
     * 清除所有 MDC 数据
     */
    public static void clear() {
        MDC.clear();
    }
}
