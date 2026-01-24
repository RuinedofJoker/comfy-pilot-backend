package org.joker.comfypilot.agent.aiservice;

import com.alibaba.fastjson2.JSON;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.data.message.*;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.memory.chat.TokenWindowChatMemory;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiTokenCountEstimator;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import dev.langchain4j.store.memory.chat.InMemoryChatMemoryStore;
import org.joker.comfypilot.BaseTest;
import org.joker.comfypilot.common.domain.message.PersistableChatMessage;
import org.joker.comfypilot.common.util.FileContentUtil;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.List;

public class MultimodalAiServiceTest extends BaseTest {

    @Test
    public void test() throws Exception {
        String DEEPSEEK_API_KEY = System.getProperty("DEEPSEEK_API_KEY");
        String QWEN_API_KEY = System.getProperty("QWEN_API_KEY");
        OpenAiChatModel model = OpenAiChatModel.builder()
                .baseUrl("https://dashscope.aliyuncs.com/compatible-mode/v1")
                .apiKey(QWEN_API_KEY)
                .modelName("qwen-vl-max")
                .build();
/*        OpenAiChatModel model = OpenAiChatModel.builder()
                .baseUrl("https://api.deepseek.com")
                .apiKey(DEEPSEEK_API_KEY)
                .modelName("deepseek-chat")
                .build();*/

        ChatMemoryStore chatMemoryStore = new InMemoryChatMemoryStore();
        ChatMemory chatMemory = MessageWindowChatMemory.builder()
                .id("12345")
                .maxMessages(100)
                .chatMemoryStore(chatMemoryStore)
                .build();

        System.out.println(FileContentUtil.getFileSize("C:\\Users\\61640\\Desktop\\35507798632-1-192.mp4"));

        UserMessage userMessage = UserMessage.from(List.of(
                TextContent.from("帮我生成一张和这个图片风格一样的图片"),
//                AudioContent.from(FileContentUtil.toBase64("C:\\Users\\61640\\Desktop\\mlk.flac"), FileContentUtil.getMimeType("C:\\Users\\61640\\Desktop\\mlk.flac"))
//                ImageContent.from("https://ir78450cc343.vicp.fun/微信图片_20260103154248_7584_34.jpg")
//                VideoContent.from("https://ir78450cc343.vicp.fun/35507798632-1-192.mp4")
                VideoContent.from(FileContentUtil.toBase64("C:\\Users\\61640\\Desktop\\35507798632-1-192.mp4"), FileContentUtil.getMimeType("C:\\Users\\61640\\Desktop\\35507798632-1-192.mp4"))
//                ImageContent.from("https://cdn.pixabay.com/photo/2020/04/13/19/40/sun-5039871_1280.jpg")
//                ImageContent.from(FileContentUtil.toBase64("C:\\Users\\61640\\Desktop\\微信图片_20260103154248_7584_34.jpg"), "image/jpeg")

        ));
        chatMemory.add(userMessage);

        ChatRequest chatRequest = ChatRequest.builder()
                .messages(chatMemory.messages())
                .build();

        ChatResponse chatResponse = model.chat(chatRequest);
        chatMemory.add(chatResponse.aiMessage());
        System.out.println(chatResponse.aiMessage().text());

        System.out.println();
    }

    @Test
    public void testJson() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();

        // 1. 演示 UserMessage 的序列化和反序列化
        System.out.println("========== 1. UserMessage 序列化演示 ==========");
        UserMessage userMessage = UserMessage.from(List.of(
                TextContent.from("帮我总结一下这个音频的内容"),
                AudioContent.from(FileContentUtil.toBase64("C:\\Users\\61640\\Desktop\\mlk.flac"), FileContentUtil.getMimeType("C:\\Users\\61640\\Desktop\\mlk.flac")),
                ImageContent.from("https://ir78450cc343.vicp.fun/微信图片_20260103154248_7584_34.jpg"),
                VideoContent.from("https://ir78450cc343.vicp.fun/35507798632-1-192.mp4"),
                ImageContent.from(FileContentUtil.toBase64("C:\\Users\\61640\\Desktop\\微信图片_20260103154248_7584_34.jpg"), "image/jpeg")
        ));

        // 转换为 PersistableChatMessage
        PersistableChatMessage persistableMsg =
            PersistableChatMessage.from(userMessage);

        // 序列化为 JSON
        String json = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(persistableMsg);
        System.out.println("序列化结果：");
        System.out.println(json);
        System.out.println();

        // 反序列化
        PersistableChatMessage restored =
            mapper.readValue(json, PersistableChatMessage.class);
        System.out.println("反序列化成功，类型：" + restored.getClass().getSimpleName());
        System.out.println();
    }

}
