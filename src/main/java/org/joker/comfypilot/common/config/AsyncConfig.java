package org.joker.comfypilot.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 异步任务配置
 * 配置线程池并支持 TraceId 传递
 */
@EnableAsync
@Configuration
public class AsyncConfig {

    /**
     * 异步任务线程池
     */
    @Bean(name = "asyncExecutor")
    public Executor asyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor() {
            @Override
            public void execute(@NonNull Runnable task) {
                // 使用 TraceId 装饰器包装任务
                super.execute(TraceIdTaskDecorator.decorate(task));
            }
        };

        // 核心线程数
        executor.setCorePoolSize(10);
        // 最大线程数
        executor.setMaxPoolSize(20);
        // 队列容量
        executor.setQueueCapacity(200);
        // 线程名称前缀
        executor.setThreadNamePrefix("async-");
        // 拒绝策略：由调用线程处理
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        // 等待所有任务完成后再关闭线程池
        executor.setWaitForTasksToCompleteOnShutdown(true);
        // 等待时间
        executor.setAwaitTerminationSeconds(60);

        executor.initialize();
        return executor;
    }
}
