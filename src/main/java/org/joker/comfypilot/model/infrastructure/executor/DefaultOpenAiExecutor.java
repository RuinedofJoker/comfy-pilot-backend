package org.joker.comfypilot.model.infrastructure.executor;

import lombok.extern.slf4j.Slf4j;
import org.joker.comfypilot.model.domain.repository.ModelProviderRepository;
import org.springframework.stereotype.Component;

/**
 * 默认 OpenAI 执行器实现
 * 直接使用父类的默认实现，无需重写任何方法
 * API Key 从 model_config JSON 中获取
 */
@Slf4j
@Component
public class DefaultOpenAiExecutor extends OpenAiExecutor {

    public DefaultOpenAiExecutor(ModelProviderRepository providerRepository) {
        super(providerRepository);
    }

    // 使用父类的默认实现：
    // - getApiBaseUrl(): 从 ModelProvider 获取
    // - getApiKey(): 从 model_config JSON 中获取 apiKey 字段
    // - getModelParameters(): 直接返回请求参数
}
