package org.joker.comfypilot.agent.domain.toolcall;

import lombok.extern.slf4j.Slf4j;
import org.joker.comfypilot.session.application.dto.client2server.AgentToolCallResponseData;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * 工具调用等待管理器
 * 用于管理Agent工具调用的异步等待和响应
 */
@Slf4j
@Component
public class ToolCallWaitManager {

    /**
     * 存储工具调用的Future
     * Key: sessionCode + requestId + toolName
     * Value: CompletableFuture<AgentToolCallResponseData>
     */
    private final Map<String, CompletableFuture<AgentToolCallResponseData>> waitingToolCalls = new ConcurrentHashMap<>();

    /**
     * 默认超时时间（秒）
     */
    private static final int DEFAULT_TIMEOUT_SECONDS = 300; // 5分钟

    /**
     * 创建工具调用等待
     *
     * @param sessionCode 会话代码
     * @param requestId   请求ID
     * @param toolName    工具名称
     * @return CompletableFuture
     */
    public CompletableFuture<AgentToolCallResponseData> createWait(String sessionCode, String requestId, String toolName) {
        String key = buildKey(sessionCode, requestId, toolName);
        CompletableFuture<AgentToolCallResponseData> future = new CompletableFuture<>();
        waitingToolCalls.put(key, future);

        log.debug("创建工具调用等待: key={}", key);

        // 设置超时处理
        future.orTimeout(DEFAULT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .exceptionally(ex -> {
                    log.warn("工具调用等待超时: key={}", key);
                    waitingToolCalls.remove(key);
                    return null;
                });

        return future;
    }

    /**
     * 完成工具调用等待
     *
     * @param sessionCode  会话代码
     * @param requestId    请求ID
     * @param toolName     工具名称
     * @param responseData 响应数据
     * @return 是否成功完成
     */
    public boolean completeWait(String sessionCode, String requestId, String toolName, AgentToolCallResponseData responseData) {
        String key = buildKey(sessionCode, requestId, toolName);
        CompletableFuture<AgentToolCallResponseData> future = waitingToolCalls.remove(key);

        if (future != null) {
            log.debug("完成工具调用等待: key={}", key);
            future.complete(responseData);
            return true;
        } else {
            log.warn("未找到对应的工具调用等待: key={}", key);
            return false;
        }
    }

    /**
     * 取消工具调用等待
     *
     * @param sessionCode 会话代码
     * @param requestId   请求ID
     * @param toolName    工具名称
     */
    public void cancelWait(String sessionCode, String requestId, String toolName) {
        String key = buildKey(sessionCode, requestId, toolName);
        CompletableFuture<AgentToolCallResponseData> future = waitingToolCalls.remove(key);

        if (future != null) {
            log.debug("取消工具调用等待: key={}", key);
            future.cancel(true);
        }
    }

    /**
     * 取消会话的所有工具调用等待
     *
     * @param sessionCode 会话代码
     */
    public void cancelSessionWaits(String sessionCode) {
        waitingToolCalls.entrySet().removeIf(entry -> {
            if (entry.getKey().startsWith(sessionCode + ":")) {
                log.debug("取消会话工具调用等待: key={}", entry.getKey());
                entry.getValue().cancel(true);
                return true;
            }
            return false;
        });
    }

    /**
     * 构建唯一键
     */
    private String buildKey(String sessionCode, String requestId, String toolName) {
        return sessionCode + ":" + requestId + ":" + toolName;
    }
}
