package org.joker.comfypilot.tool.infrastructure.tool;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.joker.comfypilot.model.application.dto.ModelCapabilityRequest;
import org.joker.comfypilot.model.application.dto.ModelCapabilityResponse;
import org.joker.comfypilot.model.application.service.ModelCapabilityService;
import org.joker.comfypilot.model.domain.enums.ModelCapability;
import org.joker.comfypilot.tool.domain.enums.ToolType;
import org.joker.comfypilot.tool.domain.service.Tool;
import org.joker.comfypilot.tool.domain.valueobject.ToolExecutionMetadata;
import org.joker.comfypilot.tool.domain.valueobject.ToolExecutionResult;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 大语言模型工具
 * 封装文本生成能力
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LlmTool implements Tool {

    private final ModelCapabilityService modelCapabilityService;

    @Override
    public ToolType getType() {
        return ToolType.LLM;
    }

    @Override
    public String getName() {
        return "llm";
    }

    @Override
    public String getDescription() {
        return "大语言模型工具，用于文本生成和对话";
    }

    @Override
    public Map<String, Object> getParameterSchema() {
        Map<String, Object> schema = new HashMap<>();
        schema.put("type", "object");

        Map<String, Object> properties = new HashMap<>();

        // prompt 参数
        Map<String, Object> promptSchema = new HashMap<>();
        promptSchema.put("type", "string");
        promptSchema.put("description", "输入的提示文本");
        properties.put("prompt", promptSchema);

        // temperature 参数
        Map<String, Object> temperatureSchema = new HashMap<>();
        temperatureSchema.put("type", "number");
        temperatureSchema.put("description", "温度参数，控制输出的随机性，范围 0-2");
        temperatureSchema.put("minimum", 0);
        temperatureSchema.put("maximum", 2);
        temperatureSchema.put("default", 0.7);
        properties.put("temperature", temperatureSchema);

        // max_tokens 参数
        Map<String, Object> maxTokensSchema = new HashMap<>();
        maxTokensSchema.put("type", "integer");
        maxTokensSchema.put("description", "最大生成的 token 数量");
        maxTokensSchema.put("minimum", 1);
        maxTokensSchema.put("default", 2000);
        properties.put("max_tokens", maxTokensSchema);

        schema.put("properties", properties);
        schema.put("required", new String[]{"prompt"});

        return schema;
    }

    @Override
    public ToolExecutionResult execute(Map<String, Object> parameters) {
        log.info("执行 LlmTool: parameters={}", parameters);

        try {
            // 1. 验证参数
            if (!parameters.containsKey("prompt")) {
                return ToolExecutionResult.failure("缺少必需参数: prompt");
            }

            // 2. 构建 ModelCapabilityRequest
            ModelCapabilityRequest request = ModelCapabilityRequest.builder()
                    .capability(ModelCapability.TEXT_GENERATION)
                    .parameters(parameters)
                    .build();

            // 3. 调用 ModelCapabilityService
            ModelCapabilityResponse response = modelCapabilityService.invoke(request);

            // 4. 构建执行元数据
            ToolExecutionMetadata metadata = ToolExecutionMetadata.builder()
                    .toolType(getType().getCode())
                    .toolName(getName())
                    .modelUsed(response.getMetadata().getModelIdentifier())
                    .inputTokens(response.getMetadata().getInputTokens())
                    .outputTokens(response.getMetadata().getOutputTokens())
                    .tokenUsed(response.getMetadata().getTotalTokens())
                    .build();

            // 5. 返回成功结果
            return ToolExecutionResult.success(response.getResult(), metadata);

        } catch (Exception e) {
            log.error("LlmTool 执行失败", e);
            return ToolExecutionResult.failure("LlmTool 执行失败: " + e.getMessage());
        }
    }
}
