package org.joker.comfypilot.agent.interfaces.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.joker.comfypilot.agent.application.dto.AgentConfigDTO;
import org.joker.comfypilot.agent.application.dto.AgentExecutionRequest;
import org.joker.comfypilot.agent.application.dto.AgentExecutionResponse;
import org.joker.comfypilot.agent.application.executor.AgentExecutor;
import org.joker.comfypilot.agent.application.service.AgentConfigService;
import org.joker.comfypilot.agent.domain.context.AgentExecutionContext;
import org.joker.comfypilot.common.interfaces.response.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Agent控制器
 */
@Tag(name = "Agent管理", description = "Agent配置和执行相关接口")
@RestController
@RequestMapping("/api/v1/agents")
public class AgentController {

    @Autowired
    private AgentConfigService agentConfigService;
    @Lazy
    @Autowired
    private AgentExecutor agentExecutor;

    @Operation(summary = "获取所有Agent", description = "获取系统中所有Agent配置列表")
    @GetMapping
    public Result<List<AgentConfigDTO>> getAllAgents() {
        List<AgentConfigDTO> agents = agentConfigService.getAllAgents();
        return Result.success(agents);
    }

    @Operation(summary = "获取已启用的Agent", description = "获取系统中所有已启用的Agent配置列表，供用户页面使用")
    @GetMapping("/enabled")
    public Result<List<AgentConfigDTO>> getEnabledAgents() {
        List<AgentConfigDTO> agents = agentConfigService.getEnabledAgents();
        return Result.success(agents);
    }

    @Operation(summary = "根据ID获取Agent", description = "根据Agent ID获取详细配置信息")
    @GetMapping("/{id}")
    public Result<AgentConfigDTO> getAgentById(
            @Parameter(description = "Agent ID", required = true)
            @PathVariable Long id) {
        AgentConfigDTO agent = agentConfigService.getAgentById(id);
        return Result.success(agent);
    }

    @Operation(summary = "根据编码获取Agent", description = "根据Agent编码获取详细配置信息")
    @GetMapping("/code/{agentCode}")
    public Result<AgentConfigDTO> getAgentByCode(
            @Parameter(description = "Agent编码", required = true)
            @PathVariable String agentCode) {
        AgentConfigDTO agent = agentConfigService.getAgentByCode(agentCode);
        return Result.success(agent);
    }

    @Operation(summary = "启用Agent", description = "启用指定的Agent")
    @PostMapping("/{id}/enable")
    public Result<Void> enableAgent(
            @Parameter(description = "Agent ID", required = true)
            @PathVariable Long id) {
        agentConfigService.enableAgent(id);
        return Result.success();
    }

    @Operation(summary = "禁用Agent", description = "禁用指定的Agent")
    @PostMapping("/{id}/disable")
    public Result<Void> disableAgent(
            @Parameter(description = "Agent ID", required = true)
            @PathVariable Long id) {
        agentConfigService.disableAgent(id);
        return Result.success();
    }

    @Operation(summary = "执行Agent", description = "执行指定的Agent并返回结果")
    @PostMapping("/{agentCode}/execute")
    public Result<AgentExecutionResponse> executeAgent(
            @Parameter(description = "Agent编码", required = true)
            @PathVariable String agentCode,
            @Parameter(description = "执行请求", required = true)
            @RequestBody AgentExecutionRequest request) {
        request.setIsStreamable(false);
        AgentExecutionContext executionContext = agentExecutor.getExecutionContext(agentCode, request);
        AgentExecutionResponse response = agentExecutor.execute(executionContext);
        return Result.success(response);
    }
}
