package org.joker.comfypilot.model.domain.service;

import dev.langchain4j.model.chat.ChatModel;
import org.joker.comfypilot.model.domain.entity.AiModel;
import org.joker.comfypilot.model.domain.entity.ModelProvider;

import java.util.Map;

/**
 * ChatModel 工厂接口
 * <p>
 * 根据模型配置和提供商信息创建 LangChain4j 的 ChatModel 实例（非流式）。
 * 支持不同的模型提供商（OpenAI、Anthropic、阿里云等）和接入方式（远程API、本地）。
 * <p>
 * <b>使用示例：</b>
 * <pre>{@code
 * // 获取模型配置
 * AiModel model = modelRepository.findByModelIdentifier("gpt-4").orElseThrow();
 * ModelProvider provider = providerRepository.findById(model.getProviderId()).orElseThrow();
 *
 * // 创建 ChatModel
 * ChatModel chatModel = factory.createChatModel(model, provider, null);
 *
 * // 使用模型
 * String response = chatModel.generate("Hello");
 * System.out.println(response);
 * }</pre>
 *
 * @see AiModel
 * @see ModelProvider
 */
public interface ChatModelFactory {

    /**
     * 创建 ChatModel 实例（非流式）
     * <p>
     * 根据模型的接入方式和提供商类型创建对应的 ChatModel：
     * <ul>
     *   <li><b>远程API接入：</b>根据提供商类型（OpenAI、Anthropic等）创建对应的客户端</li>
     *   <li><b>本地接入：</b>创建本地模型客户端（需要本地模型服务支持）</li>
     * </ul>
     * <p>
     * <b>模型配置格式（modelConfig JSON）：</b>
     * <pre>{@code
     * {
     *   "apiKey": "sk-xxx",           // API密钥（远程API必需）
     *   "temperature": 0.7,           // 温度参数（可选，默认0.7）
     *   "maxTokens": 2000,            // 最大token数（可选）
     *   "topP": 0.9,                  // Top-P采样（可选）
     *   "timeout": 60                 // 超时时间（秒，可选，默认60）
     * }
     * }</pre>
     * <p>
     * <b>Agent配置覆盖：</b>
     * agentConfig 中的参数会覆盖模型默认配置，支持的参数包括：
     * <ul>
     *   <li>temperature: 温度参数</li>
     *   <li>maxTokens: 最大token数</li>
     *   <li>topP: Top-P采样</li>
     *   <li>timeout: 超时时间（秒）</li>
     * </ul>
     *
     * @param modelIdentifier       模型唯一标识
     * @param agentConfig Agent指定的模型配置，用于覆盖模型默认配置（可为null或空Map）
     * @return ChatModel 实例
     * @throws org.joker.comfypilot.common.exception.BusinessException 创建失败时抛出
     */
    ChatModel createChatModel(String modelIdentifier, Map<String, Object> agentConfig);
}
