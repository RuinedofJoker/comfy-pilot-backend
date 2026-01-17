package org.joker.comfypilot.tool.application.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.joker.comfypilot.common.exception.BusinessException;
import org.joker.comfypilot.tool.application.service.ToolExecutor;
import org.joker.comfypilot.tool.domain.enums.ToolType;
import org.joker.comfypilot.tool.domain.service.Tool;
import org.joker.comfypilot.tool.domain.service.ToolRegistry;
import org.joker.comfypilot.tool.domain.valueobject.ToolExecutionResult;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * 工具执行器实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ToolExecutorImpl implements ToolExecutor {

    private final ToolRegistry toolRegistry;

    @Override
    public ToolExecutionResult execute(ToolType type, Map<String, Object> parameters) {
        log.info("执行工具: type={}, parameters={}", type, parameters);

        Tool tool = toolRegistry.getTool(type);
        if (tool == null) {
            String errorMsg = "工具不存在: " + type;
            log.error(errorMsg);
            return ToolExecutionResult.failure(errorMsg);
        }

        try {
            long startTime = System.currentTimeMillis();
            ToolExecutionResult result = tool.execute(parameters);
            long executionTime = System.currentTimeMillis() - startTime;

            // 设置执行时间
            if (result.getMetadata() != null) {
                result.getMetadata().setExecutionTimeMs(executionTime);
            }

            log.info("工具执行完成: type={}, success={}, executionTime={}ms",
                    type, result.isSuccess(), executionTime);

            return result;
        } catch (Exception e) {
            log.error("工具执行失败: type={}", type, e);
            return ToolExecutionResult.failure("工具执行失败: " + e.getMessage());
        }
    }

    @Override
    public ToolExecutionResult execute(String toolName, Map<String, Object> parameters) {
        log.info("执行工具: name={}, parameters={}", toolName, parameters);

        Tool tool = toolRegistry.getTool(toolName);
        if (tool == null) {
            String errorMsg = "工具不存在: " + toolName;
            log.error(errorMsg);
            return ToolExecutionResult.failure(errorMsg);
        }

        return execute(tool.getType(), parameters);
    }
}
