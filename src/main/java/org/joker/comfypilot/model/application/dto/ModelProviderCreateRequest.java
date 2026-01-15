package org.joker.comfypilot.model.application.dto;

import lombok.Data;

/**
 * 模型提供商创建请求
 */
@Data
public class ModelProviderCreateRequest {

    private String name;
    private String code;
    private String baseUrl;
    private String apiKey;
    private Integer rateLimit;
    private Integer timeoutMs;
    private Integer retryTimes;
}
