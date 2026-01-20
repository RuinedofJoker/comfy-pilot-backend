package org.joker.comfypilot.agent.interfaces.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.joker.comfypilot.agent.application.dto.AgentConfigDTO;
import org.joker.comfypilot.agent.application.dto.AgentRuntimeConfigDTO;
import org.joker.comfypilot.agent.application.executor.AgentExecutor;
import org.joker.comfypilot.agent.application.service.AgentConfigService;
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

    @Operation(summary = "运行时获取已启用的Agent", description = "获取系统中所有已启用的Agent配置列表，供用户页面使用")
    @GetMapping("/runtime/enabled")
    public Result<List<AgentRuntimeConfigDTO>> getEnabledRuntimeAgents() {
        List<AgentRuntimeConfigDTO> agents = agentConfigService.getEnabledRuntimeAgents();
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

    @Operation(summary = "运行时根据编码获取Agent", description = "根据Agent编码获取详细配置信息，供用户页面使用")
    @GetMapping("/runtime/code/{agentCode}")
    public Result<AgentRuntimeConfigDTO> getRuntimeAgentByCode(
            @Parameter(description = "Agent编码", required = true)
            @PathVariable String agentCode) {
        AgentRuntimeConfigDTO agent = agentConfigService.getRuntimeAgentByCode(agentCode);
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

}
