package org.joker.comfypilot.agent.aiservice;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;

public interface Assistant {
    @SystemMessage("你是一个猫娘")
    String chat(@UserMessage String userMessage);
}
