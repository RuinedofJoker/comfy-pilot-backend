package org.joker.comfypilot.agent.domain.toolcall;

import lombok.extern.slf4j.Slf4j;
import org.joker.comfypilot.session.application.dto.AgentCallToolResult;
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
     * Key: toolCallId + toolName
     * Value: CompletableFuture<AgentToolCallResponseData>
     */
    private final Map<String, CompletableFuture<AgentCallToolResult>> waitingToolCalls = new ConcurrentHashMap<>(1024);

    /**
     * 默认超时时间（秒）
     */
    private static final int DEFAULT_TIMEOUT_SECONDS = 3600; // 一小时(连接断开时设置了回调，这个事件可以稍微长一点)

    /**
     * 创建工具调用等待
     *
     * @param toolCallId 请求ID
     * @param toolName   工具名称
     * @return CompletableFuture
     */
    public CompletableFuture<AgentCallToolResult> createWait(String toolCallId, String toolName) {
        String key = buildKey(toolCallId, toolName);
        CompletableFuture<AgentCallToolResult> future = new CompletableFuture<>();
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
     * @param toolCallId     请求ID
     * @param toolName       工具名称
     * @param callToolResult 响应数据
     * @return 是否成功完成
     */
    public boolean completeWait(String toolCallId, String toolName, AgentCallToolResult callToolResult) {
        String key = buildKey(toolCallId, toolName);
        CompletableFuture<AgentCallToolResult> future = waitingToolCalls.remove(key);

        if (future != null) {
            log.debug("完成工具调用等待: key={}", key);
            future.complete(callToolResult);
            return true;
        } else {
            log.warn("未找到对应的工具调用等待: key={}", key);
            return false;
        }
    }

    /**
     * 取消工具调用等待
     *
     * @param toolCallId 请求ID
     * @param toolName   工具名称
     */
    public void cancelWait(String toolCallId, String toolName) {
        String key = buildKey(toolCallId, toolName);
        CompletableFuture<AgentCallToolResult> future = waitingToolCalls.remove(key);

        if (future != null) {
            log.debug("取消工具调用等待: key={}", key);
            future.cancel(true);
        }
    }

    /**
     * 构建唯一键
     */
    private String buildKey(String requestId, String toolName) {
        return requestId + ":" + toolName;
    }
}
