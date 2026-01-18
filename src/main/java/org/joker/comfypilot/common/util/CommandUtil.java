package org.joker.comfypilot.common.util;

import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * 命令执行工具类
 * 基于 ProcessBuilder 实现本机命令执行功能
 *
 * <p>主要功能：</p>
 * <ul>
 *   <li>支持同步/异步命令执行</li>
 *   <li>支持超时控制</li>
 *   <li>支持工作目录设置</li>
 *   <li>支持环境变量配置</li>
 *   <li>支持输出重定向</li>
 *   <li>支持字符编码配置</li>
 *   <li>自动区分 Windows/Linux 命令</li>
 *   <li>完整的错误处理和日志记录</li>
 * </ul>
 */
@Slf4j
public class CommandUtil {

    /**
     * 默认超时时间（秒）
     */
    private static final long DEFAULT_TIMEOUT = 60;

    /**
     * 默认字符编码
     */
    private static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;

    /**
     * Windows 系统标识
     */
    private static final boolean IS_WINDOWS = System.getProperty("os.name").toLowerCase().contains("windows");

    /**
     * PowerShell 可用性（延迟初始化）
     */
    private static Boolean isPowerShellAvailable = null;

    /**
     * 执行命令（使用默认配置）
     *
     * @param command 命令字符串
     * @return 命令执行结果
     * @throws IOException          IO 异常
     * @throws InterruptedException 中断异常
     */
    public static CommandResult execute(String command) throws IOException, InterruptedException {
        return execute(command, null);
    }

    /**
     * 执行命令并指定工作目录
     *
     * @param command    命令字符串
     * @param workingDir 工作目录
     * @return 命令执行结果
     * @throws IOException          IO 异常
     * @throws InterruptedException 中断异常
     */
    public static CommandResult execute(String command, String workingDir)
            throws IOException, InterruptedException {
        CommandConfig config = CommandConfig.builder()
                .command(command)
                .workingDir(workingDir)
                .build();
        return execute(config);
    }

    /**
     * 执行命令并实时输出（使用默认配置）
     *
     * @param command        命令字符串
     * @param outputCallback 实时输出回调（每行输出都会调用）
     * @return 命令执行结果
     * @throws IOException          IO 异常
     * @throws InterruptedException 中断异常
     */
    public static CommandResult executeWithRealTimeOutput(String command,
                                                          java.util.function.Consumer<String> outputCallback)
            throws IOException, InterruptedException {
        CommandConfig config = CommandConfig.builder()
                .command(command)
                .outputCallback(outputCallback)
                .build();
        return execute(config);
    }

    /**
     * 使用自定义配置执行命令
     *
     * @param config 命令配置
     * @return 命令执行结果
     * @throws IOException          IO 异常
     * @throws InterruptedException 中断异常
     */
    public static CommandResult execute(CommandConfig config) throws IOException, InterruptedException {
        log.info("执行命令: {}", config.command);

        // 1. 构建命令数组
        List<String> commandList = buildCommandList(config.command, config.shellType);

        // 2. 创建 ProcessBuilder
        ProcessBuilder processBuilder = new ProcessBuilder(commandList);

        // 3. 设置工作目录
        if (config.workingDir != null && !config.workingDir.isEmpty()) {
            processBuilder.directory(new File(config.workingDir));
            log.debug("工作目录: {}", config.workingDir);
        }

        // 4. 设置环境变量
        if (config.environment != null && !config.environment.isEmpty()) {
            Map<String, String> env = processBuilder.environment();
            env.putAll(config.environment);
            log.debug("添加环境变量: {}", config.environment.size());
        }

        // 5. 合并错误流到标准输出
        processBuilder.redirectErrorStream(config.redirectErrorStream);

        // 6. 启动进程
        Process process = processBuilder.start();

        // 7. 读取输出
        StringBuilder output = new StringBuilder();
        StringBuilder error = new StringBuilder();

        Thread outputThread = new Thread(() -> {
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream(), config.charset))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append(System.lineSeparator());
                    // 实时输出回调
                    if (config.outputCallback != null) {
                        config.outputCallback.accept(line);
                    }
                }
            } catch (IOException e) {
                log.error("读取标准输出失败", e);
            }
        });

        Thread errorThread = new Thread(() -> {
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getErrorStream(), config.charset))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    error.append(line).append(System.lineSeparator());
                }
            } catch (IOException e) {
                log.error("读取错误输出失败", e);
            }
        });

        outputThread.start();
        errorThread.start();

        // 8. 等待进程完成（带超时）
        boolean finished = process.waitFor(config.timeout, TimeUnit.SECONDS);

        if (!finished) {
            process.destroyForcibly();
            throw new IOException("命令执行超时: " + config.timeout + " 秒");
        }

        // 9. 等待输出线程完成
        outputThread.join();
        errorThread.join();

        // 10. 获取退出码
        int exitCode = process.exitValue();

        log.info("命令执行完成，退出码: {}", exitCode);

        return new CommandResult(exitCode, output.toString(), error.toString());
    }

    /**
     * 构建命令列表（自动适配 Windows/Linux）
     *
     * @param command 命令字符串
     * @return 命令列表
     */
    private static List<String> buildCommandList(String command) {
        return buildCommandList(command, ShellType.AUTO);
    }

    /**
     * 构建命令列表（支持手动指定终端类型）
     *
     * @param command   命令字符串
     * @param shellType 终端类型
     * @return 命令列表
     */
    private static List<String> buildCommandList(String command, ShellType shellType) {
        List<String> commandList = new ArrayList<>();

        // 如果是自动选择，根据操作系统决定
        if (shellType == ShellType.AUTO) {
            if (IS_WINDOWS) {
                // Windows 系统优先使用 PowerShell，其次使用 cmd
                if (isPowerShellAvailable()) {
                    shellType = ShellType.POWERSHELL;
                } else {
                    shellType = ShellType.CMD;
                }
            } else {
                // Linux/Mac 系统使用 bash
                shellType = ShellType.BASH;
            }
        }

        // 根据终端类型构建命令
        switch (shellType) {
            case POWERSHELL:
                commandList.add("powershell");
                commandList.add("-Command");
                commandList.add(command);
                break;

            case CMD:
                commandList.add("cmd");
                commandList.add("/c");
                commandList.add(command);
                break;

            case BASH:
                commandList.add("bash");
                commandList.add("-c");
                commandList.add(command);
                break;

            case SH:
                commandList.add("sh");
                commandList.add("-c");
                commandList.add(command);
                break;

            default:
                throw new IllegalArgumentException("不支持的终端类型: " + shellType);
        }

        return commandList;
    }

    /**
     * 检测 PowerShell 是否可用（延迟初始化，结果会被缓存）
     *
     * @return true 表示 PowerShell 可用，false 表示不可用
     */
    private static boolean isPowerShellAvailable() {
        if (isPowerShellAvailable != null) {
            return isPowerShellAvailable;
        }

        // 尝试执行 PowerShell 版本命令来检测是否可用
        try {
            ProcessBuilder pb = new ProcessBuilder("powershell", "-Command", "$PSVersionTable.PSVersion.Major");
            pb.redirectErrorStream(true);
            Process process = pb.start();

            boolean finished = process.waitFor(5, TimeUnit.SECONDS);

            if (finished && process.exitValue() == 0) {
                isPowerShellAvailable = true;
                log.info("检测到 PowerShell 可用，将优先使用 PowerShell 执行命令");
            } else {
                isPowerShellAvailable = false;
                log.warn("PowerShell 不可用，将使用 cmd 执行命令");
            }
        } catch (Exception e) {
            isPowerShellAvailable = false;
            log.warn("PowerShell 检测失败，将使用 cmd 执行命令: {}", e.getMessage());
        }

        return isPowerShellAvailable;
    }

    /**
     * 命令配置类
     * 用于配置命令执行的各种参数
     */
    public static class CommandConfig {
        /**
         * 命令字符串（必填）
         */
        private final String command;

        /**
         * 工作目录（可选）
         */
        private final String workingDir;

        /**
         * 超时时间（秒）
         */
        private final long timeout;

        /**
         * 字符编码
         */
        private final Charset charset;

        /**
         * 环境变量
         */
        private final Map<String, String> environment;

        /**
         * 是否合并错误流到标准输出
         */
        private final boolean redirectErrorStream;

        /**
         * 实时输出回调（每行输出都会调用）
         */
        private final Consumer<String> outputCallback;

        /**
         * 终端类型（默认自动选择）
         */
        private final ShellType shellType;

        private CommandConfig(Builder builder) {
            this.command = builder.command;
            this.workingDir = builder.workingDir;
            this.timeout = builder.timeout;
            this.charset = builder.charset;
            this.environment = builder.environment;
            this.redirectErrorStream = builder.redirectErrorStream;
            this.outputCallback = builder.outputCallback;
            this.shellType = builder.shellType;
        }

        public static Builder builder() {
            return new Builder();
        }

        /**
         * 命令配置构建器
         */
        public static class Builder {
            private String command;
            private String workingDir;
            private long timeout = DEFAULT_TIMEOUT;
            private Charset charset = DEFAULT_CHARSET;
            private Map<String, String> environment;
            private boolean redirectErrorStream = false;
            private java.util.function.Consumer<String> outputCallback;
            private ShellType shellType = ShellType.AUTO;

            public Builder command(String command) {
                this.command = command;
                return this;
            }

            public Builder workingDir(String workingDir) {
                this.workingDir = workingDir;
                return this;
            }

            public Builder timeout(long timeout) {
                this.timeout = timeout;
                return this;
            }

            public Builder charset(Charset charset) {
                this.charset = charset;
                return this;
            }

            public Builder environment(Map<String, String> environment) {
                this.environment = environment;
                return this;
            }

            public Builder addEnvironment(String key, String value) {
                if (this.environment == null) {
                    this.environment = new java.util.HashMap<>();
                }
                this.environment.put(key, value);
                return this;
            }

            public Builder redirectErrorStream(boolean redirectErrorStream) {
                this.redirectErrorStream = redirectErrorStream;
                return this;
            }

            public Builder outputCallback(java.util.function.Consumer<String> outputCallback) {
                this.outputCallback = outputCallback;
                return this;
            }

            public Builder shellType(ShellType shellType) {
                this.shellType = shellType;
                return this;
            }

            public CommandConfig build() {
                if (command == null || command.isEmpty()) {
                    throw new IllegalArgumentException("命令不能为空");
                }
                return new CommandConfig(this);
            }
        }
    }
}
