package org.joker.comfypilot.common.config;

import org.joker.comfypilot.common.util.TraceIdUtil;
import org.springframework.lang.NonNull;

import java.util.concurrent.Callable;

/**
 * TraceId 任务装饰器
 * 用于在异步任务中传递 TraceId
 */
public class TraceIdTaskDecorator {

    /**
     * 装饰 Runnable 任务
     *
     * @param runnable 原始任务
     * @return 装饰后的任务
     */
    public static Runnable decorate(@NonNull Runnable runnable) {
        // 获取当前线程的 TraceId
        String traceId = TraceIdUtil.getTraceId();
        return () -> {
            try {
                // 在新线程中设置 TraceId
                if (traceId != null) {
                    TraceIdUtil.setTraceId(traceId);
                }
                runnable.run();
            } finally {
                // 清除 TraceId
                TraceIdUtil.clear();
            }
        };
    }

    /**
     * 装饰 Callable 任务
     *
     * @param callable 原始任务
     * @param <T>      返回值类型
     * @return 装饰后的任务
     */
    public static <T> Callable<T> decorate(@NonNull Callable<T> callable) {
        // 获取当前线程的 TraceId
        String traceId = TraceIdUtil.getTraceId();
        return () -> {
            try {
                // 在新线程中设置 TraceId
                if (traceId != null) {
                    TraceIdUtil.setTraceId(traceId);
                }
                return callable.call();
            } finally {
                // 清除 TraceId
                TraceIdUtil.clear();
            }
        };
    }
}
