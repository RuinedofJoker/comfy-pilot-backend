package org.joker.comfypilot.model.application.dto;

import lombok.Data;

/**
 * 模型提供商更新请求
 */
@Data
public class ModelProviderUpdateRequest {

    private String name;
    private String baseUrl;
    private String apiKey;
    private Integer rateLimit;
    private Integer timeoutMs;
    private Integer retryTimes;
}
