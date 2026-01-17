package org.joker.comfypilot.model.infrastructure.executor;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import dev.langchain4j.model.output.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.joker.comfypilot.common.exception.BusinessException;
import org.joker.comfypilot.model.domain.entity.AiModel;
import org.joker.comfypilot.model.domain.entity.ModelProvider;
import org.joker.comfypilot.model.domain.enums.ModelCapability;
import org.joker.comfypilot.model.domain.repository.ModelProviderRepository;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * OpenAI 执行器抽象类
 * 使用 langchain4j 实现 OpenAI API 调用
 * 从 model_config JSON 中获取 API Key
 */
@Slf4j
@RequiredArgsConstructor
public abstract class OpenAiExecutor extends RemoteApiExecutor {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private final ModelProviderRepository providerRepository;

    @Override
    protected Map<String, Object> doExecute(AiModel model, ModelCapability capability,
                                            Map<String, Object> parameters) {
        log.info("调用 OpenAI API: model={}, capability={}",
                 model.getModelIdentifier(), capability);

        if (capability == ModelCapability.TEXT_GENERATION) {
            return executeTextGeneration(model, parameters);
        } else if (capability == ModelCapability.EMBEDDING) {
            return executeEmbedding(model, parameters);
        } else {
            throw new BusinessException("不支持的能力类型: " + capability);
        }
    }

    /**
     * 执行文本生成
     */
    private Map<String, Object> executeTextGeneration(AiModel model, Map<String, Object> parameters) {
        String prompt = (String) parameters.get("prompt");
        if (prompt == null || prompt.isEmpty()) {
            throw new BusinessException("prompt 参数不能为空");
        }

        // 构建 OpenAiChatModel
        OpenAiChatModel chatModel = buildChatModel(model, parameters);

        ChatRequest request = ChatRequest.builder()
                .messages(UserMessage.from(prompt))
                .build();
        log.debug("ChatRequest:\n{}", request);

        // 调用模型
        ChatResponse response = chatModel.chat(request);
        log.debug("ChatResponse:\n{}", response);

        // 构建返回结果
        Map<String, Object> result = new HashMap<>();
        result.put("text", response.aiMessage().text());
        result.put("finish_reason", "stop");
        result.put("model", model.getModelIdentifier());

        // 添加 token 使用信息（如果有）
        if (response.tokenUsage() != null) {
            result.put("input_tokens", response.tokenUsage().inputTokenCount());
            result.put("output_tokens", response.tokenUsage().outputTokenCount());
            result.put("total_tokens", response.tokenUsage().totalTokenCount());
        }

        return result;
    }

    /**
     * 执行向量生成
     */
    private Map<String, Object> executeEmbedding(AiModel model, Map<String, Object> parameters) {
        String text = (String) parameters.get("text");
        if (text == null || text.isEmpty()) {
            throw new BusinessException("text 参数不能为空");
        }

        // 构建 EmbeddingModel
        EmbeddingModel embeddingModel = buildEmbeddingModel(model, parameters);

        // 调用模型
        Response<Embedding> response = embeddingModel.embed(text);

        // 构建返回结果
        Map<String, Object> result = new HashMap<>();
        result.put("vector", response.content().vector());
        result.put("dimension", response.content().dimension());
        result.put("model", model.getModelIdentifier());

        return result;
    }

    /**
     * 构建 OpenAiChatModel
     */
    private OpenAiChatModel buildChatModel(AiModel model, Map<String, Object> parameters) {
        String apiBaseUrl = getApiBaseUrl(model);
        String apiKey = getApiKey(model);
        Map<String, Object> modelParams = getModelParameters(model, parameters);

        OpenAiChatModel.OpenAiChatModelBuilder builder = OpenAiChatModel.builder()
                .baseUrl(apiBaseUrl)
                .apiKey(apiKey)
                .modelName(model.getModelIdentifier())
                .timeout(Duration.ofSeconds(60));

        // 应用模型参数
        if (modelParams.containsKey("temperature")) {
            builder.temperature(((Number) modelParams.get("temperature")).doubleValue());
        }
        if (modelParams.containsKey("max_tokens")) {
            builder.maxTokens(((Number) modelParams.get("max_tokens")).intValue());
        }
        if (modelParams.containsKey("top_p")) {
            builder.topP(((Number) modelParams.get("top_p")).doubleValue());
        }

        return builder.build();
    }

    /**
     * 构建 EmbeddingModel
     */
    private EmbeddingModel buildEmbeddingModel(AiModel model, Map<String, Object> parameters) {
        String apiBaseUrl = getApiBaseUrl(model);
        String apiKey = getApiKey(model);

        return OpenAiEmbeddingModel.builder()
                .baseUrl(apiBaseUrl)
                .apiKey(apiKey)
                .modelName(model.getModelIdentifier())
                .timeout(Duration.ofSeconds(60))
                .build();
    }

    /**
     * 获取 API Base URL（子类可重写）
     * 默认从 ModelProvider 中获取
     */
    protected String getApiBaseUrl(AiModel model) {
        ModelProvider provider = providerRepository.findById(model.getProviderId())
                .orElseThrow(() -> new BusinessException("提供商不存在: " + model.getProviderId()));

        String baseUrl = provider.getApiBaseUrl();
        if (baseUrl == null || baseUrl.isEmpty()) {
            throw new BusinessException("提供商未配置 API Base URL");
        }

        return baseUrl;
    }

    /**
     * 获取 API Key（子类可重写）
     * 从 model_config JSON 中获取 apiKey 字段
     */
    protected String getApiKey(AiModel model) {
        try {
            String modelConfig = model.getModelConfig();
            if (modelConfig == null || modelConfig.isBlank()) {
                throw new BusinessException("模型配置为空");
            }

            JsonNode root = OBJECT_MAPPER.readTree(modelConfig);
            JsonNode apiKeyNode = root.get("apiKey");

            if (apiKeyNode == null || apiKeyNode.isNull()) {
                throw new BusinessException("模型配置中未找到 apiKey");
            }

            String apiKey = apiKeyNode.asText();
            if (apiKey == null || apiKey.isBlank()) {
                throw new BusinessException("模型配置中的 apiKey 为空");
            }

            return apiKey;
        } catch (Exception e) {
            log.error("解析模型配置失败: modelIdentifier={}", model.getModelIdentifier(), e);
            throw new BusinessException("解析模型配置失败: " + e.getMessage());
        }
    }

    /**
     * 获取模型参数（子类可重写）
     * 默认返回请求参数
     */
    protected Map<String, Object> getModelParameters(AiModel model, Map<String, Object> requestParameters) {
        return new HashMap<>(requestParameters);
    }

    @Override
    protected boolean supportsProviderType(AiModel model) {
        // 检查提供商类型是否为 OpenAI
        return providerRepository.findById(model.getProviderId())
                .map(provider -> "OPENAI".equals(provider.getProviderType().name()))
                .orElse(false);
    }

    @Override
    public Set<ModelCapability> supportedCapabilities() {
        // OpenAI 支持文本生成和 Embedding
        return Set.of(
            ModelCapability.TEXT_GENERATION,
            ModelCapability.EMBEDDING
        );
    }
}
