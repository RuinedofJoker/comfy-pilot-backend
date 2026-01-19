package org.joker.comfypilot.model.domain.model;

import dev.langchain4j.model.chat.ChatModel;
import org.joker.comfypilot.common.util.SpringContextUtil;
import org.joker.comfypilot.model.domain.entity.AiModel;
import org.joker.comfypilot.model.domain.enums.ModelCallingType;
import org.joker.comfypilot.model.domain.service.AbstractModelTemplate;
import org.joker.comfypilot.model.domain.service.ChatModelFactory;
import org.joker.comfypilot.model.domain.service.ModelTemplate;

import java.util.Map;

public class ApiLLMModelTemplate extends AbstractModelTemplate implements ModelTemplate {

    private ChatModel chatModel;

    public ApiLLMModelTemplate(AiModel aiModel) {
        super(aiModel);
        SpringContextUtil.getBean(ChatModelFactory.class).createChatModel(aiModel.getModelIdentifier(), aiModel.getModelConfig());
    }

    @Override
    public ModelCallingType callingType() {
        return ModelCallingType.API_LLM;
    }

    @Override
    public Map<String, Object> configFormat() {
        return Map.of(
                "apiKey", "第三方apiKey，可选，没填用模型上的",
                "temperature", "模型温度",
                "maxTokens", "最大token数",
                "topP", "",
                "timeout", "超时时间"
        );
    }

    public String chat(String userMessage) {
        return chatModel.chat(userMessage);
    }

}
