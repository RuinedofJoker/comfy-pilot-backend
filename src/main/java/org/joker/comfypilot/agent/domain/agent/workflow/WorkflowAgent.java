package org.joker.comfypilot.agent.domain.agent.workflow;

import lombok.extern.slf4j.Slf4j;
import org.joker.comfypilot.agent.application.dto.AgentExecutionRequest;
import org.joker.comfypilot.agent.application.dto.AgentExecutionResponse;
import org.joker.comfypilot.agent.domain.context.AgentExecutionContext;
import org.joker.comfypilot.agent.domain.enums.ExecutionStatus;
import org.joker.comfypilot.agent.domain.service.Agent;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 简单对话Agent
 * 用于基本的对话交互
 */
@Slf4j
@Component
public class WorkflowAgent implements Agent {

    @Override
    public String getAgentCode() {
        return "WORKFLOW_CHAT";
    }

    @Override
    public String getAgentName() {
        return "ComfyUI工作流编辑对话助手";
    }

    @Override
    public String getDescription() {
        return "提供ComfyUI工作流建议与指南的agent助手";
    }

    @Override
    public String getVersion() {
        return "1.0.0";
    }

    @Override
    public Map<String, Object> getAgentConfig() {
        Map<String, Object> config = new HashMap<>();
        config.put("temperature", 0.7);
        config.put("maxTokens", 20000);
        return config;
    }

    @Override
    public Map<String, Object> getAgentScopeConfig() {
        Map<String, Object> config = new HashMap<>();
        config.put("SystemPrompt", "你是一个友好的AI助手，专注于回答用户的问题。请保持回答简洁、准确、有帮助。");
        return config;
    }

    @Override
    public void execute(AgentExecutionContext executionContext) {
        AgentExecutionRequest request = executionContext.getRequest();
        log.info("SimpleAgent开始执行: sessionId={}, input={}, isStreamable={}",
                request.getSessionId(), request.getInput(), request.getIsStreamable());

        try {
            if (Boolean.TRUE.equals(request.getIsStreamable())) {
                // 流式的agent直接返回
                executionContext.setResponse(
                        AgentExecutionResponse.builder()
                                .status(ExecutionStatus.RUNNING.name())
                                .build()
                );
            } else {
                // TODO: 集成langchain4j实现真实的LLM调用
                // 当前为模拟实现
                String output = processInput(request.getInput());

                executionContext.setResponse(
                        AgentExecutionResponse.builder()
                                .output(output)
                                .status(ExecutionStatus.SUCCESS.name())
                                .build()
                );
            }
        } catch (Exception e) {
            log.error("WorkflowAgent执行失败", e);
            executionContext.setResponse(
                    AgentExecutionResponse.builder()
                            .status(ExecutionStatus.FAILED.name())
                            .errorMessage(e.getMessage())
                            .build()
            );
        }
    }

    /**
     * 处理用户输入（模拟实现）
     */
    private String processInput(String input) {
        // 模拟处理逻辑
        return "收到您的消息：" + input + "\n这是一个简单的回复示例。";
    }
}
