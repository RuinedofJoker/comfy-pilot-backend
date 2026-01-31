package org.joker.comfypilot.agent.domain.service;

import lombok.extern.slf4j.Slf4j;
import org.joker.comfypilot.agent.application.dto.AgentExecutionRequest;
import org.joker.comfypilot.agent.application.dto.AgentExecutionResponse;
import org.joker.comfypilot.agent.domain.context.AgentExecutionContext;
import org.joker.comfypilot.agent.domain.enums.ExecutionStatus;
import org.joker.comfypilot.common.exception.BusinessException;

@Slf4j
public abstract class AbstractAgent implements Agent {

    @Override
    public void execute(AgentExecutionContext executionContext) {
        AgentExecutionRequest request = executionContext.getRequest();
        log.info("{}开始执行: sessionId={}, input={}, isStreamable={}",
                getClass().getSimpleName(),
                request.getSessionId(), request.getUserMessage(), request.getIsStreamable());

        try {
            if (Boolean.TRUE.equals(request.getIsStreamable())) {
                // 流式的agent调用
                executeWithStreaming(executionContext);

                executionContext.setResponse(
                        AgentExecutionResponse.builder()
                                .status(ExecutionStatus.RUNNING.name())
                                .build()
                );
            } else {
                // 非流式的agent调用
                String output = executeWithoutStreaming(executionContext);

                executionContext.setResponse(
                        AgentExecutionResponse.builder()
                                .output(output)
                                .status(ExecutionStatus.SUCCESS.name())
                                .build()
                );
            }
        } catch (Exception e) {
            log.error(getClass().getSimpleName() + "执行失败", e);
            executionContext.setResponse(
                    AgentExecutionResponse.builder()
                            .status(ExecutionStatus.FAILED.name())
                            .errorMessage(e.getMessage())
                            .build()
            );
        }
    }

    protected String executeWithoutStreaming(AgentExecutionContext executionContext) throws Exception  {
        throw new BusinessException("当前" + getClass() + "暂不支持非流式调用");
    }

    protected void executeWithStreaming(AgentExecutionContext executionContext) throws Exception {
        throw new BusinessException("当前" + getClass() + "暂不支持非流式调用");
    }

}
