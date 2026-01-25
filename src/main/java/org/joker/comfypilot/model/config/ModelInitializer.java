package org.joker.comfypilot.model.config;

import lombok.extern.slf4j.Slf4j;
import org.joker.comfypilot.model.domain.entity.AiModel;
import org.joker.comfypilot.model.domain.enums.ModelAccessType;
import org.joker.comfypilot.model.domain.enums.ModelCallingType;
import org.joker.comfypilot.model.domain.enums.ModelType;
import org.joker.comfypilot.model.domain.enums.ProviderType;
import org.joker.comfypilot.model.domain.repository.AiModelRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Slf4j
@Component
@DependsOn("embeddedDatabaseConfig")
public class ModelInitializer implements CommandLineRunner {

    @Autowired
    private AiModelRepository aiModelRepository;

    @Override
    public void run(String... args) throws Exception {
        log.info("开始初始化模型...");

        initApiLLM();
    }

    private void initApiLLM() {
        List<List<String>> models = new ArrayList<>();
        models.add(List.of("deepseek-chat", "https://api.deepseek.com", ""));
        models.add(List.of("qwen-vl-max", "https://dashscope.aliyuncs.com/compatible-mode/v1", ""));

        for (List<String> model : models) {
            String modelName = model.get(0);
            String apiBaseUrl = model.get(1);
            String description = model.get(2);

            AiModel dbModel = aiModelRepository.findByModelIdentifier(modelName).orElse(null);
            if (dbModel != null) {
                log.info("模型" + modelName + "已加载");
                continue;
            }

            dbModel = initApiLLMModel(modelName, apiBaseUrl, description);
            aiModelRepository.save(dbModel);

            log.info("加载模型" + modelName + "完成...");
        }
    }

    private AiModel initApiLLMModel(String modelName, String apiBaseUrl, String description) {
        return AiModel.builder()
                .modelIdentifier(modelName)
                .modelName(modelName)
                .accessType(ModelAccessType.REMOTE_API)
                .modelType(ModelType.LLM)
                .modelCallingType(ModelCallingType.API_LLM)
                .providerType(ProviderType.OPENAI)
                .modelConfig(new HashMap<>())
                .description(description)
                .apiBaseUrl(apiBaseUrl)
                .isEnabled(false)
                .build();
    }

}
