package org.joker.comfypilot.model.infrastructure.executor;

import lombok.extern.slf4j.Slf4j;
import org.joker.comfypilot.model.domain.entity.AiModel;
import org.joker.comfypilot.model.domain.repository.ModelApiKeyRepository;
import org.joker.comfypilot.model.domain.repository.ModelProviderRepository;

import java.util.HashMap;
import java.util.Map;

/**
 * 自定义 OpenAI 执行器示例
 * 演示如何通过重写方法来自定义 URL、API Key 和参数
 *
 * 使用场景：
 * 1. 使用固定的 API Key（如从配置文件读取）
 * 2. 使用自定义的 API Base URL（如代理地址）
 * 3. 添加默认参数或参数转换逻辑
 */
@Slf4j
// @Component  // 如果需要使用此实现，取消注释
public class CustomOpenAiExecutor extends OpenAiExecutor {

    public CustomOpenAiExecutor(ModelProviderRepository providerRepository,
                                ModelApiKeyRepository apiKeyRepository) {
        super(providerRepository, apiKeyRepository);
    }

    /**
     * 重写：使用自定义的 API Base URL
     * 例如：使用代理地址或内部转发地址
     */
    @Override
    protected String getApiBaseUrl(AiModel model) {
        // 示例：使用固定的代理地址
        // return "https://api.openai-proxy.com/v1";

        // 或者根据模型动态选择
        // if (model.getModelIdentifier().startsWith("gpt-4")) {
        //     return "https://api.openai-gpt4.com/v1";
        // }

        // 默认使用父类实现
        return super.getApiBaseUrl(model);
    }

    /**
     * 重写：使用自定义的 API Key
     * 例如：从配置文件、环境变量或密钥管理服务获取
     */
    @Override
    protected String getApiKey(AiModel model) {
        // 示例：从环境变量获取
        // String apiKey = System.getenv("OPENAI_API_KEY");
        // if (apiKey != null && !apiKey.isEmpty()) {
        //     return apiKey;
        // }

        // 示例：从配置文件获取
        // return configService.getOpenAiApiKey();

        // 默认使用父类实现（从数据库获取）
        return super.getApiKey(model);
    }

    /**
     * 重写：自定义模型参数
     * 例如：添加默认参数、参数转换、参数校验等
     */
    @Override
    protected Map<String, Object> getModelParameters(AiModel model, Map<String, Object> requestParameters) {
        Map<String, Object> params = new HashMap<>(requestParameters);

        // 示例：添加默认参数
        params.putIfAbsent("temperature", 0.7);
        params.putIfAbsent("max_tokens", 2000);

        // 示例：参数转换
        if (params.containsKey("max_length")) {
            params.put("max_tokens", params.remove("max_length"));
        }

        // 示例：参数校验和限制
        if (params.containsKey("temperature")) {
            double temp = ((Number) params.get("temperature")).doubleValue();
            if (temp < 0 || temp > 2) {
                log.warn("temperature 超出范围 [0, 2]，使用默认值 0.7");
                params.put("temperature", 0.7);
            }
        }

        return params;
    }
}
