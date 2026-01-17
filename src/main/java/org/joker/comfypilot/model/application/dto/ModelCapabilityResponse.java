package org.joker.comfypilot.model.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.joker.comfypilot.model.domain.enums.ModelCapability;
import org.joker.comfypilot.model.domain.valueobject.ModelExecutionMetadata;

import java.io.Serializable;
import java.util.Map;

/**
 * 模型能力调用响应
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModelCapabilityResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 能力类型
     */
    private ModelCapability capability;

    /**
     * 执行结果（灵活的键值对）
     * 不同能力类型的结果不同：
     * - TEXT_GENERATION: text, finish_reason 等
     * - EMBEDDING: vector 等
     * - CLASSIFICATION: label, confidence 等
     */
    private Map<String, Object> result;

    /**
     * 执行元数据
     */
    private ModelExecutionMetadata metadata;
}
