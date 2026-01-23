package org.joker.comfypilot.agent.domain.react;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.ToolExecutionResultMessage;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.request.ToolChoice;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;
import dev.langchain4j.model.output.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.joker.comfypilot.agent.domain.callback.AgentCallback;
import org.joker.comfypilot.agent.domain.context.AgentExecutionContext;
import org.joker.comfypilot.agent.domain.event.*;
import org.joker.comfypilot.agent.domain.toolcall.ToolCallRequest;
import org.joker.comfypilot.agent.domain.toolcall.ToolCallResult;
import org.joker.comfypilot.agent.domain.toolcall.ToolCallWaitManager;
import org.joker.comfypilot.session.application.dto.client2server.AgentToolCallResponseData;
import org.joker.comfypilot.session.domain.enums.AgentPromptType;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * ReAct 执行器
 * 实现 Reasoning and Acting 循环
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ReactExecutor {

    private final ToolCallWaitManager toolCallWaitManager;
    private final ObjectMapper objectMapper;

    /**
     * 最大迭代次数
     */
    private static final int MAX_ITERATIONS = 10;

    /**
     * 执行 ReAct 循环（响应式，非阻塞）
     *
     * @param streamingModel 流式聊天模型
     * @param chatRequest    初始聊天请求
     * @param context        执行上下文
     */
    public void executeReactLoop(
            StreamingChatModel streamingModel,
            ChatRequest chatRequest,
            AgentExecutionContext context
    ) {
        AgentCallback callback = context.getAgentCallback();
        AtomicInteger iteration = new AtomicInteger(0);

        // 发送思考中提示
        callback.onPrompt(AgentPromptType.THINKING, null);

        // 启动第一轮迭代
        executeNextIteration(streamingModel, chatRequest, context, iteration);
    }

    /**
     * 执行下一轮迭代（递归调用，响应式）
     */
    private void executeNextIteration(
            StreamingChatModel streamingModel,
            ChatRequest chatRequest,
            AgentExecutionContext context,
            AtomicInteger iteration
    ) {
        AgentCallback callback = context.getAgentCallback();

        // 检查终止条件
        if (iteration.get() >= MAX_ITERATIONS) {
            log.warn("ReAct 循环达到最大迭代次数: {}", MAX_ITERATIONS);
            callback.onPrompt(AgentPromptType.ERROR,
                    "达到最大迭代次数 " + MAX_ITERATIONS + "，执行终止");
            callback.onStreamComplete(null);
            return;
        }

        if (callback.isInterrupted()) {
            log.info("ReAct 循环被中断");
            callback.onPrompt(AgentPromptType.INTERRUPTED, "执行已被用户中断");
            callback.onStreamComplete(null);
            return;
        }

        log.debug("ReAct 循环迭代: iteration={}, sessionCode={}",
                iteration.get(), context.getSessionCode());

        // 发布迭代开始事件
        if (context.getEventPublisher() != null) {
            IterationStartEvent startEvent = new IterationStartEvent(context, iteration.get(), MAX_ITERATIONS);
            context.getEventPublisher().publishEvent(startEvent);
            if (startEvent.isCancelled()) {
                log.info("迭代被事件监听器取消");
                callback.onStreamComplete(null);
                return;
            }
        }

        // 发布 LLM 调用前事件（可修改消息和工具）
        ChatRequest finalRequest = chatRequest;
        ChatRequest.Builder requestBuilder = ChatRequest.builder()
                .messages(chatRequest.messages())
                .toolChoice(chatRequest.toolChoice())
                .toolSpecifications(chatRequest.toolSpecifications());
        if (context.getEventPublisher() != null) {
            BeforeLlmCallEvent beforeLlmEvent = new BeforeLlmCallEvent(context, iteration.get(), requestBuilder, chatRequest);
            context.getEventPublisher().publishEvent(beforeLlmEvent);
            if (beforeLlmEvent.isCancelled()) {
                log.info("LLM 调用被事件监听器取消");
                callback.onStreamComplete(null);
                return;
            }
            // 使用修改后的请求
            finalRequest = beforeLlmEvent.buildModifiedRequest();
        }

        // 1. 异步调用 LLM
        callLlmStreamingAsync(streamingModel, finalRequest, callback)
                .thenAccept(response -> {
                    if (response == null) {
                        log.warn("LLM 返回空响应");
                        callback.onPrompt(AgentPromptType.ERROR, "LLM 返回空响应");
                        callback.onStreamComplete(null);
                        return;
                    }

                    AiMessage aiMessage = response.aiMessage();

                    // 发布 LLM 调用后事件
                    if (context.getEventPublisher() != null) {
                        AfterLlmCallEvent afterLlmEvent = new AfterLlmCallEvent(context, iteration.get(), response);
                        context.getEventPublisher().publishEvent(afterLlmEvent);
                    }

                    // 2. 检查是否有工具调用
                    if (aiMessage.hasToolExecutionRequests()) {
                        log.info("检测到工具调用请求: count={}", aiMessage.toolExecutionRequests().size());

                        // 发送工具调用中提示
                        callback.onPrompt(AgentPromptType.TOOL_CALLING,
                                String.format("检测到 %d 个工具调用", aiMessage.toolExecutionRequests().size()));

                        // 3. 将 AI 消息添加到历史（带事件）
                        addMessageWithEvent(callback, context, iteration.get(), aiMessage);

                        // 4. 异步处理工具调用
                        handleToolCallsAsync(aiMessage.toolExecutionRequests(), context)
                                .thenAccept(toolResults -> {
                                    // 5. 将工具结果添加到历史（带事件）
                                    for (ToolExecutionResultMessage toolResult : toolResults) {
                                        addMessageWithEvent(callback, context, iteration.get(), toolResult);
                                    }

                                    // 发送工具完成提示
                                    callback.onPrompt(AgentPromptType.TOOL_COMPLETE,
                                            String.format("已完成 %d 个工具调用，继续分析...", toolResults.size()));

                                    // 6. 构建下一轮请求
                                    ChatRequest nextRequest = ChatRequest.builder()
                                            .messages(callback.getMemoryMessages())
                                            .toolSpecifications(chatRequest.toolSpecifications())
                                            .toolChoice(ToolChoice.AUTO)
                                            .build();

                                    // 7. 递归执行下一轮迭代
                                    iteration.incrementAndGet();
                                    executeNextIteration(streamingModel, nextRequest, context, iteration);
                                })
                                .exceptionally(ex -> {
                                    log.error("处理工具调用失败", ex);
                                    callback.onPrompt(AgentPromptType.ERROR, "工具调用处理失败: " + ex.getMessage());
                                    callback.onStreamComplete(null);
                                    return null;
                                });
                    } else {
                        // 没有工具调用，对话完成
                        log.info("ReAct 循环完成: iterations={}", iteration.get());
                        addMessageWithEvent(callback, context, iteration.get(), aiMessage);
                        callback.onStreamComplete(null);
                    }
                })
                .exceptionally(ex -> {
                    log.error("LLM 调用失败", ex);
                    callback.onPrompt(AgentPromptType.ERROR, "LLM 调用失败: " + ex.getMessage());
                    callback.onStreamComplete(null);
                    return null;
                });
    }

    /**
     * 异步调用 LLM 流式输出
     */
    private CompletableFuture<ChatResponse> callLlmStreamingAsync(
            StreamingChatModel streamingModel,
            ChatRequest chatRequest,
            AgentCallback callback
    ) {
        CompletableFuture<ChatResponse> future = new CompletableFuture<>();

        streamingModel.chat(chatRequest, new StreamingChatResponseHandler() {
            @Override
            public void onPartialResponse(String partialResponse) {
                // 处理流式输出片段
                if (partialResponse != null) {
                    callback.onStream(partialResponse);
                }
            }


            @Override
            public void onCompleteResponse(ChatResponse completeResponse) {
                log.debug("LLM 流式调用完成");
                future.complete(completeResponse);
            }

            @Override
            public void onError(Throwable error) {
                log.error("LLM 流式调用失败", error);
                future.completeExceptionally(error);
            }
        });

        return future;
    }

    /**
     * 异步处理工具调用
     */
    private CompletableFuture<List<ToolExecutionResultMessage>> handleToolCallsAsync(
            List<dev.langchain4j.agent.tool.ToolExecutionRequest> toolExecutionRequests,
            AgentExecutionContext context
    ) {
        AgentCallback callback = context.getAgentCallback();

        // 创建所有工具调用的 Future 列表
        List<CompletableFuture<ToolExecutionResultMessage>> toolFutures = new ArrayList<>();

        for (dev.langchain4j.agent.tool.ToolExecutionRequest request : toolExecutionRequests) {
            String toolName = request.name();
            String toolArgs = request.arguments();
            String toolCallId = request.id();

            log.info("处理工具调用: toolName={}, toolCallId={}", toolName, toolCallId);

            // 1. 通知客户端工具调用
            callback.onToolCall(toolName, toolArgs);

            // 2. 创建异步等待
            CompletableFuture<AgentToolCallResponseData> responseFuture = toolCallWaitManager.createWait(
                    context.getSessionCode(),
                    context.getRequestId(),
                    toolName
            );

            // 3. 将响应 Future 转换为结果消息 Future
            CompletableFuture<ToolExecutionResultMessage> resultFuture = responseFuture
                    .handle((responseData, ex) -> {
                        if (ex != null) {
                            log.error("等待工具调用响应失败: toolName={}", toolName, ex);
                            responseData = AgentToolCallResponseData.builder()
                                    .toolName(toolName)
                                    .isAllow(false)
                                    .success(false)
                                    .error("等待响应超时或被中断: " + ex.getMessage())
                                    .build();
                        }

                        // 4. 构建工具执行结果消息
                        String resultText = buildToolResultText(responseData);
                        ToolExecutionResultMessage resultMessage = ToolExecutionResultMessage.from(
                                toolCallId,
                                toolName,
                                resultText
                        );

                        log.info("工具调用完成: toolName={}, success={}", toolName, responseData.getSuccess());
                        return resultMessage;
                    });

            toolFutures.add(resultFuture);
        }

        // 5. 等待所有工具调用完成，组合成一个 Future
        return CompletableFuture.allOf(toolFutures.toArray(new CompletableFuture[0]))
                .thenApply(v -> toolFutures.stream()
                        .map(CompletableFuture::join)
                        .collect(java.util.stream.Collectors.toList()));
    }

    /**
     * 构建工具结果文本
     */
    private String buildToolResultText(AgentToolCallResponseData responseData) {
        if (!Boolean.TRUE.equals(responseData.getIsAllow())) {
            return "工具调用被拒绝";
        }

        if (!Boolean.TRUE.equals(responseData.getSuccess())) {
            return "工具执行失败: " + responseData.getError();
        }

        return responseData.getResult();
    }

    /**
     * 添加消息并发布事件
     */
    private void addMessageWithEvent(AgentCallback callback, AgentExecutionContext context,
                                     int iteration, ChatMessage message) {
        // 发布消息添加前事件
        if (context.getEventPublisher() != null) {
            BeforeMessageAddEvent beforeEvent = new BeforeMessageAddEvent(context, iteration, message);
            context.getEventPublisher().publishEvent(beforeEvent);
            if (beforeEvent.isCancelled()) {
                log.info("消息添加被事件监听器取消: messageType={}", message.type());
                return;
            }
            // 使用可能被修改的消息
            message = beforeEvent.getMessage();
        }

        // 添加消息到内存
        ChatMessage finalMessage = message;
        callback.addMemoryMessage(message,
                msg -> {
                    // 成功回调
                    if (context.getOnMessageAdded() != null) {
                        context.getOnMessageAdded().accept(msg);
                    }
                    // 发布消息添加后事件
                    if (context.getEventPublisher() != null) {
                        AfterMessageAddEvent afterEvent = new AfterMessageAddEvent(context, iteration, finalMessage, true);
                        context.getEventPublisher().publishEvent(afterEvent);
                    }
                },
                msg -> {
                    // 失败回调
                    if (context.getOnMessageAddFailed() != null) {
                        context.getOnMessageAddFailed().accept(msg);
                    }
                    // 发布消息添加后事件（失败）
                    if (context.getEventPublisher() != null) {
                        AfterMessageAddEvent afterEvent = new AfterMessageAddEvent(context, iteration, finalMessage, false);
                        context.getEventPublisher().publishEvent(afterEvent);
                    }
                }
        );
    }
}
