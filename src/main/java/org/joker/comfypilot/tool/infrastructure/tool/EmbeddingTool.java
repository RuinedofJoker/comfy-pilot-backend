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
 * 向量生成工具
 * 封装向量生成能力
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EmbeddingTool implements Tool {

    private final ModelCapabilityService modelCapabilityService;

    @Override
    public ToolType getType() {
        return ToolType.EMBEDDING;
    }

    @Override
    public String getName() {
        return "embedding";
    }

    @Override
    public String getDescription() {
        return "向量生成工具，用于文本向量化";
    }

    @Override
    public Map<String, Object> getParameterSchema() {
        Map<String, Object> schema = new HashMap<>();
        schema.put("type", "object");

        Map<String, Object> properties = new HashMap<>();

        // text 参数
        Map<String, Object> textSchema = new HashMap<>();
        textSchema.put("type", "string");
        textSchema.put("description", "需要向量化的文本");
        properties.put("text", textSchema);

        schema.put("properties", properties);
        schema.put("required", new String[]{"text"});

        return schema;
    }

    @Override
    public ToolExecutionResult execute(Map<String, Object> parameters) {
        log.info("执行 EmbeddingTool: parameters={}", parameters);

        try {
            // 1. 验证参数
            if (!parameters.containsKey("text")) {
                return ToolExecutionResult.failure("缺少必需参数: text");
            }

            // 2. 构建 ModelCapabilityRequest
            ModelCapabilityRequest request = ModelCapabilityRequest.builder()
                    .capability(ModelCapability.EMBEDDING)
                    .parameters(parameters)
                    .build();

            // 3. 调用 ModelCapabilityService
            ModelCapabilityResponse response = modelCapabilityService.invoke(request);

            // 4. 构建执行元数据
            ToolExecutionMetadata metadata = ToolExecutionMetadata.builder()
                    .toolType(getType().getCode())
                    .toolName(getName())
                    .modelUsed(response.getMetadata().getModelIdentifier())
                    .build();

            // 5. 返回成功结果
            return ToolExecutionResult.success(response.getResult(), metadata);

        } catch (Exception e) {
            log.error("EmbeddingTool 执行失败", e);
            return ToolExecutionResult.failure("EmbeddingTool 执行失败: " + e.getMessage());
        }
    }
}
