package org.joker.comfypilot.model.interfaces.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.joker.comfypilot.common.interfaces.response.Result;
import org.joker.comfypilot.model.application.dto.CreateProviderRequest;
import org.joker.comfypilot.model.application.dto.ModelProviderDTO;
import org.joker.comfypilot.model.application.dto.UpdateProviderRequest;
import org.joker.comfypilot.model.application.service.ModelProviderService;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;

import jakarta.validation.Valid;
import java.util.List;

/**
 * 模型提供商管理控制器
 */
@Tag(name = "模型提供商管理", description = "模型提供商的创建、查询、更新、删除等管理接口")
@RestController
@RequestMapping("/api/v1/model-providers")
public class ModelProviderController {

    @Autowired
    private ModelProviderService providerService;

    @Operation(summary = "创建模型提供商", description = "创建新的模型提供商")
    @PostMapping
    public Result<ModelProviderDTO> createProvider(
            @Parameter(description = "创建提供商请求", required = true)
            @Valid @RequestBody CreateProviderRequest request) {
        ModelProviderDTO provider = providerService.createProvider(request);
        return Result.success(provider);
    }

    @Operation(summary = "根据ID查询提供商", description = "根据提供商ID查询详细信息")
    @GetMapping("/{id}")
    public Result<ModelProviderDTO> getProviderById(
            @Parameter(description = "提供商ID", required = true)
            @PathVariable Long id) {
        ModelProviderDTO provider = providerService.getById(id);
        return Result.success(provider);
    }

    @Operation(summary = "查询所有提供商", description = "查询系统中所有的模型提供商列表")
    @GetMapping
    public Result<List<ModelProviderDTO>> listProviders() {
        List<ModelProviderDTO> providers = providerService.listProviders();
        return Result.success(providers);
    }

    @Operation(summary = "更新提供商", description = "更新模型提供商信息")
    @PutMapping("/{id}")
    public Result<ModelProviderDTO> updateProvider(
            @Parameter(description = "提供商ID", required = true)
            @PathVariable Long id,
            @Parameter(description = "更新提供商请求", required = true)
            @Valid @RequestBody UpdateProviderRequest request) {
        ModelProviderDTO provider = providerService.updateProvider(id, request);
        return Result.success(provider);
    }

    @Operation(summary = "删除提供商", description = "删除模型提供商")
    @DeleteMapping("/{id}")
    public Result<Void> deleteProvider(
            @Parameter(description = "提供商ID", required = true)
            @PathVariable Long id) {
        providerService.deleteProvider(id);
        return Result.success();
    }
}
