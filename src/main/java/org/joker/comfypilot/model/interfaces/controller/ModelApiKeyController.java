package org.joker.comfypilot.model.interfaces.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.joker.comfypilot.common.interfaces.response.Result;
import org.joker.comfypilot.model.application.dto.CreateApiKeyRequest;
import org.joker.comfypilot.model.application.dto.ModelApiKeyDTO;
import org.joker.comfypilot.model.application.dto.UpdateApiKeyRequest;
import org.joker.comfypilot.model.application.service.ModelApiKeyService;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

/**
 * 模型API密钥管理控制器
 */
@Tag(name = "模型API密钥管理", description = "模型API密钥的创建、查询、更新、删除等管理接口")
@RestController
@RequestMapping("/api/v1/model-api-keys")
@RequiredArgsConstructor
public class ModelApiKeyController {

    private final ModelApiKeyService apiKeyService;

    @Operation(summary = "创建API密钥", description = "为指定提供商创建新的API密钥")
    @PostMapping
    public Result<ModelApiKeyDTO> createApiKey(
            @Parameter(description = "创建API密钥请求", required = true)
            @Valid @RequestBody CreateApiKeyRequest request) {
        ModelApiKeyDTO apiKey = apiKeyService.createApiKey(request);
        return Result.success(apiKey);
    }

    @Operation(summary = "根据ID查询API密钥", description = "根据密钥ID查询详细信息")
    @GetMapping("/{id}")
    public Result<ModelApiKeyDTO> getApiKeyById(
            @Parameter(description = "密钥ID", required = true)
            @PathVariable Long id) {
        ModelApiKeyDTO apiKey = apiKeyService.getById(id);
        return Result.success(apiKey);
    }

    @Operation(summary = "查询提供商的所有密钥", description = "查询指定提供商的所有API密钥")
    @GetMapping("/provider/{providerId}")
    public Result<List<ModelApiKeyDTO>> listApiKeysByProvider(
            @Parameter(description = "提供商ID", required = true)
            @PathVariable Long providerId) {
        List<ModelApiKeyDTO> apiKeys = apiKeyService.listByProviderId(providerId);
        return Result.success(apiKeys);
    }

    @Operation(summary = "更新API密钥", description = "更新API密钥信息")
    @PutMapping("/{id}")
    public Result<ModelApiKeyDTO> updateApiKey(
            @Parameter(description = "密钥ID", required = true)
            @PathVariable Long id,
            @Parameter(description = "更新API密钥请求", required = true)
            @Valid @RequestBody UpdateApiKeyRequest request) {
        ModelApiKeyDTO apiKey = apiKeyService.updateApiKey(id, request);
        return Result.success(apiKey);
    }

    @Operation(summary = "删除API密钥", description = "删除指定的API密钥")
    @DeleteMapping("/{id}")
    public Result<Void> deleteApiKey(
            @Parameter(description = "密钥ID", required = true)
            @PathVariable Long id) {
        apiKeyService.deleteApiKey(id);
        return Result.success();
    }
}
