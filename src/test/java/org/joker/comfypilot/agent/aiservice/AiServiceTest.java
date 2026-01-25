package org.joker.comfypilot.agent.aiservice;

import com.fasterxml.jackson.core.JsonProcessingException;
import dev.langchain4j.agent.tool.ToolExecutionRequest;
import dev.langchain4j.memory.chat.TokenWindowChatMemory;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiTokenCountEstimator;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import dev.langchain4j.store.memory.chat.InMemoryChatMemoryStore;
import org.joker.comfypilot.BaseTest;
import org.joker.comfypilot.agent.Tools;
import org.junit.jupiter.api.Test;

public class AiServiceTest extends BaseTest {

    @Test
    public void testAiService() {
        String DEEPSEEK_API_KEY = System.getProperty("DEEPSEEK_API_KEY");
        OpenAiChatModel model = OpenAiChatModel.builder()
                .baseUrl("https://api.deepseek.com")
                .apiKey(DEEPSEEK_API_KEY)
                .modelName("deepseek-chat")
                .sendThinking(true)
                .returnThinking(true)
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

//        System.out.println(assistant.chat("你发现这个特殊的加法法则规律了吗"));

        System.out.println(chatMemoryStore.getMessages("12345").size());

        System.out.println();

        /*AiMessage aiMessage = AiMessage.builder()
                .text("xxx")
                .toolExecutionRequests(List.of(
                        ToolExecutionRequest.builder()
                                .id("xxx")
                                .name("xxx")
                                .arguments("xxx")
                                .build()
                ))
                .build();
        ToolExecutionResultMessage toolExecutionResultMessage = ToolExecutionResultMessage.from(aiMessage.toolExecutionRequests().get(0), "xxx");*/

    }

}
