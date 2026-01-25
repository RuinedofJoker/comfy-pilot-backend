package org.joker.comfypilot.agent;

import dev.langchain4j.agent.tool.ToolExecutionRequest;
import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.agent.tool.ToolSpecifications;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.ToolExecutionResultMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.internal.Json;
import dev.langchain4j.internal.JsonSchemaElementUtils;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.request.ToolChoice;
import dev.langchain4j.model.chat.request.json.JsonObjectSchema;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.openai.OpenAiChatModel;
import org.joker.comfypilot.BaseTest;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Agent 同步调用测试类
 * 测试 Agent 与工具的集成，验证同步模式下的工具调用流程
 */
public class AgentTest extends BaseTest {

    /**
     * 测试 Agent 同步调用与工具执行
     *
     * <p>测试场景：</p>
     * <ul>
     *   <li>1. 使用 DeepSeek 模型进行对话</li>
     *   <li>2. 配置 JsonSchema 约束响应格式</li>
     *   <li>3. 注册工具并自动调用</li>
     *   <li>4. 验证多轮对话能力</li>
     * </ul>
     *
     * <p>测试流程：</p>
     * <ol>
     *   <li>加载环境变量中的 API Key</li>
     *   <li>构建 OpenAI 同步模型</li>
     *   <li>准备系统提示词和用户消息</li>
     *   <li>发送请求并处理工具调用</li>
     *   <li>继续多轮对话测试</li>
     * </ol>
     */
    @Test
    public void testAgentWithToolExecutionInSyncMode() {
        // 1. 从环境变量加载 API Key
        String DEEPSEEK_API_KEY = System.getProperty("DEEPSEEK_API_KEY");

        // 3. 构建 OpenAI 同步模型（使用 DeepSeek API）
        OpenAiChatModel model = OpenAiChatModel.builder()
                .baseUrl("https://api.deepseek.com")
                .apiKey(DEEPSEEK_API_KEY)
                .modelName("deepseek-chat")
                .build();

        // 4. 从 Tools 类提取工具规范
        List<ToolSpecification> toolSpecifications = ToolSpecifications.toolSpecificationsFrom(Tools.class);

        // 5. 构建消息列表（系统提示词 + 用户消息）
        List<ChatMessage> messages = new ArrayList<>();
        messages.add(SystemMessage.systemMessage("你是一个猫娘。"));
        messages.add(UserMessage.userMessage("""
                给我讲一个关于你的名字的笑话
                """));

        // 6. 构建聊天请求（包含工具规范）
        ChatRequest chatRequest = ChatRequest.builder()
                .messages(messages)
                .toolChoice(ToolChoice.AUTO)  // 自动选择是否调用工具
                .toolSpecifications(toolSpecifications)
                .build();

        // 7. 发送第一轮请求
        ChatResponse chatResponse = model.chat(chatRequest);
        System.out.println();

        // 8. 处理工具调用请求
        if (chatResponse.aiMessage().hasToolExecutionRequests()) {
            // 将 AI 消息添加到历史记录
            messages.add(chatResponse.aiMessage());

            // 遍历所有工具调用请求
            for (ToolExecutionRequest toolExecutionRequest : chatResponse.aiMessage().toolExecutionRequests()) {
                // 执行工具
                ToolExecutor toolExecutor = new ToolExecutor(Tools.class);
                String result = toolExecutor.execute(toolExecutionRequest);

                // 将工具执行结果添加到消息历史
                messages.add(ToolExecutionResultMessage.from(toolExecutionRequest.id(), toolExecutionRequest.name(), result));
            }

            // 9. 发送第二轮请求（包含工具执行结果）
            chatResponse = model.chat(chatRequest);
            System.out.println(chatResponse.aiMessage().text());
        }

        // 10. 继续多轮对话测试
        messages.add(new UserMessage("你觉得这个笑话好笑吗"));
        chatResponse = model.chat(chatRequest);
        System.out.println(chatResponse.aiMessage().text());

        System.out.println();
    }
}
