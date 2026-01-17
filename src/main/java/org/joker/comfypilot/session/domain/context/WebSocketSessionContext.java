package org.joker.comfypilot.session.domain.context;

import lombok.Builder;
import lombok.Data;
import org.springframework.web.socket.WebSocketSession;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * WebSocket会话执行上下文
 * 管理单个WebSocket连接的执行状态
 */
@Data
@Builder
public class WebSocketSessionContext {

    /**
     * WebSocket会话
     */
    private WebSocketSession webSocketSession;

    /**
     * 聊天会话编码
     */
    private String sessionCode;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 是否正在执行
     */
    private AtomicBoolean executing;

    /**
     * 中断标志
     */
    private AtomicBoolean interrupted;

    /**
     * 用户响应Future（用于等待用户输入）
     */
    private volatile CompletableFuture<String> userResponseFuture;

    /**
     * 创建时间
     */
    private Long createTime;

    /**
     * 最后活跃时间
     */
    private Long lastActiveTime;

    /**
     * 检查是否可以执行
     */
    public boolean canExecute() {
        return !executing.get() && !interrupted.get();
    }

    /**
     * 开始执行
     */
    public void startExecution() {
        executing.set(true);
        interrupted.set(false);
    }

    /**
     * 完成执行
     */
    public void completeExecution() {
        executing.set(false);
    }

    /**
     * 请求中断
     */
    public void requestInterrupt() {
        interrupted.set(true);
    }

    /**
     * 检查是否被中断
     */
    public boolean isInterrupted() {
        return interrupted.get();
    }

    /**
     * 更新活跃时间
     */
    public void updateActiveTime() {
        this.lastActiveTime = System.currentTimeMillis();
    }

    /**
     * 请求用户输入（创建等待Future）
     *
     * @return 用户响应的Future
     */
    public CompletableFuture<String> requestUserInput() {
        this.userResponseFuture = new CompletableFuture<>();
        return this.userResponseFuture;
    }

    /**
     * 提供用户响应（完成Future）
     *
     * @param response 用户响应内容
     */
    public void provideUserResponse(String response) {
        if (this.userResponseFuture != null && !this.userResponseFuture.isDone()) {
            this.userResponseFuture.complete(response);
        }
    }

    /**
     * 检查是否正在等待用户输入
     *
     * @return true表示正在等待
     */
    public boolean isWaitingForUserInput() {
        return this.userResponseFuture != null && !this.userResponseFuture.isDone();
    }
}
