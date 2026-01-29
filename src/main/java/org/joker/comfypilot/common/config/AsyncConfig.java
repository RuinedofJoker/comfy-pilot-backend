package org.joker.comfypilot.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 异步配置
 * 配置支持 TraceId 传递的线程池
 */
@Configuration
@EnableAsync
@EnableScheduling
public class AsyncConfig {

    /**
     * 命令执行线程池
     */
    @Bean
    public ThreadPoolExecutor fetchCommandExecutor() {
        return new ThreadPoolExecutor(
                Runtime.getRuntime().availableProcessors() + 1,
                Runtime.getRuntime().availableProcessors() + 1,
                30,
                TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(100)
        );
    }

    /**
     * CompletableFuture 默认线程池
     * 使用 ForkJoinPool 配置,支持 TraceId 传递
     */
    @Bean(name = "agentExecutor")
    public Executor agentExecutor() {
        // 使用与 ForkJoinPool.commonPool() 相同的并行度
        int parallelism = ForkJoinPool.getCommonPoolParallelism();

        return new ThreadPoolExecutor(
                parallelism,
                parallelism,
                60L,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(),
                new ThreadFactory() {
                    private AtomicInteger count = new AtomicInteger(0);
                    @Override
                    public Thread newThread(Runnable r) {
                        Thread thread = new Thread(r);
                        thread.setName("agent-executor-" + count.getAndIncrement());
                        thread.setDaemon(true);
                        return thread;
                    }
                }
        );
    }

}
