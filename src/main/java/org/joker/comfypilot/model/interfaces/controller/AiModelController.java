package org.joker.comfypilot.model.interfaces.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.joker.comfypilot.common.interfaces.response.Result;
import org.joker.comfypilot.model.application.dto.AiModelDTO;
import org.joker.comfypilot.model.application.dto.AiModelSimpleDTO;
import org.joker.comfypilot.model.application.dto.CreateModelRequest;
import org.joker.comfypilot.model.application.dto.UpdateModelRequest;
import org.joker.comfypilot.model.application.service.AiModelService;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;

import jakarta.validation.Valid;

import java.util.List;

/**
 * AI模型管理控制器
 * 管理员可以通过此接口管理模型：
 * - 远程API创建的模型：可以完全管理（创建/修改/删除）
 * - 代码预定义的模型：只能编辑基本信息（modelName, description），不能删除
 */
@Tag(name = "AI模型管理", description = "AI模型的创建、查询、更新、删除等管理接口")
@RestController
@RequestMapping("/api/v1/models")
public class AiModelController {

    @Autowired
    private AiModelService modelService;

    @Operation(summary = "创建AI模型", description = "创建新的AI模型（通过API创建的模型标记为远程API来源，可完全管理）")
    @PostMapping
    public Result<AiModelDTO> createModel(
            @Parameter(description = "创建模型请求", required = true)
            @Valid @RequestBody CreateModelRequest request) {
        AiModelDTO model = modelService.createModel(request);
        return Result.success(model);
    }

    @Operation(summary = "根据ID查询AI模型", description = "根据模型ID查询模型详细信息")
    @GetMapping("/{id}")
    public Result<AiModelDTO> getModelById(
            @Parameter(description = "模型ID", required = true)
            @PathVariable Long id) {
        AiModelDTO model = modelService.getById(id);
        return Result.success(model);
    }

    @Operation(summary = "查询所有AI模型", description = "查询系统中所有的AI模型列表")
    @GetMapping
    public Result<List<AiModelDTO>> listModels() {
        List<AiModelDTO> models = modelService.listModels();
        return Result.success(models);
    }

    @Operation(summary = "更新AI模型",
            description = "更新AI模型信息。注意：代码预定义的模型只能更新基本信息（modelName, description），不能修改模型配置")
    @PutMapping("/{id}")
    public Result<AiModelDTO> updateModel(
            @Parameter(description = "模型ID", required = true)
            @PathVariable Long id,
            @Parameter(description = "更新模型请求", required = true)
            @Valid @RequestBody UpdateModelRequest request) {
        AiModelDTO model = modelService.updateModel(id, request);
        return Result.success(model);
    }

    @Operation(summary = "删除AI模型",
            description = "删除AI模型。注意：只能删除远程API创建的模型，代码预定义的模型不能删除")
    @DeleteMapping("/{id}")
    public Result<Void> deleteModel(
            @Parameter(description = "模型ID", required = true)
            @PathVariable Long id) {
        modelService.deleteModel(id);
        return Result.success();
    }

    @Operation(summary = "启用AI模型", description = "启用指定的AI模型")
    @PostMapping("/{id}/enable")
    public Result<Void> enableModel(
            @Parameter(description = "模型ID", required = true)
            @PathVariable Long id) {
        modelService.enableModel(id);
        return Result.success();
    }

    @Operation(summary = "禁用AI模型", description = "禁用指定的AI模型")
    @PostMapping("/{id}/disable")
    public Result<Void> disableModel(
            @Parameter(description = "模型ID", required = true)
            @PathVariable Long id) {
        modelService.disableModel(id);
        return Result.success();
    }

    @Operation(summary = "查询启用的AI模型（前台）", description = "查询所有启用状态的AI模型简化信息，用于前台展示")
    @GetMapping("/enabled")
    public Result<List<AiModelSimpleDTO>> listEnabledModels(@Parameter(description = "模型调用方式", required = true) String modelCallingType) {
        List<AiModelSimpleDTO> models = modelService.listEnabledModels(modelCallingType);
        return Result.success(models);
    }

    @Operation(summary = "获取模型配置格式说明", description = "根据模型调用方式获取对应的模型配置格式说明（JSON格式）")
    @GetMapping("/config-format")
    public Result<String> getModelConfigFormat(
            @Parameter(description = "模型调用方式（如：api_llm, api_embedding, sentence_transformers_embedding）", required = true)
            @RequestParam String modelCallingType) {
        String configFormat = modelService.getModelConfigFormat(modelCallingType);
        return Result.success(configFormat);
    }
}
