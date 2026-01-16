package org.joker.comfypilot.model.infrastructure.persistence.po;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.joker.comfypilot.common.infrastructure.persistence.po.BasePO;

/**
 * 模型API密钥持久化对象
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName("model_api_key")
public class ModelApiKeyPO extends BasePO {

    private static final long serialVersionUID = 1L;

    /**
     * 提供商ID
     */
    private Long providerId;

    /**
     * 密钥名称
     */
    private String keyName;

    /**
     * API密钥（加密存储）
     */
    private String apiKey;

    /**
     * 是否启用
     */
    private Boolean isEnabled;
}
