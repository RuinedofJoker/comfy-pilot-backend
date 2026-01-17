package org.joker.comfypilot.model.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.joker.comfypilot.model.domain.enums.ModelCapability;
import org.joker.comfypilot.model.domain.valueobject.CapabilityConstraints;

import java.io.Serializable;
import java.util.Map;

/**
 * 模型能力调用请求
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModelCapabilityRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 能力类型
     */
    private ModelCapability capability;

    /**
     * 请求参数（灵活的键值对）
     * 不同能力类型的参数不同：
     * - TEXT_GENERATION: messages, max_tokens, temperature 等
     * - EMBEDDING: text, model 等
     * - CLASSIFICATION: text, labels 等
     */
    private Map<String, Object> parameters;

    /**
     * 约束条件（可选）
     */
    private CapabilityConstraints constraints;
}
