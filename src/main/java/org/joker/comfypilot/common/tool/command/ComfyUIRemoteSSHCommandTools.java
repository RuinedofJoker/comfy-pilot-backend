package org.joker.comfypilot.common.tool.command;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import lombok.extern.slf4j.Slf4j;
import org.joker.comfypilot.agent.domain.context.AgentExecutionContext;
import org.joker.comfypilot.agent.domain.context.AgentExecutionContextHolder;
import org.joker.comfypilot.cfsvr.application.dto.ComfyuiServerAdvancedFeaturesDTO;
import org.joker.comfypilot.cfsvr.application.dto.SshConnectionConfigDTO;
import org.joker.comfypilot.common.annotation.ToolSet;
import org.joker.comfypilot.common.exception.BusinessException;
import org.joker.comfypilot.common.util.CommandResult;
import org.joker.comfypilot.common.util.SshCommandUtil;
import org.joker.comfypilot.session.domain.enums.AgentPromptType;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * ComfyUI 远程命令执行工具集
 * <p>
 * 提供通过 SSH 在 ComfyUI 服务器上执行远程命令的功能
 * <p>
 * 安全限制：
 * - 命令执行有超时限制（默认 1 小时）
 * - 使用 SSH 加密连接
 * - 支持密码认证和密钥认证
 * - 支持指定工作目录和环境初始化脚本
 * <p>
 * 使用场景：
 * - 在远程 ComfyUI 服务器上执行系统命令
 * - 运行脚本文件
 * - 执行构建命令（如 npm, pip, conda 等）
 * - 文件操作命令
 * - 环境管理命令
 */
@Slf4j
@Component
@ToolSet
public class ComfyUIRemoteSSHCommandTools {

    /**
     * 在 ComfyUI 服务器上通过 SSH 执行远程命令
     *
     * @param command 要执行的命令字符串（必填）
     * @return 命令执行结果，包含退出码、标准输出和错误输出
     * @throws IOException 如果命令执行失败或超时
     */
    @Tool(
            name = "executeComfyUIRemoteSSHCommand",
            value = "通过SSH在ComfyUI服务器上执行远程命令，默认使用UTF-8编码做终端输入与返回编码。" +
                    "支持执行复杂命令、脚本文件、环境管理等操作。"
    )
    public String executeComfyUIRemoteSSHCommand(
            @P(value = "要执行的命令字符串，例如: ls -la, pwd, python script.py, conda activate env", required = true)
            String command
    ) throws IOException {

        log.info("开始执行远程命令: {}", command);

        // 1. 获取 Agent 执行上下文
        AgentExecutionContext executionContext = AgentExecutionContextHolder.get();
        if (executionContext == null) {
            throw new BusinessException("找不到当前工具执行上下文");
        }

        // 2. 检查是否开启高级功能
        Map<String, Object> agentScope = executionContext.getAgentScope();
        if (!Boolean.TRUE.equals(agentScope.get("AdvancedFeaturesEnabled"))) {
            throw new BusinessException("当前未开启ComfyUI高级功能");
        }

        // 3. 获取高级功能配置
        if (agentScope.get("AdvancedFeatures") == null ||
                !(agentScope.get("AdvancedFeatures") instanceof ComfyuiServerAdvancedFeaturesDTO advancedFeatures)) {
            throw new BusinessException("Agent上下文中找不到ComfyUI高级功能对象");
        }

        // 5. 提取配置信息
        String workingDir = advancedFeatures.getWorkingDirectory();
        String environmentInitScript = advancedFeatures.getEnvironmentInitScript();

        try {
            // 6. 通知前端开始输出终端内容
            executionContext.getAgentCallback().onPrompt(AgentPromptType.TERMINAL_OUTPUT_START, null, false);

            // 7. 构建 SSH 连接配置
            SshCommandUtil.SshConfig sshConfig = buildSshConfig(advancedFeatures);

            // 8. 构建命令配置
            SshCommandUtil.SshCommandConfig.Builder configBuilder = SshCommandUtil.SshCommandConfig.builder()
                    .sshConfig(sshConfig)
                    .command(command);

            // 9. 设置工作目录
            if (workingDir != null && !workingDir.trim().isEmpty()) {
                configBuilder.workingDir(workingDir.trim());
            }

            // 10. 设置环境初始化脚本
            if (environmentInitScript != null && !environmentInitScript.trim().isEmpty()) {
                configBuilder.environmentInitScript(environmentInitScript.trim());
            }

            // 11. 设置实时输出回调和中断处理
            AtomicBoolean interrupted = new AtomicBoolean(false);
            configBuilder.outputCallback((chunk, isError, channel) -> {
                if (isError) {
                    chunk = "<span class=\"f-terminal-error\">" + chunk + "</span>";
                }
                // 实时输出到前端
                executionContext.getAgentCallback().onStream(chunk);

                // 检查是否需要中断
                if (executionContext.isInterrupted() && channel != null && interrupted.compareAndSet(false, true)) {
                    try {
                        log.warn("检测到中断信号，正在关闭 SSH 通道");
                        channel.close();
                    } catch (Exception e) {
                        log.error("关闭 SSH 通道失败", e);
                    }
                }
            });

            SshCommandUtil.SshCommandConfig commandConfig = configBuilder.build();

            // 12. 执行远程命令
            CommandResult result = SshCommandUtil.execute(commandConfig);

            // 13. 构建返回结果
            StringBuilder output = new StringBuilder();
            output.append("命令执行完成\n");
            output.append("退出码: ").append(result.getExitCode()).append("\n");
            output.append("执行状态: ").append(result.isSuccess() ? "成功" : "失败").append("\n");
            output.append("\n");

            // 14. 添加标准输出
            if (result.getOutput() != null && !result.getOutput().trim().isEmpty()) {
                output.append("标准输出:\n");
                output.append(result.getOutput().trim()).append("\n");
            }

            // 15. 添加错误输出
            if (result.getError() != null && !result.getError().trim().isEmpty()) {
                output.append("\n错误输出:\n");
                output.append(result.getError().trim()).append("\n");
            }

            String resultStr = output.toString();
            log.info("远程命令执行完成，退出码: {}, 输出长度: {} 字符",
                    result.getExitCode(), resultStr.length());

            return resultStr;

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            String errorMsg = "远程命令执行被中断: " + command;
            log.error(errorMsg, e);
            throw new IOException(errorMsg, e);
        } catch (IOException e) {
            String errorMsg = "远程命令执行失败: " + command + ", 错误: " + e.getMessage();
            log.error(errorMsg, e);
            throw new IOException(errorMsg, e);
        } finally {
            // 16. 通知前端结束终端输出
            executionContext.getAgentCallback().onPrompt(AgentPromptType.TERMINAL_OUTPUT_END, null, false);
        }
    }

    /**
     * 构建 SSH 连接配置
     *
     * @param advancedFeatures 高级功能配置
     * @return SSH 连接配置
     */
    private SshCommandUtil.SshConfig buildSshConfig(
            ComfyuiServerAdvancedFeaturesDTO advancedFeatures
    ) {
        SshCommandUtil.SshConfig.Builder builder = SshCommandUtil.SshConfig.builder();

        SshConnectionConfigDTO sshConfig = advancedFeatures.getSshConfig();

        // 1. 设置主机和端口
        String sshHost = sshConfig.getHost();
        Integer sshPort = sshConfig.getPort();

        if (sshHost == null || sshHost.trim().isEmpty()) {
            throw new BusinessException("SSH 主机地址不能为空");
        }

        builder.host(sshHost.trim());

        if (sshPort != null && sshPort > 0) {
            builder.port(sshPort);
        }

        // 2. 设置用户名
        String sshUsername = sshConfig.getUsername();
        if (sshUsername == null || sshUsername.trim().isEmpty()) {
            throw new BusinessException("SSH 用户名不能为空");
        }
        builder.username(sshUsername.trim());

        // 3. 设置认证方式（密码、私钥内容或私钥路径）
        String sshPassword = sshConfig.getPassword();
        String sshPrivateKeyContent = sshConfig.getPrivateKeyContent();

        if (sshPassword != null && !sshPassword.trim().isEmpty()) {
            // 使用密码认证
            builder.password(sshPassword);
            log.debug("使用密码认证连接 SSH 服务器");
        } else if (sshPrivateKeyContent != null && !sshPrivateKeyContent.trim().isEmpty()) {
            // 使用私钥内容认证（优先）
            builder.privateKeyContent(sshPrivateKeyContent.trim());
            log.debug("使用私钥内容认证连接 SSH 服务器");
        } else {
            throw new BusinessException("必须提供 SSH 密码、私钥内容或私钥路径");
        }

        return builder.build();
    }
}
