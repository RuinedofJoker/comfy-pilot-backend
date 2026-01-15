package org.joker.comfypilot.model.interfaces.controller;

import lombok.RequiredArgsConstructor;
import org.joker.comfypilot.common.interfaces.response.Result;
import org.joker.comfypilot.model.application.dto.ModelProviderCreateRequest;
import org.joker.comfypilot.model.application.dto.ModelProviderDTO;
import org.joker.comfypilot.model.application.dto.ModelProviderUpdateRequest;
import org.joker.comfypilot.model.application.service.ModelProviderApplicationService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 模型提供商管理控制器
 */
@RestController
@RequestMapping("/api/v1/model-providers")
@RequiredArgsConstructor
public class ModelProviderController {

    private final ModelProviderApplicationService modelProviderApplicationService;

    /**
     * 创建提供商
     */
    @PostMapping
    public Result<ModelProviderDTO> createProvider(@RequestBody ModelProviderCreateRequest request) {
        // TODO: 实现创建逻辑
        return Result.success(null);
    }

    /**
     * 更新提供商
     */
    @PutMapping("/{id}")
    public Result<ModelProviderDTO> updateProvider(@PathVariable Long id, @RequestBody ModelProviderUpdateRequest request) {
        // TODO: 实现更新逻辑
        return Result.success(null);
    }

    /**
     * 获取提供商
     */
    @GetMapping("/{id}")
    public Result<ModelProviderDTO> getProvider(@PathVariable Long id) {
        // TODO: 实现查询逻辑
        return Result.success(null);
    }

    /**
     * 获取所有激活的提供商
     */
    @GetMapping
    public Result<List<ModelProviderDTO>> getAllActiveProviders() {
        // TODO: 实现查询逻辑
        return Result.success(null);
    }

    /**
     * 测试提供商连接
     */
    @PostMapping("/{id}/test")
    public Result<Boolean> testConnection(@PathVariable Long id) {
        // TODO: 实现连接测试逻辑
        return Result.success(null);
    }

    /**
     * 激活提供商
     */
    @PostMapping("/{id}/activate")
    public Result<Void> activateProvider(@PathVariable Long id) {
        // TODO: 实现激活逻辑
        return Result.success(null);
    }

    /**
     * 停用提供商
     */
    @PostMapping("/{id}/deactivate")
    public Result<Void> deactivateProvider(@PathVariable Long id) {
        // TODO: 实现停用逻辑
        return Result.success(null);
    }

    /**
     * 删除提供商
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteProvider(@PathVariable Long id) {
        // TODO: 实现删除逻辑
        return Result.success(null);
    }
}
