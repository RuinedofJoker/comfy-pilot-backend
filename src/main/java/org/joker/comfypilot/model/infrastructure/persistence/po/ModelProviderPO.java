package org.joker.comfypilot.model.infrastructure.persistence.po;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.joker.comfypilot.common.infrastructure.persistence.po.BasePO;

/**
 * 模型提供商持久化对象
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName("model_provider")
public class ModelProviderPO extends BasePO {

    private static final long serialVersionUID = 1L;

    /**
     * 提供商名称
     */
    private String providerName;

    /**
     * 提供商类型
     */
    private String providerType;

    /**
     * API基础URL
     */
    private String apiBaseUrl;

    /**
     * 描述信息
     */
    private String description;

    /**
     * 是否启用
     */
    private Boolean isEnabled;
}
