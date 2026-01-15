package org.joker.comfypilot.model.infrastructure.persistence.po;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.joker.comfypilot.common.infrastructure.persistence.po.BasePO;

/**
 * 模型提供商持久化对象
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("model_provider")
public class ModelProviderPO extends BasePO {

    private String name;
    private String code;
    private String baseUrl;
    private String apiKey;
    private String status;
    private Integer rateLimit;
    private Integer timeoutMs;
    private Integer retryTimes;
}
