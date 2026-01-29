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
import java.util.stream.Collectors;

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
     * 输出回调接口（带进程控制）
     * 参数：输出内容、是否错误输出、Process 对象（用于中断）
     */
    @FunctionalInterface
    public interface OutputCallbackWithProcess {
        void accept(String output, Boolean isError, Process process);
    }

    /**
     * 默认超时时间（秒）
     */
    private static final long DEFAULT_TIMEOUT = 60 * 60;

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
                .charset(StandardCharsets.UTF_8)
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
    public static CommandResult executeWithRealTimeOutput(
            String command,
            OutputCallbackWithProcess outputCallback
    ) throws IOException, InterruptedException {
        return executeWithRealTimeOutput(command, null, outputCallback);
    }

    /**
     * 执行命令并实时输出（使用默认配置）
     *
     * @param command        命令字符串
     * @param workingDir     工作目录
     * @param outputCallback 实时输出回调（每行输出都会调用）
     * @return 命令执行结果
     * @throws IOException          IO 异常
     * @throws InterruptedException 中断异常
     */
    public static CommandResult executeWithRealTimeOutput(
            String command,
            String workingDir,
            OutputCallbackWithProcess outputCallback
    ) throws IOException, InterruptedException {
        CommandConfig config = CommandConfig.builder()
                .command(command)
                .charset(StandardCharsets.UTF_8)
                .workingDir(workingDir)
                .outputCallbackWithProcess(outputCallback)
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

        // 7. 输出命令本身（如果有回调）
        if (config.outputCallbackWithProcess != null) {
            config.outputCallbackWithProcess.accept(commandList.stream().collect(Collectors.joining(System.lineSeparator())) + System.lineSeparator(), false, null);
        }

        // 6. 启动进程
        Process process = processBuilder.start();

        // 8. 读取输出
        StringBuilder output = new StringBuilder();
        StringBuilder error = new StringBuilder();

        Thread outputThread = new Thread(() -> {
            char[] buf = new char[1024];
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream(), config.charset))) {
                int flag;
                while ((flag = reader.read(buf, 0, 1024)) != -1) {
                    String next = new String(buf, 0, flag);
                    // 实时输出回调
                    if (config.outputCallbackWithProcess != null) {
                        config.outputCallbackWithProcess.accept(next, false, process);
                    }
                    output.append(next);
                }
            } catch (IOException e) {
                log.error("读取标准输出失败", e);
            }
        });

        Thread errorThread = new Thread(() -> {
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getErrorStream(), config.charset))) {
                char[] buf = new char[1024];
                int flag;
                while ((flag = reader.read(buf, 0, 1024)) != -1) {
                    String next = new String(buf, 0, flag);
                    // 实时输出回调
                    if (config.outputCallbackWithProcess != null) {
                        config.outputCallbackWithProcess.accept(next, true, process);
                    }
                    error.append(next);
                }
            } catch (IOException e) {
                log.error("读取错误输出失败", e);
            }
        });

        outputThread.start();
        errorThread.start();

        // 9. 等待进程完成（带超时）
        boolean finished = process.waitFor(config.timeout, TimeUnit.SECONDS);

        if (!finished) {
            process.destroyForcibly();
            throw new IOException("命令执行超时: " + config.timeout + " 秒");
        }

        // 10. 等待输出线程完成
        outputThread.join();
        errorThread.join();

        // 11. 获取退出码
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
            case POWERSHELL -> {
                commandList.add("cmd");
                commandList.add("/c");

                // 构建完整的 PowerShell 命令（包含编码设置）
                String fullCommand = getPowerShellEncodingPrefix(StandardCharsets.UTF_8) + command;

                // 转义命令中的特殊字符（主要是引号）
                String escapedCommand = escapeForCmdPowerShell(fullCommand);

                // chcp 65001 设置 UTF-8 代码页，>nul 隐藏输出
                // 然后启动 PowerShell 执行命令
                commandList.add("chcp 65001 >nul && powershell -Command \"" + escapedCommand + "\"");
            }
            case CMD -> {
                commandList.add("cmd");
                commandList.add("/c");

                // 先设置代码页，再执行命令
                String chcpPrefix = getCmdCodePagePrefix(StandardCharsets.UTF_8);
                commandList.add(chcpPrefix + command);
            }
            case BASH -> {
                commandList.add("bash");
                commandList.add("-c");
                String langPrefix = getBashLangPrefix(StandardCharsets.UTF_8);
                commandList.add(langPrefix + command);
            }
            case SH -> {
                commandList.add("sh");
                commandList.add("-c");
                String langPrefix = getBashLangPrefix(StandardCharsets.UTF_8);
                commandList.add(langPrefix + command);
            }
            default -> {
                throw new IllegalArgumentException("不支持的终端类型: " + shellType);
            }
        }

        return commandList;
    }

    /**
     * 获取 PowerShell 编码设置前缀
     * 根据指定的字符编码生成 PowerShell 编码设置命令
     *
     * @param charset 字符编码
     * @return PowerShell 编码设置命令前缀
     */
    private static String getPowerShellEncodingPrefix(Charset charset) {
        String encodingName;

        if (charset.equals(StandardCharsets.UTF_8)) {
            encodingName = "UTF8";
        } else if (charset.equals(StandardCharsets.UTF_16)) {
            encodingName = "Unicode";
        } else if (charset.equals(StandardCharsets.UTF_16BE)) {
            encodingName = "BigEndianUnicode";
        } else if (charset.equals(StandardCharsets.UTF_16LE)) {
            encodingName = "Unicode";
        } else if (charset.equals(StandardCharsets.US_ASCII)) {
            encodingName = "ASCII";
        } else if (charset.equals(StandardCharsets.ISO_8859_1)) {
            encodingName = "Latin1";
        } else if (charset.name().equalsIgnoreCase("GBK")) {
            encodingName = "Default";  // GBK 使用系统默认编码
        } else {
            // 其他编码尝试使用编码名称
            encodingName = charset.name();
        }

        // 同时设置三个编码相关配置，确保输入输出编码一致
        // 1. [Console]::OutputEncoding - 控制台输出编码
        // 2. [Console]::InputEncoding - 控制台输入编码
        // 3. $OutputEncoding - PowerShell 管道输出编码
        return "[Console]::OutputEncoding = [System.Text.Encoding]::" + encodingName + "; " +
                "[Console]::InputEncoding = [System.Text.Encoding]::" + encodingName + "; " +
                "$OutputEncoding = [System.Text.Encoding]::" + encodingName + "; ";
    }

    /**
     * 获取 CMD 代码页设置前缀
     * 根据指定的字符编码生成 CMD chcp 命令
     *
     * @param charset 字符编码
     * @return CMD 代码页设置命令前缀
     */
    private static String getCmdCodePagePrefix(Charset charset) {
        int codePage;

        if (charset.equals(StandardCharsets.UTF_8)) {
            codePage = 65001;  // UTF-8
        } else if (charset.equals(StandardCharsets.US_ASCII)) {
            codePage = 20127;  // US-ASCII
        } else if (charset.equals(StandardCharsets.ISO_8859_1)) {
            codePage = 28591;  // ISO-8859-1
        } else if (charset.name().equalsIgnoreCase("GBK")) {
            codePage = 936;    // GBK/GB2312
        } else if (charset.name().equalsIgnoreCase("GB2312")) {
            codePage = 936;    // GB2312
        } else if (charset.name().equalsIgnoreCase("Big5")) {
            codePage = 950;    // Big5
        } else if (charset.name().equalsIgnoreCase("Shift_JIS")) {
            codePage = 932;    // Shift_JIS
        } else if (charset.name().equalsIgnoreCase("EUC-KR")) {
            codePage = 949;    // EUC-KR
        } else {
            // 默认使用 UTF-8
            codePage = 65001;
        }

        // 移除所有重定向，确保 chcp 命令完全生效
        // chcp 命令需要输出到控制台才能真正改变编码
        return "chcp " + codePage + " && ";
    }

    /**
     * 获取 Bash/Sh LANG 环境变量设置前缀
     * 根据指定的字符编码生成 LANG 环境变量设置命令
     *
     * @param charset 字符编码
     * @return Bash LANG 环境变量设置命令前缀
     */
    private static String getBashLangPrefix(Charset charset) {
        String langValue;

        if (charset.equals(StandardCharsets.UTF_8)) {
            langValue = "en_US.UTF-8";
        } else if (charset.equals(StandardCharsets.US_ASCII)) {
            langValue = "C";
        } else if (charset.equals(StandardCharsets.ISO_8859_1)) {
            langValue = "en_US.ISO-8859-1";
        } else if (charset.name().equalsIgnoreCase("GBK")) {
            langValue = "zh_CN.GBK";
        } else if (charset.name().equalsIgnoreCase("GB2312")) {
            langValue = "zh_CN.GB2312";
        } else if (charset.name().equalsIgnoreCase("Big5")) {
            langValue = "zh_TW.Big5";
        } else if (charset.name().equalsIgnoreCase("Shift_JIS")) {
            langValue = "ja_JP.SJIS";
        } else if (charset.name().equalsIgnoreCase("EUC-KR")) {
            langValue = "ko_KR.EUC-KR";
        } else {
            // 默认使用 UTF-8
            langValue = "en_US.UTF-8";
        }

        return "export LANG=" + langValue + "; export LC_ALL=" + langValue + "; ";
    }

    /**
     * 转义命令字符串，使其可以安全地在 CMD 中传递给 PowerShell
     * <p>
     * 转义规则：
     * 1. 双引号 " 转义为 \"
     * 2. 反斜杠 \ 在双引号前需要转义为 \\
     *
     * @param command 原始命令字符串
     * @return 转义后的命令字符串
     */
    private static String escapeForCmdPowerShell(String command) {
        if (command == null || command.isEmpty()) {
            return command;
        }

        // 替换反斜杠（必须先处理反斜杠，再处理引号）
        // 将 \ 替换为 \\，但只在引号前的反斜杠需要转义
        String escaped = command.replace("\\", "\\\\");

        // 替换双引号
        escaped = escaped.replace("\"", "\\\"");

        return escaped;
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
         * 实时输出回调（带进程控制，每行输出都会调用）
         */
        private final OutputCallbackWithProcess outputCallbackWithProcess;

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
            this.outputCallbackWithProcess = builder.outputCallbackWithProcess;
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
            private OutputCallbackWithProcess outputCallbackWithProcess;
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

            public Builder outputCallbackWithProcess(OutputCallbackWithProcess outputCallbackWithProcess) {
                this.outputCallbackWithProcess = outputCallbackWithProcess;
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
