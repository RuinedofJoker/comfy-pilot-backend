package org.joker.comfypilot.agent;

import dev.langchain4j.agent.tool.ToolExecutionRequest;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ToolExecutionResultMessage;
import dev.langchain4j.data.message.UserMessage;

import java.util.List;

public class TestCreateMessage {

    public void test() {
        UserMessage userMessage = UserMessage.from("一条空的用户消息");

        AiMessage aiMessage1 = AiMessage.from("一条空的AI消息");

        AiMessage aiMessage2 = AiMessage.from("一条空的AI消息", List.of(
                ToolExecutionRequest.builder()
                        .id("111")
                        .name("a_tool")
                        .arguments("{}")
                        .build()
        ));

        if (aiMessage2.hasToolExecutionRequests()) {
            List<ToolExecutionRequest> toolExecutionRequests = aiMessage2.toolExecutionRequests();
            for (ToolExecutionRequest toolExecutionRequest : toolExecutionRequests) {
                ToolExecutionResultMessage toolExecutionResultMessage = ToolExecutionResultMessage.from(toolExecutionRequest.id(), toolExecutionRequest.name(), "执行结果A");
            }
        }


    }

}
