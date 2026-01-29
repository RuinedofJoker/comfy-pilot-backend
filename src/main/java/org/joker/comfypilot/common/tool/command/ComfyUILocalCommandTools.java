package org.joker.comfypilot.common.tool.command;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import lombok.extern.slf4j.Slf4j;
import org.joker.comfypilot.agent.domain.context.AgentExecutionContext;
import org.joker.comfypilot.agent.domain.context.AgentExecutionContextHolder;
import org.joker.comfypilot.cfsvr.application.dto.ComfyuiServerAdvancedFeaturesDTO;
import org.joker.comfypilot.common.annotation.ToolSet;
import org.joker.comfypilot.common.exception.BusinessException;
import org.joker.comfypilot.common.util.CommandResult;
import org.joker.comfypilot.common.util.CommandUtil;
import org.joker.comfypilot.session.domain.enums.AgentPromptType;
import org.springframework.stereotype.Component;

import java.io.*;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 本地命令执行工具集
 * <p>
 * 提供在 ComfyUI服务器上执行本地命令的功能
 * <p>
 * 安全限制：
 * - 命令执行有超时限制（默认 60 秒）
 * - 自动根据操作系统选择合适的 Shell（Windows: PowerShell/CMD, Linux/Mac: Bash）
 * - 支持指定工作目录
 * <p>
 * 使用场景：
 * - 执行系统命令（如 ls, pwd, whoami 等）
 * - 运行脚本文件
 * - 执行构建命令（如 npm, maven, gradle 等）
 * - 文件操作命令
 */
@Slf4j
@Component
@ToolSet
public class ComfyUILocalCommandTools {

    /**
     * 在 ComfyUI服务器上执行本地命令
     *
     * @param command    要执行的命令字符串（必填）
     * @return 命令执行结果，包含退出码、标准输出和错误输出
     * @throws IOException 如果命令执行失败或超时
     */
    @Tool(name = "executeComfyUILocalCommand", value = "在ComfyUI服务器上执行本地命令，默认使用UTF-8编码做终端输入与返回编码")
    public String executeCommand(
            @P(value = "要执行的命令字符串，例如: ls -la, pwd, npm install", required = true) String command
    ) throws IOException {

        log.info("开始执行本地命令: {}", command);

        AgentExecutionContext executionContext = AgentExecutionContextHolder.get();
        if (executionContext == null) {
            throw new BusinessException("找不到当前工具执行上下文");
        }
        Map<String, Object> agentScope = executionContext.getAgentScope();
        if (!Boolean.TRUE.equals(agentScope.get("AdvancedFeaturesEnabled"))) {
            throw new BusinessException("当前未开启ComfyUI高级功能");
        }

        if (agentScope.get("AdvancedFeatures") == null || !(agentScope.get("AdvancedFeatures") instanceof ComfyuiServerAdvancedFeaturesDTO advancedFeatures)) {
            throw new BusinessException("Agent上下文中找不到ComfyUI高级功能对象");
        }

        String workingDir = advancedFeatures.getWorkingDirectory();
        String environmentInitScript = advancedFeatures.getEnvironmentInitScript();

        try {
            executionContext.getAgentCallback().onPrompt(AgentPromptType.TERMINAL_OUTPUT_START, null, false);

            // 构建最终执行的命令
            String finalCommand = buildFinalCommand(command, environmentInitScript);
            log.debug("最终执行命令: {}", finalCommand);

            AtomicBoolean interrupted = new AtomicBoolean(false);

            // 使用 CommandUtil 执行命令
            CommandResult result;
            if (workingDir != null && !workingDir.trim().isEmpty()) {
                result = CommandUtil.executeWithRealTimeOutput(finalCommand, workingDir.trim(), (chunk, isError, process) -> {
                    executionContext.getAgentCallback().onStream((isError ? "1 " : "0 ") + chunk);
                    if (executionContext.isInterrupted() && interrupted.compareAndSet(false, true)) {
                        process.destroy();
                    }
                });
            } else {
                result = CommandUtil.executeWithRealTimeOutput(finalCommand, (chunk, isError, process) -> {
                    executionContext.getAgentCallback().onStream((isError ? "1 " : "0 ") + chunk);
                    if (executionContext.isInterrupted() && interrupted.compareAndSet(false, true)) {
                        process.destroy();
                    }
                });
            }

            // 构建返回结果
            StringBuilder output = new StringBuilder();
            output.append("命令执行完成\n");
            output.append("退出码: ").append(result.getExitCode()).append("\n");
            output.append("执行状态: ").append(result.isSuccess() ? "成功" : "失败").append("\n");
            output.append("\n");

            // 添加标准输出
            if (result.getOutput() != null && !result.getOutput().trim().isEmpty()) {
                output.append("标准输出:\n");
                output.append(result.getOutput().trim()).append("\n");
            }

            // 添加错误输出
            if (result.getError() != null && !result.getError().trim().isEmpty()) {
                output.append("\n错误输出:\n");
                output.append(result.getError().trim()).append("\n");
            }

            String resultStr = output.toString();
            log.info("命令执行完成，退出码: {}, 输出长度: {} 字符",
                    result.getExitCode(), resultStr.length());

            return resultStr;

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            String errorMsg = "命令执行被中断: " + command;
            log.error(errorMsg, e);
            throw new IOException(errorMsg, e);
        } catch (IOException e) {
            String errorMsg = "命令执行失败: " + command + ", 错误: " + e.getMessage();
            log.error(errorMsg, e);
            throw new IOException(errorMsg, e);
        } finally {
            executionContext.getAgentCallback().onPrompt(AgentPromptType.TERMINAL_OUTPUT_END, null, false);
        }
    }

    /**
     * 构建最终执行的命令
     * 如果有环境初始化脚本，则先执行初始化脚本，再执行用户命令
     *
     * @param userCommand            用户要执行的命令
     * @param environmentInitScript  环境初始化脚本（可选）
     * @return 最终执行的命令字符串
     */
    private String buildFinalCommand(String userCommand, String environmentInitScript) {
        // 如果没有初始化脚本，直接返回用户命令
        if (environmentInitScript == null || environmentInitScript.trim().isEmpty()) {
            return userCommand;
        }

        String initScript = environmentInitScript.trim();

        // 判断操作系统类型
        boolean isWindows = System.getProperty("os.name").toLowerCase().contains("windows");

        if (isWindows) {
            // Windows: 使用 && 连接命令（PowerShell 和 CMD 都支持）
            return initScript + " && " + userCommand;
        } else {
            // Linux/Mac: 使用 && 连接命令
            return initScript + " && " + userCommand;
        }
    }
}
