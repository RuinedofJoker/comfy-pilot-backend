package org.joker.comfypilot.agent.domain.react;

import dev.langchain4j.agent.tool.ToolExecutionRequest;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.ToolExecutionResultMessage;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.request.ToolChoice;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.joker.comfypilot.agent.domain.callback.AgentCallback;
import org.joker.comfypilot.agent.domain.context.AgentExecutionContext;
import org.joker.comfypilot.agent.domain.event.*;
import org.joker.comfypilot.agent.domain.toolcall.ToolCallWaitManager;
import org.joker.comfypilot.common.exception.BusinessException;
import org.joker.comfypilot.session.application.dto.AgentCallToolResult;
import org.springframework.beans.factory.annotation.Autowired;
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
public class ReactExecutor {

    @Autowired
    private ToolCallWaitManager toolCallWaitManager;

    /**
     * 执行 ReAct 循环（响应式，非阻塞）
     *
     * @param streamingModel 流式聊天模型
     * @param chatRequest    初始聊天请求
     * @param context        执行上下文
     * @param maxIterations  最大迭代次数
     */
    public void executeReactLoop(
            StreamingChatModel streamingModel,
            ChatRequest chatRequest,
            AgentExecutionContext context,
            final int maxIterations
    ) {
        AtomicInteger iteration = new AtomicInteger(0);

        // 启动第一轮迭代
        executeNextIteration(streamingModel, chatRequest, context, iteration, maxIterations);
    }

    /**
     * 执行下一轮迭代（递归调用，响应式）
     */
    private void executeNextIteration(
            StreamingChatModel streamingModel,
            ChatRequest chatRequest,
            AgentExecutionContext context,
            AtomicInteger iteration,
            final int maxIterations
    ) {
        log.debug("ReAct 循环迭代: iteration={}, sessionCode={}",
                iteration.get(), context.getSessionCode());

        // 发布迭代开始事件
        if (context.getEventPublisher() != null) {
            IterationStartEvent startEvent = new IterationStartEvent(context, iteration.get(), maxIterations);
            context.getEventPublisher().publishEvent(startEvent);
            if (startEvent.isCancelled()) {
                log.info("迭代被事件监听器取消");
                // 发布迭代结束事件
                IterationEndEvent iterationEndEvent = new IterationEndEvent(context, iteration.get(), false, false, false, true, null);
                context.getEventPublisher().publishEvent(iterationEndEvent);
                return;
            }
        }

        // 检查终止条件
        if (iteration.get() >= maxIterations) {
            log.warn("ReAct 循环达到最大迭代次数: {}", maxIterations);
            // 发布迭代结束事件
            if (context.getEventPublisher() != null) {
                IterationEndEvent iterationEndEvent = new IterationEndEvent(context, iteration.get(), false, false, false, false, new BusinessException(new InterruptedException("循环达到最大迭代次数，强制中断")));
                context.getEventPublisher().publishEvent(iterationEndEvent);
            }
            return;
        }

        // 检查是否被中断（通过 AgentCallback 查询）
        if (context.getAgentCallback() != null && context.getAgentCallback().isInterrupted()) {
            log.info("ReAct 循环被中断");
            // 发布迭代结束事件
            if (context.getEventPublisher() != null) {
                IterationEndEvent iterationEndEvent = new IterationEndEvent(context, iteration.get(), false, false, false, false, new BusinessException(new InterruptedException("迭代被手动中断")));
                context.getEventPublisher().publishEvent(iterationEndEvent);
            }
            return;
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
                log.info("迭代被事件监听器取消");
                // 发布迭代结束事件
                IterationEndEvent iterationEndEvent = new IterationEndEvent(context, iteration.get(), false, false, false, true, null);
                context.getEventPublisher().publishEvent(iterationEndEvent);
                return;
            }
            // 使用修改后的请求
            finalRequest = beforeLlmEvent.buildModifiedRequest();
        }

        // 1. 异步调用 LLM
        callLlmStreamingAsync(streamingModel, finalRequest, context, iteration.get())
                .thenAccept(response -> {
                    AiMessage aiMessage = response.aiMessage();

                    // 发布 LLM 调用后事件
                    if (context.getEventPublisher() != null) {
                        AfterLlmCallEvent afterLlmEvent = new AfterLlmCallEvent(context, iteration.get(), response);
                        context.getEventPublisher().publishEvent(afterLlmEvent);

                        // 记录 token 消费统计
                        if (response.tokenUsage() != null) {
                            log.info("LLM 调用完成 - iteration={}, inputTokens={}, outputTokens={}, totalTokens={}, messageCount={}",
                                    iteration.get(),
                                    response.tokenUsage().inputTokenCount(),
                                    response.tokenUsage().outputTokenCount(),
                                    response.tokenUsage().totalTokenCount(),
                                    context.getAgentCallback().getMemoryMessages().size() + 1);
                        } else {
                            log.info("LLM 调用完成 - iteration={}, messageCount={} (token统计不可用)",
                                    iteration.get(),
                                    context.getAgentCallback().getMemoryMessages().size() + 1);
                        }
                    }

                    // 2. 检查是否有工具调用
                    if (aiMessage.hasToolExecutionRequests()) {
                        log.info("检测到工具调用请求: count={}", aiMessage.toolExecutionRequests().size());

                        // 3. 将 AI 消息添加到历史（带事件）
                        addMessageWithEvent(context, iteration.get(), aiMessage);

                        // 4. 异步处理工具调用
                        handleToolCallsAsync(aiMessage.toolExecutionRequests(), context, iteration.get())
                                .thenAccept(toolResults -> {
                                    // 发布工具调用后
                                    if (context.getEventPublisher() != null) {
                                        AfterToolCallEvent afterToolCallEvent = new AfterToolCallEvent(context, iteration.get(), false, toolResults, null);
                                        context.getEventPublisher().publishEvent(afterToolCallEvent);
                                    }

                                    // 5. 将工具结果添加到历史（带事件）
                                    for (ToolExecutionResultMessage toolResult : toolResults) {
                                        addMessageWithEvent(context, iteration.get(), toolResult);
                                    }

                                    // 检查是否被中断
                                    if (context.isInterrupted()) {
                                        log.info("工具返回后被中断");
                                        throw new BusinessException(new InterruptedException("迭代被手动中断"));
                                    }

                                    // 6. 构建下一轮请求
                                    ChatRequest nextRequest = ChatRequest.builder()
                                            .messages(context.getAgentCallback().getMemoryMessages())
                                            .toolSpecifications(chatRequest.toolSpecifications())
                                            .toolChoice(ToolChoice.AUTO)
                                            .build();

                                    // 7. 递归执行下一轮迭代
                                    iteration.incrementAndGet();
                                    if (context.getEventPublisher() != null) {
                                        IterationEndEvent iterationEndEvent = new IterationEndEvent(context, iteration.get(), true, true, true, false, null);
                                        context.getEventPublisher().publishEvent(iterationEndEvent);
                                    }
                                    executeNextIteration(streamingModel, nextRequest, context, iteration, maxIterations);
                                })
                                .exceptionally(ex -> {
                                    log.error("处理工具调用失败", ex);

                                    // 发布工具调用后
                                    if (context.getEventPublisher() != null) {
                                        AfterToolCallEvent afterToolCallEvent = new AfterToolCallEvent(context, iteration.get(), true, null, ex);
                                        context.getEventPublisher().publishEvent(afterToolCallEvent);
                                    }

                                    if (context.getEventPublisher() != null) {
                                        IterationEndEvent iterationEndEvent = new IterationEndEvent(context, iteration.get(), true, false, false, false, ex);
                                        context.getEventPublisher().publishEvent(iterationEndEvent);
                                    }
                                    return null;
                                });
                    } else {
                        // 没有工具调用，对话完成
                        log.info("ReAct 循环完成: iterations={}", iteration.get());
                        addMessageWithEvent(context, iteration.get(), aiMessage);

                        // 发布迭代结束事件
                        if (context.getEventPublisher() != null) {
                            IterationEndEvent iterationEndEvent = new IterationEndEvent(context, iteration.get(), false, false, true, false, null);
                            context.getEventPublisher().publishEvent(iterationEndEvent);
                        }
                    }
                })
                .exceptionally(ex -> {
                    log.error("LLM 调用失败", ex);
                    // 发布迭代结束事件
                    if (context.getEventPublisher() != null) {
                        IterationEndEvent iterationEndEvent = new IterationEndEvent(context, iteration.get(), false, false, false, false, ex);
                        context.getEventPublisher().publishEvent(iterationEndEvent);
                    }
                    return null;
                });
    }

    /**
     * 异步调用 LLM 流式输出
     */
    private CompletableFuture<ChatResponse> callLlmStreamingAsync(
            StreamingChatModel streamingModel,
            ChatRequest chatRequest,
            AgentExecutionContext context,
            int iteration
    ) {
        CompletableFuture<ChatResponse> future = new CompletableFuture<>();

        streamingModel.chat(chatRequest, new StreamingChatResponseHandler() {
            @Override
            public void onPartialResponse(String partialResponse) {
                // 检查是否被中断
                if (context.isInterrupted()) {
                    log.info("流式输出被中断");
                    throw new BusinessException(new InterruptedException("迭代被手动中断"));
                }

                // 发布流式输出事件
                if (partialResponse != null && context.getEventPublisher() != null) {
                    StreamEvent streamEvent = new StreamEvent(context, iteration, partialResponse);
                    context.getEventPublisher().publishEvent(streamEvent);
                }
            }


            @Override
            public void onCompleteResponse(ChatResponse completeResponse) {
                log.debug("LLM 流式调用完成");
                // 发布流式输出完成事件
                if (context.getEventPublisher() != null) {
                    StreamCompleteEvent completeEvent = new StreamCompleteEvent(context, completeResponse);
                    context.getEventPublisher().publishEvent(completeEvent);
                }
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
            List<ToolExecutionRequest> toolExecutionRequests,
            AgentExecutionContext context,
            int iteration
    ) {
        // 创建所有工具调用的 Future 列表
        List<CompletableFuture<ToolExecutionResultMessage>> toolFutures = new ArrayList<>();

        for (ToolExecutionRequest request : toolExecutionRequests) {
            // 检查是否被中断
            if (context.isInterrupted()) {
                log.info("工具调用被中断");
                CompletableFuture<ToolExecutionResultMessage> interruptedFuture = new CompletableFuture<>();
                interruptedFuture.completeExceptionally(new InterruptedException("迭代被手动中断"));
                toolFutures.add(interruptedFuture);
                continue;
            }

            String toolName = request.name();
            String toolArgs = request.arguments();
            String toolCallId = request.id();

            log.info("处理工具调用: toolName={}, toolCallId={}", toolName, toolCallId);

            // 发布工具调用前事件
            if (context.getEventPublisher() != null) {
                BeforeToolCallEvent beforeToolCallEvent = new BeforeToolCallEvent(context, iteration, request);
                context.getEventPublisher().publishEvent(beforeToolCallEvent);
            }

            // 创建异步等待回调
            CompletableFuture<AgentCallToolResult> responseFuture = toolCallWaitManager.createWait(
                    toolCallId,
                    toolName
            );

            // 将响应 Future 转换为结果消息 Future
            CompletableFuture<ToolExecutionResultMessage> resultFuture = responseFuture
                    .handle((responseData, ex) -> {
                        if (ex != null) {
                            log.error("等待工具调用响应失败: toolName={}", toolName, ex);
                            responseData = AgentCallToolResult.builder()
                                    .toolCallId(toolCallId)
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

            // 发布工具调用通知事件
            if (context.getEventPublisher() != null) {
                ToolCallNotifyEvent notifyEvent = new ToolCallNotifyEvent(
                        context, iteration, toolName, toolArgs, toolCallId
                );
                context.getEventPublisher().publishEvent(notifyEvent);
            }
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
    private String buildToolResultText(AgentCallToolResult responseData) {
        if (!Boolean.TRUE.equals(responseData.getIsAllow())) {
            return "工具调用被拒绝";
        }

        if (!Boolean.TRUE.equals(responseData.getSuccess())) {
            return "工具执行失败: " + responseData.getError() + (StringUtils.isNotBlank(responseData.getResult()) ? "\n" + responseData.getResult() : "");
        }

        return responseData.getResult();
    }

    /**
     * 添加消息并发布事件
     */
    private void addMessageWithEvent(AgentExecutionContext context,
                                     int iteration, ChatMessage message) {
        AgentCallback callback = context.getAgentCallback();

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
                    // 发布消息添加后事件（成功）
                    if (context.getEventPublisher() != null) {
                        AfterMessageAddEvent afterEvent = new AfterMessageAddEvent(context, iteration, finalMessage, true);
                        context.getEventPublisher().publishEvent(afterEvent);
                    }
                },
                msg -> {
                    // 发布消息添加后事件（失败）
                    if (context.getEventPublisher() != null) {
                        AfterMessageAddEvent afterEvent = new AfterMessageAddEvent(context, iteration, finalMessage, false);
                        context.getEventPublisher().publishEvent(afterEvent);
                    }
                }
        );
    }
}
