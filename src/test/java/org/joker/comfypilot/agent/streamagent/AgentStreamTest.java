package org.joker.comfypilot.agent.streamagent;

import dev.langchain4j.agent.tool.ToolExecutionRequest;
import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.agent.tool.ToolSpecifications;
import dev.langchain4j.data.message.*;
import dev.langchain4j.internal.Json;
import dev.langchain4j.internal.JsonSchemaElementUtils;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.request.ToolChoice;
import dev.langchain4j.model.chat.request.json.JsonObjectSchema;
import dev.langchain4j.model.chat.response.*;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import org.joker.comfypilot.BaseTest;
import org.joker.comfypilot.agent.Response;
import org.joker.comfypilot.agent.ToolExecutor;
import org.joker.comfypilot.agent.Tools;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.LockSupport;

/**
 * Agent 流式调用测试类
 * 测试 Agent 与工具的集成，验证流式模式下的工具调用流程和异步处理
 */
public class AgentStreamTest extends BaseTest {

    /**
     * 测试 Agent 流式调用与工具执行
     *
     * <p>测试场景：</p>
     * <ul>
     *   <li>1. 使用 DeepSeek 流式模型进行对话</li>
     *   <li>2. 配置 JsonSchema 约束响应格式</li>
     *   <li>3. 注册工具并在流式响应中自动调用</li>
     *   <li>4. 验证多轮对话能力</li>
     *   <li>5. 使用 LockSupport 实现线程同步</li>
     * </ul>
     *
     * <p>测试流程：</p>
     * <ol>
     *   <li>加载环境变量中的 API Key</li>
     *   <li>构建 OpenAI 流式模型</li>
     *   <li>准备系统提示词和用户消息</li>
     *   <li>发送流式请求并处理部分响应</li>
     *   <li>在完整响应中处理工具调用</li>
     *   <li>继续多轮对话测试</li>
     * </ol>
     */
    @Test
    public void testAgentWithToolExecutionInStreamingMode() {
        // 1. 从环境变量加载 API Key
        String DEEPSEEK_API_KEY = System.getProperty("DEEPSEEK_API_KEY");

        // 2. 构建 JsonSchema 约束响应格式
        JsonObjectSchema rootSchemaElement = (JsonObjectSchema) JsonSchemaElementUtils.jsonSchemaElementFrom(Response.class);

        // 3. 构建 OpenAI 流式模型（使用 DeepSeek API）
        OpenAiStreamingChatModel streamingModel = OpenAiStreamingChatModel.builder()
                .baseUrl("https://api.deepseek.com")
                .apiKey(DEEPSEEK_API_KEY)
                .modelName("deepseek-chat")
                .build();

        // 4. 从 Tools 类提取工具规范
        List<ToolSpecification> toolSpecifications = ToolSpecifications.toolSpecificationsFrom(Tools.class);

        // 5. 构建消息列表（系统提示词 + 用户消息）
        List<ChatMessage> messages = new ArrayList<>();
        messages.add(SystemMessage.systemMessage("你是一个猫娘。你现在的返回结果需要严格按照以下JsonSchema返回：\n" + Json.toJson(rootSchemaElement) + "\n"));
        messages.add(UserMessage.userMessage("""
                给我讲一个关于你的名字的笑话
                """));

        // 6. 构建聊天请求（包含工具规范）
        ChatRequest chatRequest = ChatRequest.builder()
                .messages(messages)
                .toolChoice(ToolChoice.AUTO)  // 自动选择是否调用工具
                .toolSpecifications(toolSpecifications)
                .build();

        // 7. 发送第一轮流式请求，并阻塞主线程等待完成
        streamingModel.chat(chatRequest, getHandler(messages, streamingModel, chatRequest));
        LockSupport.park();  // 阻塞主线程，等待异步响应完成

        System.out.println();

        // 8. 继续多轮对话测试
        messages.add(UserMessage.userMessage("""
                你觉得这个笑话好笑吗
                """));
        streamingModel.chat(chatRequest, getHandler(messages, streamingModel, chatRequest));
        LockSupport.park();  // 再次阻塞主线程

        System.out.println();
    }

    /**
     * 创建流式响应处理器
     *
     * <p>处理器功能：</p>
     * <ul>
     *   <li>处理部分响应（流式输出）</li>
     *   <li>处理思考过程（如果模型支持）</li>
     *   <li>处理完整响应并执行工具调用</li>
     *   <li>使用计数器和 LockSupport 实现线程同步</li>
     * </ul>
     *
     * @param messages 消息历史列表
     * @param streamingModel 流式模型实例
     * @param chatRequest 聊天请求对象
     * @return 流式响应处理器
     */
    private static StreamingChatResponseHandler getHandler(List<ChatMessage> messages, OpenAiStreamingChatModel streamingModel, ChatRequest chatRequest) {
        // 保存主线程引用，用于后续唤醒
        Thread mainThread = Thread.currentThread();

        // 使用原子计数器跟踪递归调用深度
        // 初始值为 1，表示当前这一轮调用
        AtomicInteger count = new AtomicInteger(1);

        return new StreamingChatResponseHandler() {

            /**
             * 处理部分响应（流式输出）
             * 每次接收到模型的部分文本时调用
             */
            @Override
            public void onPartialResponse(PartialResponse partialResponse, PartialResponseContext context) {
                System.out.printf(partialResponse.text());
            }

            /**
             * 处理部分思考过程
             * 某些模型（如 DeepSeek）支持输出思考过程
             */
            @Override
            public void onPartialThinking(PartialThinking partialThinking, PartialThinkingContext context) {
                System.out.printf(partialThinking.text());
            }

            /**
             * 处理完整响应
             * 当流式响应完成时调用，处理工具调用和线程同步
             */
            @Override
            public void onCompleteResponse(ChatResponse completeResponse) {
                // 获取 AI 消息
                AiMessage aiMessage = completeResponse.aiMessage();

                // 将 AI 消息添加到历史记录
                messages.add(aiMessage);

                // 检查是否有工具调用请求
                if (aiMessage.hasToolExecutionRequests()) {
                    // 遍历所有工具调用请求
                    for (ToolExecutionRequest toolExecutionRequest : aiMessage.toolExecutionRequests()) {
                        // 执行工具
                        ToolExecutor toolExecutor = new ToolExecutor(Tools.class);
                        String result = toolExecutor.execute(toolExecutionRequest);

                        // 将工具执行结果添加到消息历史
                        messages.add(ToolExecutionResultMessage.from(toolExecutionRequest.id(), toolExecutionRequest.name(), result));
                    }

                    // 递归调用：增加计数器，表示开始新一轮调用
                    count.incrementAndGet();

                    // 递归发送请求（包含工具执行结果）
                    streamingModel.chat(chatRequest, this);
                }

                // 减少计数器，如果为 0 表示所有递归调用都已完成
                if (count.decrementAndGet() == 0) {
                    // 唤醒主线程
                    LockSupport.unpark(mainThread);
                }
            }

            /**
             * 处理错误
             * 当流式响应发生错误时调用，确保主线程被唤醒
             */
            @Override
            public void onError(Throwable error) {
                // 发生错误时也要唤醒主线程，避免主线程永久阻塞
                LockSupport.unpark(mainThread);
            }
        };
    }

}
