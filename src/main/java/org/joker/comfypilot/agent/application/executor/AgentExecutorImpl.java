package org.joker.comfypilot.agent.application.executor;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.joker.comfypilot.agent.application.dto.AgentExecutionRequest;
import org.joker.comfypilot.agent.application.dto.AgentExecutionResponse;
import org.joker.comfypilot.agent.domain.context.AgentExecutionContext;
import org.joker.comfypilot.agent.domain.entity.AgentConfig;
import org.joker.comfypilot.agent.domain.entity.AgentExecutionLog;
import org.joker.comfypilot.agent.domain.enums.ExecutionStatus;
import org.joker.comfypilot.agent.domain.repository.AgentConfigRepository;
import org.joker.comfypilot.agent.domain.repository.AgentExecutionLogRepository;
import org.joker.comfypilot.agent.domain.service.Agent;
import org.joker.comfypilot.agent.domain.service.AgentRegistry;
import org.joker.comfypilot.common.exception.BusinessException;
import org.joker.comfypilot.common.util.TraceIdUtil;
import org.joker.comfypilot.session.domain.enums.AgentPromptType;
import org.joker.comfypilot.tool.infrastructure.service.ClientTool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Agent执行器实现
 */
@Slf4j
@Service
public class AgentExecutorImpl implements AgentExecutor {

    @Lazy
    @Autowired
    private AgentRegistry agentRegistry;
    @Autowired
    private AgentExecutionLogRepository executionLogRepository;
    @Autowired
    private AgentConfigRepository agentConfigRepository;

    @Override
    public AgentExecutionContext getExecutionContext(String agentCode, AgentExecutionRequest request) {
        // 1. 查找Agent
        Agent agent = Optional.ofNullable(agentRegistry.getAgentByCode(agentCode))
                .orElseThrow(() -> new BusinessException("Agent不存在: " + agentCode));
        AgentConfig agentConfig = agentConfigRepository.findByAgentCode(agentCode)
                .orElseThrow(() -> new BusinessException("Agent配置不存在: " + agentCode));

        // 2. 创建执行日志
        AgentExecutionLog executionLog = AgentExecutionLog.builder()
                .sessionId(request.getSessionId())
                .input(request.getUserMessage())
                .status(ExecutionStatus.RUNNING)
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build();

        Map<String, Object> agentRuntimeConfig = new HashMap<>();
        if (request.getAgentConfig() != null) {
            agentRuntimeConfig.putAll(request.getAgentConfig());
        }

        // 将ClientTool收集起来
        List<ClientTool> clientTools = Optional.ofNullable(request.getToolSchemas()).orElse(Collections.emptyList()).stream().map(ClientTool::new).toList();
        Set<String> clientToolNames = new HashSet<>(clientTools.size());
        for (ClientTool clientTool : clientTools) {
            if (StringUtils.isBlank(clientTool.toolName())) {
                throw new BusinessException("客户端工具名" + clientTool.toolName() + "为空");
            }
            if (!clientToolNames.add(clientTool.toolName())) {
                throw new BusinessException("客户端工具名" + clientTool.toolName() + "重复");
            }
        }

        return AgentExecutionContext.builder()
                .agentCode(agentCode)
                .agent(agent)
                .agentConfig(agentRuntimeConfig)
                .agentScope(Collections.unmodifiableMap(agentConfig.getAgentScopeConfig()))
                .userId(request.getUserId())
                .requestId(request.getRequestId())
                .sessionId(request.getSessionId())
                .request(request)
                .clientTools(clientTools)
                .executionLog(executionLog)
                .build();
    }

    @Override
    public AgentExecutionResponse execute(AgentExecutionContext executionContext) {
        String agentCode = executionContext.getAgentCode();
        AgentExecutionRequest request = executionContext.getRequest();

        Agent agent = executionContext.getAgent();
        AgentExecutionLog executionLog = executionContext.getExecutionLog();

        // 保存执行日志
        executionLog = executionLogRepository.save(executionLog);

        long startTime = System.currentTimeMillis();

        try {
            // 3. 执行Agent
            log.info("开始执行Agent: code={}, sessionId={}, version={}",
                    agentCode, request.getSessionId(), agent.getVersion());

            // 检查是否被中断
            if (executionContext.isInterrupted()) {
                log.warn("Agent执行被中断: code={}, sessionId={}", agentCode, request.getSessionId());
                long executionTime = System.currentTimeMillis() - startTime;
                executionLog.markFailed("执行被用户中断", executionTime);
                executionLogRepository.update(executionLog);

                return AgentExecutionResponse.builder()
                        .logId(executionLog.getId())
                        .status(ExecutionStatus.FAILED.name())
                        .errorMessage("执行被用户中断")
                        .executionTimeMs(executionTime)
                        .build();
            }

            agent.execute(executionContext);
            AgentExecutionResponse response = executionContext.getResponse();
            response.setExecutionStartMs(startTime);
            response.setLogId(executionLog.getId());

            if (ExecutionStatus.SUCCESS.name().equals(response.getStatus())) {
                // 4. 记录成功
                long executionTime = System.currentTimeMillis() - startTime;
                executionLog.markSuccess(response.getOutput(), executionTime);
                log.info("Agent执行成功: code={}, executionTime={}ms", agentCode, executionTime);
                response.setExecutionTimeMs(executionTime);
                executionLogRepository.update(executionLog);
            } else if (ExecutionStatus.FAILED.name().equals(response.getStatus())) {
                // 5. 记录失败
                long executionTime = System.currentTimeMillis() - startTime;
                executionLog.markFailed(TraceIdUtil.getTraceId() + " " + response.getErrorMessage(), executionTime);
                log.info("Agent执行失败: code={}, executionTime={}ms, error={}", agentCode, executionTime, response.getErrorMessage());
                response.setExecutionTimeMs(executionTime);
                executionLogRepository.update(executionLog);
            }

            return response;

        } catch (Exception e) {
            // 5. 记录失败
            long executionTime = System.currentTimeMillis() - startTime;
            executionLog.markFailed(e.getMessage(), executionTime);
            executionLogRepository.update(executionLog);

            log.error("Agent执行失败: code={}, error={}", agentCode, e.getMessage(), e);

            // 如果有流式回调，通知错误
            if (executionContext.getAgentCallback() != null) {
                executionContext.getAgentCallback().onPrompt(AgentPromptType.ERROR, e.getMessage());
            }

            return AgentExecutionResponse.builder()
                    .logId(executionLog.getId())
                    .status(ExecutionStatus.FAILED.name())
                    .errorMessage(TraceIdUtil.getTraceId() + " " + e.getMessage())
                    .executionTimeMs(executionTime)
                    .build();
        }
    }
}
