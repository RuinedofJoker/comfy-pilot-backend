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

    private volatile ChatModel chatModel;

    public ApiLLMModelTemplate(AiModel aiModel) {
        super(aiModel);
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

    private void init() {
        if (chatModel == null) {
            synchronized (this) {
                if (chatModel == null) {
                    SpringContextUtil.getBean(ChatModelFactory.class).createChatModel(getAiModel().getModelIdentifier(), getAiModel().getModelConfig());
                }
            }
        }
    }

    public String chat(String userMessage) {
        init();
        return chatModel.chat(userMessage);
    }

}
