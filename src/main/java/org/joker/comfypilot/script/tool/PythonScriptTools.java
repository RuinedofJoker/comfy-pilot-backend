package org.joker.comfypilot.script.tool;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.joker.comfypilot.common.annotation.ToolSet;
import org.joker.comfypilot.script.config.ScriptRuntimeConfig;
import org.joker.comfypilot.script.context.ScriptRuntimeContext;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Python 脚本执行工具集
 * <p>
 * 提供 Python 脚本执行、包管理等功能
 * <p>
 * 安全限制：
 * - 所有操作都需要 Python 运行时可用
 * - 脚本执行有超时限制
 * - 文件路径需要存在且可读
 */
@Slf4j
@Component
@ToolSet
@RequiredArgsConstructor
public class PythonScriptTools {

    private final ScriptRuntimeConfig config;

    /**
     * 执行 Python 脚本字符串
     *
     * @param scriptContent Python 脚本内容
     * @return 脚本执行输出
     * @throws IllegalStateException 如果 Python 不可用
     * @throws IOException           如果执行失败
     */
//    @Tool(name = "executePythonScript", value = "执行一段 Python 脚本代码字符串，返回执行输出结果")
    public String executeScript(
            @P(value = "要执行的 Python 脚本内容", required = true) String scriptContent
    ) throws IOException {

        // 检查 Python 是否可用
        ScriptRuntimeContext.requirePython();

        String pythonExecutable = ScriptRuntimeContext.getPythonExecutable();
        log.info("开始执行 Python 脚本，使用解释器: {}", pythonExecutable);
        log.debug("脚本内容长度: {} 字符", scriptContent.length());

        try {
            // 构建命令：python -c "script content"
            ProcessBuilder pb = new ProcessBuilder(
                    pythonExecutable,
                    "-c",
                    scriptContent
            );
            pb.redirectErrorStream(true);

            // 执行脚本
            Process process = pb.start();

            // 等待执行完成，带超时
            int timeoutSeconds = config.getPython().getScriptTimeout();
            boolean finished = process.waitFor(timeoutSeconds, TimeUnit.SECONDS);

            if (!finished) {
                process.destroyForcibly();
                String errorMsg = String.format("脚本执行超时（超过 %d 秒）", timeoutSeconds);
                log.error(errorMsg);
                throw new IOException(errorMsg);
            }

            // 读取输出
            String output = readProcessOutput(process);
            int exitCode = process.exitValue();

            if (exitCode != 0) {
                log.error("脚本执行失败，退出码: {}, 输出: {}", exitCode, output);
                throw new IOException("脚本执行失败（退出码: " + exitCode + "）\n输出:\n" + output);
            }

            log.info("脚本执行成功，输出长度: {} 字符", output.length());
            return output;

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("脚本执行被中断", e);
        }
    }

    /**
     * 读取进程输出
     *
     * @param process 进程对象
     * @return 进程输出内容
     * @throws IOException IO 异常
     */
    private String readProcessOutput(Process process) throws IOException {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
            StringBuilder output = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
            return output.toString().trim();
        }
    }

    /**
     * 使用 pip 安装 Python 包
     *
     * @param packageSpec 包规范，例如: "requests", "numpy==1.21.0", "pandas>=1.3.0"
     * @param extraArgs   额外的 pip 参数，例如: "--upgrade", "--no-cache-dir"（可选）
     * @return pip 执行输出
     * @throws IllegalStateException 如果 Python 不可用
     * @throws IOException           如果安装失败
     */
    @Tool(name = "pipInstall", value = "使用 pip 安装 Python 包，支持指定版本和额外参数")
    public String pipInstall(
            @P(value = "要安装的包名称或规范，例如: requests, numpy==1.21.0", required = true) String packageSpec,
            @P(value = "额外的 pip 参数，例如: --upgrade --no-cache-dir", required = false) String extraArgs
    ) throws IOException {

        // 检查 Python 是否可用
        ScriptRuntimeContext.requirePython();

        String pythonExecutable = ScriptRuntimeContext.getPythonExecutable();
        log.info("开始安装 Python 包: {}", packageSpec);
        if (extraArgs != null && !extraArgs.trim().isEmpty()) {
            log.info("额外参数: {}", extraArgs);
        }

        try {
            // 构建命令：python -m pip install <package> [extra args]
            List<String> command = new ArrayList<>();
            command.add(pythonExecutable);
            command.add("-m");
            command.add("pip");
            command.add("install");
            command.add(packageSpec);

            // 添加额外参数
            if (extraArgs != null && !extraArgs.trim().isEmpty()) {
                String[] args = extraArgs.trim().split("\\s+");
                for (String arg : args) {
                    if (!arg.isEmpty()) {
                        command.add(arg);
                    }
                }
            }

            log.debug("执行命令: {}", String.join(" ", command));

            ProcessBuilder pb = new ProcessBuilder(command);
            pb.redirectErrorStream(true);

            // 执行命令
            Process process = pb.start();

            // 等待执行完成，带超时
            int timeoutSeconds = config.getPython().getScriptTimeout();
            boolean finished = process.waitFor(timeoutSeconds, TimeUnit.SECONDS);

            if (!finished) {
                process.destroyForcibly();
                String errorMsg = String.format("pip 安装超时（超过 %d 秒）", timeoutSeconds);
                log.error(errorMsg);
                throw new IOException(errorMsg);
            }

            // 读取输出
            String output = readProcessOutput(process);
            int exitCode = process.exitValue();

            if (exitCode != 0) {
                log.error("pip 安装失败，退出码: {}, 输出: {}", exitCode, output);
                throw new IOException("pip 安装失败（退出码: " + exitCode + "）\n输出:\n" + output);
            }

            log.info("pip 安装成功: {}", packageSpec);
            return output;

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("pip 安装被中断", e);
        }
    }

    /**
     * 执行指定路径的 Python 脚本文件
     *
     * @param scriptPath 脚本文件的完整路径
     * @param args       传递给脚本的参数（可选）
     * @return 脚本执行输出
     * @throws IllegalStateException 如果 Python 不可用
     * @throws IOException           如果文件不存在或执行失败
     */
    @Tool(name = "executePythonFile", value = "执行指定路径的 Python 脚本文件，支持传递命令行参数")
    public String executeFile(
            @P(value = "Python 脚本文件的完整路径", required = true) String scriptPath,
            @P(value = "传递给脚本的命令行参数，多个参数用空格分隔", required = false) String args
    ) throws IOException {

        // 检查 Python 是否可用
        ScriptRuntimeContext.requirePython();

        // 检查文件是否存在
        Path filePath = Paths.get(scriptPath);
        if (!Files.exists(filePath)) {
            throw new IOException("脚本文件不存在: " + scriptPath);
        }
        if (!Files.isRegularFile(filePath)) {
            throw new IOException("不是文件: " + scriptPath);
        }
        if (!Files.isReadable(filePath)) {
            throw new IOException("文件不可读: " + scriptPath);
        }

        String pythonExecutable = ScriptRuntimeContext.getPythonExecutable();
        log.info("开始执行 Python 脚本文件: {}", scriptPath);
        if (args != null && !args.trim().isEmpty()) {
            log.info("脚本参数: {}", args);
        }

        try {
            // 构建命令：python <script_path> [args]
            List<String> command = new ArrayList<>();
            command.add(pythonExecutable);
            command.add(scriptPath);

            // 添加脚本参数
            if (args != null && !args.trim().isEmpty()) {
                String[] scriptArgs = args.trim().split("\\s+");
                for (String arg : scriptArgs) {
                    if (!arg.isEmpty()) {
                        command.add(arg);
                    }
                }
            }

            log.debug("执行命令: {}", String.join(" ", command));

            ProcessBuilder pb = new ProcessBuilder(command);
            pb.redirectErrorStream(true);

            // 执行脚本
            Process process = pb.start();

            // 等待执行完成，带超时
            int timeoutSeconds = config.getPython().getScriptTimeout();
            boolean finished = process.waitFor(timeoutSeconds, TimeUnit.SECONDS);

            if (!finished) {
                process.destroyForcibly();
                String errorMsg = String.format("脚本执行超时（超过 %d 秒）", timeoutSeconds);
                log.error(errorMsg);
                throw new IOException(errorMsg);
            }

            // 读取输出
            String output = readProcessOutput(process);
            int exitCode = process.exitValue();

            if (exitCode != 0) {
                log.error("脚本执行失败，退出码: {}, 输出: {}", exitCode, output);
                throw new IOException("脚本执行失败（退出码: " + exitCode + "）\n输出:\n" + output);
            }

            log.info("脚本执行成功，输出长度: {} 字符", output.length());
            return output;

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("脚本执行被中断", e);
        }
    }
}
