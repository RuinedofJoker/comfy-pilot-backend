package org.joker.comfypilot.model.interfaces.controller;

import lombok.RequiredArgsConstructor;
import org.joker.comfypilot.common.interfaces.response.Result;
import org.joker.comfypilot.model.application.dto.AIModelCreateRequest;
import org.joker.comfypilot.model.application.dto.AIModelDTO;
import org.joker.comfypilot.model.application.dto.AIModelUpdateRequest;
import org.joker.comfypilot.model.application.service.AIModelApplicationService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * AI模型管理控制器
 */
@RestController
@RequestMapping("/api/v1/models")
@RequiredArgsConstructor
public class AIModelController {

    private final AIModelApplicationService aiModelApplicationService;

    /**
     * 创建模型
     */
    @PostMapping
    public Result<AIModelDTO> createModel(@RequestBody AIModelCreateRequest request) {
        // TODO: 实现创建逻辑
        return Result.success(null);
    }

    /**
     * 更新模型
     */
    @PutMapping("/{id}")
    public Result<AIModelDTO> updateModel(@PathVariable Long id, @RequestBody AIModelUpdateRequest request) {
        // TODO: 实现更新逻辑
        return Result.success(null);
    }

    /**
     * 获取模型
     */
    @GetMapping("/{id}")
    public Result<AIModelDTO> getModel(@PathVariable Long id) {
        // TODO: 实现查询逻辑
        return Result.success(null);
    }

    /**
     * 获取所有激活的模型
     */
    @GetMapping
    public Result<List<AIModelDTO>> getAllActiveModels() {
        // TODO: 实现查询逻辑
        return Result.success(null);
    }

    /**
     * 激活模型
     */
    @PostMapping("/{id}/activate")
    public Result<Void> activateModel(@PathVariable Long id) {
        // TODO: 实现激活逻辑
        return Result.success(null);
    }

    /**
     * 停用模型
     */
    @PostMapping("/{id}/deactivate")
    public Result<Void> deactivateModel(@PathVariable Long id) {
        // TODO: 实现停用逻辑
        return Result.success(null);
    }

    /**
     * 删除模型
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteModel(@PathVariable Long id) {
        // TODO: 实现删除逻辑
        return Result.success(null);
    }
}
