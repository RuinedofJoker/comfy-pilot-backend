package org.joker.comfypilot.model.infrastructure.executor;

import lombok.extern.slf4j.Slf4j;
import org.joker.comfypilot.model.domain.repository.ModelApiKeyRepository;
import org.joker.comfypilot.model.domain.repository.ModelProviderRepository;
import org.springframework.stereotype.Component;

/**
 * 默认 OpenAI 执行器实现
 * 直接使用父类的默认实现，无需重写任何方法
 */
@Slf4j
@Component
public class DefaultOpenAiExecutor extends OpenAiExecutor {

    public DefaultOpenAiExecutor(ModelProviderRepository providerRepository,
                                 ModelApiKeyRepository apiKeyRepository) {
        super(providerRepository, apiKeyRepository);
    }

    // 使用父类的默认实现：
    // - getApiBaseUrl(): 从 ModelProvider 获取
    // - getApiKey(): 从 ModelApiKey 获取第一个启用的密钥
    // - getModelParameters(): 直接返回请求参数
}
