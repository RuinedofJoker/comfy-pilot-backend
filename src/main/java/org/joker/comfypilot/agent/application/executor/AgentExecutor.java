package org.joker.comfypilot.agent.application.executor;

import org.joker.comfypilot.agent.application.dto.AgentExecutionRequest;
import org.joker.comfypilot.agent.application.dto.AgentExecutionResponse;
import org.joker.comfypilot.agent.domain.context.AgentExecutionContext;

/**
 * Agent执行器接口
 */
public interface AgentExecutor {

    /**
     * 构建执行请求上下文
     *
     * @param agentCode Agent编码
     * @param request 执行请求
     * @return 执行请求上下文
     */
    AgentExecutionContext getExecutionContext(String agentCode, AgentExecutionRequest request);

    /**
     * 执行Agent
     *
     * @param executionContext   执行请求上下文
     * @return 执行响应
     */
    AgentExecutionResponse execute(AgentExecutionContext executionContext);
}
