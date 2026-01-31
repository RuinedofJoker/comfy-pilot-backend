package org.joker.comfypilot.model.infrastructure.service;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import jakarta.annotation.Nullable;
import lombok.extern.slf4j.Slf4j;
import org.joker.comfypilot.common.exception.BusinessException;
import org.joker.comfypilot.model.domain.entity.AiModel;
import org.joker.comfypilot.model.domain.entity.ModelProvider;
import org.joker.comfypilot.model.domain.enums.ModelCallingType;
import org.joker.comfypilot.model.domain.enums.ProviderType;
import org.joker.comfypilot.model.domain.repository.AiModelRepository;
import org.joker.comfypilot.model.domain.repository.ModelProviderRepository;
import org.joker.comfypilot.model.domain.service.ChatModelFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;

/**
 * ChatModel 工厂实现类
 * <p>
 * 根据模型配置和提供商信息创建对应的 ChatModel 实例。
 * 支持多种模型提供商和接入方式。
 */
@Slf4j
@Service
public class ChatModelFactoryImpl extends AbstractChatModelFactory implements ChatModelFactory {

    @Autowired
    private AiModelRepository aiModelRepository;
    @Autowired
    private ModelProviderRepository modelProviderRepository;

    @Override
    public ChatModel createChatModel(String modelIdentifier, Map<String, Object> agentConfig) {
        // 根据模型标识符查询模型实体
        AiModel model = aiModelRepository.findByModelIdentifier(modelIdentifier)
                .orElseThrow(() -> new BusinessException("模型不存在: " + modelIdentifier));

        if (!ModelCallingType.API_LLM.equals(model.getModelCallingType())) {
            // 只提供远程API接入的LLM模型
            throw new BusinessException("模型标识" + modelIdentifier + "对应模型" + model.getModelName() + "不是远程API调用的LLM模型");
        }

        ModelProvider provider = null;
        if (model.getProviderId() != null) {
            provider = modelProviderRepository.findById(model.getProviderId())
                    .orElseThrow(() -> new BusinessException(
                            "模型提供商不存在: providerId=" + model.getProviderId()));
        }

        log.info("创建 ChatModel: modelIdentifier={}, accessType={}, hasAgentConfig={}",
                model.getModelIdentifier(), model.getAccessType(), agentConfig != null && !agentConfig.isEmpty());

        // 验证模型是否启用
        if (!model.isEnabled()) {
            throw new BusinessException("模型未启用: " + model.getModelIdentifier());
        }

        // 根据是否有模型提供商
        if (provider != null) {
            return createModel(model, provider, agentConfig);
        } else {
            return createModel(model, agentConfig);
        }
    }

    /**
     * 创建模型
     *
     * @param model       模型实体
     * @param provider    提供商实体
     * @param agentConfig Agent配置
     * @return StreamingChatModel 实例
     */
    private ChatModel createModel(AiModel model, ModelProvider provider, Map<String, Object> agentConfig) {
        if (provider == null) {
            throw new BusinessException("远程API接入方式必须指定提供商");
        }

        if (!provider.isEnabled()) {
            throw new BusinessException("提供商未启用: " + provider.getProviderName());
        }

        // 解析模型配置
        ModelConfig config = parseModelConfig(model, provider, model.getModelConfig(), agentConfig);

        // 根据提供协议类型创建对应的模型
        ProviderType providerType = provider.getProviderType();
        log.debug("创建远程API模型: providerType={}, modelIdentifier={}",
                providerType, model.getModelIdentifier());

        return switch (providerType) {
            case OPENAI -> createOpenAiModel(model, provider, config);
            case ANTHROPIC -> throw new BusinessException("暂不支持 Anthropic 提供商");
            default -> throw new BusinessException("暂不支持" + providerType.getName());
        };
    }

    /**
     * 创建 OpenAI 模型
     *
     * @param model    模型实体
     * @param provider 提供商实体
     * @param config   模型配置
     * @return ChatModel 实例
     */
    private ChatModel createOpenAiModel(AiModel model, @Nullable ModelProvider provider, ModelConfig config) {
        log.debug("创建 OpenAI 模型: modelIdentifier={}, apiBaseUrl={}",
                model.getModelName(), Optional.ofNullable(provider).map(ModelProvider::getApiBaseUrl).orElse(config.apiBaseUrl()));

        OpenAiChatModel.OpenAiChatModelBuilder builder = OpenAiChatModel.builder()
                .modelName(model.getModelName());

        // 设置模型 URL（如果提供商配置了使用提供商的）
        if (config.apiBaseUrl() != null && !config.apiBaseUrl().isBlank()) {
            builder.baseUrl(config.apiBaseUrl());
        } else if (provider != null && provider.getApiBaseUrl() != null && !provider.getApiBaseUrl().isBlank()) {
            builder.baseUrl(provider.getApiBaseUrl());
        } else {
            throw new BusinessException("模型" + model.getModelIdentifier() + ":" + model.getModelName() + "没有提供apiBaseUrl");
        }

        if (config.apiKey() != null && !config.apiKey().isBlank()) {
            builder.apiKey(config.apiKey());
        } else if (provider != null && provider.getApiKey() != null && !provider.getApiKey().isBlank()) {
            builder.apiKey(provider.getApiKey());
        }

        // 设置可选参数
        if (config.timeoutSeconds() != null) {
            builder.timeout(Duration.ofSeconds(config.timeoutSeconds()));
        }
        if (config.temperature() != null) {
            builder.temperature(config.temperature());
        }
        if (config.maxTokens() != null) {
            builder.maxTokens(config.maxTokens());
        }
        if (config.topP() != null) {
            builder.topP(config.topP());
        }

        return builder.build();
    }

    /**
     * 创建模型
     *
     * @param model       模型实体
     * @param agentConfig Agent配置
     * @return StreamingChatModel 实例
     */
    private ChatModel createModel(AiModel model, Map<String, Object> agentConfig) {
        log.debug("创建本地模型: modelIdentifier={}", model.getModelIdentifier());

        // 解析模型配置
        ModelConfig config = parseModelConfig(model, null, model.getModelConfig(), agentConfig);

        // 本地模型需要配置本地服务的 URL
        if (config.apiBaseUrl() == null || config.apiBaseUrl().isBlank()) {
            throw new BusinessException("本地接入方式必须在模型配置中指定 apiBaseUrl");
        }

        ProviderType providerType = model.getProviderType();

        return switch (providerType) {
            case null -> throw new BusinessException("当前LLM模型未配置模型提供协议类型");
            case OPENAI -> createOpenAiModel(model, null, config);
            case ANTHROPIC -> throw new BusinessException("暂不支持 Anthropic 提供商");
            default -> throw new BusinessException("暂不支持" + providerType.getName());
        };
    }
}
