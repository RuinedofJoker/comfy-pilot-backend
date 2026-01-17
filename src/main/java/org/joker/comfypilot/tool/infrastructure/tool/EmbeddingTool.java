package org.joker.comfypilot.tool.infrastructure.tool;

import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.agent.tool.P;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.joker.comfypilot.model.application.dto.ModelCapabilityRequest;
import org.joker.comfypilot.model.application.dto.ModelCapabilityResponse;
import org.joker.comfypilot.model.application.service.ModelCapabilityService;
import org.joker.comfypilot.model.domain.enums.ModelCapability;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 向量生成工具
 * 提供文本向量化能力
 * 符合 langchain4j 规范的工具类
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EmbeddingTool {

    private final ModelCapabilityService modelCapabilityService;

    /**
     * 生成文本向量
     * 将文本转换为向量表示，用于语义搜索和相似度计算
     *
     * @param text 需要向量化的文本
     * @return 向量生成结果的字符串表示
     */
    @Tool("将文本转换为向量表示")
    public String generateEmbedding(@P("需要向量化的文本") String text) {

        log.info("执行 EmbeddingTool.generateEmbedding: text={}", text);

        try {
            // 1. 验证参数
            if (text == null || text.trim().isEmpty()) {
                return "错误：文本不能为空";
            }

            // 2. 构建请求参数
            Map<String, Object> parameters = new HashMap<>();
            parameters.put("text", text);

            // 3. 构建 ModelCapabilityRequest
            ModelCapabilityRequest request = ModelCapabilityRequest.builder()
                    .capability(ModelCapability.EMBEDDING)
                    .parameters(parameters)
                    .build();

            // 4. 调用 ModelCapabilityService
            ModelCapabilityResponse response = modelCapabilityService.invoke(request);

            // 5. 处理返回结果
            Object result = response.getResult();

            if (result instanceof List) {
                @SuppressWarnings("unchecked")
                List<Double> embedding = (List<Double>) result;
                log.info("EmbeddingTool.generateEmbedding 执行成功，向量维度: {}", embedding.size());
                return "向量生成成功，维度: " + embedding.size();
            } else {
                log.info("EmbeddingTool.generateEmbedding 执行成功");
                return "向量生成成功";
            }

        } catch (Exception e) {
            log.error("EmbeddingTool.generateEmbedding 执行失败", e);
            return "错误：向量生成失败 - " + e.getMessage();
        }
    }
}
