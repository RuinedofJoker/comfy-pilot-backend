package org.joker.comfypilot.agent.aiservice;

import dev.langchain4j.agent.tool.ToolExecutionRequest;
import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.agent.tool.ToolSpecifications;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.internal.JsonSchemaElementUtils;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.memory.chat.TokenWindowChatMemory;
import dev.langchain4j.model.chat.request.json.JsonObjectSchema;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiTokenCountEstimator;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import dev.langchain4j.store.memory.chat.InMemoryChatMemoryStore;
import org.joker.comfypilot.BaseTest;
import org.joker.comfypilot.agent.Response;
import org.joker.comfypilot.agent.Tools;
import org.junit.jupiter.api.Test;

import java.util.List;

public class AiServiceTest extends BaseTest {

    @Test
    public void testAiService() {
        String DEEPSEEK_API_KEY = System.getProperty("DEEPSEEK_API_KEY");
        JsonObjectSchema rootSchemaElement = (JsonObjectSchema) JsonSchemaElementUtils.jsonSchemaElementFrom(Response.class);
        OpenAiChatModel model = OpenAiChatModel.builder()
                .baseUrl("https://api.deepseek.com")
                .apiKey(DEEPSEEK_API_KEY)
                .modelName("deepseek-chat")
                .build();

        OpenAiTokenCountEstimator tokenCountEstimator = new OpenAiTokenCountEstimator("gpt-4");


        ChatMemoryStore chatMemoryStore = new InMemoryChatMemoryStore();
        TokenWindowChatMemory chatMemory = TokenWindowChatMemory.builder()
                .id("12345")
                .chatMemoryStore(chatMemoryStore)
                .maxTokens(200_000, tokenCountEstimator)
                .build();
        System.out.println(chatMemoryStore.getMessages("12345").size());

        /*ChatMemory chatMemory = MessageWindowChatMemory.builder()
                .id("12345")
                .maxMessages(100)
                .chatMemoryStore(chatMemoryStore)
                .build();*/

        Assistant assistant = AiServices.builder(Assistant.class)
                .chatModel(model)
                .chatMemory(chatMemory)
                .tools(new Tools())
                .build();

        System.out.println(assistant.chat("你是谁？"));

        System.out.println(assistant.chat("帮我同时用特殊的加法法则计算1+1等于几和2+2等于几"));

        System.out.println(assistant.chat("你发现这个特殊的加法法则规律了吗"));

        System.out.println(chatMemoryStore.getMessages("12345").size());

        System.out.println();

        AiMessage.builder()
                .text("xxx")
                .toolExecutionRequests(List.of(
                        ToolExecutionRequest.builder()
                                .id("xxx")
                                .name("xxx")
                                .arguments("xxx")
                                .build()
                ));

    }

}
