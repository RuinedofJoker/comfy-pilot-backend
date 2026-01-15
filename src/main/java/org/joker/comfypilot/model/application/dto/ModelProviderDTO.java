package org.joker.comfypilot.model.application.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.joker.comfypilot.common.application.dto.BaseDTO;

/**
 * 模型提供商DTO
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ModelProviderDTO extends BaseDTO {

    private Long id;
    private String name;
    private String code;
    private String baseUrl;
    private String status;
    private Integer rateLimit;
    private Integer timeoutMs;
    private Integer retryTimes;
}
