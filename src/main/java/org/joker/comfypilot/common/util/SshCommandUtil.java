package org.joker.comfypilot.common.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.sshd.client.SshClient;
import org.apache.sshd.client.channel.ClientChannel;
import org.apache.sshd.client.channel.ClientChannelEvent;
import org.apache.sshd.client.session.ClientSession;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * SSH 命令执行工具类
 * 基于 Apache MINA SSHD 实现远程命令执行功能
 *
 * <p>主要功能：</p>
 * <ul>
 *   <li>支持 SSH 密码认证和密钥认证</li>
 *   <li>支持 Shell 通道交互式命令执行</li>
 *   <li>支持实时输出回调</li>
 *   <li>支持超时控制</li>
 *   <li>支持环境变量设置</li>
 *   <li>支持工作目录切换</li>
 *   <li>完整的错误处理和日志记录</li>
 * </ul>
 *
 * <p>使用示例：</p>
 * <pre>
 * // 1. 创建 SSH 配置
 * SshConfig config = SshConfig.builder()
 *     .host("192.168.1.100")
 *     .port(22)
 *     .username("user")
 *     .password("password")
 *     .build();
 *
 * // 2. 执行命令
 * CommandResult result = SshCommandUtil.execute(config, "ls -la");
 *
 * // 3. 实时输出
 * SshCommandUtil.executeWithRealTimeOutput(config, "npm install", (output, isError, process) -> {
 *     System.out.println(output);
 * });
 * </pre>
 */
@Slf4j
public class SshCommandUtil {

    /**
     * 默认超时时间（秒）
     */
    private static final long DEFAULT_TIMEOUT = 60 * 60;

    /**
     * 默认字符编码
     */
    private static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;

    /**
     * 默认 SSH 端口
     */
    private static final int DEFAULT_SSH_PORT = 22;

    /**
     * 输出回调接口（带 ClientChannel 控制）
     * 参数：输出内容、是否错误输出、ClientChannel 对象（用于中断）
     */
    @FunctionalInterface
    public interface OutputCallbackWithChannel {
        void accept(String output, Boolean isError, ClientChannel channel);
    }

    /**
     * 执行 SSH 命令（使用默认配置）
     *
     * @param sshConfig SSH 连接配置
     * @param command   命令字符串
     * @return 命令执行结果
     * @throws IOException          IO 异常
     * @throws InterruptedException 中断异常
     */
    public static CommandResult execute(SshConfig sshConfig, String command)
            throws IOException, InterruptedException {
        SshCommandConfig config = SshCommandConfig.builder()
                .sshConfig(sshConfig)
                .command(command)
                .charset(DEFAULT_CHARSET)
                .timeout(DEFAULT_TIMEOUT)
                .build();
        return execute(config);
    }

    /**
     * 执行 SSH 命令并实时输出
     *
     * @param sshConfig      SSH 连接配置
     * @param command        命令字符串
     * @param outputCallback 实时输出回调（每行输出都会调用）
     * @return 命令执行结果
     * @throws IOException          IO 异常
     * @throws InterruptedException 中断异常
     */
    public static CommandResult executeWithRealTimeOutput(
            SshConfig sshConfig,
            String command,
            OutputCallbackWithChannel outputCallback
    ) throws IOException, InterruptedException {
        SshCommandConfig config = SshCommandConfig.builder()
                .sshConfig(sshConfig)
                .command(command)
                .charset(DEFAULT_CHARSET)
                .timeout(DEFAULT_TIMEOUT)
                .outputCallback(outputCallback)
                .build();
        return execute(config);
    }

    /**
     * 使用自定义配置执行 SSH 命令
     *
     * @param config SSH 命令配置
     * @return 命令执行结果
     * @throws IOException          IO 异常
     * @throws InterruptedException 中断异常
     */
    public static CommandResult execute(SshCommandConfig config) throws IOException, InterruptedException {
        log.info("执行 SSH 命令: {}", config.command);

        SshClient client = null;
        ClientSession session = null;

        try {
            // 1. 创建并启动 SSH 客户端
            client = SshClient.setUpDefaultClient();
            client.start();
            log.debug("SSH 客户端已启动");

            // 2. 连接到 SSH 服务器
            SshConfig sshConfig = config.sshConfig;
            session = client.connect(
                    sshConfig.username,
                    sshConfig.host,
                    sshConfig.port
            ).verify(10, TimeUnit.SECONDS).getSession();
            log.debug("已连接到 SSH 服务器: {}:{}", sshConfig.host, sshConfig.port);

            // 3. 认证
            if (sshConfig.password != null && !sshConfig.password.isEmpty()) {
                // 密码认证
                session.addPasswordIdentity(sshConfig.password);
                log.debug("使用密码认证");
            } else if (sshConfig.privateKeyContent != null && !sshConfig.privateKeyContent.isEmpty()) {
                // 使用私钥内容认证
                loadPrivateKeyFromContent(session, sshConfig.privateKeyContent);
                log.debug("使用私钥内容认证");
            } else {
                throw new IllegalArgumentException("必须提供密码、私钥内容或私钥路径");
            }

            session.auth().verify(10, TimeUnit.SECONDS);
            log.debug("SSH 认证成功");

            // 4. 构建最终执行的命令
            String finalCommand = buildFinalCommand(config);
            log.debug("最终执行命令: {}", finalCommand);

            // 5. 创建 Shell 通道并执行命令
            CommandResult result = executeInShellChannel(session, finalCommand, config);

            log.info("SSH 命令执行完成，退出码: {}", result.getExitCode());
            return result;

        } finally {
            // 清理资源
            if (session != null) {
                try {
                    session.close();
                } catch (Exception e) {
                    log.warn("关闭 SSH 会话失败", e);
                }
            }
            if (client != null) {
                try {
                    client.stop();
                } catch (Exception e) {
                    log.warn("停止 SSH 客户端失败", e);
                }
            }
        }
    }

    /**
     * 在 Shell 通道中执行命令
     *
     * @param session SSH 会话
     * @param command 要执行的命令
     * @param config  命令配置
     * @return 命令执行结果
     * @throws IOException          IO 异常
     * @throws InterruptedException 中断异常
     */
    private static CommandResult executeInShellChannel(
            ClientSession session,
            String command,
            SshCommandConfig config
    ) throws IOException, InterruptedException {

        // 创建管道流（用于实时写入命令）
        PipedOutputStream pipedOut = new PipedOutputStream();
        PipedInputStream pipedIn = new PipedInputStream(pipedOut);

        PipedOutputStream outputStream = new PipedOutputStream();
        PipedOutputStream errorStream = new PipedOutputStream();
        PipedInputStream pipedOutput = new PipedInputStream(outputStream);
        PipedInputStream pipedError = new PipedInputStream(errorStream);

        ClientChannel channel = session.createShellChannel();

        try {
            // 设置输入输出流
            channel.setIn(pipedIn);           // 使用管道输入流
            channel.setOut(outputStream);     // 标准输出
            channel.setErr(errorStream);      // 错误输出

            // 打开 Shell 通道
            channel.open().verify(10, TimeUnit.SECONDS);
            log.debug("Shell 通道已打开");

            // 启动输出读取线程
            StringBuilder output = new StringBuilder();
            StringBuilder error = new StringBuilder();
            // 标准输出读取线程
            Thread outputThread = new Thread(() -> {
                char[] buf = new char[1024];
                try (InputStreamReader reader = new InputStreamReader(pipedOutput, config.charset)) {
                    int flag;
                    while ((flag = reader.read(buf)) != -1) {
                        String line = new String(buf, 0, flag);
                        output.append(line);
                        if (config.outputCallback != null) {
                            config.outputCallback.accept(line, false, channel);
                        }
                    }
                } catch (IOException e) {
                    log.error("读取标准输出失败", e);
                }
            });

            // 错误输出读取线程
            Thread errorThread = new Thread(() -> {
                char[] buf = new char[1024];
                try (InputStreamReader reader = new InputStreamReader(pipedError, config.charset)) {
                    int flag;
                    while ((flag = reader.read(buf)) != -1) {
                        String line = new String(buf, 0, flag);
                        error.append(line);
                        if (config.outputCallback != null) {
                            config.outputCallback.accept(line, false, channel);
                        }
                    }
                } catch (IOException e) {
                    log.error("读取错误输出失败", e);
                }
            });

            outputThread.start();
            errorThread.start();

            // 在单独的线程中发送命令
            Thread commandThread = new Thread(() -> {
                try {
                    // 发送命令
                    sendCommand(pipedOut, command);

                    // 发送 exit 命令关闭 Shell
                    sendCommand(pipedOut, "exit");

                    // 关闭输出流
                    pipedOut.close();

                } catch (Exception e) {
                    log.error("发送命令失败", e);
                }
            });

            // 启动命令发送线程
            commandThread.start();

            // 等待 Shell 通道关闭（命令执行完成）
            Set<ClientChannelEvent> events = channel.waitFor(
                    EnumSet.of(ClientChannelEvent.CLOSED),
                    TimeUnit.SECONDS.toMillis(config.timeout)
            );

            // 检查是否超时
            if (!events.contains(ClientChannelEvent.CLOSED)) {
                throw new IOException("命令执行超时: " + config.timeout + " 秒");
            }

            // 等待命令线程结束
            commandThread.join(5000);

            // 获取退出状态
            Integer exitStatus = channel.getExitStatus();
            int exitCode = exitStatus != null ? exitStatus : -1;

            return new CommandResult(exitCode, output.toString(), error.toString());

        } finally {
            // 关闭资源
            try {
                channel.close();
            } catch (Exception e) {
                log.warn("关闭通道失败", e);
            }
            try {
                outputStream.close();
            } catch (Exception e) {
                log.warn("关闭输出流失败", e);
            }
            try {
                errorStream.close();
            } catch (Exception e) {
                log.warn("关闭错误流失败", e);
            }
        }
    }

    /**
     * 发送命令到 Shell
     *
     * @param outputStream 输出流
     * @param command      命令
     * @throws IOException IO 异常
     */
    private static void sendCommand(OutputStream outputStream, String command) throws IOException {
        log.debug("发送命令: {}", command);
        outputStream.write((command + "\n").getBytes(StandardCharsets.UTF_8));
        outputStream.flush();
    }

    /**
     * 从私钥内容加载密钥对并添加到会话
     *
     * @param session           SSH 会话
     * @param privateKeyContent 私钥内容（PEM 格式）
     * @throws IOException 如果加载私钥失败
     */
    private static void loadPrivateKeyFromContent(ClientSession session, String privateKeyContent) throws IOException {
        /*try {
            // 使用 Apache MINA SSHD 的 SecurityUtils 加载私钥
            org.apache.sshd.common.config.keys.FilePasswordProvider passwordProvider = null;

            // 将私钥内容转换为 InputStream
            ByteArrayInputStream keyInputStream = new ByteArrayInputStream(
                    privateKeyContent.getBytes(StandardCharsets.UTF_8)
            );

            // 加载私钥（支持 RSA, DSA, ECDSA, Ed25519 等格式）
            Iterable<java.security.KeyPair> keyPairs = org.apache.sshd.common.config.keys.loader.KeyPairResourceLoader.loadKeyPairs(
                    null,
                    null,
                    passwordProvider,
                    keyInputStream
            );

            // 添加所有加载的密钥对到会话
            for (java.security.KeyPair keyPair : keyPairs) {
                session.addPublicKeyIdentity(keyPair);
                log.debug("已添加私钥到会话");
            }

        } catch (Exception e) {
            throw new IOException("加载私钥内容失败: " + e.getMessage(), e);
        }*/
    }

    /**
     * 构建最终执行的命令
     * 如果有环境初始化脚本或工作目录，则先执行初始化，再执行用户命令
     *
     * <p>执行顺序：</p>
     * <ol>
     *   <li>环境初始化脚本（类似 source ~/.bashrc，完整的脚本内容）</li>
     *   <li>额外的环境变量设置（补充或覆盖初始化脚本中的变量）</li>
     *   <li>切换工作目录（在环境准备好后再切换）</li>
     *   <li>用户命令（最后执行）</li>
     * </ol>
     *
     * @param config 命令配置
     * @return 最终执行的命令字符串
     */
    private static String buildFinalCommand(SshCommandConfig config) {
        StringBuilder commandBuilder = new StringBuilder();

        // 执行环境初始化脚本（完整脚本内容，类似 source ~/.bashrc）
        if (config.environmentInitScript != null && !config.environmentInitScript.isEmpty()) {
            commandBuilder.append(config.environmentInitScript.trim()).append("\n");
        }

        // 切换工作目录（在环境准备好后再切换）
        if (config.workingDir != null && !config.workingDir.isEmpty()) {
            commandBuilder.append("cd ")
                    .append(config.workingDir)
                    .append("\n");
        }

        // 执行用户命令
        commandBuilder.append(config.command);

        return commandBuilder.toString();
    }

    /**
     * SSH 连接配置类
     */
    public static class SshConfig {
        /**
         * SSH 服务器地址（必填）
         */
        private final String host;

        /**
         * SSH 端口（默认 22）
         */
        private final int port;

        /**
         * 用户名（必填）
         */
        private final String username;

        /**
         * 密码（密码认证时必填）
         */
        private final String password;


        /**
         * 私钥内容（密钥认证时使用）
         */
        private final String privateKeyContent;

        private SshConfig(Builder builder) {
            this.host = builder.host;
            this.port = builder.port;
            this.username = builder.username;
            this.password = builder.password;
            this.privateKeyContent = builder.privateKeyContent;
        }

        public static Builder builder() {
            return new Builder();
        }

        /**
         * SSH 配置构建器
         */
        public static class Builder {
            private String host;
            private int port = DEFAULT_SSH_PORT;
            private String username;
            private String password;
            private String privateKeyContent;

            public Builder host(String host) {
                this.host = host;
                return this;
            }

            public Builder port(int port) {
                this.port = port;
                return this;
            }

            public Builder username(String username) {
                this.username = username;
                return this;
            }

            public Builder password(String password) {
                this.password = password;
                return this;
            }

            public Builder privateKeyContent(String privateKeyContent) {
                this.privateKeyContent = privateKeyContent;
                return this;
            }

            public SshConfig build() {
                if (host == null || host.isEmpty()) {
                    throw new IllegalArgumentException("SSH 服务器地址不能为空");
                }
                if (username == null || username.isEmpty()) {
                    throw new IllegalArgumentException("用户名不能为空");
                }

                // 验证认证方式：必须提供密码或私钥内容之一
                boolean hasPassword = password != null && !password.isEmpty();
                boolean hasPrivateKeyContent = privateKeyContent != null && !privateKeyContent.isEmpty();

                if (!hasPassword && !hasPrivateKeyContent) {
                    throw new IllegalArgumentException("必须提供密码或私钥内容");
                }

                return new SshConfig(this);
            }
        }
    }

    /**
     * SSH 命令配置类
     * 用于配置 SSH 命令执行的各种参数
     */
    public static class SshCommandConfig {
        /**
         * SSH 连接配置（必填）
         */
        private final SshConfig sshConfig;

        /**
         * 命令字符串（必填）
         */
        private final String command;

        /**
         * 工作目录（可选）
         */
        private final String workingDir;

        /**
         * 环境初始化脚本（可选）
         */
        private final String environmentInitScript;

        /**
         * 超时时间（秒）
         */
        private final long timeout;

        /**
         * 字符编码
         */
        private final Charset charset;
        /**
         * 实时输出回调（带通道控制，每行输出都会调用）
         */
        private final OutputCallbackWithChannel outputCallback;

        private SshCommandConfig(Builder builder) {
            this.sshConfig = builder.sshConfig;
            this.command = builder.command;
            this.workingDir = builder.workingDir;
            this.environmentInitScript = builder.environmentInitScript;
            this.timeout = builder.timeout;
            this.charset = builder.charset;
            this.outputCallback = builder.outputCallback;
        }

        public static Builder builder() {
            return new Builder();
        }

        /**
         * SSH 命令配置构建器
         */
        public static class Builder {
            private SshConfig sshConfig;
            private String command;
            private String workingDir;
            private String environmentInitScript;
            private long timeout = DEFAULT_TIMEOUT;
            private Charset charset = DEFAULT_CHARSET;
            private OutputCallbackWithChannel outputCallback;

            public Builder sshConfig(SshConfig sshConfig) {
                this.sshConfig = sshConfig;
                return this;
            }

            public Builder command(String command) {
                this.command = command;
                return this;
            }

            public Builder workingDir(String workingDir) {
                this.workingDir = workingDir;
                return this;
            }

            public Builder environmentInitScript(String environmentInitScript) {
                this.environmentInitScript = environmentInitScript;
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

            public Builder outputCallback(OutputCallbackWithChannel outputCallback) {
                this.outputCallback = outputCallback;
                return this;
            }

            public SshCommandConfig build() {
                if (sshConfig == null) {
                    throw new IllegalArgumentException("SSH 连接配置不能为空");
                }
                if (command == null || command.isEmpty()) {
                    throw new IllegalArgumentException("命令不能为空");
                }
                return new SshCommandConfig(this);
            }
        }
    }
}
