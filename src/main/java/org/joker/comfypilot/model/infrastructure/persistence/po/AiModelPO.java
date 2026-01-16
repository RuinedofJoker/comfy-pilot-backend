package org.joker.comfypilot.model.infrastructure.persistence.po;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.joker.comfypilot.common.infrastructure.persistence.po.BasePO;

/**
 * AI模型持久化对象
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName("ai_model")
public class AiModelPO extends BasePO {

    private static final long serialVersionUID = 1L;

    /**
     * 模型名称
     */
    private String modelName;

    /**
     * 模型标识符
     */
    private String modelIdentifier;

    /**
     * 接入方式（远程API/本地）
     */
    private String accessType;

    /**
     * 模型类型（LLM/Embedding等）
     */
    private String modelType;

    /**
     * 模型来源（远程API创建/代码预定义）
     */
    private String modelSource;

    /**
     * 提供商ID
     */
    private Long providerId;

    /**
     * 模型配置
     */
    private String modelConfig;

    /**
     * 描述信息
     */
    private String description;

    /**
     * 是否启用
     */
    private Boolean isEnabled;
}
