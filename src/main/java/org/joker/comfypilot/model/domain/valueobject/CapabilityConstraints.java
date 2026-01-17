package org.joker.comfypilot.model.domain.valueobject;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.joker.comfypilot.model.domain.enums.ModelAccessType;

import java.io.Serializable;

/**
 * 能力约束条件值对象
 * 用于在选择模型时指定约束条件
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CapabilityConstraints implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 优先接入方式
     * 如：优先使用本地模型
     */
    private ModelAccessType preferredAccessType;

    /**
     * 最大成本限制（每1000个token的美元成本）
     */
    private Double maxCostPer1kTokens;

    /**
     * 最小优先级要求
     */
    private Integer minPriority;

    /**
     * 是否只使用启用的模型
     */
    @Builder.Default
    private Boolean onlyEnabled = true;

    /**
     * 指定模型标识符（如果指定，则直接使用该模型）
     */
    private String modelIdentifier;
}
