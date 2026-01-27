package org.joker.comfypilot.model.domain.model;

import dev.langchain4j.model.chat.ChatModel;
import org.joker.comfypilot.common.util.SpringContextUtil;
import org.joker.comfypilot.model.domain.entity.AiModel;
import org.joker.comfypilot.model.domain.enums.ModelCallingType;
import org.joker.comfypilot.model.domain.service.AbstractModelTemplate;
import org.joker.comfypilot.model.domain.service.ChatModelFactory;
import org.joker.comfypilot.model.domain.service.ModelTemplate;

import java.util.LinkedHashMap;
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
        Map<String, Object> parameters = new LinkedHashMap<>();

        parameters.put("apiKey", "");
        parameters.put("$apiKey", "第三方apiKey");
        parameters.put("temperature", 0.7);
        parameters.put("$temperature", "模型温度");
        parameters.put("maxTokens", 1024);
        parameters.put("$maxTokens", "上下文最大Token数");
        parameters.put("maxMessages", 200);
        parameters.put("$maxMessages", "上下文最大消息数");
        parameters.put("topP", 0.9);
        parameters.put("$topP", "Top-P");
        parameters.put("timeout", 200);
        parameters.put("$timeout", "超时时间");

        parameters.put("supportImageMultimodal", false);
        parameters.put("$supportImageMultimodal", "模型是否支持图片多模态");
        parameters.put("supportVideoMultimodal", false);
        parameters.put("$supportVideoMultimodal", "模型是否支持图片多模态");
        parameters.put("supportAudioMultimodal", false);
        parameters.put("$supportAudioMultimodal", "模型是否支持音频多模态");
        parameters.put("supportPdfFileMultimodal", false);
        parameters.put("$supportPdfFileMultimodal", "模型是否支持PDF文件多模态");
        return parameters;
    }

    private void ensureInit() {
        if (chatModel == null) {
            synchronized (this) {
                if (chatModel == null) {
                    SpringContextUtil.getBean(ChatModelFactory.class).createChatModel(getAiModel().getModelIdentifier(), getAiModel().getModelConfig());
                }
            }
        }
    }

    public String chat(String userMessage) {
        ensureInit();
        return chatModel.chat(userMessage);
    }

}
